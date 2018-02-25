package com.mackenziehigh.cascade.allocators;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Chars;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import java.nio.charset.Charset;

/**
 *
 */
public final class OperandStack
{
    private final int height;

    private final int operandSize;

    private final OperandStack below;

    private final Object object;

    private final long bytes0;

    private final long bytes1;

    private final long bytes2;

    private final long bytes3;

    private final long bytes4;

    private final long bytes5;

    private final long bytes6;

    private final long bytes7;

    private OperandStack ()
    {
        height = 0;
        operandSize = 0;
        below = this;
        object = null;
        bytes0 = 0;
        bytes1 = 0;
        bytes2 = 0;
        bytes3 = 0;
        bytes4 = 0;
        bytes5 = 0;
        bytes6 = 0;
        bytes7 = 0;
    }

    private OperandStack (final OperandStack under,
                          final int size,
                          final long long0,
                          final long long1,
                          final long long2,
                          final long long3,
                          final long long4,
                          final long long5,
                          final long long6,
                          final long long7)
    {
        height = under.height + 1;
        operandSize = size;
        below = under;
        object = null;
        bytes0 = long0;
        bytes1 = long1;
        bytes2 = long2;
        bytes3 = long3;
        bytes4 = long4;
        bytes5 = long5;
        bytes6 = long6;
        bytes7 = long7;
    }

    public OperandStack push (Object value)
    {
        return null;
    }

    public Object asObject ()
    {
        return object;
    }

    /**
     * Getter.
     *
     * @return the size of the top operand on the operand-stack in bytes.
     */
    public int operandSize ()
    {
        return operandSize;
    }

    /**
     * Getter.
     *
     * @return the number of operands that are on this operand-stack.
     */
    public int stackSize ()
    {
        return height;
    }

    /**
     * Getter.
     *
     * @return true, iff the operandSize() is zero.
     */
    public boolean isOperandEmpty ()
    {
        return operandSize() == 0;
    }

    /**
     * Getter.
     *
     * @return true, iff the stackSize() is zero.
     */
    public boolean isStackEmpty ()
    {
        return stackSize() == 0;
    }

