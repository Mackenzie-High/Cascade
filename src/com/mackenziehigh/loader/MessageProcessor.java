package com.mackenziehigh.loader;

import com.google.common.collect.ImmutableMap;

/**
 * An instance of this class performs processing of messages
 * sent to it via one or more of the message-queues herein.
 */
public interface MessageProcessor
{
    /**
     * This method retrieves the controller that controls this processor.
     *
     * @return the controller.
     */
    public Controller controller ();

    /**
     * This method retrieves the name of this processor.
     *
     * @return the name of this processor.
     */
    public String name ();

    /**
     * This value uniquely identifies this processor in time and space.
     *
     * @return the unique-ID of this processor.
     */
    public UniqueID uniqueID ();

    /**
     * This method retrieves the message-queues that supplies
     * messages to this processor whenever a message becomes available.
     *
     * @return a map that maps the name of a queue to the queue itself.
     */
    public ImmutableMap<String, MessageQueue> queues ();
}
