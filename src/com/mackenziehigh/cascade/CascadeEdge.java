package com.mackenziehigh.cascade;

import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import java.time.Duration;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;

/**
 * An edge is a directed connection between two nodes.
 */
public interface CascadeEdge
{
    /**
     * Edge Throughput Statistics.
     *
     * <p>
     * Monitoring must be explicitly turned on.
     * By default, monitoring is turned off.
     * Toggling monitoring on/off/on will reset the underlying counters.
     * </p>
     */
    public interface EdgeStats
    {
        /**
         * Getter.
         *
         * @return the edge that this object monitors.
         */
        public CascadeEdge edge ();

        /**
         * Use this method to turn on monitoring.
         *
         * @return this.
         */
        public EdgeStats enable ();

        /**
         * Use this method to turn off monitoring.
         *
         * @return this.
         */
        public EdgeStats disable ();

        /**
         * Getter.
         *
         * @return true, if monitoring is turned on.
         */
        public boolean isEnabled ();

        /**
         * Getter.
         *
         * @return the total number of messages enqueued,
         * or empty, if monitoring is turned off.
         */
        public OptionalLong getTotalEnqueues ();

        /**
         * Getter.
         *
         * @return the total number of messages dequeued,
         * or empty, if monitoring is turned off.
         */
        public OptionalLong getTotalDequeues ();

        /**
         * Getter.
         *
         * <p>
         * This method is *not* suitable for detecting rate spikes.
         * </p>
         *
         * @return the averaged number of messages per second,
         * or empty, if monitoring is turned off.
         */
        public OptionalLong getMessageRate ();

        /**
         * Getter.
         *
         * <p>
         * This method is *not* suitable for detecting rate spikes.
         * </p>
         *
         * @return the averaged number of bytes per second,
         * or empty, if monitoring is turned off.
         */
        public OptionalLong getBandwidth ();

        /**
         * Getter.
         *
         * @return the total number of bytes enqueued,
         * or empty, if monitoring is turned off.
         */
        public OptionalLong getTotalBytesEnqueued ();

        /**
         * Getter.
         *
         * @return the total number of bytes dequeued,
         * or empty, if monitoring is turned off.
         */
        public OptionalLong getTotalBytesDequeued ();

        /**
         * Getter.
         *
         * @return the maximum number of bytes enqueued in a single message,
         * or empty, if monitoring is turned off or no messages were received.
         */
        public OptionalLong getMaxBytes ();

        /**
         * Getter.
         *
         * @return the minimum number of bytes enqueued in a single message,
         * or empty, if monitoring is turned off or no messages were received.
         */
        public OptionalLong getMinBytes ();

        /**
         * Getter.
         *
         * @return the average number of bytes enqueued in a single message,
         * or empty, if monitoring is turned off or no messages were received.
         */
        public OptionalLong getAvgBytes ();

        /**
         * Getter.
         *
         * @return the number of nanoseconds between the first and last message,
         * or empty, if monitoring is turned off or no messages were received.
         */
        public Optional<Duration> getDuration ();

        /**
         * Getter.
         *
         * @return the maximum that backlogSize() has reached,
         * or empty, if monitoring is turned off or no messages were received.
         */
        public OptionalInt getMaxBacklogSize ();

        /**
         * Getter.
         *
         * @return the average that backlogSize() has reached,
         * or empty, if monitoring is turned off or no messages were received.
         */
        public OptionalInt getAvgBacklogSize ();

        /**
         * Getter.
         *
         * @return the maximum that queueSize() has reached,
         * or empty, if monitoring is turned off or no messages were received.
         */
        public OptionalInt getMaxQueueSize ();

        /**
         * Getter.
         *
         * @return the average that queueSize() has reached,
         * or empty, if monitoring is turned off or no messages were received.
         */
        public OptionalInt getAvgQueueSize ();
    }

    /**
     * Getter.
     *
     * @return the system that this edge is part of.
     */
    public Cascade cascade ();

    /**
     * Getter.
     *
     * @return the full-name of this edge.
     */
    public default String name ()
    {
        return String.format("%s -> %s", supplier().name(), consumer().name());
    }

    /**
     * Getter.
     *
     * @return the simple-name of this edge.
     */
    public default String simpleName ()
    {
        return String.format("%s -> %s", supplier().simpleName(), consumer().simpleName());
    }

    /**
     * Getter.
     *
     * @return the throughput monitoring statistics.
     */
    public EdgeStats stats ();

    /**
     * This is the node that sends messages to the consumer.
     *
     * @return the supply-side of the edge.
     */
    public CascadeNode supplier ();

    /**
     * This is the node that receives messages from the supplier.
     *
     * @return the consumer-side of the edge.
     */
    public CascadeNode consumer ();

