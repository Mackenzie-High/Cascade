package com.mackenziehigh.cascade;

/**
 * Scripts define how actors behave in-response to events.
 */
public interface CascadeScript
{
    /**
     * This event-handler will be executed when the enclosing actor is created.
     *
     * @param ctx provides access to the actor itself, etc.
     * @throws Throwable if something horrible happens.
     */
    public default void onSetup (CascadeContext ctx)
            throws Throwable
    {
        // Pass
    }

    /**
     * This event-handler will be executed whenever the enclosing actor
     * dequeues an event-message for processing herein.
     *
     * @param ctx provides access to the actor itself, etc.
     * @param event identifies the event that created the message.
     * @param stack contains the content of the message.
     * @throws Throwable if something horrible happens.
     */
    public default void onMessage (CascadeContext ctx,
                                   CascadeToken event,
                                   CascadeStack stack)
            throws Throwable
    {
        // Pass
    }

    /**
     * This event-handler will be executed whenever the enclosing
     * actor throws and unhandled exception of any kind.
     *
     * <p>
     * This method will be invoked given an unhandled exception
     * that is thrown by any of the other event-handlers,
     * but not those thrown by itself.
     * </p>
     *
     * @param ctx provides access to the actor itself, etc.
     * @param cause is the unhandled exception that was thrown.
     * @throws Throwable if something horrible happens.
     */
    public default void onUnhandledException (CascadeContext ctx,
                                              Throwable cause)
            throws Throwable
    {
        // Pass
    }

    /**
     * This event-handler will be executed whenever the enclosing actor
     * sends a message, but the message is not delivered,
     * because no one is interested in the message.
     *
     * @param ctx provides access to the actor itself, etc.
     * @param event identifies the event that created the message.
     * @param stack contains the content of the message.
     * @throws Throwable if something horrible happens.
     */
    public default void onUndeliveredMessage (CascadeContext ctx,
                                              CascadeToken event,
                                              CascadeStack stack)
            throws Throwable
    {
        // Pass
    }

    /**
     * This event-handler will be executed when the enclosing actor is closed.
     *
     * @param ctx provides access to the actor itself, etc.
     * @throws Throwable if something horrible happens.
     */
    public default void onClose (CascadeContext ctx)
            throws Throwable
    {
        // Pass
    }
}
