package com.mackenziehigh.cascade;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableSet;
import com.mackenziehigh.cascade.internal.ArrayInflowQueue;
import com.mackenziehigh.cascade.internal.BoundedInflowQueue;
import com.mackenziehigh.cascade.internal.Dispatcher;
import com.mackenziehigh.cascade.internal.InflowQueue;
import com.mackenziehigh.cascade.internal.LinkedInflowQueue;
import com.mackenziehigh.cascade.internal.NotificationInflowQueue;
import com.mackenziehigh.cascade.internal.SwappableInflowQueue;
import com.mackenziehigh.cascade.internal.SynchronizedInflowQueue;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Actors receive, process, and send event-messages.
 *
 * <p>
 * Actors are responsible for executing the onSetup(), onMessage(),
 * and onClose() handlers of the underlying script().
 * </p>
 *
 * <p>
 * An actor will never execute the same script() concurrently.
 * Moreover, an actor will synchronize execution of the script()
 * in order to ensure memory-consistency.
 * </p>
 *
 * <p>
 * By default, actors use effectively unbounded linked inflow-queues.
 * </p>
 *
 * <p>
 * By default, actors use the Drop Incoming overflow-policy.
 * </p>
 */
public final class CascadeActor
{
    /**
     * A builder that builds actor objects.
     */
    public interface Builder
    {
        /**
         * A factory that creates builders that build actor objects.
         *
         * @param <T> is the type of builder that will be created.
         */
        @FunctionalInterface
        public interface Factory<T extends Builder>
        {
            /**
             * Getter.
             *
             * @param stage will contain actors built via the builder.
             * @return the new builder.
             */
            public T newBuilder (CascadeStage stage);
        }

        /**
         * Getter.
         *
         * @return the stage that will contain the actor.
         */
        public CascadeStage stage ();

        /**
         * Getter.
         *
         * @return the newly built actor.
         */
        public CascadeActor build ();

        /**
         * Getter.
         *
         * @return the newly built and started actor.
         */
        public default CascadeActor buildAndStart ()
        {
            final CascadeActor actor = build();
            actor.start();
            return actor;
        }
    }

    /**
     * The life-cycle phases of an actor in chronological order.
     */
    public enum ActorLifeCycle
    {
        /**
         * The actor has been created, but start() has not yet been called.
         */
        EGG,

        /**
         * start() was just called, but the setup script has not yet finished.
         */
        STARTING,
        /**
         * Setup is complete and the actor is ready to process messages.
         */
        ACTIVE,
        /**
         * close() was just called, but the close script has not yet finished.
         */
        CLOSING,
        /**
         * The close script has completed after the call to close().
         */
        DEAD
    }

    public enum ActorChangeTypes
    {
        EGG,
        STARTING,
        ACTIVE,
        CLOSING,
        DEAD,
        SUBSCRIBE,
        UNSUBSCRIBE,
        QUEUE_CHANGED,
        POWER_CHANGED,
        NAME_CHANGED,
        UNHANDLED_EXCEPTION;

        private final CascadeToken token;

        private ActorChangeTypes ()
        {
            token = CascadeToken.token(name());
        }

        public CascadeToken asToken ()
        {
            return token;
        }
    }

    public final class ActorMetrics
    {
        private ActorMetrics ()
        {
            // Pass
        }

        /**
         * Getter.
         *
         * @return the current number of messages enqueued in the inflow queue.
         */
        public int getBacklogSize ()
        {
            return syncInflowQueue.size();
        }

        /**
         * Getter.
         *
         * @return the current capacity of the inflow queue.
         */
        public int getBacklogCapacity ()
        {
            return syncInflowQueue.capacity();
        }

        /**
         * Getter.
         *
         * @return the total number of messages sent to this actor.
         */
        public long getOfferedMessageCount ()
        {
            return 0; // TODO
        }

        /**
         * Getter.
         *
         * @return the total number of messages sent to this actor, thus far,
         * that were enqueued without being immediately dropped.
         */
        public long getAcceptedMessageCount ()
        {
            return boundedInflowQueue.accepted(); // Thread-Safe
        }

