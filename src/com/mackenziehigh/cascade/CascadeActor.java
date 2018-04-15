package com.mackenziehigh.cascade;

import com.google.common.base.Preconditions;
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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

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

    private static final int UNSTARTED = 0;

    private static final int STARTING = 1;

    private static final int ACTIVE = 2;

    private static final int CLOSING = 3;

    private static final int CLOSED = 4;

    private final CascadeActor SELF = this;

    /**
     * This UUID uniquely identifies this actor.
     */
    private final UUID uuid = UUID.randomUUID();

    /**
     * This is the name of this actor, which may change over time.
     */
    private volatile String name = uuid.toString();

    private final AtomicInteger state = new AtomicInteger(UNSTARTED);

    private final CascadeStage stage;

    private final Dispatcher dispatcher;

    private final CopyOnWriteArraySet<CascadeToken> subscriptions = new CopyOnWriteArraySet<>();

    /**
     * True, if the script is executing.
     */
    private final AtomicBoolean acting = new AtomicBoolean(false);

    /**
     * Used to implement awaitStart().
     */
    private final CountDownLatch awaitStartLatch = new CountDownLatch(1);

    /**
     * Used to implement awaitClose().
     */
    private final CountDownLatch awaitCloseLatch = new CountDownLatch(1);

    /**
     * The script will be passed this context in order to provide
     * the script with access to this actor and send messages.
     */
    private final CascadeContext context = () -> SELF; // TODO: This is unclear.

    /**
     * This script defines how this actor behaves.
     */
    private final CascadeScript script = new CascadeScript();

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

    /**
     * This is a callback function that will cause this actor to
     * be scheduled for execution by the enclosing stage's executor.
     */
    private final Runnable scheduler;

    /**
     * Sole Constructor.
     *
     * @param stage contains this actor.
     * @param remover will remove this actor from the stage, when the actor dies.
     */
    CascadeActor (final CascadeStage stage,
                  final Dispatcher dispatcher,
                  final Runnable scheduler,
                  final Consumer<CascadeActor> remover)
    {
        this.stage = Objects.requireNonNull(stage, "stage");
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.storageInflowQueue = new LinkedInflowQueue(Integer.MAX_VALUE);
        this.boundedInflowQueue = new BoundedInflowQueue(BoundedInflowQueue.OverflowPolicy.DROP_INCOMING, storageInflowQueue);
        this.swappableInflowQueue = new SwappableInflowQueue(boundedInflowQueue);
        this.schedulerInflowQueue = new NotificationInflowQueue(swappableInflowQueue, q -> onQueueAdd(q));
        this.syncInflowQueue = new SynchronizedInflowQueue(schedulerInflowQueue);
    }

    /**
     * Setter.
     *
     * @param name will henceforth be the name of this actor.
     * @return this.
     */
    public CascadeActor named (final String name)
    {
        this.name = Objects.requireNonNull(name, "name");
        return this;
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
    public String name ()
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
    public CascadeActor useArrayInflowQueue (final int capacity)
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
    public CascadeActor useLinkedInflowQueue (final int capacity)
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
    public CascadeActor useOverflowPolicyDropAll ()
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
    public CascadeActor useOverflowPolicyDropPending ()
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
    public CascadeActor useOverflowPolicyDropOldest ()
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
    public CascadeActor useOverflowPolicyDropNewest ()
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
    public CascadeActor useOverflowPolicyDropIncoming ()
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
     * @return true, if and only if, startup is in-progress.
     */
    public boolean isStarting ()
    {
        return state.get() == STARTING;
    }

    /**
     * Getter.
     *
     * @return true, if and only if, startup has completed.
     */
    public boolean isStarted ()
    {
        return state.get() > STARTING;
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
     * Getter.
     *
     * @return true, if and only if, this actor is not unstarted, closed, or closing.
     */
    public boolean isActive ()
    {
        return state.get() == ACTIVE;
    }

    /**
     * Getter.
     *
     * @return true, if and only if, this actor is leaving the stage.
     */
    public boolean isClosing ()
    {
        return state.get() == CLOSING;
    }

    /**
     * Getter.
     *
     * @return true, if and only if, this actor has left the stage.
     */
    public boolean isClosed ()
    {
        return state.get() == CLOSED;
    }

    /**
     * Getter.
     *
     * @return the current number of messages enqueued in the inflow queue.
     */
    public int backlogSize ()
    {
        return syncInflowQueue.size();
    }

    /**
     * Getter.
     *
     * @return the current capacity of the inflow queue.
     */
    public int backlogCapacity ()
    {
        return syncInflowQueue.capacity();
    }

    /**
     * Getter.
     *
     * @return the total number of messages sent to this actor, thus far,
     * that were enqueued without being immediately dropped.
     */
    public long acceptedMessages ()
    {
        return 0;
    }

    /**
     * Getter.
     *
     * @return the total number of messages that this actor
     * has dropped upon receiving them, thus far.
     */
    public long droppedMessages ()
    {
        return 0;
    }

    /**
     * Getter.
     *
     * @return the total number of messages that this actor
     * has actually processed using the script(), thus far.
     */
    public long consumedMessages ()
    {
        return 0;
    }

    /**
     * Getter.
     *
     * @return the number of unhandled exceptions that
     * have been thrown by the script(), thus far.
     */
    public long unhandledExceptions ()
    {
        return 0;
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

    private static final AtomicInteger told = new AtomicInteger();

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
     * @return this.
     */
    public CascadeActor start ()
    {
        state.set(STARTING);
        scheduler.run();
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
    public CascadeActor perform ()
    {
        synchronized (this)
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
        state.set(CLOSING);
        scheduler.run();
        return this;
    }

    /**
     * This method blocks, until this actor starts.
     *
     * <p>
     * If this actor already started, then this method is a no-op.
     * </p>
     *
     * @param timeout is the maximum amount of time to wait.
     * @return this.
     * @throws java.lang.InterruptedException
     */
    public CascadeActor awaitStart (final Duration timeout)
            throws InterruptedException
    {
        final long nanos = timeout.toNanos();
        awaitStartLatch.await(nanos, TimeUnit.NANOSECONDS);
        return this; // TODO: boolean instead?
    }

    /**
     * This method blocks, until this actor dies.
     *
     * <p>
     * If this actor already closed, then this method is a no-op.
     * </p>
     *
     * @param timeout is the maximum amount of time to wait.
     * @return this.
     * @throws java.lang.InterruptedException
     */
    public CascadeActor awaitClose (final Duration timeout)
            throws InterruptedException
    {
        awaitCloseLatch.await(timeout.getNano(), TimeUnit.NANOSECONDS);
        return this; // TODO: boolean instead?
    }

    private void setupIfNeeded ()
            throws Throwable
    {
        if (isStarting())
        {
            try
            {
                script.onSetup(context);
            }
            finally
            {
                state.set(ACTIVE);
                awaitStartLatch.countDown();
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
            script.onMessage(context(), event.get(), stack.get());
        }
    }

    private void shutdownIfNeeded ()
            throws Throwable
    {
        if (isClosing())
        {
            try
            {
                script().onClose(context);
            }
            finally
            {
                state.set(CLOSED);
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

        scheduler.run();
    }

    private void replaceQueue (final BoundedInflowQueue.OverflowPolicy policy,
                               final InflowQueue newStorageQueue)
    {
        final Runnable action = () ->
        {
            storageInflowQueue = newStorageQueue;
            boundedInflowQueue = new BoundedInflowQueue(policy, newStorageQueue);
            swappableInflowQueue.replaceDelegate(boundedInflowQueue);
        };

        /**
         * The replacement must be synchronized with regard to other queue operations.
         */
        syncInflowQueue.sync(action);
    }

}
