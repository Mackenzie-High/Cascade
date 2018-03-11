package com.mackenziehigh.cascade.old.internal;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * An instance of this interface allows threads to individually
 * process tasks from a set of streams of tasks, such that no
 * two threads are processing tasks from the same stream at
 * the same time and tasks are fairly scheduled.
 *
 * @param <T> provides tasks.
 */
public interface Scheduler<T>
{
    /**
     * An instance of this interface is a lockable source of tasks.
     *
     * @param <T>
     */
    public interface TaskStream<T>
    {
        /**
         * Getter.
         *
         * @return the actual source of tasks.
         */
        public T source ();

        /**
         * Use this method to release the consumer lock on this stream,
         * so that other consumer threads can receive tasks therefrom.
         */
        public void release ();
    }

    /**
     * Getter.
     *
     * @return the task streams managed by this scheduler.
     */
    public Map<T, TaskStream<T>> streams ();

    /**
     * Use this method in order to notify the scheduler that a given
     * stream has enqueued a new task that needs to be scheduled for
     * consumption by one of the consumer threads.
     *
     * @param stream contains the source of the new task.
     * @throws ClassCastException if stream is not managed by this scheduler.
     */
    public void addTask (TaskStream<T> stream);

    /**
     * Use this method in order to wait for a task to become available.
     *
     * <p>
     * The returned task-stream will be conceptually locked,
     * so that only the caller can use the task-stream,
     * until the caller explicitly invokes the release()
     * method thereon.
     * </p>
     *
     * @param timeout is the maximum amount of time to wait.
     * @param timeoutUnit describes the timeout.
     * @return a task-stream containing a task for the caller,
     * or null, if no task is available or awake() was called.
     * @throws java.lang.InterruptedException
     */
    public TaskStream<T> pollTask (long timeout,
                                   TimeUnit timeoutUnit)
            throws InterruptedException;

}
