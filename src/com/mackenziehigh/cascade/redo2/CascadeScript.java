package com.mackenziehigh.cascade.redo2;

/**
 *
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
                                   CascadeOperand stack)
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
