package com.mackenziehigh.cascade;

import com.mackenziehigh.cascade.internal.messages.DynamicAllocator;
import java.nio.ByteBuffer;

/**
 * Use this interface to allocate message objects.
 */
public interface CascadeAllocator
{
    /**
     * Default Implementation.
     */
    public static CascadeAllocator DEFAULT = new DynamicAllocator("default");

    /**
     * Getter.
     *
     * @return the default instance.
     */
    public static CascadeAllocator instance ()
    {
        return DEFAULT;
    }

    /**
     * Getter.
     *
     * @return the user-defined name of this allocator.
     */
    public String name ();

    /**
     * Allocate a message.
     *
     * @param data is the content of the message.
     * @return the newly allocated message object.
     */
    public default CascadeOperand alloc (byte[] data)
    {
        return alloc(data, 0, data.length);
    }

    /**
     * Allocate a message.
     *
     * @param offset is the start position of the data in the buffer.
     * @param length is the length of the data in the buffer.
     * @param buffer contains the content of the message.
     * @return the newly allocated message object.
     */
    public CascadeOperand alloc (byte[] buffer,
                                 int offset,
                                 int length);

    /**
     * Convert the given value to an array of bytes
     * and allocate a message to store those bytes.
     *
     * <p>
     * True becomes a single byte equal to (1).
     * False becomes a single byte equal to (0).
     * </p>
     *
     * @param value will be converted to a byte-array.
     * @return the newly allocated message.
     */
    public default CascadeOperand from (final boolean value)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(1);
        final byte[] array = buffer.put((byte) (value ? 1 : 0)).array();
        return alloc(array);
    }

    /**
     * Convert the given value to an array of bytes
     * and allocate a message to store those bytes.
     *
     * @param value will be converted to a byte-array.
     * @return the newly allocated message.
     */
    public default CascadeOperand from (final char value)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(2);
        final byte[] array = buffer.putChar(value).array();
        return alloc(array);
    }

    /**
     * Convert the given value to an array of bytes
     * and allocate a message to store those bytes.
     *
     * @param value will be converted to a byte-array.
     * @return the newly allocated message.
     */
    public default CascadeOperand from (final byte value)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(1);
        final byte[] array = buffer.put(value).array();
        return alloc(array);
    }

    /**
     * Convert the given value to an array of bytes
     * and allocate a message to store those bytes.
     *
     * <p>
     * The value will be encoded as two big-endian bytes.
     * </p>
     *
     * @param value will be converted to a byte-array.
     * @return the newly allocated message.
     */
    public default CascadeOperand from (final short value)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(2);
        final byte[] array = buffer.putShort(value).array();
        return alloc(array);
    }

    /**
     * Convert the given value to an array of bytes
     * and allocate a message to store those bytes.
     *
     * <p>
     * The value will be encoded as four big-endian bytes.
     * </p>
     *
     * @param value will be converted to a byte-array.
     * @return the newly allocated message.
     */
    public default CascadeOperand from (final int value)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(4);
        final byte[] array = buffer.putInt(value).array();
        return alloc(array);
    }

    /**
     * Convert the given value to an array of bytes
     * and allocate a message to store those bytes.
     *
     * <p>
     * The value will be encoded as eight big-endian bytes.
     * </p>
     *
     * @param value will be converted to a byte-array.
     * @return the newly allocated message.
     */
    public default CascadeOperand from (final long value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }

    /**
     * Convert the given value to an array of bytes
     * and allocate a message to store those bytes.
     *
     * <p>
     * The value will be encoded as four big-endian bytes.
     * </p>
     *
     * @param value will be converted to a byte-array.
     * @return the newly allocated message.
     */
    public default CascadeOperand from (final float value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }

    /**
     * Convert the given value to an array of bytes
     * and allocate a message to store those bytes.
     *
     * <p>
     * The value will be encoded as eight big-endian bytes.
     * </p>
     *
     * @param value will be converted to a byte-array.
     * @return the newly allocated message.
     */
    public default CascadeOperand from (final double value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }

    /**
     * Convert the given value to an array of bytes
     * and allocate a message to store those bytes.
     *
     * <p>
     * The value will be encoded using UTF-8 series of bytes.
     * All of the bytes pertain to characters in the string.
     * </p>
     *
     * @param value will be converted to a byte-array.
     * @return the newly allocated message.
     */
    public default CascadeOperand from (final String value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }

    /**
     * Convert the given value to an array of bytes
     * and allocate a message to store those bytes.
     *
     *
     * @param value will be converted to a byte-array.
     * @return the newly allocated message.
     */
    public default CascadeOperand from (final boolean[] value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }

    /**
     * Convert the given value to an array of bytes
     * and allocate a message to store those bytes.
     *
     * @param value will be converted to a byte-array.
     * @return the newly allocated message.
     */
    public default CascadeOperand from (final char[] value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }

    /**
     * Convert the given value to an array of bytes
     * and allocate a message to store those bytes.
     *
     * @param value will be converted to a byte-array.
     * @return the newly allocated message.
     */
    public default CascadeOperand from (final byte[] value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }

    /**
     * Convert the given value to an array of bytes
     * and allocate a message to store those bytes.
     *
     * @param value will be converted to a byte-array.
     * @return the newly allocated message.
     */
    public default CascadeOperand from (final short[] value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }

    /**
     * Convert the given value to an array of bytes
     * and allocate a message to store those bytes.
     *
     * @param value will be converted to a byte-array.
     * @return the newly allocated message.
     */
    public default CascadeOperand from (final int[] value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }

    /**
     * Convert the given value to an array of bytes
     * and allocate a message to store those bytes.
     *
     * @param value will be converted to a byte-array.
     * @return the newly allocated message.
     */
    public default CascadeOperand from (final long[] value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }

    /**
     * Convert the given value to an array of bytes
     * and allocate a message to store those bytes.
     *
     * @param value will be converted to a byte-array.
     * @return the newly allocated message.
     */
    public default CascadeOperand from (final float[] value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }

    /**
     * Convert the given value to an array of bytes
     * and allocate a message to store those bytes.
     *
     * @param value will be converted to a byte-array.
     * @return the newly allocated message.
     */
    public default CascadeOperand from (final double[] value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }

    /**
     * Convert the given value to an array of bytes
     * and allocate a message to store those bytes.
     *
     * @param value will be converted to a byte-array.
     * @return the newly allocated message.
     */
    public default CascadeOperand from (final String[] value)
    {
        final byte[] bytes = new byte[1];
        return alloc(bytes);
    }
}
