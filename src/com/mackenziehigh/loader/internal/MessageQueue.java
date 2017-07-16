package com.mackenziehigh.loader.internal;

import com.mackenziehigh.loader.TopicKey;
import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * Message Queue.
 */
final class MessageQueue
{

    private final BlockingQueue<Entry<TopicKey, Object>> queue = new LinkedBlockingQueue<>();

    public void add (final TopicKey topic,
                     final Object message)
    {
        queue.add(new AbstractMap.SimpleImmutableEntry<>(topic, message));
    }

    public void poll (final long timeout,
                      final BiConsumer<TopicKey, Object> handler)
    {
        try
        {
            final Entry<TopicKey, Object> entry = queue.poll(timeout, TimeUnit.MILLISECONDS);

            if (entry != null)
            {
                handler.accept(entry.getKey(), entry.getValue());
            }
        }
        catch (InterruptedException ex)
        {
            ex.printStackTrace();
            // Pass
        }
    }
}
