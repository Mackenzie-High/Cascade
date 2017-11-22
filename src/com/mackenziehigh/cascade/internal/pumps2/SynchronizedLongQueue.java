package com.mackenziehigh.cascade.internal.pumps2;

import com.google.common.base.Preconditions;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This is a simple efficient thread-safe queue implementation
 * for storing values of primitive-type long.
 */
public final class SynchronizedLongQueue
{
    private final int capacity;

    private volatile int size = 0;

    private final long[] circularBuffer;

    private int tail = -1;

    private int head = -1;

    public SynchronizedLongQueue (final int capacity)
    {
        Preconditions.checkArgument(capacity >= 0, "capacity < 0");
        this.capacity = capacity;
        this.circularBuffer = new long[capacity];
    }

    /**
     * Use this method add a value to the tail of the queue,
     * if doing so would not violate the capacity restriction.
     *
     * @param value is the value to add, if possible.
     * @return true, if the value was actually added to the queue.
     */
    public synchronized boolean offer (final long value)
    {
        if (size >= capacity)
        {
            return false;
        }
        else
        {
            ++size;
            ++tail;
            circularBuffer[tail % capacity] = value;
            return true;
        }
    }

    /**
     * Use this method to retrieve the head of the queue, if possible.
     *
     * @param out will receive the value, if any.
     * @return true, if a value was actually retrieved.
     */
    public synchronized boolean poll (final AtomicLong out)
    {
        if (size == 0)
        {
            return false;
        }
        else
        {
            --size;
            ++head;
            out.set(circularBuffer[head % capacity]);
            return true;
        }
    }

    /**
     * Use this method to remove all elements from this queue instantly.
     */
    public synchronized void clear ()
    {
        size = 0;
        head = -1;
        tail = -1;
    }

    /**
     * Getter.
     *
     * @return the current size of the queue.
     */
    public int size ()
    {
        return size;
    }

    /**
     * Getter.
     *
     * @return the maximum size of the queue.
     */
    public int capacity ()
    {
        return capacity;
    }
}
