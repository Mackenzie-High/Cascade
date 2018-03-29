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
     * This method will be invoked whenever the given actor
     * receives an incoming message before it can be processed,
     * even if the message is ultimately dropped.
     *
     * <p>
     * This method will be executed on the thread that sent the message.
     * </p>
     *
     * @param actor was the intended destination of the dropped message.
     * @param event identifies the event that produced the message.
     * @param stack contains the content of the message.
     */
    public default void onReceivedMessage (CascadeActor actor,
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
    public default void onUnhandledException (CascadeActor actor,
                                              Throwable cause)
    {
        // Pass
    }

    /**
     * This method will be invoked whenever the given actor sends a message.
     *
     * <p>
     * This method will be executed on the same thread as the actor itself.
     * </p>
     *
     * @param sender sent the message.
     * @param event identifies the event that produced the message.
     * @param stack contains the content of the message.
     */
    public default void onProducedMessage (CascadeActor sender,
                                           CascadeToken event,
                                           CascadeStack stack)
    {
        // Pass
    }

    /**
     * This method will be invoked whenever the given actor sends a message,
     * but was not delivered, because no one is subscribed to the event.
     *
     * <p>
     * This method will be executed on the same thread as the actor itself.
     * </p>
     *
     * @param sender sent the message.
     * @param event identifies the event that produced the message.
     * @param stack contains the content of the message.
     */
    public default void onUndeliveredMessage (CascadeActor sender,
                                              CascadeToken event,
                                              CascadeStack stack)
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
