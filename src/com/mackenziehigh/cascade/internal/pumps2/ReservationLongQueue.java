package com.mackenziehigh.cascade.internal.pumps2;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An instance of this class is a thread-safe finite-sized queue that allows
 * producer threads to make reservations before inserting elements into the queue,
 * which avoids thread-safety issues involving full queues.
 *
 * <p>
 * This type of queue is for storing elements of primitive-type (long) only.
 * </p>
 */
public final class ReservationLongQueue
{
    /**
     * An instance of this class will receive a single element
     * from a producer thread, store it until space becomes
     * available in the underlying queue, and then transfer
     * the element to the queue once space is available.
     */
    private final class ReservationBucket
    {
        /**
         * A producer thread must obtain this lock
         * in order to be able to add an element hereto.
         */
        public final Semaphore producerLock = new Semaphore(1);

        /**
         * This is the value obtained from the producer thread
         * that is waiting to be transferred to the queue.
         */
        private volatile long value;

        /**
         * This flag is true, iff there is a value in this bucket.
         */
        private volatile boolean mark;

        public synchronized void set (final long value)
        {
            Verify.verify(mark == false);
            this.value = value;
            this.mark = true;
        }

        public synchronized long get ()
        {
            Verify.verify(mark);
            return value;
        }

        public synchronized boolean transfer (final SynchronizedLongQueue queue)
        {
            /**
             * If the reservation bucket contains a value
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
                return true; // Transfer Occurred
            }
            else
            {
                return false; // No Transfer Occurred
            }
        }
    }

    /**
     * The number of available permits in this counting semaphore
     * is conceptually equal to the size of the underlying queue.
     * Consumers will obtain a permit from this semaphore in order
     * to be allowed to remove elements from the underlying queue.
     * In other words, a permit is only present in this semaphore,
     * if there is a corresponding element present in the queue.
     */
    private final Semaphore consumerPermits;

    /**
     * This is the underlying queue that we will add/remove values from.
     * We will not be directly adding values to this queue.
     * Instead, values will be placed into the reservation buckets
     * and then transferred into this queue as space becomes available.
     */
    private final SynchronizedLongQueue queue;

    /**
     * These are the reservation buckets the can be reserved by producers
     * in order to safely add values to the underlying queue without
     * risking violating the capacity restrictions.
     */
    private final ReservationBucket[] reservations;

    /**
     * These are the indexes of buckets that contain elements
     * that are waiting to be transferred to the underlying queue
     * as soon as space becomes available in the underlying queue.
     */
    private final SynchronizedLongQueue neededTransfers;

    /**
     * Sole Constructor.
     *
     * @param maxReservations is the number of reservation buckets.
     * @param capacity is the maximum size of the underlying queue.
     */
    public ReservationLongQueue (final int maxReservations,
                                 final int capacity)
    {
        Preconditions.checkArgument(maxReservations > 0, "reservationCount <= 0");
        Preconditions.checkArgument(capacity > 0, "capacity <= 0");
        this.consumerPermits = new Semaphore(capacity);
        this.consumerPermits.drainPermits();
        this.reservations = new ReservationBucket[maxReservations];
        this.neededTransfers = new SynchronizedLongQueue(maxReservations);
        this.queue = new SynchronizedLongQueue(capacity);

        for (int i = 0; i < reservations.length; i++)
        {
            reservations[i] = new ReservationBucket();
        }
    }

    /**
     * Getter.
     *
     * @return the current size of the underlying queue.
     */
    public int size ()
    {
        return queue.size();
    }

    /**
     * Getter.
     *
     * @return the maximum size of the underlying queue.
     */
    public int capacity ()
    {
        return queue.capacity();
    }

    /**
     * Use this method in order to try and make a reservation.
     *
     * @param key is the index of a pre-allocated reservation bucket.
     * @return true, if the reservation was made.
     */
    public boolean reserveAsync (final int key)
    {
        return reservations[key].producerLock.tryAcquire();
    }

    /**
     * Use this method in order to try and make a reservation,
     * waiting up to the given timeout, if necessary.
     *
     * @param key is the index of a pre-allocated reservation bucket.
     * @param timeout is the maximum amount of time to block.
     * @param units describes the timeout.
     * @return true, if the reservation was made.
     */
    public boolean reserveSync (final int key,
                                final long timeout,
                                final TimeUnit units)
    {
        try
        {
            return reservations[key].producerLock.tryAcquire(timeout, units);
        }
        catch (InterruptedException ex)
        {
            return false;
        }
    }

    /**
     * Use this method to add a value to this queue,
     * after you obtain a reservation.
     *
     * <p>
     * <b>Warning:</b> You must have already obtained a reservation
     * using the same key that you are passing-in to this method.
     * </p>
     *
     * @param key is the index of a pre-allocated reservation bucket.
     * @param value will be added to the queue, eventually.
     */
    public void put (final int key,
                     final long value)
    {
        /**
         * Add the value to the reservation area,
         * since the queue may be at capacity.
         */
        reservations[key].set(value);
        neededTransfers.offer(key);

        /**
         * Transfer as many elements from the reservation area
         * to the actual queue as possible, without violating
         * the capacity restrictions of the queue itself.
         */
        transferReservationsToQueue();

        /**
         * Notify any waiting consumers that a new element is available.
         */
        consumerPermits.release();
    }

    /**
     * Use this method to retrieve and remove the head of this queue,
     * if possible, waiting up to the given timeout for an element
     * to become available, if necessary.
     *
     * @param out will receive the value of the head element,
     * or zero, if no value is present.
     * @param timeout is the maximum amount of time to wait.
     * @param units describes the timeout.
     * @return true, if a value was actually retrieved.
     */
    public boolean poll (final AtomicLong out,
                         final long timeout,
                         final TimeUnit units)

    {
        try
        {
            /**
             * Wait for elements to be added to this queue.
             */
            if (consumerPermits.tryAcquire(timeout, units) == false)
            {
                out.set(0);
                return false;
            }
        }
        catch (InterruptedException ex)
        {
            out.set(0);
            return false;
        }

        /**
         * Transfer as many elements from the reservation area
         * to the actual queue as possible, without violating
         * the capacity restrictions of the queue itself.
         */
        transferReservationsToQueue();

        /**
         * Since we successfully obtained a permit,
         * there must exist an element in the queue for us.
         */
        final long value = queue.poll();
        out.set(value);

        return true;
    }

    /**
     * Note: This method must be synchronized; otherwise,
     * a race-condition may cause duplicate poll() calls
     * to the needed-transfers queue!
     */
    private synchronized void transferReservationsToQueue ()
    {
        /**
         * This loop is bounded by the length of the reservation array,
         * because we want a deterministic upper-bound on the number
         * of iterations. Otherwise, a race-condition could occur.
         */
        for (int i = 0; i < reservations.length && neededTransfers.size() > 0; i++)
        {
            /**
             * This identifies a bucket that definitely contains
             * data that needs to be transferred to the queue.
             */
            final int index = (int) neededTransfers.peek();

            /**
             * Try to transfer the data to the queue,
             * which will fail, if the queue is too full.
             */
            final boolean transferOccurred = reservations[index].transfer(queue);

            /**
             * If the transfer failed, then we will need to try again later,
             * so do not remove the index from the needed-transfers queue.
             */
            if (transferOccurred)
            {
                neededTransfers.poll();
            }
        }
    }
}
