package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.redo2.CascadeToken;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Conceptually, this is a queue containing event-messages,
 * which are awaiting processing.
 *
 * <p>
 * The transaction-lock described herein is a conceptual entity.
 * In reality, the transaction-lock may consist of multiple
 * locks that must be obtained in sequence, etc.
 * </p>
 *
 * <p>
 * The transaction methods herein return or use an access-key.
 * The access-key is simply passed around in order to promote
 * proper usage of instances of this class, by requiring
 * that only the thread holding the access-key can manipulate
 * the transaction.
 * </p>
 */
public interface InflowQueue
{

    /**
     * Use this method to attempt to obtain the transaction-lock,
     * blocking if necessary upto the given timeout.
     *
     * <p>
     * At some point after successfully obtaining an access-key
     * from this method, you must invoke unlock(*).
     * </p>
     *
     * @param timeout is the maximum amount of time to wait.
     * @param timeoutUnits describes the timeout.
     * @return an access-key, if the lock was obtained; otherwise, return null.
     * @throws java.lang.InterruptedException without obtaining the lock.
     */
    public Object lock (long timeout,
                        TimeUnit timeoutUnits)
            throws InterruptedException;

    /**
     * Use this method to attempt to obtain the transaction-lock,
     * without blocking if the lock cannot immediately be obtained.
     *
     * <p>
     * At some point after successfully obtaining an access-key
     * from this method, you must invoke unlock(*).
     * </p>
     *
     * @return an access-key, if the lock was obtained; otherwise, return null.
     */
    public Object lock ();

    /**
     * Use this method to enqueue a message after successfully
     * obtaining the transaction-lock.
     *
     * @param key is the access-key obtained from lock().
     * @param event identifies the event-channel that the message will be sent to.
     * @param message will be enqueued herein.
     */
    public void commit (Object key,
                        CascadeToken event,
                        OperandStack message);

    /**
     * Use this method to release the transaction-lock.
     *
     * <p>
     * If the given key is null, then this method is a no-op.
     * </p>
     *
     * @param key is the access-key obtained from lock(), or null.
     */
    public void unlock (Object key);

    /**
     * Getter.
     *
     * <p>
     * This method *must* be implemented as a constant-time operation,
     * rather than a linear summation.
     * </p>
     *
     * @return the number of messages that are currently enqueued herein.
     */
    public int size ();

    /**
     * Getter.
     *
     * <p>
     * This method *must* be implemented as a constant-time operation,
     * rather than a linear summation.
     * </p>
     *
     * @return the maximum number of messages that can be enqueued herein.
     */
    public int capacity ();

    /**
     * Release any special resources herein.
     */
    public void close ();

    /**
     * This method sets the functor that will be invoked whenever
     * an event-message is committed.
     *
     * @param functor is the event-handler
     */
    public void setCallback (Consumer<InflowQueue> functor);

    /**
     * Invoke this method in order to dequeue an event-message, if available.
     *
     * @param out will receive the event-message, if one is available.
     * @return the name of the event-channel, if a message is available;
     * otherwise, return null.
     */
    public CascadeToken poll (OperandStack out);
}
