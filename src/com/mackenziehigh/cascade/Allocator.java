package com.mackenziehigh.cascade;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 *
 */
public interface Allocator
{
    public Message alloc (byte[] data);

    public Message alloc (int size,
                          byte[] source);

    public Message alloc (int size,
                          int offset,
                          byte[] source);

    public Message alloc (int size,
                          int offset,
                          InputStream source);

    public default Message from (final boolean value)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(1);
        final byte[] array = buffer.put((byte) (value ? 1 : 0)).array();
        return alloc(array);
    }

    public default Message from (final char value)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(2);
        final byte[] array = buffer.putChar(value).array();
        return alloc(array);
    }

    public default Message from (final byte value)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(1);
        final byte[] array = buffer.put(value).array();
        return alloc(array);
    }

    public default Message from (final short value)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(2);
        final byte[] array = buffer.putShort(value).array();
        return alloc(array);
    }

    public default Message from (final int value)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(4);
        final byte[] array = buffer.putInt(value).array();
        return alloc(array);
    }

    public default Message from (final long value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }

    public default Message from (final float value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }

    public default Message from (final double value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }

    public default Message from (final String value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }

    public default Message from (final boolean[] value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }

    public default Message from (final char[] value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }

    public default Message from (final byte[] value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }

    public default Message from (final short[] value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }

    public default Message from (final int[] value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }

    public default Message from (final long[] value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }

    public default Message from (final float[] value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }

    public default Message from (final double[] value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }

    public default Message from (final String[] value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }
}
