package com.mackenziehigh.cascade.allocator;

import com.google.common.base.Preconditions;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongConsumer;

/**
 * An instance of this class is a simple concurrent stack
 * implementation that can be used to store primitive longs.
 */
public final class ConcurrentLongStack
{
    private final Lock lock = new ReentrantLock();

    private final AtomicLongArray values;

    private final AtomicInteger topOfStack = new AtomicInteger(-1);

    /**
     * Sole Constructor.
     *
     * @param capacity is the maximum number of elements allowed at one time.
     */
    public ConcurrentLongStack (final int capacity)
    {
        Preconditions.checkArgument(capacity >= 0, "capacity < 0");
        this.values = new AtomicLongArray(capacity);
    }

    /**
     * This method retrieves the maximum number of elements
     * that are allowed in the stack at a single moment.
     *
     * @return the maximum number of elements herein.
     */
    public int capacity ()
    {
        return values.length();
    }

    /**
     * This method retrieves the number of elements currently herein.
     *
     * @return the size of this stack.
     */
    public int size ()
    {
        return topOfStack.get() + 1;
    }

    /**
     *
     * @return
     */
    public boolean isEmpty ()
    {
        return size() == 0;
    }

    /**
     * Use this method to push a new value onto the stack.
     *
     * @param value is the new value.
     * @return true, iff the push was successful.
     */
    public boolean push (final long value)
    {
        try
        {
            lock.lock();

            final int top = topOfStack.incrementAndGet();

            if (top >= values.length())
            {
                topOfStack.decrementAndGet(); // Undo incrementAndGet().
                return false;

            }
            else
            {
                values.set(top, value);
                return true;
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Use this method to get, but not remove, the top value on the stack.
     *
     * @param receiver will be passed the top value on the stack, if any.
     * @return true, iff there was a value to pass to the receiver.
     */
    public boolean peek (final LongConsumer receiver)
    {
        long value;

        try
        {
            lock.lock();

            final int top = this.topOfStack.get();

            if (top >= 0)
            {
                value = values.get(top);
            }
            else
            {
                return false;
            }
        }
        finally
        {
            lock.unlock();
        }

        receiver.accept(value);
        return true;
    }

    /**
     * Use this method to get and remove the top value from the stack.
     *
     * @param receiver will be passed the top value on the stack, if any.
     * @return true, iff there was a value to pass to the receiver.
     */
    public boolean pop (final LongConsumer receiver)
    {
        long value;

        try
        {
            lock.lock();

            final int top = topOfStack.decrementAndGet() + 1;

            if (top >= 0)
            {
                value = values.get(top);
            }
            else
            {
                topOfStack.incrementAndGet(); // Undo decrementAndGet().
                return false;
            }
        }
        finally
        {
            lock.unlock();
        }

        receiver.accept(value);
        return true;
    }
}
