package com.mackenziehigh.loader.internal;

import com.google.common.base.Preconditions;
import com.mackenziehigh.loader.Message;
import com.mackenziehigh.loader.MessageHandler;
import com.mackenziehigh.loader.MessageProcessor;
import com.mackenziehigh.loader.MessageQueue;
import com.mackenziehigh.loader.UniqueID;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author mackenzie
 */
final class DirectProcessor
        extends AbstractProcessor
{

    @Override
    public void declareQueue (final String name)
    {
        final MessageProcessor SELF = this;

        final UniqueID queueID = UniqueID.random();

        final List<MessageHandler> handlers = new CopyOnWriteArrayList<>();

        final List<MessageHandler> unmodifiableHandlers = Collections.unmodifiableList(handlers);

        final MessageQueue queue = new MessageQueue()
        {
            @Override
            public MessageProcessor processor ()
            {
                return SELF;
            }

            @Override
            public String name ()
            {
                return name;
            }

            @Override
            public UniqueID uniqueID ()
            {
                return queueID;
            }

            @Override
            public int backlog ()
            {
                return 0;
            }

            @Override
            public int queueSize ()
            {
                return 0;
            }

            @Override
            public int maximumQueueSize ()
            {
                return 1;
            }

            @Override
            public List<MessageHandler> handlers ()
            {
                return unmodifiableHandlers;
            }

            @Override
            public MessageQueue bind (final MessageHandler action)
            {
                Preconditions.checkNotNull(action, "action");
                handlers.add(action);
                return this;
            }

            @Override
            public boolean send (final Message message)
            {
                final int size = handlers.size();
                for (int i = 0; i < size; i++)
                {
                    try
                    {
                        handlers.get(i).accept(message);
                    }
                    catch (Throwable ex)
                    {
                        logger.error(ex);
                    }
                }

                return true;
            }
        };

        super.messageQueues.put(name, queue);
        super.controller.queues.put(name, queue);
    }

    @Override
    public void start ()
    {
        // Pass
    }

    @Override
    public void stop ()
    {
        // Pass
    }
}
