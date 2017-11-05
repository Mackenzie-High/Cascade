package com.mackenziehigh.cascade;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import java.nio.charset.Charset;

/**
 * Immutable Binary Message.
 */
public interface CascadeOperand
{
    /**
     * Getter.
     *
     * @return the allocator that created this operand.
     */
    public CascadeAllocator allocator ();

    /**
     * Getter.
     *
     * @return the size of this message in bytes.
     */
    public int size ();

    /**
     * Getter.
     *
     * @return the maximum size of this message in bytes.
     */
    public int capacity ();

    /**
     * Use this method to efficiently retrieve a
     * byte at a given index within this message.
     *
     * @param index identifies the byte in this message.
     * @return the byte at the given index.
     * @throws IndexOutOfBoundsException if index is illegal.
     */
    public byte byteAt (int index);

    /**
     * Use this method to copy the content of this message into a given buffer.
     *
     * @param buffer is the buffer that will receive the content.
     * @return the number of bytes copied into the buffer,
     * which may be less than the size() of the message,
     * if the buffer is too small.
     */
    public default int memcpy (byte[] buffer)
    {
        return memcpy(buffer, 0);
    }

    /**
     * Use this method to copy the content of this message into a given buffer,
     * starting at a given offset in the buffer.
     *
     * @param buffer is the buffer that will receive the content.
     * @param offset is an index into the buffer.
     * @return (-1) if the offset is out-of-range; otherwise,
     * return the number of bytes copied into the buffer,
     * which may be less than the size() of the message,
     * if the buffer is too small.
     */
    public default int memcpy (final byte[] buffer,
                               final int offset)
    {
        if (offset + size() >= buffer.length)
        {
            return -1;
        }

        final int size = size();

        for (int i = 0; i < size; i++)
        {
            buffer[offset + i] = byteAt(i);
        }

        return size;
    }

    /**
     * Data Conversion: byte[] to boolean.
     *
     * @return the converted value.
     * @throws IllegalStateException if size() &ne (1).
     */
    public default boolean asBoolean ()
    {
        Preconditions.checkState(size() == 1, "Wrong Size");
        return asByte() == 1;
    }

    /**
     * Data Conversion: byte[] to char.
     *
     * <p>
     * The bytes must be in big-endian byte-order.
     * </p>
     *
     * @return the converted value.
     * @throws IllegalStateException if size() &ne (2).
     */
    public default char asChar ()
    {
        Preconditions.checkState(size() == 2, "Wrong Size");
        return (char) asShort();
    }

    /**
     * Data Conversion: byte[] to byte.
     *
     * @return the converted value.
     * @throws IllegalStateException if size() &ne (1).
     */
    public default byte asByte ()
    {
        Preconditions.checkState(size() == 1, "Wrong Size");
        return byteAt(0);
    }

    /**
     * Data Conversion: byte[] to short.
     *
     * <p>
     * The bytes must be in big-endian byte-order.
     * </p>
     *
     * @return the converted value.
     * @throws IllegalStateException if size() &ne (2).
     */
    public default short asShort ()
    {
        Preconditions.checkState(size() == 2, "Wrong Size");
        return Shorts.fromBytes(byteAt(0), byteAt(1));
    }

    /**
     * Data Conversion: byte[] to int.
     *
     * <p>
     * The bytes must be in big-endian byte-order.
     * </p>
     *
     * @return the converted value.
     * @throws IllegalStateException if size() &ne (4).
     */
    public default int asInt ()
    {
        Preconditions.checkState(size() == 4, "Wrong Size");
        return Ints.fromBytes(byteAt(0),
                              byteAt(1),
                              byteAt(2),
                              byteAt(3));
    }

