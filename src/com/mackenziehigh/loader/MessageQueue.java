package com.mackenziehigh.loader;

/**
 * An instance of this class stores messages that
 * are awaiting processing by a message-processor.
 */
public interface MessageQueue
{
    /**
     * This method retrieves the processor that
     * this queue supplies work to.
     *
     * @return the recipient worker.
     */
    public MessageProcessor processor ();

    /**
     * This method retrieves the name of this queue.
     *
     * @return the name of this queue.
     */
    public String name ();

    /**
     * This value uniquely identifies this queue in time and space.
     *
     * @return the unique-ID of this queue.
     */
    public UniqueID uniqueID ();

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
     * Thus, this result may differ from queueSize().
     * </p>
     *
     * @return the total number of back-logged messages.
     */
    public int backlog ();

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
     * Note: The maximum queue-size and the maximum backlog
     * are conceptually the same, even though the definition
     * of queue-size and back-log may differ.
     * </p>
     *
     * @return the maximum queue size.
     */
    public int maximumQueueSize ();

    /**
     * This method computes the current amount of back-pressure
     * in this processor, which is simply the percent of the
     * back-log queue that is currently full.
     *
     * @return the current back-pressure reading.
     */
    public default double backpressure ()
    {
        return maximumQueueSize() == 0 ? 1.0 : backlog() / maximumQueueSize();
    }

    /**
     * This method adds an action to this queue,
     * which will be performed by the message-processor(s)
     * upon receiving messages from this queue.
     *
     * @param action will be added hereto.
     * @return this.
     */
    public MessageQueue bind (final MessageHandler action);

    /**
     * This method sends a message to all message-handlers.
     *
     * <p>
     * If the backlog() has reached the maximumQueueSize(),
     * then this method will return false.
     * </p>
     *
     * <p>
     * This method is intentionally vaguely defined
     * in order to give implementors maximal control
     * over the behavioral details.
     * </p>
     *
     * @param message is the message to add to the queue.
     * @return true, iff the message was successfully enqueued.
     */
    public boolean send (Object message);
}
