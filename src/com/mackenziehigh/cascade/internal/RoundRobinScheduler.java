package com.mackenziehigh.cascade.internal;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This is a task-scheduler that uses a simple round-robin algorithm
 * in order to schedule tasks, such that no two tasks run simultaneously.
 *
 * @param <T>
 */
public final class RoundRobinScheduler<T>
        implements Scheduler<T>
{

    private final Map<T, TaskStream<T>> streams = Maps.newConcurrentMap();

    private final Map<T, TaskStream<T>> unmodStreams = Collections.unmodifiableMap(streams);

    private final BlockingQueue<TaskStreamImp> queue;

    /**
     * Sole Constructor.
     *
     * @param sources are usually queues of awaiting tasks.
     */
    public RoundRobinScheduler (final Collection<T> sources)
    {
        sources.forEach(x -> streams.put(x, new TaskStreamImp(x)));

        /**
         * This queue must be big enough to hold all of the task-streams.
         * If no task-streams are passed-in, then use a size of one;
         * otherwise, an exception would occur.
         */
        queue = new ArrayBlockingQueue<>(sources.isEmpty() ? 1 : sources.size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<T, TaskStream<T>> streams ()
    {
        return unmodStreams;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addTask (final TaskStream<T> stream)
    {
        final TaskStreamImp tasker = (TaskStreamImp) stream;
        tasker.addTask();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TaskStream<T> pollTask (final long timeout,
                                   final TimeUnit timeoutUnit)
            throws InterruptedException
    {
        final TaskStreamImp task = queue.poll(timeout, timeoutUnit);
        return task;
    }

    private final class TaskStreamImp
            implements TaskStream<T>
    {
        private final T source;

        /**
         * This flag is true, when either one of two things is true.
         * 1. There are pending tasks in this stream, but no consumer
         * is currently processing a task from this stream.
         * 2. Exactly one consumer is currently processing a task
         * from this stream, and thus, that thread holds the conceptual
         * lock that must be released by calling release() herein.
         */
        private volatile boolean scheduled = false;

        /**
         * This is the number of pending tasks in this stream.
         */
        private volatile int count = 0;

        public TaskStreamImp (final T src)
        {
            this.source = src;
        }

        @Override
        public T source ()
        {
            return source;
        }

        /**
         * This method is synchronized, which ensures that the addTask() method
         * cannot be invoked simultaneously on *this* particular task-stream.
         */
        @Override
        public synchronized void release ()
        {
            --count;

            if (count < 0)
            {
                throw new IllegalStateException("Too Many release() Calls = Bug!");
            }
            else if (count == 0)
            {
                /**
                 * We do *not* need to add the task to the queue, anymore,
                 * until the producer thread adds another task to the stream,
                 * because there are no pending tasks.
                 */
                scheduled = false;
            }
            else // count > 0
            {
                /**
                 * Although one task has now been completed, more tasks are pending;
                 * therefore, we need to add this stream to the scheduling queue,
                 * so that the consumer threads can (eventually) process those tasks.
                 */
                queue.add(this);
            }
        }

        /**
         * This method is synchronized, which ensures that the release() method
         * cannot be invoked simultaneously on *this* particular task-stream.
         */
        public synchronized void addTask ()
        {
            if (scheduled)
            {
                /**
                 * Since the stream has already been scheduled,
                 * there are already tasks that are *either*
                 * enqueued or actively being processed.
                 * Therefore, we only want to increment the counter,
                 * because adding the stream to the scheduling queue
                 * now could result in two consumer threads processing
                 * messages from the same stream simultaneously,
                 * which would be contractually inappropriate.
                 */
                ++count;
            }
            else
            {
                /**
                 * Until this flag is set to false, the release()
                 * and pollTask() methods will be responsible for
                 * adding and removing this task-stream to/from
                 * the scheduling queue.
                 */
                scheduled = true;
                ++count;
                queue.add(this);
            }
        }
    }
}
