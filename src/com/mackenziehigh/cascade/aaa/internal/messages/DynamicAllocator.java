package com.mackenziehigh.cascade.aaa.internal.messages;

import com.mackenziehigh.cascade.Allocator;
import com.mackenziehigh.cascade.Message;
import java.io.InputStream;

/**
 * This class implements MemoryAllocator using Java object allocation.
 */
public final class DynamicAllocator
        implements Allocator
{
    private final class MessageImp
            implements Message
    {
        private final byte[] data;

        public MessageImp (final byte[] data)
        {
            this.data = data;
        }

        @Override
        public void free ()
        {
            // Pass
        }

        @Override
        public boolean isFree ()
        {
            return false;
        }

        @Override
        public boolean isGarbageCollected ()
        {
            return true;
        }

        @Override
        public int size ()
        {
            return data.length;
        }

        @Override
        public int capacity ()
        {
            return size();
        }

        @Override
        public byte byteAt (int index)
        {
            return data[index];
        }

        @Override
        public int memcpy (int start,
                           int length,
                           byte[] buffer,
                           int offset)
        {
            return 0;
        }

    }

    private static Allocator instance = null;

    public static synchronized Allocator instance ()
    {
        instance = instance == null ? new DynamicAllocator() : instance;
        return instance;
    }

    @Override
    public Message alloc (final byte[] data)
    {
        return new MessageImp(data);
    }

    @Override
    public Message alloc (int size,
                          byte[] data)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Message alloc (int size,
                          int offset,
                          byte[] data)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Message alloc (int size,
                          int offset,
                          InputStream data)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
