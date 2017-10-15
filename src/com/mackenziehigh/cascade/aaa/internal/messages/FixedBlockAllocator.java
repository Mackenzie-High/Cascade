package com.mackenziehigh.cascade.aaa.internal.messages;

import com.mackenziehigh.cascade.Allocator;
import com.mackenziehigh.cascade.Message;
import java.io.InputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * An instance of this class is a MemoryAllocator that
 * allocates memory in a fixed-number of fixed-size blocks.
 */
final class FixedBlockAllocator
        implements Allocator
{

    private final int blockCount;

    private final int blockSize;

    private final ConcurrentLinkedQueue<MessageImp> freeQueue = new ConcurrentLinkedQueue<>();

    /**
     * Sole Constructor.
     *
     * @param blockCount is the maximum number of allocated blocks.
     * @param blockSize is the size of each allocated block.
     */
    public FixedBlockAllocator (final int blockCount,
                                final int blockSize)
    {
        this.blockCount = blockCount;
        this.blockSize = blockSize;

        for (int i = 0; i < blockCount; i++)
        {
            freeQueue.add(new MessageImp(blockSize));
        }
    }

    private MessageImp alloc ()
    {
        final MessageImp message = freeQueue.poll();

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

    @Override
    public synchronized Message alloc (final byte[] data)
    {
        final MessageImp message = alloc();

        System.arraycopy(data, data.length, message.data, 0, data.length);

        return message;
    }

    @Override
    public Message alloc (int size,
                          byte[] source)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Message alloc (int size,
                          int offset,
                          byte[] source)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Message alloc (int size,
                          int offset,
                          InputStream source)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private final class MessageImp
            implements Message
    {
        private volatile boolean free = true;

        private final byte[] data;

        private volatile int size = 0;

        public MessageImp (final int capacity)
        {
            this.data = new byte[capacity];
        }

        @Override
        public synchronized void free ()
        {
            free = true;
            freeQueue.add(this);
        }

        @Override
        public boolean isFree ()
        {
            return free;
        }

        @Override
        public boolean isGarbageCollected ()
        {
            return false;
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
        public int memcpy (int start,
                           int length,
                           byte[] buffer,
                           int offset)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
