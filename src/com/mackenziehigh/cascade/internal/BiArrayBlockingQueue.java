package com.mackenziehigh.cascade.internal;

import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 *
 */
final class BiArrayBlockingQueue<K, V>
{
    private final int capacity;

    private final ArrayBlockingQueue<Entry<K, V>> queue;

    public BiArrayBlockingQueue (final int capacity)
    {
        this.capacity = capacity;
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    public void put (final K key,
                     final V value)
            throws InterruptedException
    {
        queue.add(new AbstractMap.SimpleImmutableEntry<>(key, value));
    }

    public void poll (final long timeout,
                      final TimeUnit units,
                      final BiConsumer<K, V> consumer)
            throws InterruptedException
    {
        final Entry<K, V> entry = queue.poll(timeout, units);
        if (entry != null)
        {
            consumer.accept(entry.getKey(), entry.getValue());
        }
    }

    public int size ()
    {
        return queue.size();
    }

    public int capacity ()
    {
        return capacity;
    }
}
