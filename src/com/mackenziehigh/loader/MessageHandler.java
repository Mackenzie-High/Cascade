package com.mackenziehigh.loader;

/**
 * An instance of this interface is an action that will be performed
 * by a message-processor when it receives a message from a queue.
 */
@FunctionalInterface
public interface MessageHandler
{
    public void accept (Message message)
            throws Throwable;
}