        /**
         * Getter.
         *
         * @return the total number of messages that this actor
         * has dropped upon receiving them, thus far.
         */
        public long getDroppedMessageCount ()
        {
            return boundedInflowQueue.dropped(); // Thread-Safe
        }

        /**
         * Getter.
         *
         * @return the total number of messages that this actor
         * has actually processed using the script(), thus far.
         */
        public long getConsumedMessageCount ()
        {
            return consumedMessageCount.get();
        }

        /**
         * Getter.
         *
         * @return the number of unhandled exceptions that
         * have been thrown by the script(), thus far.
         */
        public long getUnhandledExceptionCount ()
        {
            return unhandledExceptionCount.get();
        }

        /**
         * Getter.
         *
         * @return the last unhandled exception, if any.
         */
        public Optional<Throwable> getLastUnhandledException ()
        {
            return Optional.empty(); // TODO
        }
    }

    private final CascadeActor SELF = this;

    /**
     * This UUID uniquely identifies this actor.
     */
    private final UUID uuid = UUID.randomUUID();

    /**
     * This is the name of this actor, which may change over time.
     */
    private volatile String name = uuid.toString();

    private final AtomicReference<ActorLifeCycle> phase = new AtomicReference<>();

    private final CascadeStage stage;

    private final Dispatcher dispatcher;

    private final CopyOnWriteArraySet<CascadeToken> subscriptions = new CopyOnWriteArraySet<>();

    private final AtomicLong consumedMessageCount = new AtomicLong();

    private final AtomicLong unhandledExceptionCount = new AtomicLong();

    /**
     * True, if the script is executing.
     */
    private final AtomicBoolean acting = new AtomicBoolean(false);

    /**
     * Used to implement await(). No latch is needed for the first phase.
     */
    private final CountDownLatch[] awaitLatches = new CountDownLatch[ActorLifeCycle.values().length - 1];

    /**
     * The script will be passed this context in order to provide
     * the script with access to this actor and send messages.
     */
    private final CascadeContext context = () -> SELF; // TODO: This is unclear.

    /**
     * This script defines how this actor behaves.
     */
    private final CascadeScript script = new CascadeScript(context, unhandledExceptionCount);

    /**
     * This inflow-queue physically stores the incoming event-messages.
     *
     * <p>
     * Whenever this field changes, the bounded-queue must change.
     * Moreover, the swappable-queue must delegate to the new bounded-queue.
     * </p>
     */
    private volatile InflowQueue storageInflowQueue;

    /**
     * This inflow-queue is a facade around the storage-queue,
     * which implements the overflow policy of this actor.
     */
    private volatile BoundedInflowQueue boundedInflowQueue;

    /**
     * This inflow-queue is a facade around the bounded-queue,
     * which facilitates hot-swapping thereof.
     */
    private final SwappableInflowQueue swappableInflowQueue;

    /**
     * This inflow-queue is a facade around the swappable-queue,
     * which will invoke onQueueAdd() whenever a message is received.
     */
    private final NotificationInflowQueue schedulerInflowQueue;

    /**
     * This inflow-queue is a facade around the scheduler-queue,
     * which ensures that all operations are queue synchronized.
     */
    private final SynchronizedInflowQueue syncInflowQueue;

    private final AtomicReference<CascadeToken> event = new AtomicReference<>();

    private final AtomicReference<CascadeStack> stack = new AtomicReference<>();

    private final CascadePowerSource executor;

    private final AtomicReference<?> pocket = new AtomicReference<>();

    private final AtomicLong pendingTasks = new AtomicLong();

    private final Consumer<CascadeActor> undertaker;

    private final ActorMetrics metrics = new ActorMetrics();

    private final CascadeToken statusEvent = CascadeToken.random();

