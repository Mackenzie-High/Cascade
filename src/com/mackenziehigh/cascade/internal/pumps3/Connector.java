package com.mackenziehigh.cascade.internal.pumps3;

import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Instances of this class facilitate the submission of operand-stack
 * based messages to consumers of those messages, optionally blocking,
 * if necessary until the consumers are able to enqueue the messages.
 *
 * <p>
 * Instances of this class help facilitate atomic senders
 * by providing a transactional API.
 * </p>
 *
 * <p>
 * Because instances of this class use operand-stacks,
 * which effectively use manual memory management,
 * you must call close() after using this object
 * in order to ensure that an underlying operand-stacks,
 * etc, are properly freed.
 * </p>
 */
public interface Connector
{
    /**
     * Conceptually, this is a transmission queue.
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
    public interface Connection
    {
        /**
         * Getter.
         *
         * <p>
         * The value returned from this method is the index
         * of this connection within the connections() list
         * obtainable from the enclosing connector object.
         * </p>
         *
         * @return the identity of this object within the enclosing connector.
         */
        public int id ();

        /**
         * Use this method to attempt to obtain the transaction-lock,
         * blocking if necessary upto the given timeout.
         *
         * <p>
         * At some point after successfully obtaining an access-key
         * from this method, you must invoke either unlock(*).
         * </p>
         *
         * @param timeout is the maximum amount of time to wait.
         * @param timeoutUnits describes the timeout.
         * @return an access-key, if the lock was obtained; otherwise, return null.
         */
        public Object lock (long timeout,
                            TimeUnit timeoutUnits);

        /**
         * Use this method to attempt to obtain the transaction-lock,
         * without blocking if the lock cannot immediately be obtained.
         *
         * <p>
         * At some point after successfully obtaining an access-key
         * from this method, you must invoke either unlock(*).
         * </p>
         *
         * @return an access-key, if the lock was obtained; otherwise, return null.
         */
        public Object lock ();

        /**
         * Use this method to enqueue a message in this connection queue
         * after successfully obtaining the transaction-lock.
         *
         * @param key is the access-key obtained from lock().
         * @param message will be enqueued herein.
         */
        public void commit (Object key,
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
         * @return the number of messages that are currently enqueued herein.
         */
        public int localSize ();

        /**
         * Getter.
         *
         * @return the maximum number of messages that can be enqueued herein.
         */
        public int localCapacity ();

        /**
         * Safely release any special resources herein.
         *
         * <p>
         * This method never throws any exceptions.
         * </p>
         */
        public void close ();
    }

    /**
     * Getter.
     *
     * @return the connection queues owned by this object.
     */
    public List<Connection> connections ();

    /**
     * Getter.
     *
     * <p>
     * This method *must* be implemented as a constant-time operation,
     * rather than a linear summation.
     * </p>
     *
     * @return the sum of the localSize() of each element in connections().
     */
    public int globalSize ();

    /**
     * Getter.
     *
     * <p>
     * This method *must* be implemented as a constant-time operation,
     * rather than a linear summation.
     * </p>
     *
     * @return the sum of the localCapacity() of each element in connections().
     */
    public int globalCapacity ();

    /**
     * Safely release any special resources herein.
     *
     * <p>
     * This method never throws any exceptions.
     * </p>
     */
    public void close ();
}
