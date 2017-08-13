package com.mackenziehigh.cascade.internal.allocator;

import com.google.common.base.Preconditions;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.stream.IntStream;

/**
 * An instance of this class is a MemoryAllocator that
 * allocates memory in a fixed-number of fixed-size blocks.
 */
final class SingleBlockAllocator
        implements MemoryAllocator
{
    /**
     * An instance of this class is a single block of memory.
     */
    private static final class MemoryBlock
    {
        public final int ptr;

        public volatile int referenceCount = 0;

        public final byte[] data;

        public volatile int size = 0;

        public MemoryBlock (final int ptr,
                            final int capacity)
        {
            this.ptr = ptr;
            this.data = new byte[capacity];
        }

    }

    private final int blockSize;

    private final AtomicReferenceArray<MemoryBlock> blocks;

    private final Queue<MemoryBlock> freeBlocks;

    /**
     * Sole Constructor.
     *
     * @param blockCount is the maximum number of allocated blocks.
     * @param blockSize is the size of each allocated block.
     */
    public SingleBlockAllocator (final int blockCount,
                                 final int blockSize)
    {
        this.blockSize = blockSize;
        this.blocks = new AtomicReferenceArray<>(blockCount);
        this.freeBlocks = new ArrayBlockingQueue<>(blockCount);
        IntStream.range(0, blockCount).forEach(i -> freeBlocks.add(new MemoryBlock(i, blockSize)));
        freeBlocks.forEach(x -> blocks.set(x.ptr, x));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int malloc (final int capacity)
    {
        Preconditions.checkArgument(capacity >= 0, "capacity < 0");
        Preconditions.checkArgument(capacity <= blockSize, "capacity > blockSize");

        final MemoryBlock block = freeBlocks.poll();

        if (block == null)
        {
            throw new InsufficientMemoryException(this, capacity);
        }

        synchronized (block)
        {
            ++block.referenceCount;
            block.size = 0;
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

        final MemoryBlock block = blocks.get(ptr);

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
        checkPtr(ptr);
        return blockSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int sizeOf (final int ptr)
    {
        checkPtr(ptr);
        final MemoryBlock block = blocks.get(ptr);
        final int size = block.size;
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
        Preconditions.checkArgument(length <= blockSize, "length > blockSize");

        final MemoryBlock block = blocks.get(ptr);

        synchronized (block)
        {
            block.size = length;

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
            Preconditions.checkArgument(block.size <= data.length, "block.size > data.length");
            System.arraycopy(block.data, 0, data, 0, block.size);
            size = block.size;
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
