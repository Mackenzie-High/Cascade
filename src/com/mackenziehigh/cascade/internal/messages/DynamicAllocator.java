package com.mackenziehigh.cascade.internal.messages;

import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeOperand;
import java.util.Arrays;

/**
 * This class implements MemoryAllocator using Java object allocation.
 */
public final class DynamicAllocator
        implements CascadeAllocator
{
    private final CascadeAllocator SELF = this;

    private final class DynamicMessage
            implements CascadeOperand
    {
        private final byte[] data;

        private volatile int refCount = 0;

        public DynamicMessage (final byte[] data)
        {
            this.data = data;
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

    public DynamicAllocator (final String name)
    {
        this.name = name;
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
        final byte[] data = Arrays.copyOfRange(buffer, 0, length);
        return new DynamicMessage(data);
    }

}
