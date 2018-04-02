package com.mackenziehigh.cascade;

/**
 * A director monitors an actor.
 *
 * <p>
 * Unlike an actor itself, the methods herein may be executed concurrently.
 * </p>
 */
public interface CascadeDirector
{
    /**
     * This method will be invoked whenever this object
     * becomes a director of the given actor.
     *
     * <p>
     * This method will be executed on the thread
     * that performs the registration of the director.
     * </p>
     *
     * @param actor will be monitored going forward.
     */
    public default void onRegistration (CascadeActor actor)
    {
        // Pass
    }

    /**
     * This method will be invoked whenever this object
     * stops being a director of the given actor.
     *
     * @param actor will not be monitored going forward.
     */
    public default void onDeregistration (CascadeActor actor)
    {
        // Pass
    }

    /**
     * This method will be invoked whenever the given actor subscribes to an event-channel.
     *
     * @param actor is the actor that just subscribed to the event-channel.
     * @param event identifies the event-channel that the actor subscribed to.
     */
    public default void onSubscription (CascadeActor actor,
                                        CascadeToken event)
    {
        // Pass
    }

    /**
     * This method will be invoked whenever the given actor unsubscribes from an event-channel.
     *
     * @param actor is the actor that just unsubscribed from the event-channel.
     * @param event identifies the event-channel that the actor unsubscribed from.
     */
    public default void onUnsubscription (CascadeActor actor,
                                          CascadeToken event)
    {
        // Pass
    }

    /**
     * This method will be invoked whenever the given actor
     * enqueues an incoming message before it can be processed.
     *
     * <p>
     * This method will not be invoked, if the message is dropped.
     * </p>
     *
     * <p>
     * This method will be executed on the thread that sent the message.
     * </p>
     *
     * @param actor was the intended destination of the dropped message.
     * @param event identifies the event that produced the message.
     * @param stack contains the content of the message.
     */
    public default void onAcceptedMessage (CascadeActor actor,
                                           CascadeToken event,
                                           CascadeStack stack)
    {
        // Pass
    }

    /**
     * This method will be invoked whenever the given actor is forced
     * to drop an incoming message before it can be processed.
     *
     * <p>
     * This method will be executed on the thread that sent the message.
     * </p>
     *
     * @param actor was the intended destination of the dropped message.
     * @param event identifies the event that produced the message.
     * @param stack contains the content of the message.
     */
    public default void onDroppedMessage (CascadeActor actor,
                                          CascadeToken event,
                                          CascadeStack stack)
    {
        // Pass
    }

    /**
     * This method will be invoked whenever the given actor
     * begins processing a message using its script.
     *
     * <p>
     * This method will be executed on the same thread as the actor itself.
     * </p>
     *
     * @param actor has just dequeued the message and is going to process it.
     * @param event identifies the event that produced the message.
     * @param stack contains the content of the message.
     */
    public default void onConsumingMessage (CascadeActor actor,
                                            CascadeToken event,
                                            CascadeStack stack)
    {
        // Pass
    }

    /**
     * This method will be invoked whenever the given actor
     * finishes processing a message using its script.
     *
     * <p>
     * This method will be executed on the same thread as the actor itself.
     * </p>
     *
     * <p>
     * If an unhandled exception occurs while the actor is processing the message,
     * then this method will not be invoked, but rather the onException() method.
     * </p>
     *
     * @param actor processed the message.
     * @param event identifies the event that produced the message.
     * @param stack contains the content of the message.
     */
    public default void onConsumedMessage (CascadeActor actor,
                                           CascadeToken event,
                                           CascadeStack stack)
    {
        // Pass
    }

    /**
     * This method will be invoked whenever the given actor
     * throws an unhandled exception of any kind.
     *
     * <p>
     * This method will be executed on the same thread as the actor itself.
     * </p>
     *
     * @param actor threw the exception.
     * @param cause was thrown.
     */
    public default void onException (CascadeActor actor,
                                     Throwable cause)
    {
        // Pass
    }

    /**
     * This method will be invoked whenever the given actor begins to close.
     *
     * <p>
     * This method will be executed on the same thread as the actor itself.
     * </p>
     *
     * @param actor is leaving the stage.
     */
    public default void onClosing (CascadeActor actor)
    {
        // Pass
    }

    /**
     * This method will be invoked whenever the given actor begins to close.
     *
     * <p>
     * This method will be executed on the same thread as the actor itself.
     * </p>
     *
     * @param actor is leaving the stage.
     */
    public default void onClosed (CascadeActor actor)
    {
        // Pass
    }
}
