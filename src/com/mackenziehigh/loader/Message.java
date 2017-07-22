package com.mackenziehigh.loader;

import java.time.Instant;

/**
 * Base Message Interface.
 */
public interface Message
{
    /**
     * This is the user-defined name of the controller that created this message.
     *
     * @return the name of the controller, or null, if unavailable.
     */
    public String controllerName ();

    /**
     * This is the unique-ID of the controller that created this message.
     *
     * @return the unique-ID of the controller, or null, if unavailable.
     */
    public UniqueID controllerID ();

    /**
     * This is the user-defined name of the processor that created this message.
     *
     * @return the name of the processor, or null, if unavailable.
     */
    public String processorName ();

    /**
     * This is the unique-ID of the processor that created this message.
     *
     * @return the unique-ID of the processor, or null, if unavailable.
     */
    public UniqueID processorID ();

    /**
     * This is the user-defined name of the module that created this message.
     *
     * @return the name of the module, or null, if unavailable.
     */
    public String moduleName ();

    /**
     * This is the unique-ID of the module that created this message.
     *
     * @return the unique-ID of the module, or null, if unavailable.
     */
    public UniqueID moduleID ();

    /**
     * This method retrieves the unique-ID of this message.
     *
     * @return the unique-ID of this message.
     */
    public UniqueID uniqueID ();

    /**
     * This method retrieves the correlation-ID of this message, if any.
     *
     * @return the correlation-ID, or null, if unavailable.
     */
    public UniqueID correlationID ();

    /**
     * This is when the message was created.
     *
     * <p>
     * If this object is the result of deserialization,
     * then this is the creation-time <b>before</b> serialization.
     * </p>
     *
     * @return the creation time.
     */
    public Instant creationTime ();

    /**
     * This is user-defined content of this message.
     *
     * @return the content.
     */
    public Object content ();
}
