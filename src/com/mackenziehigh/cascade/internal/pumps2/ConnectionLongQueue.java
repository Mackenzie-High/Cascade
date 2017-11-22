package com.mackenziehigh.cascade.internal.pumps2;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fairness??
 */
public final class ConnectionLongQueue
{
    private final class OverflowBucket
    {
        public final Semaphore producerLock = new Semaphore(1);

        private volatile int value;

        private volatile boolean mark;

        public synchronized void set (final int value)
        {
            Verify.verify(mark == false);
            this.value = value;
            this.mark = true;
        }

        public synchronized int get ()
        {
            Verify.verify(mark);
            return value;
        }

        public synchronized void transfer (final Queue<Integer> queue)
        {
            /**
             * If the overflow bucket contains a value
             * and space is available inside of the queue,
             * then transfer the value to the queue and
             * then empty the bucket.
             */
            if (mark && queue.offer(value))
            {
                // Empty
                value = 0;
                mark = false;
                producerLock.release();
            }
        }
    }

    private final int producerCount;

    private final int capacity;

    private final Semaphore consumerLock;

    private final AtomicInteger size = new AtomicInteger();

    private final OverflowBucket[] overflow;

    private final Queue<Integer> queue = new ConcurrentLinkedQueue<>();

    public ConnectionLongQueue (final int producerCount,
                           final int capacity)
    {
        Preconditions.checkArgument(producerCount > 0, "producerCount <= 0");
        Preconditions.checkArgument(capacity > 0, "capacity <= 0");
        this.producerCount = producerCount;
        this.capacity = capacity;
        this.consumerLock = new Semaphore(capacity);
        this.consumerLock.drainPermits();
        this.overflow = new OverflowBucket[producerCount];

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
        /**
         * Add the value to the overflow area,
         * since the queue may be at capacity.
         */
        overflow[key].set(value);

        /**
         * Transfer as many elements from the overflow area
         * to the actual queue as possible, without violating
         * the capacity restrictions of the queue itself.
         */
        fixup();

        /**
         * Notify any waiting consumers that a new element is available.
         */
        size.incrementAndGet();
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
        else
        {
            size.decrementAndGet();
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
        final Integer value = queue.poll();
        Verify.verify(value != null);
        out.set(value);

        return true;
    }

    private void fixup ()
    {
        for (int i = 0; i < producerCount; i++)
        {
            overflow[i].transfer(queue);
        }
    }
}
