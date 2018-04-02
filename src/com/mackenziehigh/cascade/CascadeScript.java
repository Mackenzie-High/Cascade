package com.mackenziehigh.cascade;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

/**
 * Scripts define how actors behave in-response to events.
 */
public interface CascadeScript
{

    /**
     * This method will be invoked in order to retrieve implementation-specific
     * human-readable key-value pairs of debug information.
     *
     * <p>
     * Unlike the other methods in this interface, this method
     * may be invoked by any thread as necessary; therefore,
     * implementors must ensure appropriate thread-safety.
     * </p>
     *
     * @return an immutable map.
     */
    public default Map<String, String> debug ()
    {
        return ImmutableMap.of();
    }

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
    public default void onException (CascadeContext ctx,
                                     Throwable cause)
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