    /**
     * Sole Constructor.
     *
     * @param stage contains this actor.
     * @param undertaker will remove this actor from the stage, when the actor dies.
     */
    CascadeActor (final CascadeStage stage,
                  final Dispatcher dispatcher,
                  final CascadePowerSource executor,
                  final Consumer<CascadeActor> undertaker)
    {
        this.stage = Objects.requireNonNull(stage, "stage");
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.undertaker = Objects.requireNonNull(undertaker, "undertaker");
        this.storageInflowQueue = new LinkedInflowQueue(Integer.MAX_VALUE);
        this.boundedInflowQueue = new BoundedInflowQueue(BoundedInflowQueue.OverflowPolicy.DROP_INCOMING, storageInflowQueue);
        this.swappableInflowQueue = new SwappableInflowQueue(boundedInflowQueue);
        this.schedulerInflowQueue = new NotificationInflowQueue(swappableInflowQueue, q -> onQueueAdd(q));
        this.syncInflowQueue = new SynchronizedInflowQueue(schedulerInflowQueue);

        for (int i = 1; i < ActorLifeCycle.values().length; i++)
        {
            awaitLatches[i - 1] = new CountDownLatch(1);
        }

        sendChangeEvent(ActorChangeTypes.EGG);
    }

    /**
     * Setter.
     *
     * @param name will henceforth be the name of this actor.
     * @return this.
     */
    public CascadeActor setName (final String name)
    {
        this.name = Objects.requireNonNull(name, "name");
        sendChangeEvent(ActorChangeTypes.NAME_CHANGED);
        return this;
    }

    /**
     * Getter.
     *
     * @return useful metrics regarding this actor.
     */
    public ActorMetrics metrics ()
    {
        return metrics;
    }

    /**
     * Getter.
     *
     * <p>
     * By default, the name of this actor is the string representation of the actor's UUID.
     * </p>
     *
     * @return the current name of this actor.
     */
    public String getName ()
    {
        return name;
    }

    /**
     * Getter.
     *
     * @return a UUID that uniquely identifies this actor in time and space.
     */
    public UUID uuid ()
    {
        return uuid;
    }

    /**
     * Getter.
     *
     * @return the identifier of the event-channel that receives status messages.
     */
    public CascadeToken statusEvent ()
    {
        return statusEvent;
    }

    /**
     * Getter.
     *
     * @return the enclosing cascade.
     */
    public Cascade cascade ()
    {
        return stage().cascade();
    }

    /**
     * Getter.
     *
     * @return the enclosing stage.
     */
    public CascadeStage stage ()
    {
        return stage;
    }

    /**
     * Getter.
     *
     * @return the script that will be executed whenever messages are received.
     */
    public CascadeScript script ()
    {
        return script;
    }

    /**
     * Replace the underlying power-source that powers this actor.
     *
     * <p>
     * This is an invasive operation that requires internal synchronization.
     * Therefore, this method will block, if the actor is currently acting.
     * </p>
     *
     * @param value will power this actor going forward.
     * @return this.
     */
    public CascadeActor setPowerSource (final CascadePowerSource value)
    {
        sendChangeEvent(ActorChangeTypes.POWER_CHANGED);
        return this; // TODO
    }

    /**
     * Retrieve the underlying power-source.
     *
     * @return the current underlying power-source immediately.
     */
    public CascadePowerSource getPowerSource ()
    {
        return executor;
    }

    /**
     * Causes this actor to switch to a fixed-size array-based
     * inflow-queue, which will be used to store the messages
     * that are pending processing by this actor.
     *
     * <p>
     * When backlogSize() reaches backlogCapacity().
     * the overflow-policy will dictate what happens
     * when new messages arrive for this actor.
     * </p>
     *
     * @param capacity will be the backlogCapacity() of the queue.
     * @return this.
     */
    public CascadeActor setArrayInflowQueue (final int capacity)
    {
        final BoundedInflowQueue.OverflowPolicy policy = boundedInflowQueue.policy();
        final InflowQueue newQueue = new ArrayInflowQueue(capacity);
        replaceQueue(policy, newQueue);
        return this;
    }

    /**
     * Getter.
     *
     * @return true, iff this actor is using an array-based inflow-queue.
     */
    public boolean hasArrayInflowQueue ()
    {
        return storageInflowQueue instanceof ArrayInflowQueue;
    }

