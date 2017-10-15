package com.mackenziehigh.cascade.internal.powerplants;

import com.google.common.base.Preconditions;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-Safe Facade for CombinedQueue.
 */
public final class SyncCombinedQueue<E>
{
    private final ReentrantReadWriteLock globalLock = new ReentrantReadWriteLock(true);

    private final Lock readLock = globalLock.readLock();

    private final Lock writeLock = globalLock.writeLock();

    private final CombinedQueue<E> combinedQueue;

    public SyncCombinedQueue (final int capacity)
    {
        this.combinedQueue = new CombinedQueue<>(capacity);
    }

    public int size ()
    {
        try
        {
            readLock.lock();
            return combinedQueue.size();
        }
        finally
        {
            readLock.unlock();
        }
    }

    public int capacity ()
    {
        try
        {
            readLock.lock();
            return combinedQueue.capacity();
        }
        finally
        {
            readLock.unlock();
        }
    }

    public SyncMemberQueue addMemberQueue (final int capacity)
    {
        try
        {
            writeLock.lock();
            return new SyncMemberQueue(capacity, combinedQueue.addMemberQueue());
        }
        finally
        {
            writeLock.unlock();
        }
    }

    public final class SyncMemberQueue
    {
        private final int capacity;

        private final CombinedQueue<E>.MemberQueue delegate;

        private SyncMemberQueue (final int capacity,
                                 final CombinedQueue<E>.MemberQueue delegate)
        {
            this.capacity = capacity;
            this.delegate = delegate;
        }

        public int size ()
        {
            try
            {
                readLock.lock();
                return delegate.size();
            }
            finally
            {
                readLock.unlock();
            }
        }

        public int capacity ()
        {
            return capacity;
        }

        public boolean add (final E value)
        {
            Preconditions.checkNotNull(value, "value");

            try
            {
                writeLock.lock();
                if (delegate.size() >= capacity)
                {
                    return false;
                }
                else if (combinedQueue.size() >= combinedQueue.capacity())
                {
                    return false;
                }
                else
                {
                    delegate.add(value);
                    return true;
                }
            }
            finally
            {
                writeLock.unlock();
            }
        }

        public E remove ()
        {
            try
            {
                writeLock.lock();
                if (delegate.size() == 0)
                {
                    return null;
                }
                else
                {
                    final E result = delegate.remove();
                    return result;
                }
            }
            finally
            {
                writeLock.unlock();
            }
        }
    }
}
