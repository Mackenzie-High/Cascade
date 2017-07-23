package com.mackenziehigh.loader.internal;

import com.mackenziehigh.loader.Message;
import java.util.ArrayDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

/**
 *
 */
final class BiArrayBlockingQueue<K, V>
{
    private final int capacity;

    private final ArrayDeque<K> keys;

    private final ArrayDeque<V> values;

    private final ReentrantLock lock = new ReentrantLock(true);

    private final Condition notFull = lock.newCondition();

    private final Condition notEmpty = lock.newCondition();

    public BiArrayBlockingQueue (final int capacity)
    {
        this.capacity = capacity;
        this.keys = new ArrayDeque<>(capacity);
        this.values = new ArrayDeque<>(capacity);
    }

    public void put (final K key,
                     final V value)
            throws InterruptedException
    {
        System.out.println("V = " + ((Message) value).content());

        lock.lock();
        try
        {
            while (keys.size() == capacity)
            {
                notFull.await();
            }
            keys.push(key);
            values.push(value);
            notEmpty.signal();
        }
        finally
        {
            lock.unlock();
        }
    }

    public void poll (final long timeout,
                      final TimeUnit units,
                      final BiConsumer<K, V> consumer)
            throws InterruptedException
    {
        K key;
        V value;

        lock.lock();
        try
        {
            while (keys.isEmpty())
            {
                if (notEmpty.await(timeout, units) == false)
                {
                    return;
                }
            }
            key = keys.pop();
            value = values.pop();
            notFull.signal();
            consumer.accept(key, value);
        }
        finally
        {
            lock.unlock();
        }

        consumer.accept(key, value);
    }

    public int size ()
    {
        return keys.size();
    }

    public int capacity ()
    {
        return capacity;
    }
}