    /**
     * Causes this actor to switch to an fixed-size array-based
     * inflow-queue, which will be used to store the messages
     * that are pending processing by this actor.
     *
     * <p>
     * When backlogSize() reaches backlogCapacity().
     * the overflow-policy will dictate what happens
     * when new messages arrive for this actor.
     * </p>
     *
     * @param capacity will be the backlogCapacity() of the queue.
     * @return this.
     */
    public CascadeActor setLinkedInflowQueue (final int capacity)
    {
        final BoundedInflowQueue.OverflowPolicy policy = boundedInflowQueue.policy();
        final InflowQueue newQueue = new LinkedInflowQueue(capacity);
        replaceQueue(policy, newQueue);
        return this;
    }

    /**
     * Getter.
     *
     * @return true, iff this actor is using a linked-list based inflow-queue.
     */
    public boolean hasLinkedInflowQueue ()
    {
        return storageInflowQueue instanceof LinkedInflowQueue;
    }

    /**
     * Causes the overflow-policy to be changed to Drop All.
     *
     * <p>
     * After this method returns, whenever backlogSize() reaches
     * backlogCapacity() and a new message arrives for processing
     * by this actor, the inflow-queue will be cleared
     * and the new message will also be dropped.
     * </p>
     *
     * @return this.
     */
    public CascadeActor setOverflowPolicyDropAll ()
    {
        replaceQueue(BoundedInflowQueue.OverflowPolicy.DROP_ALL, storageInflowQueue);
        return this;
    }

    /**
     * Getter.
     *
     * @return true, iff this actor is using the Drop All overflow-policy.
     */
    public boolean isOverflowPolicyDropAll ()
    {
        return boundedInflowQueue.policy() == BoundedInflowQueue.OverflowPolicy.DROP_ALL;
    }

    /**
     * Causes the overflow-policy to be changed to Drop Pending.
     *
     * <p>
     * After this method returns, whenever backlogSize() reaches
     * backlogCapacity() and a new message arrives for processing
     * by this actor, the inflow-queue will be cleared and then
     * the new message will be enqueued.
     * </p>
     *
     * @return this.
     */
    public CascadeActor setOverflowPolicyDropPending ()
    {
        replaceQueue(BoundedInflowQueue.OverflowPolicy.DROP_PENDING, storageInflowQueue);
        return this;
    }

    /**
     * Getter.
     *
     * @return true, iff this actor is using the Drop Pending overflow-policy.
     */
    public boolean isOverflowPolicyDropPending ()
    {
        return boundedInflowQueue.policy() == BoundedInflowQueue.OverflowPolicy.DROP_PENDING;
    }

    /**
     * Causes the overflow-policy to be changed to Drop Oldest.
     *
     * <p>
     * After this method returns, whenever backlogSize() reaches
     * backlogCapacity() and a new message arrives for processing
     * by this actor, the message that has been in the inflow-queue
     * for the longest period of time will be removed and then
     * the new message will be enqueued.
     * </p>
     *
     * @return this.
     */
    public CascadeActor setOverflowPolicyDropOldest ()
    {
        replaceQueue(BoundedInflowQueue.OverflowPolicy.DROP_OLDEST, storageInflowQueue);
        return this;
    }

    /**
     * Getter.
     *
     * @return true, iff this actor is using the Drop Oldest overflow-policy.
     */
    public boolean isOverflowPolicyDropOldest ()
    {
        return boundedInflowQueue.policy() == BoundedInflowQueue.OverflowPolicy.DROP_OLDEST;
    }

    /**
     * Causes the overflow-policy to be changed to Drop Newest.
     *
     * <p>
     * After this method returns, whenever backlogSize() reaches
     * backlogCapacity() and a new message arrives for processing
     * by this actor, the message that has been in the inflow-queue
     * for the shortest period of time will be removed and then
     * the new message will be enqueued.
     * </p>
     *
     * @return this.
     */
    public CascadeActor setOverflowPolicyDropNewest ()
    {
        replaceQueue(BoundedInflowQueue.OverflowPolicy.DROP_NEWEST, storageInflowQueue);
        return this;
    }

    /**
     * Getter.
     *
     * @return true, iff this actor is using the Drop Newest overflow-policy.
     */
    public boolean isOverflowPolicyDropNewest ()
    {
        return boundedInflowQueue.policy() == BoundedInflowQueue.OverflowPolicy.DROP_NEWEST;
    }

