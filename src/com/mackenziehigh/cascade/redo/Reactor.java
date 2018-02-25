package com.mackenziehigh.cascade.redo;

import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.allocators.Allocator;
import com.mackenziehigh.cascade.allocators.OperandStack;
import java.util.List;
import java.util.Set;

/**
 * Instances of this interface receive messages from other reactors,
 * process those messages, and send messages to yet other reactors.
 */
public interface Reactor
{
    /**
     * Getter.
     *
     * <p>
     * For a given node instance, this method always returns the same object.
     * Thus, the node can safely use the result between event-handler invocations.
     * </p>
     *
     * <p>
     * This method never returns null.
     * </p>
     *
     * @return the system that this reactor is part of.
     */
    public Cascade cascade ();

    /**
     * Getter.
     *
     * <p>
     * For a given node instance, this method always returns the same object.
     * Thus, the node can safely use the result between event-handler invocations.
     * </p>
     *
     * <p>
     * This method never returns null.
     * </p>
     *
     * @return the full-name of this reactor.
     */
    public CascadeToken name ();

    /**
     * Getter.
     *
     * <p>
     * This method never returns null.
     * </p>
     *
     * @return the event-handler that implements the business-logic.
     */
    public ReactorCore core ();

    /**
     * Getter.
     *
     * @param name identifies a parameter.
     * @return the values associated with the named parameter.
     */
    public List<Param> param (String name);

    /**
     * Getter.
     *
     * <p>
     * For a given node instance, this method always returns the same object.
     * Thus, the node can safely use the result between event-handler invocations.
     * </p>
     *
     * <p>
     * This method never returns null.
     * </p>
     *
     * @return the logger intended for use by this reactor.
     */
    public CascadeLogger logger ();

    /**
     * Getter.
     *
     * <p>
     * For a given node instance, this method always returns the same object.
     * Thus, the node can safely use the result between event-handler invocations.
     * </p>
     *
     * <p>
     * This method never returns null.
     * </p>
     *
     * @return the allocator intended for use by this reactor.
     */
    public Allocator allocator ();

    /**
     * This method retrieves the number of messages that
     * are currently enqueued awaiting processing.
     *
     * @return the total number of pending event-messages.
     */
    public int queueSize ();

    /**
     * This method retrieves the maximum number of messages
     * that can be enqueued at one time awaiting processing.
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
        if (queueCapacity() == 0)
        {
            return 100.0;
        }
        else
        {
            final double bp = queueSize() / (double) queueCapacity();
            return bp;
        }
    }

    /**
     * Getter.
     *
     * @return the number of event-messages processed thus far.
     */
    public long eventCount ();

    /**
     * Getter.
     *
     * @return an immutable set containing the full-names of the event-channels
     * that this reactor is currently interested in receiving event-messages from.
     */
    public Set<CascadeToken> subscriptions ();

    /**
     * Subscribe this reactor to a named event-channel,
     * so that this reactor will begin receiving messages therefrom.
     *
     * @param event identifies the event-channel.
     * @return this.
     */
    public Reactor subscribe (CascadeToken event);

    /**
     * Unsubscribe this reactor to a named event-channel,
     * so that this reactor will no longer receive messages therefrom.
     *
     * @param event identifies the event-channel.
     * @return this.
     */
    public Reactor unsubscribe (CascadeToken event);

    /**
     * Use this method to send an event-message.
     *
     * @param event identifies the event-channel to send the message to.
     * @param message is the message to send.
     * @return this.
     */
    public default Reactor send (final String event,
                                 final OperandStack message)
    {
        final CascadeToken token = CascadeToken.create(event);
        return send(token, message);
    }

    /**
     * Use this method to send an event-message.
     *
     * @param event identifies the event-channel to send the message to.
     * @param message is the message to send.
     * @return this.
     */
    public Reactor send (CascadeToken event,
                         OperandStack message);

}
