package com.mackenziehigh.cascade;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import java.nio.charset.Charset;

/**
 * Immutable Binary Message.
 *
 * <p>
 * An instance of this message may be based on pre-allocated buffers;
 * therefore, it may be necessary to invoke free() after use.
 * </p>
 */
public interface Message
{
    /**
     * Use this method to free this message and
     * return it to the pool of free messages.
     */
    public void free ();

    /**
     * This method determines whether this message is free.
     *
     * @return true, iff this message is currently free.
     */
    public boolean isFree ();

    /**
     * Use this method to determine whether free()
     * needs to be called when this message is no
     * longer in-use in order to avoid memory leaks.
     *
     * @return true, if free() is *not* needed.
     */
    public boolean isGarbageCollected ();

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
    public default int memcpy (byte[] buffer,
                               int offset)
    {
        return memcpy(0, size(), buffer, offset);
    }

    public int memcpy (int start,
                       int length,
                       byte[] buffer,
                       int offset);

    public default boolean asBoolean ()
    {
        Preconditions.checkState(size() == 1, "Wrong Size");
        return asByte() == 1;
    }

    public default char asChar ()
    {
        Preconditions.checkState(size() == 2, "Wrong Size");
        return (char) asShort();
    }

    public default byte asByte ()
    {
        Preconditions.checkState(size() == 1, "Wrong Size");
        return byteAt(0);
    }

    public default short asShort ()
    {
        Preconditions.checkState(size() == 2, "Wrong Size");
        return Shorts.fromBytes(byteAt(0), byteAt(1));
    }

    public default int asInt ()
    {
        Preconditions.checkState(size() == 4, "Wrong Size");
        return Ints.fromBytes(byteAt(0),
                              byteAt(1),
                              byteAt(2),
                              byteAt(3));
    }

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

    public default float asFloat ()
    {
        Preconditions.checkState(size() == 4, "Wrong Size");
        return Float.intBitsToFloat(asInt());
    }

    public default double asDouble ()
    {
        Preconditions.checkState(size() == 8, "Wrong Size");
        return Double.longBitsToDouble(asLong());
    }

    public default String asString ()
    {
        return new String(asByteArray(), Charset.forName("UTF-8"));
    }

    public default boolean[] asBooleanArray ()
    {
        return null;
    }

    public default char[] asCharArray ()
    {
        Preconditions.checkState(size() % 2 == 0, "Invalid Size");
        return null;
    }

    public default byte[] asByteArray ()
    {
        final byte[] array = new byte[size()];
        Verify.verify(memcpy(array) == size());
        return array;
    }

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

    public default String[] asStringArray ()
    {
        return null;
    }
}
