package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.redo2.CascadeToken;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Partial Implementation of InflowQueue.
 */
public abstract class AbstractInflowQueue
        implements InflowQueue
{
    /**
     * Implementation-Specific Queue Operations.
     *
     * @param event is being enqueued.
     * @param message is being enqueued.
     */
    protected abstract void doCommit (CascadeToken event,
                                      CascadeAllocator.OperandStack message);

    /**
     * Implementation-Specific Queue Operations.
     *
     * @param out will receive the dequeued message.
     * @return the dequeued event, if available; otherwise, return null.
     */
    protected abstract CascadeToken doPoll (CascadeAllocator.OperandStack out);

    /**
     * This function will be invoked whenever a new event-message
     * is enqueued in order to notify the consumers.
     */
    private volatile Consumer<InflowQueue> callback = x ->
    {
        // Pass
    };

    /**
     * This is the capacity(), which is the maximum number of
     * event-messages that can be simultaneously enqueued herein.
     */
    private final int capacity;

    /**
     * This semaphore is used to keep the number of currently enqueued
     * event-messages below the capacity of this queue at all times.
     */
    private final Semaphore producerPermits;

    /**
     * This lock prevents concurrent consumers from causing
     * the internal state of this object to become invalid.
     */
    private final Lock consumerLock = new ReentrantLock();

    /**
     * In order for a caller to commit() a transaction,
     * they must pass-in this lock/key, which they
     * obtained from one of the lock(*) methods.
     * This is used to help ensure proper usage.
     */
    private final Object transactionKey = new Object();

    /**
     * This lock must be held by whatever thread that invokes the commit(*) method.
     *
     * <p>
     * Since this is a *reentrant* lock, please notice an important fact.
     * If (transactionInProgress && transactionLock.tryLock()) is true,
     * then the current thread already holds the transaction lock.
     * The tryLock() method will only return true (when the lock is held),
     * if the calling thread is the thread that holds the lock.
     * </p>
     */
    private final Lock transactionLock = new ReentrantLock();

    /**
     * In effect, this flag is used to prevent the transaction-lock
     * from being a reentrant lock, since we do not want that.
     */
    private volatile boolean transactionInProgress = false;

    /**
     * Sole Constructor.
     *
     * @param capacity will be the maximum size of the queue at any time.
     */
    public AbstractInflowQueue (final int capacity)
    {
        Preconditions.checkArgument(capacity >= 0, "capacity < 0");

        this.capacity = capacity;
        this.producerPermits = new Semaphore(capacity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object lock (final long timeout,
                        final TimeUnit timeoutUnits)
            throws InterruptedException
    {
        if (transactionInProgress && transactionLock.tryLock())
        {
            /**
             * In this case, we know that the current thread already
             * holds the transaction-lock and it is attempting to
             * start another simultaneous transaction.
             * Only one transaction can occur at a time,
             * even if both are being performed by the same thread.
             * Thus, do not allow the thread to obtain another
             * producer permit or the access-key (again).
             */
            return null;
        }
        else if (producerPermits.tryAcquire(timeout, timeoutUnits))
        {
            Verify.verify(size() < capacity());
            transactionLock.lock();
            transactionInProgress = true;
            return transactionKey;
        }
        else
        {
            /**
             * The queue is full. No producer permit was available.
             */
            Verify.verify(size() == capacity());
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object lock ()
    {
        if (transactionInProgress && transactionLock.tryLock())
        {
            /**
             * In this case, we know that the current thread already
             * holds the transaction-lock and it is attempting to
             * start another simultaneous transaction.
             * Only one transaction can occur at a time,
             * even if both are being performed by the same thread.
             * Thus, do not allow the thread to obtain another
             * producer permit or the access-key (again).
             */
            return null;
        }
        else if (producerPermits.tryAcquire())
        {
            Verify.verify(size() < capacity());
            transactionLock.lock();
            transactionInProgress = true;
            return transactionKey;
        }
        else
        {
            /**
             * The queue is full. No producer permit was available.
             */
            Verify.verify(size() == capacity());
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit (final Object key,
                        final CascadeToken event,
                        final CascadeAllocator.OperandStack message)
    {
        if (key == transactionKey) // Deliberate Identity Equals
        {
            /**
             * Implementation-Specific Queue Operations.
             */
            doCommit(event, message);

            /**
             * Notify the consumers of a new event-message.
             */
            callback.accept(this);
        }
        else if (key != null)
        {
            throw new IllegalArgumentException("Bug! - Wrong Key");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unlock (final Object key)
    {
        if (key == transactionKey) // Deliberate Identity Equals
        {
            transactionInProgress = false; // Do *not* move this below unlock()!
            transactionLock.unlock();
        }
        else if (key != null)
        {
            throw new IllegalArgumentException("Bug! - Wrong Key");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int capacity ()
    {
        return capacity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCallback (final Consumer<InflowQueue> functor)
    {
        callback = Objects.requireNonNull(functor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeToken poll (final CascadeAllocator.OperandStack out)
    {
        consumerLock.lock();

        try
        {
            /**
             * Implementation-Specific Queue Operations.
             */
            final CascadeToken event = doPoll(out);

            if (event != null)
            {
                /**
                 * We removed an event-message,
                 * so there is now room for a new one.
                 */
                producerPermits.release();
            }

            return event;
        }
        finally
        {
            consumerLock.unlock();
        }
    }
}
