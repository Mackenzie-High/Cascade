package com.mackenziehigh.cascade.internal.allocator;

import com.google.common.base.Preconditions;
import java.util.stream.IntStream;

/**
 * An instance of this class is a MemoryAllocator that
 * allocates memory in a fixed-number of fixed-size blocks.
 */
final class SingleBlockAllocator
        implements MemoryAllocator
{
    private static final int HEADER_SIZE = 2;

    private static final int REFCOUNT_OFFSET = 0;

    private static final int SIZE_OFFSET = 1;

    private final int blockCount;

    private final int blockSize;

    private final long[][] memory;

    private final LongArrayBlockingQueue freeBlocks;

    /**
     * Sole Constructor.
     *
     * @param blockCount is the maximum number of allocated blocks.
     * @param blockSize is the size of each allocated block.
     */
    public SingleBlockAllocator (final int blockCount,
                                 final int blockSize)
    {
        this.blockCount = blockCount;
        this.blockSize = blockSize;
        final int elementsPerBlock = HEADER_SIZE + (blockSize / 4 + (blockSize % 4 == 0 ? 0 : 1));
        this.memory = new long[blockCount][elementsPerBlock];
        this.freeBlocks = new LongArrayBlockingQueue(blockCount);
        IntStream.range(0, blockCount).forEach(i -> freeBlocks.offer(i));
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
        Preconditions.checkArgument(length <= blockSize, "length > blockSize");

        final int idx = (int) freeBlocks.poll(-1);

        if (idx < 0)
        {
            throw new InsufficientMemoryException(this, length);
        }

        memory[idx][REFCOUNT_OFFSET] = 1;

        set(idx, data, offset, length);

        return idx;
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
        Preconditions.checkArgument(length <= blockSize, "length > blockSize");

        memory[idx][SIZE_OFFSET] = length;

//        for (int i = 0; i < length; i++)
//        {
//            final int address = 2 + (i / 4);
//            final long element = memory[idx][address];
//            final int num = i % 4;
//            final long value = (((long) data[i]) << (num * 8)) | (element & (~(0xFF << (num * 8))));
//            memory[idx][address] = value;
//        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void increment (final long ptr)
    {
        final int idx = checkPtr(ptr);

        synchronized (memory[idx])
        {
            ++memory[idx][REFCOUNT_OFFSET];
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void decrement (final long ptr)
    {
        final int idx = checkPtr(ptr);

        synchronized (memory[idx])
        {
            if (--memory[idx][REFCOUNT_OFFSET] <= 0L)
            {
                freeBlocks.offer(idx);
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
        final int size = (int) memory[idx][SIZE_OFFSET];
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

        final int size = sizeOf(ptr);

        Preconditions.checkArgument(size <= data.length, "block.size > data.length");

//        for (int i = 0; i < size; i++)
//        {
//            final int address = HEADER_SIZE + (i / 4);
//            final long element = memory[idx][address];
//            final int num = i % 4;
//            final byte value = ByteUtils.byteAt(element, num);
//            data[i] = value;
//        }
        return size;
    }

    private int checkPtr (final long ptr)
    {
        final int idx = (int) (0x00000000FFFFFFFFL & ptr);

        if (ptr < 0 || ptr >= memory.length || memory[idx][REFCOUNT_OFFSET] <= 0)
        {
            throw new InvalidPointerException(this, idx);
        }

        return idx;
    }

    public static void main (String[] args)
    {
        final MemoryAllocator ax = new SingleBlockAllocator(5, 10);
        final long ptr = ax.malloc("Emma".getBytes(), 0, 4);
        final byte[] out = new byte[10];
        ax.get(ptr, out);
        System.out.println("X = " + new String(out));
        ax.decrement(ptr);
    }
}
