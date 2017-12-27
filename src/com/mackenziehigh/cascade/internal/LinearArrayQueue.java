package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Single Queue, Single Node, Single Thread.
 */
public final class LinearArrayQueue
        implements Connection
{
    private final int capacity;

    private final Semaphore producerPermits;

    private final Lock consumerLock = new ReentrantLock();

    private final Object transactionKey = new Object();

    private final ArrayBlockingQueue<CascadeToken> eventQueue;

    private final LongSynchronizedQueue messageQueue;

    private final OperandStackStorage messageStorage;

    private volatile Consumer<Connection> callback;

    /**
     * This lock must be held by whatever thread
     * that invokes the commit(*) method.
     */
    private final Lock transactionLock = new ReentrantLock();

    public LinearArrayQueue (final CascadeAllocator allocator,
                             final int capacity)
    {
        this.capacity = capacity;
        this.producerPermits = new Semaphore(capacity);
        this.messageStorage = new OperandStackStorage(allocator, capacity);
        this.eventQueue = new ArrayBlockingQueue<>(capacity);
        this.messageQueue = new LongSynchronizedQueue(capacity);
    }

    @Override
    public Object lock (final long timeout,
                        final TimeUnit timeoutUnits)
    {
        try
        {
            if (producerPermits.tryAcquire(timeout, timeoutUnits))
            {
                transactionLock.lock();
                return transactionKey;
            }
            else
            {
                return null;
            }
        }
        catch (InterruptedException ex)
        {
            return null; // TODO: Propagate?????
        }
    }

    @Override
    public Object lock ()
    {
        if (producerPermits.tryAcquire())
        {
            transactionLock.lock();
            return transactionKey;
        }
        else
        {
            return null;
        }
    }

    @Override
    public void commit (final Object key,
                        final CascadeToken event,
                        final CascadeAllocator.OperandStack message)
    {
        if (key == transactionKey)
        {
            eventQueue.offer(event);
            final int pos = messageStorage.set(message);
            messageQueue.offer(pos);
            callback.accept(this);
        }
        else if (key != null)
        {
            throw new IllegalArgumentException("Wrong Key");
        }
    }

    @Override
    public void unlock (final Object key)
    {
        if (key == transactionKey)
        {
            transactionLock.unlock();
        }
        else if (key != null)
        {
            throw new IllegalArgumentException("Wrong Key");
        }
    }

    @Override
    public int localSize ()
    {
        return eventQueue.size();
    }

    @Override
    public int localCapacity ()
    {
        return capacity;
    }

    @Override
    public int globalSize ()
    {
        return localSize();
    }

    @Override
    public int globalCapacity ()
    {
        return localCapacity();
    }

    @Override
    public void close ()
    {
        messageStorage.close();
    }

    @Override
    public void setCallback (final Consumer<Connection> functor)
    {
        callback = Objects.requireNonNull(functor);
    }

    @Override
    public CascadeToken poll (final OperandStack out)
    {
        consumerLock.lock();

        try
        {
            final CascadeToken event = eventQueue.poll();

            if (event != null)
            {
                final int idx = (int) messageQueue.poll();
                messageStorage.get(idx, out);
                producerPermits.release();
                return event;
            }
            else
            {
                return null;
            }
        }
        finally
        {
            consumerLock.unlock();
        }
    }

}
