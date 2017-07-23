package com.mackenziehigh.loader.internal;

import com.mackenziehigh.loader.Controller;
import com.mackenziehigh.loader.MessageQueue;

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
