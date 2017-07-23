package com.mackenziehigh.loader.internal;

import com.google.common.base.Preconditions;
import com.mackenziehigh.loader.Message;
import com.mackenziehigh.loader.MessageHandler;
import com.mackenziehigh.loader.MessageProcessor;
import com.mackenziehigh.loader.MessageQueue;
import com.mackenziehigh.loader.UniqueID;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author mackenzie
 */
final class DedicatedProcessor
        extends AbstractProcessor
{
    public int threadCount;

    public String threadName;

    public int threadPriority;

    public ArrayBlockingQueue<Message> messages;

    public MessageQueue overflowQueue = null;

    @Override
    public void declareLog (final String name)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void declareQueue (final String name)
    {
        final MessageProcessor SELF = this;

        final UniqueID queueID = UniqueID.random();

        final List<MessageHandler> handlers = new CopyOnWriteArrayList<>();

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
            public MessageQueue bind (final MessageHandler action)
            {
                Preconditions.checkNotNull(action, "action");
                handlers.add(action);
                return this;
            }

            @Override
            public boolean send (final Message message)
            {
                final boolean overflow = queueSize() >= maximumQueueSize();

                if (overflow && overflowQueue != null)
                {
                    return overflowQueue.send(message);
                }
                else if (overflow)
                {
                    return false;
                }
                else
                {
                    return messages.add(message);
                }
            }
        };

        super.queues.put(name, queue);
        super.controller.queues.put(name, queue);
    }

    @Override
    public void start ()
    {
        for (int i = 0; i < threadCount; i++)
        {
            final Thread thread = new Thread(() -> runloop());
            thread.setDaemon(true);
            thread.setName(threadName);
            thread.setPriority(threadPriority);
            thread.start();
        }
    }

    private void runloop ()
    {
        while (true)
        {
            try
            {
                final Message message = messages.poll(1000, TimeUnit.SECONDS);

                if (message != null)
                {

                }
            }
            catch (Throwable ex)
            {
                // TODO
            }
        }
    }

}
