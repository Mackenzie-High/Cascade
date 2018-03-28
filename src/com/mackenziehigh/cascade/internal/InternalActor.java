package com.mackenziehigh.cascade.internal;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeContext;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadeScript;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeStage;
import com.mackenziehigh.cascade.CascadeSupervisor;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.internal.BoundedInflowQueue.OverflowPolicy;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This is the actual implementation of the CascadeActor interface.
 */
public final class InternalActor
        implements CascadeActor
{
    /**
     * This UUID uniquely identifies this actor.
     */
    private final UUID uuid = UUID.randomUUID();

    /**
     * The birth-date of this actor.
     */
    private final Instant creationTime = Instant.now();

    /**
     * This stage contains this actor.
     */
    private final InternalStage stage;

    /**
     * This is the logger that the script will use.
     * If this is null, then this is the logger of the stage().
     */
    private volatile CascadeLogger logger;

    /**
     * True, if the script is executing.
     */
    private final AtomicBoolean acting = new AtomicBoolean(false);

    /**
     * True, if this actor is closing, but not yet closed.
     */
    private final AtomicBoolean closing = new AtomicBoolean(false);

    /**
     * True, if not closing or closed.
     */
    private final AtomicBoolean alive = new AtomicBoolean(true);

    /**
     * This is how many messages have been sent to event-streams
     * that do not have any subscribers.
     */
    private final AtomicLong undeliveredMessageCount = new AtomicLong();

    /**
     * This is how many messages have been sent, period.
     */
    private final AtomicLong producedMessageCount = new AtomicLong();

    /**
     * Used to implement awaitClose().
     */
    private final CountDownLatch actorAwaitCloseLatch = new CountDownLatch(1);

    /**
     * The script will be passed this context in order to provide
     * the script with access to this actor and send messages.
     */
    private final InternalContext context = new InternalContext(this);

    /**
     * This script defines how this actor behaves.
     */
    private final InternalScript script;

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
     * These tokens identify all of the event-streams that this
     * actor is interested in receiving event-messages from.
     */
    private final Set<CascadeToken> subscriptions = Sets.newConcurrentHashSet();

    /**
     * This object will be used to schedule this actor for execution by a thread.
     */
    private final Scheduler.Process<InternalActor> task;

    /**
     * Sole Constructor.
     *
     * @param stage contains this actor.
     * @param script defines how this actor behaves.
     */
    public InternalActor (final InternalStage stage,
                          final CascadeScript script)
    {
        this.stage = stage;
        this.logger = null;
        this.script = new InternalScript(script);
        final InflowQueue initialInflowQueue = new LinkedInflowQueue(Integer.MAX_VALUE);
        this.boundedInflowQueue = new BoundedInflowQueue(BoundedInflowQueue.OverflowPolicy.DROP_INCOMING, initialInflowQueue);
        this.swappableInflowQueue = new SwappableInflowQueue(boundedInflowQueue);
        this.schedulerInflowQueue = new NotificationInflowQueue(swappableInflowQueue, q -> onQueueAdd(q));
        this.syncInflowQueue = new SynchronizedInflowQueue(schedulerInflowQueue);
        this.task = stage.scheduler().newProcess(0, this);
    }

    /**
     * Send a message and count it.
     *
     * @param event identifies the event that produced the message.
     * @param stack is the content of the message.
     * @return true, if the message was delivered to at least one interested recipient.
     */
    public boolean send (final CascadeToken event,
                         final CascadeStack stack)
    {
        producedMessageCount.incrementAndGet();

        final boolean undelivered = stage.dispatcher().send(event, stack);

        if (undelivered == false)
        {
            undeliveredMessageCount.incrementAndGet();
        }

        return !undelivered;
    }

    /**
     * This method will be executed whenever an event-message is received,
     * even if the message is ultimately dropped by this actor.
     *
     * @param queue just received the new message.
     */
    private void onQueueAdd (final InflowQueue queue)
    {
        task.schedule();
    }

    public CascadeContext context ()
    {
        return context;
    }

    public InflowQueue inflowQueue ()
    {
        return syncInflowQueue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID uuid ()
    {
        return uuid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cascade cascade ()
    {
        return stage.cascade();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeStage stage ()
    {
        return stage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeActor useLogger (final CascadeLogger logger)
    {
        this.logger = Objects.requireNonNull(logger, "logger");
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeLogger logger ()
    {
        return logger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeScript script ()
    {
        return script;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeActor useArrayInflowQueue (final int capacity)
    {
        final OverflowPolicy policy = boundedInflowQueue.policy();
        final InflowQueue newQueue = new ArrayInflowQueue(capacity);
        replaceQueue(policy, newQueue);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeActor useGrowableArrayInflowQueue (int size,
                                                     int capacity,
                                                     int delta)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeActor useLinkedInflowQueue (int capacity)
    {
        final OverflowPolicy policy = boundedInflowQueue.policy();
        final InflowQueue newQueue = new LinkedInflowQueue(capacity);
        replaceQueue(policy, newQueue);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeActor useOverflowPolicyDropAll ()
    {
        replaceQueue(OverflowPolicy.DROP_ALL, storageInflowQueue);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeActor useOverflowPolicyDropPending ()
    {
        replaceQueue(OverflowPolicy.DROP_PENDING, storageInflowQueue);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeActor useOverflowPolicyDropOldest ()
    {
        replaceQueue(OverflowPolicy.DROP_OLDEST, storageInflowQueue);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeActor useOverflowPolicyDropNewest ()
    {
        replaceQueue(OverflowPolicy.DROP_NEWEST, storageInflowQueue);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeActor useOverflowPolicyDropIncoming ()
    {
        replaceQueue(OverflowPolicy.DROP_INCOMING, storageInflowQueue);
        return this;
    }

    private void replaceQueue (final OverflowPolicy policy,
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

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeActor subscribe (final CascadeToken event)
    {
        /**
         * Synchronize to prevent concurrent unsubscriptions of the same event.
         */
        synchronized (this)
        {
            stage.dispatcher().register(event, syncInflowQueue);
            subscriptions.add(event);
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeActor unsubscribe (final CascadeToken event)
    {
        /**
         * Synchronize to prevent concurrent subscriptions of the same event.
         */
        synchronized (this)
        {
            stage.dispatcher().deregister(event, syncInflowQueue);
            subscriptions.remove(event);
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<CascadeToken> subscriptions ()
    {
        return ImmutableSet.copyOf(subscriptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActing ()
    {
        return acting.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive ()
    {
        return alive.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClosing ()
    {
        return closing.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClosed ()
    {
        return !isActive();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close ()
    {
        // TODO
        closing.set(true);
        alive.set(false);
        closing.set(false);
        acting.set(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void awaitClose (final Duration timeout)
            throws InterruptedException
    {
        Objects.requireNonNull(timeout, "timeout");
        actorAwaitCloseLatch.await(timeout.getNano(), TimeUnit.NANOSECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Instant creationTime ()
    {
        return creationTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int backlogSize ()
    {
        return syncInflowQueue.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int backlogCapacity ()
    {
        return syncInflowQueue.capacity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long receivedMessages ()
    {
        return boundedInflowQueue.offered(); // Thread-Safe via AtomicLong
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long droppedMessages ()
    {
        return boundedInflowQueue.dropped();  // Thread-Safe via AtomicLong
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long consumedMessages ()
    {
        return script.consumedMessageCount.get(); // Thread-Safe via AtomicLong
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long producedMessages ()
    {
        return producedMessageCount.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long undeliveredMessages ()
    {
        return undeliveredMessageCount.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long unhandledExceptions ()
    {
        return script.unhandledExceptionCount.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeActor registerSupervisor (final CascadeSupervisor supervisor)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeActor deregisterSupervisor (final CascadeSupervisor supervisor)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
