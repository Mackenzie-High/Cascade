package com.mackenziehigh.cascade.internal;

import com.google.common.base.Verify;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

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
     * This avoids unnecessary wastage of space.
     * </p>
     */
    private final BlockingQueue<Process<E>> scheduled = new PriorityBlockingQueue<>(32, Comparator.reverseOrder());

    private final Object lock = new Object();

    /**
     * Use this method to define a new process that can
     * be scheduled for execution using this scheduler.
     *
     * @param priority will be the priority of the new process.
     * @param userObject a user-defined object to associate with the new process.
     * @return the newly defined process.
     */
    public Process<E> newProcess (final int priority,
                                  final E userObject)
    {
        if (priority < 0)
        {
            throw new IllegalArgumentException("priority < 0");
        }
        else
        {
            return new Process(this, priority, userObject);
        }
    }

    /**
     * Use this method to retrieve and remove the Process that needs executed next,
     * blocking if necessary, for a Process to become available.
     *
     * @param timeout is the maximum number of milliseconds to wait.
     * @return the next scheduled Process, or null, if the timeout occurred.
     * @throws InterruptedException if an interruption occurs while waiting.
     */
    public Process<E> poll (final long timeout)
            throws InterruptedException
    {
        if (timeout < 1)
        {
            throw new IllegalArgumentException("timeout < 1");
        }
        else
        {

            final Process<E> next = scheduled.poll(timeout, TimeUnit.MILLISECONDS);
            Verify.verify(next == null || next.locked.get());
            return next;
        }
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
        private final T userObject;

        /**
         * This is the Scheduler that is able to schedule this Process.
         */
        private final Scheduler owner;

        /**
         * This is the priority with which the Scheduler will treat
         * this Process relative to other Processes.
         *
         * <p>
         * Larger numbers equal higher priorities.
         * </p>
         */
        private final int priority;

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

        private Process (final Scheduler owner,
                         final int priority,
                         final T userObject)
        {
            this.owner = Objects.requireNonNull(owner, "owner");
            this.priority = priority;
            this.userObject = Objects.requireNonNull(userObject, "userObject");
        }

        /**
         * Getter.
         *
         * @return the user-specified object corresponding to this Process.
         */
        public T getUserObject ()
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
            if (priority != other.priority)
            {
                return Integer.compare(priority, other.priority);
            }
            else if (sequenceNumber.get() > other.sequenceNumber.get())
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

    public static void main (String[] args)
            throws InterruptedException
    {
        final Scheduler<String> ss = new Scheduler<>();

        ss.newProcess(0, "A").schedule();
        ss.newProcess(1, "B").schedule();

        while (true)
        {
            final Process<String> ps = ss.poll(1000L);
            if (ps != null)
            {
                try (Process cs = ps)
                {
                    System.out.println("X = " + cs.getUserObject());
                }
            }
        }
    }
}
