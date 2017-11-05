package com.mackenziehigh.cascade.internal.messages;

import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeOperand;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * An instance of this class is a MemoryAllocator that
 * allocates memory in a fixed-number of fixed-size blocks.
 */
final class FixedBlockAllocator
        implements CascadeAllocator
{
    private final CascadeAllocator SELF = this;

    private final class FixedMessage
            implements CascadeOperand
    {
        private volatile boolean free = true;

        private final byte[] data;

        private volatile int size = 0;

        private volatile int refCount = 0;

        public FixedMessage (final int capacity)
        {
            this.data = new byte[capacity];
        }

        @Override
        public int size ()
        {
            return size;
        }

        @Override
        public int capacity ()
        {
            return blockSize;
        }

        @Override
        public byte byteAt (final int index)
        {
            return data[index];
        }

        @Override
        public CascadeAllocator allocator ()
        {
            return SELF;
        }
    }

    private final String name;

    private final int blockCount;

    private final int blockSize;

    private final ConcurrentLinkedQueue<FixedMessage> freeQueue = new ConcurrentLinkedQueue<>();

    /**
     * Sole Constructor.
     *
     * @param name is the name of this allocator.
     * @param blockCount is the maximum number of allocated blocks.
     * @param blockSize is the size of each allocated block.
     */
    public FixedBlockAllocator (final String name,
                                final int blockCount,
                                final int blockSize)
    {
        this.name = name;
        this.blockCount = blockCount;
        this.blockSize = blockSize;

        for (int i = 0; i < blockCount; i++)
        {
            freeQueue.add(new FixedMessage(blockSize));
        }
    }

    @Override
    public String name ()
    {
        return name;
    }

    @Override
    public CascadeOperand alloc (final byte[] buffer,
                                 final int offset,
                                 final int length)
    {
        final FixedMessage message = alloc();

        return message;
    }

    private FixedMessage alloc ()
    {
        final FixedMessage message = freeQueue.poll();

        if (message == null)
        {
            // TODO: ERROR
            throw new IllegalStateException();
        }
        else
        {
            return message;
        }
    }

}
