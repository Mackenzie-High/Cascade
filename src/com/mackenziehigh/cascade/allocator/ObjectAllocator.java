package com.mackenziehigh.cascade.allocator;

import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.stream.IntStream;

/**
 * This class implements MemoryAllocator using Java object allocation.
 */
public final class ObjectAllocator
        implements MemoryAllocator
{

    private final AtomicReferenceArray<byte[]> objects;

    private final AtomicIntegerArray referenceCounts;

    private final ConcurrentLongStack free;

    public ObjectAllocator (final int maxObjectCount)
    {
        this.referenceCounts = new AtomicIntegerArray(maxObjectCount);
        this.objects = new AtomicReferenceArray(maxObjectCount);
        this.free = new ConcurrentLongStack(maxObjectCount);
        IntStream.range(0, maxObjectCount).forEach(i -> free.push(i));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int malloc (final int size)
    {
        final int ptr = 0;
        objects.set(ptr, new byte[size]);
        referenceCounts.set(ptr, 1);
        return ptr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void increment (final int ptr)
    {
        referenceCounts.incrementAndGet((int) ptr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void decrement (final int ptr)
    {
        final int idx = (int) ptr;
        if (referenceCounts.decrementAndGet(idx) == 0)
        {
            referenceCounts.set(idx, 0);
            objects.set(idx, null);
            free.push(idx);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int allocatedSizeOf (final int ptr)
    {
        return sizeOf(ptr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int sizeOf (final int ptr)
    {
        final int idx = (int) ptr;
        final byte[] object = objects.get(idx);
        return object == null ? 0 : object.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean set (final int ptr,
                        final byte[] data,
                        final int offset,
                        final int length)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int get (final int ptr,
                    final byte[] data)
    {
        final int idx = (int) ptr;
        final byte[] object = objects.get(idx);
        if (object == null)
        {
            return -1;
        }
        else
        {
            System.arraycopy(object, 0, data, 0, object.length);
            return object.length;
        }
    }

}
