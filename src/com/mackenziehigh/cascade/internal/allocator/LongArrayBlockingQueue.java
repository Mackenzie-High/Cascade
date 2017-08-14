package com.mackenziehigh.cascade.internal.allocator;

import com.google.common.base.Preconditions;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongConsumer;

/**
 * This class provides a blocking-queue implementation that
 * can efficiently stores primitive-long elements without
 * the need to perform auto-boxing, etc.
 */
public final class LongArrayBlockingQueue
{
    private final ReentrantLock lock = new ReentrantLock();

    private final Condition notFull = lock.newCondition();

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
     * Use this method to add an element to the queue,
     * waiting until room is available if necessary.
     *
     * <p>
     * If the queue is full, then this method will block until
     * room becomes available or a thread interrupt occurs.
     * </p>
     *
     * @param value is the element to add to the queue.
     * @throws InterruptedException if this thread is interrupted.
     */
    public void put (final long value)
            throws InterruptedException
    {
        while (offer(value) == false)
        {
            notFull.await();
        }
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
     * @param receiver will be passed the retrieved value, if any.
     * @return true, if a value was successfully retrieved;
     * otherwise, return false.
     */
    public boolean poll (final LongConsumer receiver)
    {
        Preconditions.checkNotNull(receiver, "receiver");

        boolean retrieved = false;
        long value;

        lock.lock();
        try
        {
            if (isEmpty())
            {
                return false;
            }
            else
            {
                value = elements[head];
                --size;
                ++head;
                head = head >= capacity() ? 0 : head;
                head = isEmpty() ? -1 : head;
                tail = isEmpty() ? -1 : tail;
                retrieved = true;
                notFull.signal();
            }
        }
        finally
        {
            lock.unlock();
        }

        if (retrieved)
        {
            receiver.accept(value);
        }

        return retrieved;
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
     * @param receiver will be passed the retrieved value, if any.
     * @param timeout is the maximum amount of time to wait.
     * @param unit is the units (seconds, milliseconds, etc) of timeout.
     * @return true, if a value was successfully retrieved;
     * otherwise, return false.
     * @throws java.lang.InterruptedException if an interrupt occurs.
     */
    public boolean poll (final LongConsumer receiver,
                         final long timeout,
                         final TimeUnit unit)
            throws InterruptedException
    {

        if (poll(receiver))
        {
            return true;
        }
        else
        {
            notEmpty.await(timeout, unit);
            return poll(receiver);
        }
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
        final LongArrayBlockingQueue q = new LongArrayBlockingQueue(3);

        q.offer(100);
        q.offer(200);
        q.offer(300);

        q.poll(x -> System.out.println(x));
        q.poll(x -> System.out.println(x));
        q.poll(x -> System.out.println(x));
        q.offer(400);
        q.offer(500);
        q.poll(x -> System.out.println(x));
        q.poll(x -> System.out.println(x));
    }
}