    /**
     * Copy an operand from the top of one operand-stack
     * onto the top of this operand-stack.
     *
     * @param value contains the operand to push.
     * @return this.
     * @throws NullPointerException if value is null.
     * @throws AllocatorMismatchException if value was not created by this allocator.
     * @throws IllegalArgumentException if value is empty.
     */
    public OperandStack push (OperandStack value)
    {
        return this; // TODO
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * <p>
     * True, becomes a single byte equal to one.
     * False, becomes a single byte equal to zero.
     * </p>
     *
     * @param value is the operand to push.
     * @return this.
     */
    public OperandStack push (final boolean value)
    {
        return push((byte) (value ? 1 : 0));
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * @param value is the operand to push.
     * @return this.
     */
    public OperandStack push (final char value)
    {
        return push(Chars.toByteArray(value));
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * @param value is the operand to push.
     * @return this.
     */
    public OperandStack push (final byte value)
    {
        final byte[] array = new byte[1];
        array[0] = value;
        return push(array);
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * @param value is the operand to push.
     * @return this.
     */
    public OperandStack push (final short value)
    {
        return push(Shorts.toByteArray(value));
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * @param value is the operand to push.
     * @return this.
     */
    public OperandStack push (final int value)
    {
        return push(Ints.toByteArray(value));
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * @param value is the operand to push.
     * @return this.
     */
    public OperandStack push (final long value)
    {
        return push(Longs.toByteArray(value));
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * @param value is the operand to push.
     * @return this.
     */
    public OperandStack push (final float value)
    {
        return push(Float.floatToIntBits(value));
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * @param value is the operand to push.
     * @return this.
     */
    public OperandStack push (final double value)
    {
        return push(Double.doubleToLongBits(value));
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * <p>
     * This method converts the string to a UTF-8
     * encoded byte-array and then pushes the array.
     * </p>
     *
     * @param value is the operand to push.
     * @return this.
     */
    public OperandStack push (final String value)
    {
        Preconditions.checkNotNull(value, "value");
        return push(value.getBytes(Charset.forName("UTF-8")));
    }

    /**
     * Push a operand onto the top of the stack.
     *
     * @param value is the operand to push.
     * @return this.
     */
    public OperandStack push (final byte[] value)
    {
        return push(value, 0, value.length);
    }

    /**
     * Push a operand onto the top of the stack.
     *
     * @param buffer is the operand to push.
     * @param offset is the start location of the data in the buffer.
     * @param length is the length of the data.
     * @return this.
     * @throws IndexOutOfBoundsException if offset is less-than zero.
     * @throws IllegalArgumentException if length is less-than zero.
     */
    public OperandStack push (final byte[] buffer,
                              final int offset,
                              final int length)
    {
        long long0;
        long long1;
        long long2;
        long long3;
        long long4;
        long long5;
        long long6;
        long long7;
        final int size = length;
        OperandStack result = null;

        switch (length)
        {
            case 64:
                long0 = Longs.fromBytes(buffer[offset + 0], buffer[offset + 1], buffer[offset + 2], buffer[offset + 3], buffer[offset + 4], buffer[offset + 5], buffer[offset + 6], buffer[offset + 7]);
                long1 = Longs.fromBytes(buffer[offset + 8], buffer[offset + 9], buffer[offset + 10], buffer[offset + 11], buffer[offset + 12], buffer[offset + 13], buffer[offset + 14], buffer[offset + 15]);
                long2 = Longs.fromBytes(buffer[offset + 16], buffer[offset + 17], buffer[offset + 18], buffer[offset + 19], buffer[offset + 20], buffer[offset + 21], buffer[offset + 22], buffer[offset + 23]);
                long3 = Longs.fromBytes(buffer[offset + 24], buffer[offset + 25], buffer[offset + 26], buffer[offset + 27], buffer[offset + 28], buffer[offset + 29], buffer[offset + 30], buffer[offset + 31]);
                long4 = Longs.fromBytes(buffer[offset + 32], buffer[offset + 33], buffer[offset + 34], buffer[offset + 35], buffer[offset + 36], buffer[offset + 37], buffer[offset + 38], buffer[offset + 39]);
                long5 = Longs.fromBytes(buffer[offset + 40], buffer[offset + 41], buffer[offset + 42], buffer[offset + 43], buffer[offset + 44], buffer[offset + 45], buffer[offset + 46], buffer[offset + 47]);
                long6 = Longs.fromBytes(buffer[offset + 48], buffer[offset + 49], buffer[offset + 50], buffer[offset + 51], buffer[offset + 52], buffer[offset + 53], buffer[offset + 54], buffer[offset + 55]);
                long7 = Longs.fromBytes(buffer[offset + 56], buffer[offset + 57], buffer[offset + 58], buffer[offset + 59], buffer[offset + 60], buffer[offset + 61], buffer[offset + 62], buffer[offset + 63]);
                result = new OperandStack(this, size, long0, long1, long2, long3, long4, long5, long6, long7);
                break;
            default:
            // TODO: Use Allocator
        }

        return result;
    }

    /**
     * Use this method to efficiently retrieve a
     * byte at a given index within this operand.
     *
     * @param index identifies the byte in this operand.
     * @return the byte at the given index.
     * @throws IndexOutOfBoundsException if index is invalid.
     * @throws IllegalStateException if stackSize() is zero.
     */
    public byte byteAt (final int index)
    {
        if (isStackEmpty())
        {
            throw new IllegalStateException("Empty Stack");
        }
        else
        {
            return 0; // TODO
        }
    }

    /**
     * Use this method to copy the content of this operand into a given buffer.
     *
     * @param buffer is the buffer that will receive the content.
     * @return the number of bytes copied into the buffer,
     * which may be less than operandSize(), if the buffer is too small.
     * @throws IndexOutOfBoundsException if index is invalid.
     * @throws IllegalStateException if stackSize() is zero.
     */
    public int copyTo (final byte[] buffer)
    {
        return copyTo(buffer, 0);
    }

    /**
     * Use this method to copy the content of this operand into a given buffer,
     * starting at a given offset in the buffer.
     *
     * @param buffer is the buffer that will receive the content.
     * @param offset is an index into the buffer.
     * @return the number of bytes copied into the buffer,
     * which may be less than operandSize(), if the buffer is too small.
     * @throws IndexOutOfBoundsException if index is invalid.
     * @throws IllegalStateException if stackSize() is zero.
     */
    public int copyTo (final byte[] buffer,
                       final int offset)
    {
        return copyTo(buffer, offset, operandSize());
    }

    /**
     * Use this method to copy the content of this operand into a given buffer,
     * starting at a given offset in the buffer.
     *
     * @param buffer is the buffer that will receive the content.
     * @param offset is an index into the buffer.
     * @param length is the maximum number of bytes to copy.
     * @return the number of bytes copied into the buffer,
     * which may be less than operandSize(), if the buffer is too small.
     * @throws IndexOutOfBoundsException if index is invalid.
     * @throws IllegalStateException if stackSize() is zero.
     */
    public int copyTo (final byte[] buffer,
                       final int offset,
                       final int length)
    {
        return 0; // TODO
    }

    /**
     * Data Conversion: byte[] to boolean.
     *
     * @return the converted value.
     * @throws IllegalStateException if operandSize() &ne (1).
     * @throws IllegalStateException if stackSize() is zero.
     */
    public boolean asBoolean ()
    {
        return asByte() != 0;
    }

    /**
     * Data Conversion: byte[] to char.
     *
     * @return the converted value.
     * @throws IllegalStateException if operandSize() &ne (1).
     * @throws IllegalStateException if stackSize() is zero.
     */
    public char asChar ()
    {
        Preconditions.checkState(operandSize() == 2, "Wrong Size");
        return Chars.fromBytes(byteAt(0), byteAt(1));
    }

    /**
     * Data Conversion: byte[] to byte.
     *
     * @return the converted value.
     * @throws IllegalStateException if operandSize() &ne (1).
     * @throws IllegalStateException if stackSize() is zero.
     */
    public byte asByte ()
    {
        Preconditions.checkState(operandSize() == 1, "Wrong Size");
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
     * @throws IllegalStateException if operandSize() &ne (2).
     * @throws IllegalStateException if stackSize() is zero.
     */
    public short asShort ()
    {
        Preconditions.checkState(operandSize() == 2, "Wrong Size");
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
     * @throws IllegalStateException if operandSize() &ne (4).
     * @throws IllegalStateException if stackSize() is zero.
     */
    public int asInt ()
    {
        Preconditions.checkState(operandSize() == 4, "Wrong Size");
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
     * @throws IllegalStateException if operandSize() &ne (8).
     * @throws IllegalStateException if stackSize() is zero.
     */
    public long asLong ()
    {
        Preconditions.checkState(operandSize() == 8, "Wrong Size");
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
     * @throws IllegalStateException if operandSize() &ne (4).
     * @throws IllegalStateException if stackSize() is zero.
     */
    public float asFloat ()
    {
        Preconditions.checkState(operandSize() == 4, "Wrong Size");
        return Float.intBitsToFloat(asInt());
    }

    /**
     * Data Conversion: byte[] to double.
     *
     * @return the converted value.
     * @throws IllegalStateException if operandSize() &ne (8).
     * @throws IllegalStateException if stackSize() is zero.
     */
    public double asDouble ()
    {
        Preconditions.checkState(operandSize() == 8, "Wrong Size");
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
     * @throws IllegalStateException if stackSize() is zero.
     */
    public String asString ()
    {
        return new String(asByteArray(), Charset.forName("UTF-8"));
    }

    /**
     * Getter.
     *
     * @return the content of the top operand on the operand-stack.
     * @throws IllegalStateException if stackSize() is zero.
     */
    public byte[] asByteArray ()
    {
        final byte[] array = new byte[operandSize()];
        copyTo(array);
        return array;
    }

    /**
     * Pop a single operand off the top of the operand-stack.
     *
     * @return this.
     * @throws IllegalStateException if stackSize() is zero.
     */
    public OperandStack pop ()
    {
        if (isStackEmpty())
        {
            throw new IllegalStateException("Empty Stack");
        }
        else
        {
            return below;
        }
    }
}
