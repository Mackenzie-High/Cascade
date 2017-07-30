package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.Controller;
import com.mackenziehigh.cascade.MessageQueue;

/**
 * An instance of this class is a lazy reference to a message-queue.
 */
final class LazyQueueRef
{
    private final Controller controller;

    private final String name;

    public LazyQueueRef (final Controller controller,
                         final String name)
    {
        this.controller = controller;
        this.name = name;
    }

    public MessageQueue get ()
    {
        return controller.queues().get(name);
    }
}
