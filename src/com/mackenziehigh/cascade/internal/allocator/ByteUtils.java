package com.mackenziehigh.cascade.internal.allocator;

/**
 *
 */
public class ByteUtils
{
    public static byte byteAt (final long value,
                               final int index)
    {
        return (byte) ((value >>> (index * 8)) & 0xFFL);
    }

    public static int high (final long value)
    {
        return (int) ((0xFFFFFFFF00000000L & value) >>> 32);
    }

    public static int low (final long value)
    {
        return (int) (0x00000000FFFFFFFFL & value);
    }
}
