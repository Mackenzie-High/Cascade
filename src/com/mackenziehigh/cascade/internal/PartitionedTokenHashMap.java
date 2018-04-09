package com.mackenziehigh.cascade.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.math.IntMath;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 */
public final class PartitionedTokenHashMap<E>
{
    private final int keylen = 16;

    private final ImmutableList<ReadWriteLock> bucketsLocks;

    private final ImmutableList<SortedMap<CascadeToken, E>> buckets;

    public PartitionedTokenHashMap ()
    {
        final List<ReadWriteLock> locks = Lists.newLinkedList();
        final List<SortedMap<CascadeToken, E>> list = Lists.newLinkedList();
        final int count = IntMath.pow(2, keylen);
        for (int i = 0; i < count; i++)
        {
            locks.add(new ReentrantReadWriteLock(true));
            final SortedMap<CascadeToken, E> map = Maps.newTreeMap();
            list.add(map);
        }
        buckets = ImmutableList.copyOf(list);
        bucketsLocks = ImmutableList.copyOf(locks);
    }

    public void put (final CascadeToken key,
                     final E value)
    {
        final int mask = IntMath.pow(2, keylen) - 1;
        final int idx = (int) (key.hashCode() & mask);
        try
        {
            bucketsLocks.get(idx).writeLock().lock();
            buckets.get(idx).put(key, value);
        }
        finally
        {
            bucketsLocks.get(idx).writeLock().unlock();
        }
    }

    public E get (final CascadeToken key,
                  final E defaultValue)
    {
        final int mask = IntMath.pow(2, keylen) - 1;
        final int idx = (int) (key.hashCode() & mask);
        try
        {
            bucketsLocks.get(idx).readLock().lock();
            return buckets.get(idx).getOrDefault(key, defaultValue);
        }
        finally
        {
            bucketsLocks.get(idx).readLock().unlock();
        }

    }

    public void remove (final CascadeToken key)
    {
        final int mask = IntMath.pow(2, keylen) - 1;
        final int idx = (int) (key.hashCode() & mask);
        try
        {
            bucketsLocks.get(idx).writeLock().lock();
            buckets.get(idx).remove(key);
        }
        finally
        {
            bucketsLocks.get(idx).writeLock().unlock();
        }
    }

    public void dumpStats ()
    {
        for (int i = 0; i < buckets.size(); i++)
        {
            System.out.printf("[%d] = %d\n", i, buckets.get(i).size());
        }
    }
}
