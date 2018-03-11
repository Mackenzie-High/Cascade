package com.mackenziehigh.cascade.internal;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Least-Recently-Used based Scheduler.
 */
public final class Scheduler<E>
{
    private final AtomicLong counter = new AtomicLong();

    private final BlockingQueue<Process> scheduled = new PriorityBlockingQueue<>(32, Scheduler::compare);

    private final CriticalSection empty = new CriticalSection(null);

    private final Object lock = new Object();

    public Process<E> newProcess (final int priority,
                                  final E element)
    {
        if (priority < 0)
        {
            throw new IllegalArgumentException("priority < 0");
        }
        else
        {
            return new Process(this, priority, element);
        }
    }

    public void schedule (final Process task)
    {
        synchronized (lock)
        {
            if (task == null)
            {
                throw new NullPointerException("task");
            }
            else if (task.owner != this)
            {
                throw new IllegalArgumentException("Wrong Scheduler for Process");
            }
            else if (task.locked.compareAndSet(false, true))
            {
                task.sequenceNumber.set(counter.incrementAndGet());
                scheduled.add(task);
            }
            else
            {
                task.count.incrementAndGet();
            }
        }
    }

    public CriticalSection enter (final long timeout)
            throws InterruptedException
    {
        if (timeout < 1)
        {
            throw new IllegalArgumentException("timeout < 1");
        }
        else
        {
            final Process next = scheduled.poll(timeout, TimeUnit.MILLISECONDS);
            return next == null ? empty : next.criticalSection;
        }
    }

    private static int compare (final Process left,
                                final Process right)
    {
        if (left.priority != right.priority)
        {
            return Integer.compare(left.priority, right.priority);
        }
        else if (left.sequenceNumber.get() > right.sequenceNumber.get())
        {
            return -1; // Higher Seq Num == Lower Priority
        }
        else if (left.sequenceNumber.get() < right.sequenceNumber.get())
        {
            return +1; // Lower Seq Num == Higher Priority
        }
        else
        {
            return 0;
        }
    }

    public static final class Process<T>
    {
        private final T element;

        private final Scheduler owner;

        private final int priority;

        private final AtomicLong sequenceNumber = new AtomicLong();

        private final AtomicBoolean locked = new AtomicBoolean();

        private final AtomicLong count = new AtomicLong();

        private final CriticalSection<T> criticalSection = new CriticalSection(this);

        private Process (final Scheduler owner,
                         final int priority,
                         final T element)
        {
            this.owner = Objects.requireNonNull(owner, "owner");
            this.priority = priority;
            this.element = Objects.requireNonNull(element, "element");
        }

        public T element ()
        {
            return element;
        }
    }

    public static final class CriticalSection<T>
            implements AutoCloseable
    {
        private final Process process;

        private CriticalSection (final Process process)
        {
            this.process = process;
        }

        public boolean isEmpty ()
        {
            return process == null;
        }

        public Process<T> task ()
        {
            return process;
        }

        @Override
        public void close ()
        {
            if (process == null)
            {
                return;
            }

            synchronized (process.owner.lock)
            {
                if (process.count.get() > 0)
                {
                    process.count.decrementAndGet();
                    process.locked.set(true);
                    process.sequenceNumber.set(process.owner.counter.incrementAndGet());
                    process.owner.scheduled.add(process);
                }
                else
                {
                    process.count.set(0);
                    process.locked.set(false);
                }
            }
        }

    }

    public static void main (String[] args)
            throws InterruptedException
    {
        final Scheduler ss = new Scheduler();

        final Process p = ss.newProcess(0, "A");

        ss.schedule(p);

        while (true)
        {
            try (Scheduler.CriticalSection cs = ss.enter(1000L))
            {
                if (cs.isEmpty())
                {
                    continue;
                }

                System.out.println("X = " + cs.task().element());

            }
        }
    }
}
