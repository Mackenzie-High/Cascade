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
 * Actors send, receive, and process event-messages.
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

    /**
     * Performance metrics related to this actor.
     */
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
            return boundedInflowQueue.offered();
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
            return Optional.ofNullable(script.lastUnhandledException);
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

    /**
     * This is the life-cycle phase that this actor is currently in.
     */
    private final AtomicReference<ActorLifeCycle> phase = new AtomicReference<>(ActorLifeCycle.EGG);

    /**
     * This stage contains this actor.
     */
    private final CascadeStage stage;

    /**
     * This object routes messages to-and-from this actor.
     */
    private final Dispatcher dispatcher;

    /**
     * These identify the event-channels that this actor is currently subscribed-to.
     */
    private final CopyOnWriteArraySet<CascadeToken> subscriptions = new CopyOnWriteArraySet<>();

    /**
     * This is how many messages have been processed by this actor thus far.
     */
    private final AtomicLong consumedMessageCount = new AtomicLong();

    /**
     * This is how many unhandled-exceptions were thrown thus far.
     */
    private final AtomicLong unhandledExceptionCount = new AtomicLong();

    /**
     * True, if the script is executing at this very moment.
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
    private final Script script = new Script();

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

    /**
     * This object provides the power to this actor, as needed.
     */
    private volatile CascadePowerSource executor;

    /**
     * This is how many tasks are pending for this actor to perform,
     * which may include startup tasks, shutdown tasks, or pending messages.
     */
    private final AtomicLong pendingTasks = new AtomicLong();

    /**
     * The executor is free to use this object to store actor-specific data,
     * for its own personal benefit, such as scheduling.
     */
    private final AtomicReference<?> pocket = new AtomicReference<>();

    /**
     * When invoked, this consumer will remove this actor from the enclosing stage.
     */
    private final Consumer<CascadeActor> undertaker;

    /**
     * These are the performance metrics pertaining to this actor.
     */
    private final ActorMetrics metrics = new ActorMetrics();

    /**
     * This is needed to dequeue messages from the inflow-queue.
     * Caching the AtomicReference avoids the need to allocate it each time.
     */
    private final AtomicReference<CascadeToken> event = new AtomicReference<>();

    /**
     * This is needed to dequeue messages from the inflow-queue.
     * Caching the AtomicReference avoids the need to allocate it each time.
     */
    private final AtomicReference<CascadeStack> stack = new AtomicReference<>();

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
        this.schedulerInflowQueue = new NotificationInflowQueue(swappableInflowQueue, q -> updatePendingTasks(false, false, true));
        this.syncInflowQueue = new SynchronizedInflowQueue(schedulerInflowQueue);

        for (int i = 0; i < awaitLatches.length; i++)
        {
            awaitLatches[i] = new CountDownLatch(1);
        }
    }

    /**
     * Retrieve the unique identifier of this actor.
     *
     * @return a UUID that uniquely identifies this actor in time and space.
     */
    public UUID uuid ()
    {
        return uuid;
    }

    /**
     * Retrieve the cascade that contains this actor indirectly.
     *
     * @return the enclosing cascade.
     */
    public Cascade cascade ()
    {
        return stage().cascade();
    }

    /**
     * Retrieve the stage that contains this actor.
     *
     * @return the enclosing stage.
     */
    public CascadeStage stage ()
    {
        return stage;
    }

    /**
     * Retrieve the script that defines how this actor behaves.
     *
     * @return the script that will be executed whenever messages are received.
     */
    public CascadeScript script ()
    {
        return script;
    }

    /**
     * Retrieve the context that this actor will pass to the script.
     *
     * @return the context that is passed to the script()
     * whenever messages are processed by this actor.
     */
    public CascadeContext context ()
    {
        return context;
    }

    /**
     * Retrieve the performance metrics regarding this actor.
     *
     * @return useful metrics regarding this actor.
     */
    public ActorMetrics metrics ()
    {
        return metrics;
    }

    /**
     * Changes the name of this actor.
     *
     * @param name will henceforth be the name of this actor.
     * @return this.
     */
    public CascadeActor setName (final String name)
    {
        this.name = Objects.requireNonNull(name, "name");
        return this;
    }

    /**
     * Retrieve the name of this actor.
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
     * Replace the underlying power-source that powers this actor.
     *
     * @param executor will power this actor going forward.
     * @return this.
     */
    public CascadeActor setPowerSource (final CascadePowerSource executor)
    {
        requireEgg();
        this.executor = Objects.requireNonNull(executor, "executor");
        return this;
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
        requireEgg();
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
        requireEgg();
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
        requireEgg();
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
        requireEgg();
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
        requireEgg();
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
        requireEgg();
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
        requireEgg();
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
     * <p>
     * This method is a no-op, if this actor is already closing.
     * </p>
     *
     * @param event identifies the event to listen for.
     * @return this.
     */
    public CascadeActor subscribe (final CascadeToken event)
    {
        /**
         * Synchronized to prevent concurrent unsubscriptions.
         *
         * When the actor dies, it will automatically unsubscribe from all subscriptions.
         * We must synchronize to prevent new subscriptions from occurring at the same time;
         * otherwise, a race-condition would exist that could cause a memory-leak.
         */
        synchronized (dispatcher) // TODO: This is global. Bad!
        {
            if (phase.get().ordinal() < ActorLifeCycle.CLOSING.ordinal())
            {
                subscriptions.add(event);
                dispatcher.subscribe(event, this);
            }
        }

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
        /**
         * Synchronized to prevent concurrent subscriptions.
         */
        synchronized (dispatcher)
        {
            dispatcher.unsubscribe(event, this);
            subscriptions.remove(event);
        }
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
     * Schedule this actor for startup.
     *
     * <p>
     * This method is a no-op, if start() or close() were already called.
     * </p>
     *
     * @return this.
     */
    public CascadeActor start ()
    {
        phaseTransitionIf(ActorLifeCycle.EGG, ActorLifeCycle.STARTING);
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
        try
        {
            final long taskCount = updatePendingTasks(true, false, false);

            if (taskCount == 0)
            {
                return this;
            }

            synchronized (SELF)
            {
                setupIfNeeded();
                processMessageIfNeeded();
                shutdownIfNeeded();
            }
        }
        finally
        {
            updatePendingTasks(false, true, false);
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
        /**
         * Stop accepting new messages.
         * If new messages are sent, drop them immediately.
         */
        boundedInflowQueue.disable();

        /**
         * Perform zero-or-one phase-transition.
         * Notice that the call order prevents more than one transition.
         */
        phaseTransitionIf(ActorLifeCycle.ACTIVE, ActorLifeCycle.CLOSING);
        phaseTransitionIf(ActorLifeCycle.STARTING, ActorLifeCycle.CLOSING);
        phaseTransitionIf(ActorLifeCycle.EGG, ActorLifeCycle.CLOSING);

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
            final CountDownLatch latch = awaitLatches[goal.ordinal() - 1];
            return latch.await(timeout.toNanos(), TimeUnit.NANOSECONDS);
        }
    }

    private void setupIfNeeded ()
    {
        if (phase.get() == ActorLifeCycle.STARTING)
        {
            try
            {
                script.executeOnSetup();
            }
            finally
            {
                phaseTransitionIf(ActorLifeCycle.STARTING, ActorLifeCycle.ACTIVE);
            }
        }
    }

    private void processMessageIfNeeded ()
    {
        if (phase.get() == ActorLifeCycle.ACTIVE)
        {
            event.set(null);
            stack.set(null);
            syncInflowQueue.removeOldest(event, stack);
            final boolean delivered = event.get() != null; // Not Always True (Overflow Effects)
            if (delivered)
            {
                consumedMessageCount.incrementAndGet();
                script.executeOnMessage(event.get(), stack.get());
            }
        }
    }

    private void shutdownIfNeeded ()
    {
        /**
         * If the actor is in the process of closing,
         * then continue processing messages,
         * until the queue is empty, which will occur,
         * because the queue is no longer accepting new messages.
         */
        if (phase.get() == ActorLifeCycle.CLOSING && syncInflowQueue.isEmpty())
        {
            try
            {
                script.executeOnClose();
            }
            finally
            {
                /**
                 * Unsubscribe this actor from all event-channels that it is subscribed-to;
                 * otherwise, a memory-leak would occur, since the dispatcher would still
                 * have references to the actor, even though the actor is dead!
                 * Synchronized to prevent concurrent subscriptions.
                 */
                synchronized (dispatcher)
                {
                    subscriptions.stream().forEach(s -> unsubscribe(s));
                }

                /**
                 * Remove this actor from the stage.
                 */
                undertaker.accept(this);

                /**
                 * This actor is now totally dead.
                 */
                phaseTransitionIf(ActorLifeCycle.CLOSING, ActorLifeCycle.DEAD);
            }
        }
    }

    /**
     * Update the counter that tracks the number of needed cranks
     * and notify the power-source as necessary.
     *
     * @param pre is true, if the call-site is the start of crank().
     * @param post is true, if the call-site is the end of crank().
     * @param add is true, if the call-site is a phase-transition
     * or due to receiving an event-message.
     * @return the number of pending cranks, if needed at the call-site.
     */
    private long updatePendingTasks (final boolean pre,
                                     final boolean post,
                                     final boolean add)
    {
        /**
         * Synchronize on the counter itself, rather than (this),
         * because we do *not* want to block, if the actor is executing.
         * Otherwise, we could not enqueue messages while executing, etc!
         */
        synchronized (pendingTasks)
        {
            if (add && pendingTasks.get() == 0)
            {
                pendingTasks.incrementAndGet();
                executor.submit(this, pocket);
            }
            else if (add)
            {
                pendingTasks.incrementAndGet();
            }
            else if (pre)
            {
                return pendingTasks.get();
            }
            else if (post && pendingTasks.get() > 1)
            {
                pendingTasks.decrementAndGet();
                executor.submit(this, pocket);
            }
            else if (post && pendingTasks.get() <= 1)
            {
                pendingTasks.set(0);
            }
            else
            {
                Verify.verify(false, "bug!");
            }
        }

        return -1;
    }

    private void replaceQueue (final BoundedInflowQueue.OverflowPolicy policy,
                               final InflowQueue newStorageQueue)
    {
        requireEgg();
        storageInflowQueue = newStorageQueue;
        boundedInflowQueue = new BoundedInflowQueue(policy, newStorageQueue);
        swappableInflowQueue.replaceDelegate(boundedInflowQueue);
    }

    private boolean phaseTransitionIf (final ActorLifeCycle expected,
                                       final ActorLifeCycle next)
    {
        Verify.verify(next.ordinal() > 0);
        Verify.verify(ActorLifeCycle.values().length == 5, "Regression Bug!");

        if (phase.compareAndSet(expected, next))
        {
            /**
             * All of the latches for the preceding phases must already be unblocked.
             */
            IntStream.rangeClosed(0, next.ordinal() - 1).forEach(i -> awaitLatches[i].countDown());

            /**
             * Tell the executor to turn the crank.
             */
            updatePendingTasks(false, false, true);

            return true;
        }
        else
        {
            return false;
        }
    }

    private void requireEgg ()
    {
        Preconditions.checkState(phase.get() == ActorLifeCycle.EGG, "Already Hatched!");
    }

    /**
     * Concrete Implementation of CascadeScript.
     */
    private final class Script
            implements CascadeScript
    {
        private final Script lock = this;

        private volatile OnSetupFunction onSetup = (ctx) -> nop();

        private volatile OnMessageFunction onMessage = (ctx, evt, stk) -> nop();

        private volatile OnCloseFunction onClose = (ctx) -> nop();

        private volatile OnExceptionFunction onException = (ctx, ex) -> nop();

        public volatile Throwable lastUnhandledException = null;

        public void executeOnSetup ()
        {
            synchronized (lock)
            {
                try
                {
                    onSetup.accept(context);
                }
                catch (Throwable ex)
                {
                    onException(context, ex);
                }
            }
        }

        public void executeOnMessage (final CascadeToken evt,
                                      final CascadeStack stk)
        {
            synchronized (lock)
            {
                try
                {
                    onMessage.accept(context, evt, stk);
                }
                catch (Throwable ex)
                {
                    onException(context, ex);
                }
            }
        }

        public void executeOnClose ()
        {
            synchronized (lock)
            {
                try
                {
                    onClose.accept(context);
                }
                catch (Throwable ex)
                {
                    onException(context, ex);
                }
            }
        }

        private void onException (final CascadeContext ctx,
                                  final Throwable cause)
        {
            synchronized (lock)
            {
                try
                {
                    unhandledExceptionCount.incrementAndGet();
                    lastUnhandledException = cause;
                    onException.accept(ctx, cause);
                }
                catch (Throwable ex)
                {
                    unhandledExceptionCount.incrementAndGet();
                    lastUnhandledException = ex;
                }
            }
        }

        @Override
        public CascadeScript onSetup (final OnSetupFunction handler)
        {
            onSetup = Objects.requireNonNull(handler, "handler");
            return this;
        }

        @Override
        public OnSetupFunction onSetup ()
        {
            return onSetup;
        }

        @Override
        public CascadeScript onMessage (final OnMessageFunction handler)
        {
            onMessage = Objects.requireNonNull(handler, "handler");
            return this;
        }

        @Override
        public OnMessageFunction onMessage ()
        {
            return onMessage;
        }

        @Override
        public CascadeScript onClose (final OnCloseFunction handler)
        {
            onClose = Objects.requireNonNull(handler, "handler");
            return this;
        }

        @Override
        public OnCloseFunction onClose ()
        {
            return onClose;
        }

        @Override
        public CascadeScript onException (final OnExceptionFunction handler)
        {
            onException = Objects.requireNonNull(handler, "handler");
            return this;
        }

        @Override
        public OnExceptionFunction onException ()
        {
            return onException;
        }
    }

    private static void nop ()
    {
        // Pass.
    }
}