    /**
     * Data Conversion: byte[] to long.
     *
     * <p>
     * The bytes must be in big-endian byte-order.
     * </p>
     *
     * @return the converted value.
     * @throws IllegalStateException if size() &ne (8).
     */
    public default long asLong ()
    {
        Preconditions.checkState(size() == 8, "Wrong Size");
        return Longs.fromBytes(byteAt(0),
                               byteAt(1),
                               byteAt(2),
                               byteAt(3),
                               byteAt(4),
                               byteAt(5),
                               byteAt(6),
                               byteAt(7));
    }

    /**
     * Data Conversion: byte[] to float.
     *
     * @return the converted value.
     * @throws IllegalStateException if size() &ne (4).
     */
    public default float asFloat ()
    {
        Preconditions.checkState(size() == 4, "Wrong Size");
        return Float.intBitsToFloat(asInt());
    }

    /**
     * Data Conversion: byte[] to double.
     *
     * @return the converted value.
     * @throws IllegalStateException if size() &ne (2).
     */
    public default double asDouble ()
    {
        Preconditions.checkState(size() == 8, "Wrong Size");
        return Double.longBitsToDouble(asLong());
    }

    /**
     * Data Conversion: Encoded byte[] to String.
     *
     * <p>
     * The bytes must be UTF-8 encoded.
     * </p>
     *
     * <p>
     * All of the bytes are considered to be part of the string.
     * No size header, etc, is present in the byte[].
     * </p>
     *
     * @return the converted value.
     * @throws IllegalStateException if size() &ne (2).
     */
    public default String asString ()
    {
        return new String(asByteArray(), Charset.forName("UTF-8"));
    }

    /**
     * Data Conversion: byte[] to boolean[].
     *
     * <p>
     * Each byte represents a single boolean value.
     * A byte equal to zero equates to false.
     * A byte not-equal to zero equates to true.
     * </p>
     *
     * @return the converted value.
     */
    public default boolean[] asBooleanArray ()
    {
        return null;
    }

    /**
     * Data Conversion: byte[] to char[].
     *
     * <p>
     * For each element, the bytes must be in big-endian byte-order.
     * </p>
     *
     * @return the converted value.
     * @throws IllegalStateException if size() &#37 (2) &ne (0).
     */
    public default char[] asCharArray ()
    {
        Preconditions.checkState(size() % 2 == 0, "Invalid Size");
        return null;
    }

    /**
     * Data Conversion: byte[] to byte[].
     *
     * @return the converted value.
     */
    public default byte[] asByteArray ()
    {
        final byte[] array = new byte[size()];
        Verify.verify(memcpy(array) == size());
        return array;
    }

    /**
     * Data Conversion: byte[] to short[].
     *
     * <p>
     * For each element, the bytes must be in big-endian byte-order.
     * </p>
     *
     * @return the converted value.
     * @throws IllegalStateException if size() &#37 (2) &ne (0).
     */
    public default short[] asShortArray ()
    {
        Preconditions.checkState(size() % 2 == 0, "Invalid Size");
        final short[] array = new short[size() / 2];
        for (int i = 0; i < array.length; i++)
        {
            array[i] = Shorts.fromBytes(byteAt(i * 2), byteAt(i * 2 + 1));
        }
        return array;
    }

    /**
     * Data Conversion: byte[] to int[].
     *
     * <p>
     * For each element, the bytes must be in big-endian byte-order.
     * </p>
     *
     * @return the converted value.
     * @throws IllegalStateException if size() &#37 (4) &ne (0).
     */
    public default int[] asIntArray ()
    {
        Preconditions.checkState(size() % 4 == 0, "Invalid Size");
        final int[] array = new int[size() / 4];
        for (int i = 0; i < array.length; i++)
        {
            array[i] = Ints.fromBytes(byteAt(i * 4 + 0),
                                      byteAt(i * 4 + 1),
                                      byteAt(i * 4 + 2),
                                      byteAt(i * 4 + 3));
        }
        return array;
    }

