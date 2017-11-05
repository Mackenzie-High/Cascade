package com.mackenziehigh.cascade;

/**
 * A pipeline is a connection between two actors.
 */
public interface CascadePipeline
{
    /**
     * Getter.
     *
     * @return the system that this pipeline is part of.
     */
    public Cascade cascade ();

    /**
     * This is the actor that sends messages to the consumer.
     *
     * @return the supply-side of the pipeline.
     */
    public CascadeActor supplier ();

    /**
     * This is the actor that receives messages from the supplier.
     *
     * @return the consumer-side of the pipeline.
     */
    public CascadeActor consumer ();

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
     * Note: The maximum queue-size and the maximum backlog
     * are conceptually the same, even though the definition
     * of queue-size and back-log may differ.
     * </p>
     *
     * @return the maximum queue size.
     */
    public int queueCapacity ();

    /**
     * This method computes the current amount of back-pressure
     * in this pipeline, which is conceptually the percentage
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
}
