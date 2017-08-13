package com.mackenziehigh.cascade.internal.allocator;

import com.google.common.base.Preconditions;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.stream.IntStream;

/**
 * An instance of this class is a MemoryAllocator that
 * allocates memory using linked fixed-size blocks.
 *
 * TODO: Allow dynamic addition of new blocks???
 */
final class MultiBlockAllocator
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

        public volatile int capacity = 0;

        public volatile int next = -1;

        public MemoryBlock (final int ptr,
                            final int blockSize)
        {
            this.ptr = ptr;
            this.data = new byte[blockSize];
        }

    }

    private final int blockCount;

    private final int blockSize;

    private final AtomicReferenceArray<MemoryBlock> blocks;

    private final Queue<MemoryBlock> freeBlocks;

    /**
     * Sole Constructor.
     *
     * @param blockCount is the maximum number of allocated blocks.
     * @param blockSize is the size of each allocated block.
     */
    public MultiBlockAllocator (final int blockCount,
                                final int blockSize)
    {
        this.blockCount = blockCount;
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
        Preconditions.checkArgument(capacity <= blockCount * blockSize, "capacity > blockCount * blockSize");

        final int requiredBlockCount = (capacity / blockSize) + (capacity % blockSize == 0 ? 0 : 1);

        MemoryBlock head = null;

        for (int i = 0; i < Math.max(1, requiredBlockCount); i++)
        {
            final MemoryBlock block = freeBlocks.poll();

            if (block == null)
            {
                free(head);
                throw new InsufficientMemoryException(this, capacity);
            }

            block.referenceCount = 0;
            block.size = 0;
            block.capacity = 0;
            block.next = head == null ? -1 : head.ptr;
            head = block;
        }

        assert head != null;

        head.referenceCount = 1;
        head.size = 0;
        head.capacity = capacity;

        return head.ptr;
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
                free(block);
            }
        }
    }

    private void free (final MemoryBlock head)
    {
        MemoryBlock p = head;

        while (p != null)
        {
            final MemoryBlock block = p;
            final int next = p.next;
            p.capacity = 0;
            p.referenceCount = 0;
            p.size = 0;
            p.next = -1;
            p = next >= 0 ? blocks.get(next) : null;
            freeBlocks.add(block);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int capacityOf (final int ptr)
    {
        checkPtr(ptr);
        final MemoryBlock block = blocks.get(ptr);
        final int size = block.capacity;
        return size;
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

        int writtenThusFar = 0;

        final MemoryBlock head = blocks.get(ptr);
        MemoryBlock p = head;

        synchronized (head)
        {
            Preconditions.checkArgument(length <= head.capacity, "length > head.capacity");

            head.size = length;

            while (p != null)
            {
                int i = 0;

                while (writtenThusFar < length && i < blockSize)
                {
                    p.data[i++] = data[writtenThusFar++];
                }

                p = p.next >= 0 ? blocks.get(p.next) : null;
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

        int readThusFar = 0;

        final MemoryBlock head = blocks.get(ptr);
        MemoryBlock p = head;

        synchronized (head)
        {
            final int size = head.size;

            Preconditions.checkArgument(size <= data.length, "size > data.length");

            while (p != null)
            {
                int i = 0;

                while (readThusFar < size && i < blockSize)
                {
                    data[readThusFar++] = p.data[i++];
                }

                p = p.next >= 0 ? blocks.get(p.next) : null;
            }
        }

        return readThusFar;
    }

    private void checkPtr (final int ptr)
    {
        if (ptr < 0 || ptr >= blocks.length() || blocks.get(ptr).referenceCount <= 0)
        {
            throw new InvalidPointerException(this, ptr);
        }
    }

    public static void main (String[] args)
    {
        final MemoryAllocator ax = new MultiBlockAllocator(5, 2);
        final int ptr = ax.malloc(6);
        ax.set(ptr, "Emma".getBytes());
        final byte[] out = new byte[10];
        ax.get(ptr, out);
        System.out.println("X = " + new String(out));
        ax.decrement(ptr);
    }
}
