package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.mackenziehigh.cascade.Message;
import com.mackenziehigh.cascade.MessageHandler;
import com.mackenziehigh.cascade.MessageProcessor;
import com.mackenziehigh.cascade.MessageQueue;
import com.mackenziehigh.cascade.UniqueID;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author mackenzie
 */
final class DedicatedProcessor
        extends AbstractProcessor
{
    public String threadName;

    public int threadCount;

    public int threadPriority = Thread.NORM_PRIORITY;

    public BiArrayBlockingQueue<MessageQueue, Message> messages;

    public LazyQueueRef overflowQueue;

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
                final boolean overflow = queueSize() >= maximumQueueSize();

                if (overflow && overflowQueue != null)
                {
                    return overflowQueue.get().send(message);
                }
                else if (overflow)
                {
                    return false;
                }
                else
                {
                    try
                    {
                        messages.put(this, message);
                        return true;
                    }
                    catch (InterruptedException ex)
                    {
                        return false;
                    }
                }
            }
        };

        super.messageQueues.put(name, queue);
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

    @Override
    public void stop ()
    {
        // TODO
    }

    private void runloop ()
    {
        while (true)
        {
            try
            {
                messages.poll(1000, TimeUnit.SECONDS, (x, y) -> onReceive(x, y));
            }
            catch (Throwable ex)
            {
                logger.error(ex);
            }
        }
    }

    private void onReceive (final MessageQueue source,
                            final Message message)
    {
        final int size = source.handlers().size();
        for (int i = 0; i < size; i++)
        {
            try
            {
                source.handlers().get(i).accept(message);
            }
            catch (Throwable ex)
            {
                logger.error(ex);
            }
        }
    }
}