    /**
     * Causes the overflow-policy to be changed to Drop Incoming.
     *
     * <p>
     * After this method returns, whenever backlogSize() reaches
     * backlogCapacity() and a new message arrives for processing
     * by this actor, the message that has just arrived will be
     * dropped and no message will be enqueued. In short, messages
     * will only be enqueued when there is actually space available.
     * </p>
     *
     * @return this.
     */
    public CascadeActor setOverflowPolicyDropIncoming ()
    {
        replaceQueue(BoundedInflowQueue.OverflowPolicy.DROP_INCOMING, storageInflowQueue);
        return this;
    }

    /**
     * Getter.
     *
     * @return true, iff this actor is using the Drop Incoming overflow-policy.
     */
    public boolean isOverflowPolicyDropIncoming ()
    {
        return boundedInflowQueue.policy() == BoundedInflowQueue.OverflowPolicy.DROP_INCOMING;
    }

    /**
     * This method causes this actor to begin receiving messages for the given event.
     *
     * @param event identifies the event to listen for.
     * @return this.
     */
    public CascadeActor subscribe (final CascadeToken event)
    {
        subscriptions.add(event);
        dispatcher.subscribe(event, this);
        sendChangeEvent(ActorChangeTypes.SUBSCRIBE);
        return this;
    }

    /**
     * This method causes this actor to stop receiving messages for the given event.
     *
     * <p>
     * If this actor is not currently subscribed to the given event,
     * then this method is simply a no-op.
     * </p>
     *
     * @param event identifies the event to no longer listen for.
     * @return this.
     */
    public CascadeActor unsubscribe (final CascadeToken event)
    {
        dispatcher.unsubscribe(event, this);
        subscriptions.remove(event);
        sendChangeEvent(ActorChangeTypes.UNSUBSCRIBE);
        return this;
    }

    /**
     * Getter.
     *
     * @return the identities of the events that this actor is listening for.
     */
    public Set<CascadeToken> subscriptions ()
    {
        return ImmutableSet.copyOf(subscriptions);
    }

    /**
     * Getter.
     *
     * @return the event-channels that this actor is currently subscribed-to.
     */
    public Set<CascadeChannel> inputs ()
    {
        final Set<CascadeChannel> set = subscriptions
                .stream()
                .map(evt -> cascade().channelOf(evt).orElse(null))
                .filter(x -> x != null)
                .collect(Collectors.toSet());

        return ImmutableSet.copyOf(set);
    }

    /**
     * Getter.
     *
     * @return the life-cycle-phase that this actor is in.
     */
    public ActorLifeCycle getLifeCyclePhase ()
    {
        return phase.get();
    }

    /**
     * Getter.
     *
     * @return true, if and only if, the script() is currently being executed.
     */
    public boolean isActing ()
    {
        return acting.get();
    }

    /**
     * True, if the life-cycle-phase is neither EGG nor DEAD.
     *
     * @return true, iff this actor is still alive.
     */
    public boolean isAlive ()
    {
        return getLifeCyclePhase() != ActorLifeCycle.EGG && getLifeCyclePhase() != ActorLifeCycle.DEAD;
    }

    /**
     * True, if this actor has already lived and then died.
     *
     * @return true, if this actor is now dead.
     */
    public boolean isDead ()
    {
        return getLifeCyclePhase() == ActorLifeCycle.DEAD;
    }

    /**
     * Getter.
     *
     * @return the context that is passed to the script()
     * whenever messages are processed by this actor.
     */
    public CascadeContext context ()
    {
        return context;
    }

    /**
     * Sends an event-message directly to this actor.
     *
     * <p>
     * The event-message will *not* be routed through the global dispatcher;
     * therefore, no other actors will receive the event-message,
     * even if they are subscribed to the same event.
     * </p>
     *
     * @param event identifies the event that produced the event-message.
     * @param stack is the content of the event-message.
     * @return this.
     */
    public CascadeActor tell (final CascadeToken event,
                              final CascadeStack stack)
    {
        Preconditions.checkNotNull(event, "event");
        Preconditions.checkNotNull(stack, "stack");
        syncInflowQueue.offer(event, stack);
        return this;
    }

