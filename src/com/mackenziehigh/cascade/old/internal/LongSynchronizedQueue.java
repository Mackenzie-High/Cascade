package com.mackenziehigh.cascade.old.internal;

import com.google.common.base.Preconditions;

/**
 * This is a simple efficient thread-safe queue implementation
 * for storing values of primitive-type (long).
 */
public final class LongSynchronizedQueue
{
    private final int capacity;

    private int size = 0;

    private final long[] circularBuffer;

    private int tail = 0;

    private int head = 0;

    public LongSynchronizedQueue (final int capacity)
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
            tail = (tail >= circularBuffer.length - 1) ? 0 : tail + 1;
            circularBuffer[tail] = value;
            return true;
        }
    }

    /**
     * Use this method to retrieve and remove the head of the queue, if possible.
     *
     * @return the value, if one was retrieved; otherwise, return zero.
     */
    public synchronized long poll ()
    {
        if (size == 0)
        {
            return 0;
        }
        else
        {
            --size;
            head = (head >= circularBuffer.length - 1) ? 0 : head + 1;
            return (circularBuffer[head]);
        }
    }

    /**
     * Use this method to retrieve, but not remove, the head of the queue, if possible.
     *
     * @return the value, if one was retrieved; otherwise, return zero.
     */
    public synchronized long peek ()
    {
        if (size == 0)
        {
            return 0;
        }
        else
        {
            final int index = (head >= circularBuffer.length - 1) ? 0 : head + 1;
            return (circularBuffer[index]);
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
     * @return true, if size() is zero.
     */
    public synchronized boolean isEmpty ()
    {
        return size() == 0;
    }

    /**
     * Getter.
     *
     * @return the current size of the queue.
     */
    public synchronized int size ()
    {
        return size;
    }

    /**
     * Getter.
     *
     * @return the maximum size of the queue.
     */
    public synchronized int capacity ()
    {
        return capacity;
    }
}
