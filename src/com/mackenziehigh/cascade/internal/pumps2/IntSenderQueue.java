package com.mackenziehigh.cascade.internal.pumps2;

import com.google.common.base.Preconditions;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Fairness??
 */
public final class IntSenderQueue
{
    private final class OverflowBucket
    {
        public final Semaphore producerLock = new Semaphore(1);

        public final Lock mutatorLock = new ReentrantLock();

        public volatile int value;

        public volatile boolean mark;
    }

    private final int producerCount;

    private final int capacity;

    private final Semaphore consumerLock;

    private final AtomicInteger size = new AtomicInteger();

    private final OverflowBucket[] overflow;

    private final Queue<Integer> queue = new LinkedList<>();

    private final Lock queueLock = new ReentrantLock();

    public IntSenderQueue (final int producerCount,
                           final int capacity)
    {
        Preconditions.checkArgument(producerCount > 0, "producerCount <= 0");
        Preconditions.checkArgument(capacity > 0, "capacity <= 0");
        this.producerCount = producerCount;
        this.capacity = capacity;
        this.consumerLock = new Semaphore(capacity);
        this.consumerLock.drainPermits();
        this.overflow = new OverflowBucket[producerCount];
//        this.queue = new int[capacity + producerCount];

        for (int i = 0; i < overflow.length; i++)
        {
            overflow[i] = new OverflowBucket();
        }
    }

    public int size ()
    {
        return size.get();
    }

    public int capacity ()
    {
        return capacity;
    }

    public boolean reserveAsync (final int key)
    {
        return overflow[key].producerLock.tryAcquire();
    }

    public boolean reserveSync (final int key,
                                final long timeout,
                                final TimeUnit units)
    {
        try
        {
            return overflow[key].producerLock.tryAcquire(timeout, units);
        }
        catch (InterruptedException ex)
        {
            return false;
        }
    }

    public void add (final int key,
                     final int value)
    {
        overflow[key].mutatorLock.lock();

        try
        {
            /**
             * Add the value to the overflow area,
             * since the queue may be at capacity.
             */
            overflow[key].value = value;
            overflow[key].mark = true;
            size.incrementAndGet();
        }
        finally
        {
            overflow[key].mutatorLock.unlock();
        }

        /**
         * Transfer as many elements from the overflow area
         * to the actual queue as possible, without violating
         * the capacity restrictions of the queue itself.
         */
        fixup();

        /**
         * Notify any waiting consumers that a new element is available.
         */
        consumerLock.release();
    }

    public boolean poll (final AtomicInteger out,
                         final long timeout,
                         final TimeUnit units)
            throws InterruptedException
    {
        /**
         * Wait for elements to be added to this queue.
         */
        if (consumerLock.tryAcquire(timeout, units) == false)
        {
            return false;
        }

        /**
         * Transfer as many elements from the overflow area
         * to the actual queue as possible, without violating
         * the capacity restrictions of the queue itself.
         */
        fixup();

        /**
         * Since we successfully obtained a permit from the consumerLock,
         * there must exist an element in the queue for us.
         */
        queueLock.lock();

        try
        {
            final int value = queue.poll();
            out.set(value);
        }
        finally
        {
            queueLock.unlock();
        }

        return true;
    }

    private void fixup ()
    {
        queueLock.lock();

        try
        {
            for (int i = 0; i < producerCount; i++)
            {
                fixup(i);
            }
        }
        finally
        {
            queueLock.unlock();
        }
    }

    private void fixup (final int i)
    {
        overflow[i].mutatorLock.lock();

        try
        {
            /**
             * If the overflow bucket contains a value,
             * then transfer the value to the actual queue
             * and then empty the bucket.
             */
            if (overflow[i].mark)
            {
                // Transfer
                queue.add(overflow[i].value);

                // Empty
                overflow[i].value = 0;
                overflow[i].mark = false;
                overflow[i].producerLock.release();
            }
        }
        finally
        {
            overflow[i].mutatorLock.unlock();
        }
    }
}