    /**
     * This method retrieves the number of messages that
     * are currently enqueued awaiting processing.
     *
     * <p>
     * If this message-queue is based upon an underlying
     * queue that is shared with other message-queues,
     * then this result will be the sum of the sizes
     * of the message-queues. In other words, this method
     * retrieves the size of the underlying queue.
     * Thus, this result may be larger than queueSize().
     * </p>
     *
     * @return the total number of back-logged messages.
     */
    public int backlogSize ();

    /**
     * This method retrieves the maximum number of messages
     * that can be simultaneously enqueued awaiting processing.
     *
     * <p>
     * If this message-queue is based upon an underlying
     * queue that is shared with other message-queues,
     * then this result will be the capacity of the
     * Thus, this result may be larger than queueCapacity().
     * </p>
     *
     * @return the maximum number of back-logged messages.
     */
    public int backlogCapacity ();

    /**
     * This method retrieves the number of messages that
     * are currently enqueued awaiting processing in this
     * particular queue only.
     *
     * @return the total number of messages in this queue.
     */
    public int queueSize ();

    /**
     * This method retrieves the maximum number of messages
     * that can be enqueued at one time awaiting processing.
     *
     * <p>
     * The queueCapacity() is always upper-bounded by the backlogCapacity().
     * </p>
     *
     * <p>
     * The queueCapacity() is a best-case upper-bound.
     * In practice, the actual available capacity may be lower.
     * For example, if this message-queue is based upon
     * a underlying finite data-structure that is shared
     * with other message-queues, then the available capacity
     * at any single moment in time will be affected by
     * the size of each of the message-queues,
     * even if each individual message-queue has
     * a queueCapacity() equal to the backlogCapacity().
     * </p>
     *
     * <p>
     * In the situation where this message-queue cannot accept
     * more messages, because backlogSize() equals backlogCapacity(),
     * then we will say that the underlying queue is <i>overcommitted</i>.
     * </p>
     *
     * @return the maximum queue size.
     */
    public int queueCapacity ();

    /**
     * This method computes the current amount of back-pressure
     * in this message-queue, which is conceptually the percentage
     * of the queue that is currently full.
     *
     * <p>
     * If this message-queue is based upon an underlying
     * queue that is shared with other message-queues,
     * then the fullness of the other queues may affect
     * the back-pressure as well.
     * </p>
     *
     * @return the current back-pressure reading (0 &#8804 X &#8804 100.0).
     */
    public default double backpressure ()
    {
        if (queueCapacity() == 0 || backlogCapacity() == 0)
        {
            return 100.0;
        }
        else
        {
            final double queueBP = queueSize() / (double) queueCapacity();
            final double backlogBP = backlogSize() / (double) backlogCapacity();
            final double bp = 100.0 * Math.max(queueBP, backlogBP);
            return bp;
        }
    }

    /**
     * Send a message without blocking.
     *
     * <p>
     * If the queueSize() of has reached its queueCapacity(),
     * then this method will simply return false, because the queue
     * will be unable to accept the message. In other words, this method
     * will fail to send the message, if the queue is at 100% back-pressure.
     * </p>
     *
     * <p>
     * The message cannot be null or empty.
     * </p>
     *
     * <p>
     * This method does not block indefinitely.
     * </p>
     *
     * @param message will be enqueued in each output edge.
     * @return true, if the message was actually sent.
     */
    public boolean async (OperandStack message);

    /**
     * Atomically send a message, blocking if necessary
     * until the queue is able to enqueue the message.
     *
     * <p>
     * If the queueSize() of has reached its queueCapacity(),
     * then this method will simply return false, because the queue
     * will be unable to accept the message. In other words, this method
     * will fail to send the message, if the queue is at 100% back-pressure.
     * </p>
     *
     * <p>
     * This method will block until the queue enqueues the message,
     * or the given timeout is reached.
     * </p>
     *
     * <p>
     * This method will make an effort to wait up to the given timeout,
     * even in the face of unexpected internal faults.
     * </p>
     *
     * <p>
     * The timeout is a goal, not a real-time guarantee.
     * </p>
     *
     * @param message will be enqueued in each output edge.
     * @param timeout is the maximum amount of time to wait.
     * @param timeoutUnits describes the timeout.
     * @return true, if the message was actually sent.
     */
    public boolean sync (final CascadeAllocator.OperandStack message,
                         final long timeout,
                         final TimeUnit timeoutUnits);

    /**
     * Send a message, blocking if necessary, until the queue enqueues the message.
     *
     * <p>
     * This method is exactly like sync(*), except that it will keep
     * trying to send the message until either the message is sent
     * of normal shutdown begins.
     * </p>
     *
     * <p>
     * If normal shutdown begins, this this method will throw a SendFailureException.
     * Usually, you will simply want to log the exception, since shutdown has begun.
     * </p>
     *
     * @param message will be enqueued in each output edge.
     * @throws CascadeNode.SendFailureException if shutdown has begun and the message cannot be sent.
     */
    public void send (final OperandStack message)
            throws CascadeNode.SendFailureException;
}