    /**
     * Henceforth, all messages that are sent to this actor,
     * including dropped messages, will be forwarded to
     * the event-channel identified by the given event.
     *
     * <p>
     * This method may be invoked repeatedly in order
     * to specify multiple independent recipients.
     * </p>
     *
     * @param event identifies an event-channel.
     * @return this.
     */
    public CascadeActor forwardTo (final CascadeToken event)
    {
        forwardConsumedTo(event);
        forwardDroppedTo(event);
        return this;
    }

    /**
     * Henceforth, all messages that are dropped by this actor,
     * will be forwarded to the identified event-channel.
     *
     * <p>
     * This method may be invoked repeatedly in order
     * to specify multiple independent recipients.
     * </p>
     *
     * @param event identifies an event-channel.
     * @return this.
     */
    public CascadeActor forwardDroppedTo (final CascadeToken event)
    {
        return this; // TODO
    }

    /**
     * Henceforth, all messages that are consumed by this actor,
     * will be forwarded to the identified event-channel.
     *
     * <p>
     * This method may be invoked repeatedly in order
     * to specify multiple independent recipients.
     * </p>
     *
     * @param event identifies an event-channel.
     * @return this.
     */
    public CascadeActor forwardConsumedTo (final CascadeToken event)
    {
        return this; // TODO
    }

    /**
     * Schedule this actor for startup.
     *
     * @return this.
     */
    public CascadeActor start ()
    {
        if (phase.compareAndSet(ActorLifeCycle.EGG, ActorLifeCycle.STARTING))
        {
            phaseTransition(ActorLifeCycle.STARTING, ActorChangeTypes.STARTING);
            awaitLatches[ActorLifeCycle.STARTING.ordinal() - 1].countDown();
            executor.addActor(this, pocket);
            schedule();
        }
        return this;
    }

    /**
     * Cause this actor to perform one unit-of-work and then return.
     *
     * <p>
     * A single unit-of-work is either executing the startup-script,
     * processing a single incoming message using the message
     * handling script, or executing the stop-script.
     * </p>
     *
     * <p>
     * Only one unit-of-work is performed during each invocation
     * in order to implement cooperative multi-tasking.
     * </p>
     *
     * <p>
     * If no work is immediately available, then this method is a no-op.
     * </p>
     *
     * @return this.
     */
    public CascadeActor crank ()
    {
        synchronized (pendingTasks)
        {
            if (pendingTasks.get() == 0)
            {
                return this;
            }
            else
            {
                pendingTasks.decrementAndGet();
            }
        }

        try
        {
            synchronized (SELF)
            {
                try
                {
                    setupIfNeeded();
                }
                catch (Throwable ex)
                {
                    //reportUnhandledException(ex);
                    close();
                    return this;
                }

                try
                {
                    processMessageIfNeeded();
                }
                catch (Throwable ex)
                {
                    //reportUnhandledException(ex);
                }

                try
                {
                    shutdownIfNeeded();
                }
                catch (Throwable ex)
                {
                    //reportUnhandledException(ex);
                }
            }
        }
        finally
        {
            synchronized (pendingTasks)
            {
                if (pendingTasks.get() > 0)
                {
                    executor.submit(this, pocket);
                }
            }
        }

        return this;
    }

    /**
     * This method kills this actor, which causes it to stop listening
     * for incoming messages, remove itself from the stage, etc.
     *
     * <p>
     * This method returns immediately; however, the actor will not close
     * until it has finished any work that it is currently performing.
     * </p>
     *
     * @return this.
     */
    public CascadeActor close ()
    {
        boolean changed = false;
        changed |= phase.compareAndSet(ActorLifeCycle.EGG, ActorLifeCycle.CLOSING);
        changed |= phase.compareAndSet(ActorLifeCycle.STARTING, ActorLifeCycle.CLOSING);
        changed |= phase.compareAndSet(ActorLifeCycle.ACTIVE, ActorLifeCycle.CLOSING);
        if (changed)
        {
            phaseTransition(ActorLifeCycle.CLOSING, ActorChangeTypes.CLOSING);

            synchronized (pendingTasks)
            {
                pendingTasks.set(0);
                executor.removeActor(this, pocket);
            }
        }
        return this;
    }

