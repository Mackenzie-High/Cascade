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
    public long malloc (final byte[] data,
                        final int offset,
                        final int length)
    {
        Preconditions.checkArgument(length >= 0, "length < 0");

        final MemoryBlock block = freeBlocks.poll();

        if (block == null)
        {
            throw new InsufficientMemoryException(this, length);
        }

        synchronized (block)
        {
            ++block.referenceCount;
            block.data = length == 0 ? EMPTY : new byte[length];
        }

        set(block.ptr, data, offset, length);

        return block.ptr;
    }

    private boolean set (final long ptr,
                         final byte[] data,
                         final int offset,
                         final int length)
    {
        final int idx = checkPtr(ptr);
        Preconditions.checkArgument(offset >= 0, "offset < 0");
        Preconditions.checkArgument(length >= 0, "length < 0");
        Preconditions.checkArgument(offset + length <= data.length, "offset + length > data.length");

        final MemoryBlock block = blocks.get(idx);

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
    public void increment (final long ptr)
    {
        final int idx = checkPtr(ptr);

        final MemoryBlock block = blocks.get(idx);

        synchronized (block)
        {
            ++block.referenceCount;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void decrement (final long ptr)
    {
        final int idx = checkPtr(ptr);

        final MemoryBlock block = blocks.get(idx);

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
    public int sizeOf (final long ptr)
    {
        final int idx = checkPtr(ptr);
        final MemoryBlock block = blocks.get(idx);
        final int size = block.data.length;
        return size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int get (final long ptr,
                    final byte[] data)
    {
        final int idx = checkPtr(ptr);

        int size;

        final MemoryBlock block = blocks.get(idx);

        synchronized (block)
        {
            Preconditions.checkArgument(block.data.length <= data.length, "block.size > data.length");
            System.arraycopy(block.data, 0, data, 0, block.data.length);
            size = block.data.length;
        }

        return size;
    }

    private int checkPtr (final long ptr)
    {
        final int idx = (int) (0x00000000FFFFFFFFL & ptr);

        if (ptr < 0 || ptr >= blocks.length() || blocks.get(idx).referenceCount <= 0)
        {
            throw new InvalidPointerException(this, idx);
        }

        return idx;
    }
}
