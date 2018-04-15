package com.mackenziehigh.cascade.internal;

import com.google.common.base.Verify;
import java.time.Duration;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Prioritized Least-Recently-Used based Scheduler.
 *
 * @param <E>
 */
public final class Scheduler<E>
{
    /**
     * These are the processes that are currently *awaiting* execution.
     *
     * <p>
     * This queue does *not* contain any duplicates.
     * If a single Process is scheduled multiple times,
     * while it is awaiting or undergoing execution,
     * then a counter in the Process will be incremented.
     * This avoids unnecessary wastage of space in this queue.
     * Otherwise, in the context of scheduling actors,
     * this queue could grow to equal the sum of the sizes
     * of the inflow-queues of the scheduled actors.
     * </p>
     */
    public final BlockingQueue<Process<E>> scheduled = new PriorityBlockingQueue<>(32, Comparator.reverseOrder());

    private final Object lock = new Object();

    /**
     * Use this method to define a new process that can
     * be scheduled for execution using this scheduler.
     *
     * @return the newly defined process.
     */
    public Process<E> newProcess ()
    {
        return new Process(this);

    }

    public boolean isEmpty ()
    {
        return scheduled.isEmpty();
    }

    /**
     * Use this method to retrieve and remove the Process that needs executed next.
     *
     * @param timeout
     * @return the next scheduled Process, or null, if none is immediately available.
     * @throws java.lang.InterruptedException
     */
    public Process<E> poll (final Duration timeout)
            throws InterruptedException
    {
        final Process<E> next = scheduled.poll(timeout.toNanos(), TimeUnit.NANOSECONDS);
        Verify.verify(next == null || next.locked.get());
        return next;
    }

    /**
     * A Process represents an executable entity that can
     * be scheduled for execution using a Scheduler object.
     *
     * @param <T>
     */
    public static final class Process<T>
            implements AutoCloseable,
                       Comparable<Process<T>>
    {
        /**
         * This means something to the user of the Scheduler,
         * but not to us here, we just need to hold it.
         */
        private final AtomicReference<T> userObject = new AtomicReference<>();

        /**
         * This is the Scheduler that is able to schedule this Process.
         */
        private final Scheduler owner;

        /**
         * This counter is incremented every time that this Process is executed.
         */
        private final AtomicLong sequenceNumber = new AtomicLong();

        /**
         * This flag is true, when this Process is currently being executed.
         */
        private final AtomicBoolean locked = new AtomicBoolean();

        /**
         * This is the number of pending executions of this Process
         * that have yet to be performed. Only one reference to
         * this Process object will be enqueued in the Scheduler
         * queue at any one time. If this Process is scheduled
         * while it is enqueued pending execution, then this
         * counter will simply be incremented, which is more
         * efficient than placing multiple entries into
         * the Scheduler queue.
         */
        private final AtomicLong pendingExecutionCount = new AtomicLong();

        private Process (final Scheduler owner)
        {
            this.owner = Objects.requireNonNull(owner, "owner");
        }

        /**
         * Getter.
         *
         * @return the user-specified object corresponding to this Process.
         */
        public AtomicReference<T> getUserObject ()
        {
            return userObject;
        }

        /**
         * Use this method to cause this Process to be scheduled for execution.
         */
        public void schedule ()
        {
            synchronized (owner.lock)
            {
                if (locked.compareAndSet(false, true))
                {
                    owner.scheduled.add(this);
                }
                else
                {
                    pendingExecutionCount.incrementAndGet();
                }
            }
        }

        /**
         * Invoke this method after executing this Process
         * in order to notify the Scheduler that this Process
         * is no longer currently being executed.
         */
        @Override
        public void close ()
        {
            synchronized (owner.lock)
            {
                sequenceNumber.incrementAndGet();

                if (pendingExecutionCount.get() > 0)
                {
                    pendingExecutionCount.decrementAndGet();
                    locked.set(true);
                    owner.scheduled.add(this);
                }
                else
                {
                    pendingExecutionCount.set(0);
                    locked.set(false);
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo (final Process<T> other)
        {
            if (sequenceNumber.get() > other.sequenceNumber.get())
            {
                return -1; // Higher Seq Num == Lower Priority
            }
            else if (sequenceNumber.get() < other.sequenceNumber.get())
            {
                return +1; // Lower Seq Num == Higher Priority
            }
            else
            {
                return 0;
            }
        }
    }

}
