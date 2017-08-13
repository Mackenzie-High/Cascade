package com.mackenziehigh.cascade.internal.allocator;

import com.google.common.base.Preconditions;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.stream.IntStream;

/**
 * This class implements MemoryAllocator using Java object allocation.
 */
final class DynamicAllocator
        implements MemoryAllocator
{
    private static final byte[] EMPTY = new byte[0];

    /**
     * An instance of this class is a single block of memory.
     */
    private static final class MemoryBlock
    {
        public final int ptr;

        public volatile int referenceCount = 0;

        public byte[] data = EMPTY;

        public MemoryBlock (final int ptr)
        {
            this.ptr = ptr;
        }

    }

    private final AtomicReferenceArray<MemoryBlock> blocks;

    private final Queue<MemoryBlock> freeBlocks;

    /**
     * Sole Constructor.
     *
     * @param blockCount is the maximum number of allocated blocks.
     * @param blockSize is the size of each allocated block.
     */
    public DynamicAllocator (final int blockCount)
    {
        this.blocks = new AtomicReferenceArray<>(blockCount);
        this.freeBlocks = new ArrayBlockingQueue<>(blockCount);
        IntStream.range(0, blockCount).forEach(i -> freeBlocks.add(new MemoryBlock(i)));
        freeBlocks.forEach(x -> blocks.set(x.ptr, x));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int malloc (final int capacity)
    {
        Preconditions.checkArgument(capacity >= 0, "capacity < 0");

        final MemoryBlock block = freeBlocks.poll();

        if (block == null)
        {
            throw new InsufficientMemoryException(this, capacity);
        }

        synchronized (block)
        {
            ++block.referenceCount;
            block.data = capacity == 0 ? EMPTY : new byte[capacity];
        }

        return block.ptr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void increment (final int ptr)
    {
        checkPtr(ptr);

        final MemoryBlock block = freeBlocks.poll();

        synchronized (block)
        {
            ++block.referenceCount;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void decrement (final int ptr)
    {
        checkPtr(ptr);

        final MemoryBlock block = blocks.get(ptr);

        synchronized (block)
        {
            --block.referenceCount;

            if (block.referenceCount <= 0)
            {
                freeBlocks.add(block);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int capacityOf (final int ptr)
    {
        return Integer.MAX_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int sizeOf (final int ptr)
    {
        checkPtr(ptr);
        final MemoryBlock block = blocks.get(ptr);
        final int size = block.data.length;
        return size;
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
        checkPtr(ptr);
        Preconditions.checkArgument(offset >= 0, "offset < 0");
        Preconditions.checkArgument(length >= 0, "length < 0");
        Preconditions.checkArgument(offset + length <= data.length, "offset + length > data.length");

        final MemoryBlock block = blocks.get(ptr);

        synchronized (block)
        {
            block.data = new byte[length];

            for (int i = 0; i < length; i++)
            {
                block.data[i] = data[offset + i];
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int get (final int ptr,
                    final byte[] data)
    {
        checkPtr(ptr);

        int size;

        final MemoryBlock block = blocks.get(ptr);

        synchronized (block)
        {
            System.arraycopy(block.data, 0, data, 0, block.data.length);
            size = block.data.length;
        }

        return size;
    }

    private void checkPtr (final int ptr)
    {
        if (ptr < 0 || ptr >= blocks.length() || blocks.get(ptr).referenceCount <= 0)
        {
            throw new InvalidPointerException(this, ptr);
        }
    }
}