    /**
     * Data Conversion: byte[] to long[].
     *
     * <p>
     * For each element, the bytes must be in big-endian byte-order.
     * </p>
     *
     * @return the converted value.
     * @throws IllegalStateException if size() &#37 (8) &ne (0).
     */
    public default long[] asLongArray ()
    {
        Preconditions.checkState(size() % 8 == 0, "Invalid Size");
        final long[] array = new long[size() / 8];
        for (int i = 0; i < array.length; i++)
        {
            array[i] = Longs.fromBytes(byteAt(i * 8 + 0),
                                       byteAt(i * 8 + 1),
                                       byteAt(i * 8 + 2),
                                       byteAt(i * 8 + 3),
                                       byteAt(i * 8 + 4),
                                       byteAt(i * 8 + 5),
                                       byteAt(i * 8 + 6),
                                       byteAt(i * 8 + 7));
        }
        return array;
    }

    /**
     * Data Conversion: byte[] to float[].
     *
     * @return the converted value.
     * @throws IllegalStateException if size() &#37 (4) &ne (0).
     */
    public default float[] asFloatArray ()
    {
        Preconditions.checkState(size() % 4 == 0, "Invalid Size");
        final float[] array = new float[size() / 4];
        for (int i = 0; i < array.length; i++)
        {
            final int bits = Ints.fromBytes(byteAt(i * 4 + 0),
                                            byteAt(i * 4 + 1),
                                            byteAt(i * 4 + 2),
                                            byteAt(i * 4 + 3));
            array[i] = Float.intBitsToFloat(bits);
        }
        return array;
    }

    /**
     * Data Conversion: byte[] to double[].
     *
     * @return the converted value.
     * @throws IllegalStateException if size() &#37 (8) &ne (0).
     */
    public default double[] asDoubleArray ()
    {
        Preconditions.checkState(size() % 8 == 0, "Invalid Size");
        final double[] array = new double[size() / 8];
        for (int i = 0; i < array.length; i++)
        {
            final long bits = Longs.fromBytes(byteAt(i * 8 + 0),
                                              byteAt(i * 8 + 1),
                                              byteAt(i * 8 + 2),
                                              byteAt(i * 8 + 3),
                                              byteAt(i * 8 + 4),
                                              byteAt(i * 8 + 5),
                                              byteAt(i * 8 + 6),
                                              byteAt(i * 8 + 7));
            array[i] = Double.longBitsToDouble(bits);
        }
        return array;
    }

    /**
     * Data Conversion: Encoded byte[] to String[].
     *
     * <p>
     * Each string must be UTF-8 encoded.
     * </p>
     *
     * <p>
     * The first four bytes is a big-endian integer,
     * which specifies the number of array elements.
     * </p>
     *
     * <p>
     * Each element in the array consists of a header and body.
     * The header is big-endian encoded four-byte integer that
     * specifies the number (N) of bytes in the body.
     * The body is (N) bytes that are the UTF-8 string itself.
     * </p>
     *
     * @return the converted value.
     * @throws IllegalStateException if size() &lt (4).
     * @throws IllegalStateException if the conversion is not
     * possible due to invalid headers in the data.
     */
    public default String[] asStringArray ()
    {
        Preconditions.checkState(size() >= 4, "Missing ArraySize Header");

        final int arraySize = Ints.fromBytes(byteAt(0), byteAt(1), byteAt(2), byteAt(3));
        Preconditions.checkState(arraySize >= 0, "Negative ArraySize Header");
        Preconditions.checkState(arraySize >= size(), "ArraySize Header is Too Large");
        final String[] array = new String[arraySize];

        int pos = 4;
        int i = 0;
        while (i < array.length && pos < (size() - 4))
        {
            final int strSize = Ints.fromBytes(byteAt(pos + 0),
                                               byteAt(pos + 1),
                                               byteAt(pos + 2),
                                               byteAt(pos + 3));
            pos += 4;
            Preconditions.checkState(strSize < (size() - pos), "StrSize is Too Large");

            final byte[] strBytes = new byte[strSize];
            // TODO:
            // 1. Get string byte
            // 2. Create string UTF-8
            // 3. Add to the array.
        }

        return null;
    }
}
