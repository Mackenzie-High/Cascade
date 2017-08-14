package com.mackenziehigh.cascade.internal.allocator;

import com.google.common.base.Preconditions;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

/**
 * This class provides a blocking-queue implementation that
 * can efficiently stores primitive-long elements without
 * the need to perform auto-boxing, etc.
 */
public final class LongArrayBlockingQueue
{
    private final ReentrantLock lock = new ReentrantLock();

    private final Condition notEmpty = lock.newCondition();

    /**
     * Elements are removed from the head of the queue.
     */
    private int head = -1;

    /**
     * Elements are added to the tail of the queue.
     */
    private int tail = -1;

    /**
     * This is the current number of enqueued elements.
     */
    private volatile int size = 0;

    /**
     * This is a circular buffer containing the enqueued elements,
     * were head points to the least-recently-added element
     * and tail points to the most-recently-added element.
     */
    private final long[] elements;

    /**
     * Sole Constructor.
     *
     * @param capacity is the maximum size() at any one time.
     */
    public LongArrayBlockingQueue (final int capacity)
    {
        Preconditions.checkArgument(capacity >= 0, "capacity < 0");
        this.elements = new long[capacity];
    }

    /**
     * Use this method in order to attempt to add an element to the queue.
     *
     * <p>
     * If the queue is full, this method will return false.
     * </p>
     *
     * @param value is the element to add to the queue.
     * @return true, iff the element was successfully added;
     * otherwise, return false.
     */
    public boolean offer (final long value)
    {
        boolean inserted = false;

        lock.lock();
        try
        {
            if (size() == capacity())
            {
                return false;
            }
            else
            {
                ++size;
                ++tail;
                tail = tail >= capacity() ? 0 : tail;
                elements[tail] = value;
                head = size == 1 ? tail : head;
                notEmpty.signal();
                inserted = true;
            }
        }
        finally
        {
            lock.unlock();
        }

        return inserted;
    }

    /**
     * Use this method in order to attempt to retrieve an element from the queue.
     *
     * <p>
     * If the queue is empty, then this method will return false.
     * </p>
     *
     * @param defaultValue will be returned, if the timeout expires.
     * @return the retrieved element (on success) or the default-value (on empty).
     */
    public long poll (final long defaultValue)
    {
        long value = defaultValue;

        lock.lock();
        try
        {
            if (isEmpty())
            {
                return defaultValue;
            }
            else
            {
                value = remove();
            }
        }
        finally
        {
            lock.unlock();
        }

        return value;
    }

    /**
     * Use this method in order to retrieve an element from the queue,
     * waiting for an element to become available if necessary.
     *
     * <p>
     * If the queue is empty, then this method will block until either
     * an element becomes available or a thread interrupt occurs.
     * </p>
     *
     * @param defaultValue will be returned, if the timeout expires.
     * @param timeout is the maximum amount of time to wait.
     * @param unit is the units (seconds, milliseconds, etc) of timeout.
     * @return the retrieved element (on success) or the default-value (on timeout).
     * @throws java.lang.InterruptedException if an interrupt occurs.
     */
    public long poll (final long defaultValue,
                      final long timeout,
                      final TimeUnit unit)
            throws InterruptedException
    {
        Preconditions.checkNotNull(unit, "unit");
        Preconditions.checkArgument(timeout >= 0, "timeout < 0");

        boolean retrieved = false;
        long value = defaultValue;

        long nanos = unit.toNanos(timeout);

        lock.lockInterruptibly();
        try
        {
            while (retrieved == false)
            {
                if (isEmpty() == false)
                {
                    value = remove();
                    retrieved = true;
                    break;
                }
                else if (nanos <= 0L)
                {
                    return defaultValue;
                }

                nanos = notEmpty.awaitNanos(nanos);
            }
        }
        finally
        {
            lock.unlock();
        }

        return value;
    }

    private long remove ()
    {
        final long value = elements[head];
        --size;
        ++head;
        head = head >= capacity() ? 0 : head;
        head = isEmpty() ? -1 : head;
        tail = isEmpty() ? -1 : tail;
        return value;
    }

    /**
     * This method retrieves the size of the queue.
     *
     * @return the current queue size.
     */
    public int size ()
    {
        return size;
    }

    /**
     * This method determines whether the queue is empty.
     *
     * @return true, if the queue is empty.
     */
    public boolean isEmpty ()
    {
        return size() == 0;
    }

    /**
     * This method retrieves the capacity of the queue.
     *
     * @return the queue capacity.
     */
    public int capacity ()
    {
        return elements.length;
    }

    public static void main (String[] args)
            throws InterruptedException
    {
        final LongArrayBlockingQueue q = new LongArrayBlockingQueue(5000 * 1000);

        final Runnable consumer = () ->
        {
            final AtomicLong sum = new AtomicLong();

            IntStream.range(0, 4000 * 1000).forEach(i ->
            {
                try
                {
                    final long value = q.poll(-17, 5, TimeUnit.SECONDS);
                    sum.addAndGet(value);
                }
                catch (InterruptedException ex)
                {
                    ex.printStackTrace(System.out);
                }
            });

            System.out.println(Thread.currentThread().getName() + " = " + sum);
        };

        final Runnable producer = () -> IntStream.range(0, 9000 * 1000).forEach(i -> q.offer(i));

        new Thread(consumer).start();
        new Thread(consumer).start();

        new Thread(producer).start();
    }
}