    /**
     * This method blocks, until this actor starts.
     *
     * <p>
     * If this actor already started, then this method is a no-op.
     * </p>
     *
     * @param goal is the life-cycle-phase that the thread will wait for the actor to reach.
     * @param timeout is the maximum amount of time to wait.
     * @return true, if the goal was reached.
     * @throws java.lang.InterruptedException
     */
    public boolean await (final ActorLifeCycle goal,
                          final Duration timeout)
            throws InterruptedException
    {
        if (goal == ActorLifeCycle.EGG)
        {
            return true;
        }
        else
        {
            return awaitLatches[goal.ordinal() + 1].await(timeout.toNanos(), TimeUnit.NANOSECONDS);
        }
    }

    private void setupIfNeeded ()
            throws Throwable
    {
        if (phase.get() == ActorLifeCycle.STARTING)
        {
            try
            {
                script.onSetup(context);
            }
            finally
            {
                phaseTransition(ActorLifeCycle.ACTIVE, ActorChangeTypes.ACTIVE);
            }
        }
    }

    private void processMessageIfNeeded ()
            throws Throwable
    {
        event.set(null);
        stack.set(null);
        syncInflowQueue.removeOldest(event, stack);
        final boolean delivered = event.get() != null; // Not Always True (Overflow Effects)
        if (delivered)
        {
            consumedMessageCount.incrementAndGet();
            script.onMessage(context(), event.get(), stack.get());
        }
    }

    private void shutdownIfNeeded ()
            throws Throwable
    {
        if (phase.get() == ActorLifeCycle.CLOSING)
        {
            try
            {
                script().onClose(context);
            }
            finally
            {
                phaseTransition(ActorLifeCycle.DEAD, ActorChangeTypes.DEAD);
                undertaker.accept(this);
            }
        }
    }

    /**
     * This method will be executed whenever an event-message is received,
     * even if the message is ultimately dropped by this actor.
     *
     * @param queue just received the new message.
     */
    private void onQueueAdd (final InflowQueue queue)
    {
        schedule();
    }

    private void schedule ()
    {
        synchronized (pendingTasks)
        {
            final long count = pendingTasks.getAndIncrement();

            if (count == 0)
            {
                executor.submit(this, pocket);
            }
        }
    }

    private void replaceQueue (final BoundedInflowQueue.OverflowPolicy policy,
                               final InflowQueue newStorageQueue)
    {
        final Runnable action = () ->
        {
            synchronized (SELF) // TODO: Is this really safe? Nested sync?
            {
                storageInflowQueue = newStorageQueue;
                boundedInflowQueue = new BoundedInflowQueue(policy, newStorageQueue);
                swappableInflowQueue.replaceDelegate(boundedInflowQueue);
            }
        };

        /**
         * The replacement must be synchronized with regard to other queue operations.
         */
        syncInflowQueue.sync(action);

        sendChangeEvent(ActorChangeTypes.QUEUE_CHANGED);
    }

    private void phaseTransition (final ActorLifeCycle next,
                                  final ActorChangeTypes changeType)
    {
        Verify.verify(next.ordinal() > 0);
        Verify.verify(ActorLifeCycle.values().length == 5, "Regression Bug!");
        phase.set(next);
        awaitLatches[next.ordinal() - 1].countDown();

        /**
         * All of the latches for the preceding phases must already be unblocked.
         */
        Verify.verify(IntStream.rangeClosed(0, next.ordinal() - 1).allMatch(i -> awaitLatches[i].getCount() == 0));

        sendChangeEvent(changeType);
    }

    private void sendChangeEvent (final ActorChangeTypes changeType)
    {
        // TODO: Send on stage statusEvent as well. Send on cascade statusEvent too???

        final CascadeStack msg = CascadeStack
                .newStack()
                .pushObject(changeType.asToken())
                .pushObject(this);

        cascade().send(statusEvent(), msg);
    }
}
