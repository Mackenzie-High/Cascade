package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeChannel;
import com.mackenziehigh.cascade.CascadeContext;
import com.mackenziehigh.cascade.CascadeDirector;
import com.mackenziehigh.cascade.CascadeScript;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeStage;
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
import java.util.concurrent.atomic.AtomicReference;

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
     * This is the name of this actor, which may change over time.
     */
    private volatile String name = uuid.toString();

    /**
     * True, iff the onSetup() of the script was already executed.
     */
    private final AtomicBoolean initialized = new AtomicBoolean();

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
     * These are the supervisors that monitor this actor.
     */
    private final InternalSupervisors directors = new InternalSupervisors();

    private final AtomicReference<CascadeToken> event = new AtomicReference<>();

    private final AtomicReference<CascadeStack> stack = new AtomicReference<>();

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
        this.script = new InternalScript(script);
        this.storageInflowQueue = new LinkedInflowQueue(Integer.MAX_VALUE);
        this.boundedInflowQueue = new BoundedInflowQueue(BoundedInflowQueue.OverflowPolicy.DROP_INCOMING, storageInflowQueue);
        this.swappableInflowQueue = new SwappableInflowQueue(boundedInflowQueue);
        this.schedulerInflowQueue = new NotificationInflowQueue(swappableInflowQueue, q -> onQueueAdd(q));
        this.syncInflowQueue = new SynchronizedInflowQueue(schedulerInflowQueue);
        this.task = stage.scheduler().newProcess(0, this);
    }

    public void act ()
    {
        synchronized (this)
        {
            try
            {
                setupIfNeeded();
            }
            catch (Throwable ex)
            {
                reportUnhandledException(ex);
                close();
                return;
            }

            try
            {
                processMessageIfNeeded();
            }
            catch (Throwable ex)
            {
                reportUnhandledException(ex);
            }

            try
            {
                shutdownIfNeeded();
            }
            catch (Throwable ex)
            {
                reportUnhandledException(ex);
            }
        }
    }

    private void setupIfNeeded ()
            throws Throwable
    {
        if (initialized.compareAndSet(false, true))
        {
            script.onSetup(context);
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
    {

    }

    private void reportUnhandledException (final Throwable cause)
    {
        try
        {
            script.unhandledExceptionCount.incrementAndGet();
            script.onException(context, cause);
        }
        catch (Throwable ex)
        {
            script.unhandledExceptionCount.incrementAndGet();
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
        task.schedule();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeContext context ()
    {
        return context;
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
    public long acceptedMessages ()
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
    public long unhandledExceptions ()
    {
        return script.unhandledExceptionCount.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeActor registerDirector (final CascadeDirector director)
    {
        Objects.requireNonNull(director, "director");
        directors.register(director);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeActor deregisterDirector (final CascadeDirector director)
    {
        Objects.requireNonNull(director, "director");
        directors.deregister(director);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeActor tell (final CascadeToken event,
                              final CascadeStack stack)
    {
        Preconditions.checkNotNull(event, "event");
        Preconditions.checkNotNull(stack, "stack");
        syncInflowQueue.offer(event, stack);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeActor named (final String name)
    {
        this.name = Objects.requireNonNull(name, "name");
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name ()
    {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasArrayInflowQueue ()
    {
        return storageInflowQueue instanceof ArrayInflowQueue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasLinkedInflowQueue ()
    {
        return storageInflowQueue instanceof LinkedInflowQueue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOverflowPolicyDropAll ()
    {
        return boundedInflowQueue.policy() == OverflowPolicy.DROP_ALL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOverflowPolicyDropPending ()
    {
        return boundedInflowQueue.policy() == OverflowPolicy.DROP_PENDING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOverflowPolicyDropOldest ()
    {
        return boundedInflowQueue.policy() == OverflowPolicy.DROP_OLDEST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOverflowPolicyDropNewest ()
    {
        return boundedInflowQueue.policy() == OverflowPolicy.DROP_NEWEST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOverflowPolicyDropIncoming ()
    {
        return boundedInflowQueue.policy() == OverflowPolicy.DROP_INCOMING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<CascadeDirector> directors ()
    {

        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<CascadeChannel> subscriptions ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
