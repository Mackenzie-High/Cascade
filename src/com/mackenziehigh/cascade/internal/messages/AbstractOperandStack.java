package com.mackenziehigh.cascade.internal.messages;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.primitives.Chars;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeAllocator.AllocationPool;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import java.nio.charset.Charset;
import java.util.function.Predicate;

/**
 * Partial Implementation Of: OperandStack.
 *
 * <p>
 * A unit-test is used to ensure that all of the methods herein
 * are either (abstract) or (final + synchronized),
 * because OperandStack needs to be thread-safe.
 * </p>
 */
public abstract class AbstractOperandStack
        implements OperandStack
{
    /**
     * {@inheritDoc}
     */
    @Override
    public abstract CascadeAllocator allocator ();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract AllocationPool pool ();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract OperandStack assign (OperandStack value);

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack clear ()
    {
        return pop(stackSize());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized void close ()
    {
        clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract int operandSize ();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract int operandCapacity ();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract int stackSize ();

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized boolean isOperandEmpty ()
    {
        return operandSize() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized boolean isStackEmpty ()
    {
        return stackSize() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract byte byteAt (int index);

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized int memcpy (byte[] buffer)
    {
        return memcpy(buffer, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized int memcpy (final byte[] buffer,
                                          final int offset)
    {
        final int size = operandSize();

        if (offset + size > buffer.length)
        {
            return -1;
        }

        for (int i = 0; i < size; i++)
        {
            buffer[offset + i] = byteAt(i);
        }

        return size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized boolean asBoolean ()
    {
        Preconditions.checkState(operandSize() == 1, "Wrong Size");
        return asByte() == 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized char asChar ()
    {
        Preconditions.checkState(operandSize() == 2, "Wrong Size");
        return (char) Chars.fromBytes(byteAt(0), byteAt(1));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized byte asByte ()
    {
        Preconditions.checkState(operandSize() == 1, "Wrong Size");
        return byteAt(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized short asShort ()
    {
        Preconditions.checkState(operandSize() == 2, "Wrong Size");
        return Shorts.fromBytes(byteAt(0), byteAt(1));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized int asInt ()
    {
        Preconditions.checkState(operandSize() == 4, "Wrong Size");
        return Ints.fromBytes(byteAt(0),
                              byteAt(1),
                              byteAt(2),
                              byteAt(3));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized long asLong ()
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
     * {@inheritDoc}
     */
    @Override
    public final synchronized float asFloat ()
    {
        Preconditions.checkState(operandSize() == 4, "Wrong Size");
        return Float.intBitsToFloat(asInt());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized double asDouble ()
    {
        Preconditions.checkState(operandSize() == 8, "Wrong Size");
        return Double.longBitsToDouble(asLong());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized String asString ()
    {
        return new String(asByteArray(), Charset.forName("UTF-8"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized boolean[] asBooleanArray ()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized char[] asCharArray ()
    {
        Preconditions.checkState(operandSize() % 2 == 0, "Invalid Size");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized byte[] asByteArray ()
    {
        final byte[] array = new byte[operandSize()];
        Verify.verify(memcpy(array) == operandSize());
        return array;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized short[] asShortArray ()
    {
        Preconditions.checkState(operandSize() % 2 == 0, "Invalid Size");
        final short[] array = new short[operandSize() / 2];
        for (int i = 0; i < array.length; i++)
        {
            array[i] = Shorts.fromBytes(byteAt(i * 2), byteAt(i * 2 + 1));
        }
        return array;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized int[] asIntArray ()
    {
        Preconditions.checkState(operandSize() % 4 == 0, "Invalid Size");
        final int[] array = new int[operandSize() / 4];
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
     * {@inheritDoc}
     */
    @Override
    public final synchronized long[] asLongArray ()
    {
        Preconditions.checkState(operandSize() % 8 == 0, "Invalid Size");
        final long[] array = new long[operandSize() / 8];
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
     * {@inheritDoc}
     */
    @Override
    public final synchronized float[] asFloatArray ()
    {
        Preconditions.checkState(operandSize() % 4 == 0, "Invalid Size");
        final float[] array = new float[operandSize() / 4];
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
     * {@inheritDoc}
     */
    @Override
    public final synchronized double[] asDoubleArray ()
    {
        Preconditions.checkState(operandSize() % 8 == 0, "Invalid Size");
        final double[] array = new double[operandSize() / 8];
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
     * {@inheritDoc}
     */
    @Override
    public final synchronized String[] asStringArray ()
    {
        Preconditions.checkState(operandSize() >= 4, "Missing ArraySize Header");

        final int arraySize = Ints.fromBytes(byteAt(0), byteAt(1), byteAt(2), byteAt(3));
        Preconditions.checkState(arraySize >= 0, "Negative ArraySize Header");
        Preconditions.checkState(arraySize >= operandSize(), "ArraySize Header is Too Large");
        final String[] array = new String[arraySize];

        int pos = 4;
        int i = 0;
        while (i < array.length && pos < (operandSize() - 4))
        {
            final int strSize = Ints.fromBytes(byteAt(pos + 0),
                                               byteAt(pos + 1),
                                               byteAt(pos + 2),
                                               byteAt(pos + 3));
            pos += 4;
            Preconditions.checkState(strSize < (operandSize() - pos), "StrSize is Too Large");

            final byte[] strBytes = new byte[strSize];
            // TODO:
            // 1. Get string byte
            // 2. Create string UTF-8
            // 3. Add to the array.
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack push (final OperandStack values,
                                                 final int count)
    {

//            if (count < 0)
//            {
//                throw new IllegalArgumentException("count < 0");
//            }
//            else if (count == 0)
//            {
//                return this;
//            }
//            else if (count == 1)
//            {
//
//            }
//            else
//            {
//
//            }
        throw new IllegalArgumentException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack pushZ (final boolean value)
    {
        final byte[] array = new byte[1];
        array[0] = (byte) (value ? 1 : 0);
        return pushBA(array);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack pushC (final char value)
    {
        return pushBA(Chars.toByteArray(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack pushB (final byte value)
    {
        final byte[] array = new byte[1];
        array[0] = value;
        return pushBA(array);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack pushS (final short value)
    {
        return pushBA(Shorts.toByteArray(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack pushI (final int value)
    {
        return pushBA(Ints.toByteArray(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack pushJ (final long value)
    {
        return pushBA(Longs.toByteArray(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack pushF (final float value)
    {
        return pushI(Float.floatToIntBits(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack pushD (final double value)
    {
        return pushBA(Longs.toByteArray(Double.doubleToLongBits(value)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack pushStr (final String value)
    {
        Preconditions.checkNotNull(value, "value");
        return pushBA(value.getBytes(Charset.forName("UTF-8")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack pushZA (boolean[] value)
    {
        throw new RuntimeException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack pushCA (char[] value)
    {
        throw new RuntimeException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack pushBA (final byte[] value)
    {
        return pushBA(value, 0, value.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack pushBA (final byte[] buffer,
                                                   final int offset,
                                                   final int length)
    {
        Preconditions.checkNotNull(buffer, "value");
        pool().alloc(this, buffer, offset, length);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack pushSA (short[] value)
    {
        throw new RuntimeException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack pushIA (int[] value)
    {
        throw new RuntimeException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack pushJA (long[] value)
    {
        throw new RuntimeException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack pushFA (float[] value)
    {
        throw new RuntimeException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack pushDA (double[] value)
    {
        throw new RuntimeException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack pushStrA (String[] value)
    {
        throw new RuntimeException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack pop ()
    {
        return pop(1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract OperandStack pop (final int count);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract OperandStack get (OperandStack out,
                                      int depth);

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack dup ()
    {
        return dup(1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack dup (final int count)
    {
        return push(this, count);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack swap ()
    {
        /**
         * Note: Allocating temporary operand-stacks will avoid
         * the need to perform an expensive byte[] heap-allocation.
         */
        try (OperandStack x = allocator().newOperandStack())
        {
            x.assign(this);
            pop();

            try (OperandStack y = allocator().newOperandStack())
            {
                y.assign(this);
                pop();

                push(x, 1);
                push(y, 1);
            }
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack divD ()
    {
        final double right = asDouble();
        pop();
        final double left = asDouble();
        pop();
        final double result = left / right;
        pushD(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack divF ()
    {
        final float right = asFloat();
        pop();
        final float left = asFloat();
        pop();
        final float result = left / right;
        pushF(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack divJ ()
    {
        final long right = asLong();
        pop();
        final long left = asLong();
        pop();
        final long result = left / right;
        pushJ(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack divI ()
    {
        final int right = asInt();
        pop();
        final int left = asInt();
        pop();
        final int result = left / right;
        pushI(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack divS ()
    {
        final short right = asShort();
        pop();
        final short left = asShort();
        pop();
        final short result = (short) (left / right);
        pushS(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack divB ()
    {
        final byte right = asByte();
        pop();
        final byte left = asByte();
        pop();
        final byte result = (byte) (left / right);
        pushB(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack divC ()
    {
        final char right = asChar();
        pop();
        final char left = asChar();
        pop();
        final char result = (char) (left / right);
        pushC(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack modD ()
    {
        final double right = asDouble();
        pop();
        final double left = asDouble();
        pop();
        final double result = left % right;
        pushD(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack modF ()
    {
        final float right = asFloat();
        pop();
        final float left = asFloat();
        pop();
        final float result = left % right;
        pushF(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack modJ ()
    {
        final long right = asLong();
        pop();
        final long left = asLong();
        pop();
        final long result = left % right;
        pushJ(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack modI ()
    {
        final int right = asInt();
        pop();
        final int left = asInt();
        pop();
        final int result = left % right;
        pushI(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack modS ()
    {
        final short right = asShort();
        pop();
        final short left = asShort();
        pop();
        final short result = (short) (left % right);
        pushS(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack modB ()
    {
        final byte right = asByte();
        pop();
        final byte left = asByte();
        pop();
        final byte result = (byte) (left % right);
        pushB(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack modC ()
    {
        final char right = asChar();
        pop();
        final char left = asChar();
        pop();
        final char result = (char) (left % right);
        pushC(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack mulD ()
    {
        final double right = asDouble();
        pop();
        final double left = asDouble();
        pop();
        final double result = left * right;
        pushD(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack mulF ()
    {
        final float right = asFloat();
        pop();
        final float left = asFloat();
        pop();
        final float result = left * right;
        pushF(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack mulJ ()
    {
        final long right = asLong();
        pop();
        final long left = asLong();
        pop();
        final long result = left * right;
        pushJ(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack mulI ()
    {
        final int right = asInt();
        pop();
        final int left = asInt();
        pop();
        final int result = left * right;
        pushI(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack mulS ()
    {
        final short right = asShort();
        pop();
        final short left = asShort();
        pop();
        final short result = (short) (left * right);
        pushS(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack mulB ()
    {
        final byte right = asByte();
        pop();
        final byte left = asByte();
        pop();
        final byte result = (byte) (left * right);
        pushB(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack mulC ()
    {
        final char right = asChar();
        pop();
        final char left = asChar();
        pop();
        final char result = (char) (left * right);
        pushC(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack addD ()
    {
        final double right = asDouble();
        pop();
        final double left = asDouble();
        pop();
        final double result = left + right;
        pushD(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack addF ()
    {
        final float right = asFloat();
        pop();
        final float left = asFloat();
        pop();
        final float result = left + right;
        pushF(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack addJ ()
    {
        final long right = asLong();
        pop();
        final long left = asLong();
        pop();
        final long result = left + right;
        pushJ(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack addI ()
    {
        final int right = asInt();
        pop();
        final int left = asInt();
        pop();
        final int result = left + right;
        pushI(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack addS ()
    {
        final short right = asShort();
        pop();
        final short left = asShort();
        pop();
        final short result = (short) (left + right);
        pushS(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack addB ()
    {
        final byte right = asByte();
        pop();
        final byte left = asByte();
        pop();
        final byte result = (byte) (left + right);
        pushB(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack addC ()
    {
        final char right = asChar();
        pop();
        final char left = asChar();
        pop();
        final char result = (char) (left + right);
        pushC(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack subD ()
    {
        final double right = asDouble();
        pop();
        final double left = asDouble();
        pop();
        final double result = left - right;
        pushD(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack subF ()
    {
        final float right = asFloat();
        pop();
        final float left = asFloat();
        pop();
        final float result = left - right;
        pushF(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack subJ ()
    {
        final long right = asLong();
        pop();
        final long left = asLong();
        pop();
        final long result = left - right;
        pushJ(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack subI ()
    {
        final int right = asInt();
        pop();
        final int left = asInt();
        pop();
        final int result = left - right;
        pushI(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack subS ()
    {
        final short right = asShort();
        pop();
        final short left = asShort();
        pop();
        final short result = (short) (left - right);
        pushS(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack subB ()
    {
        final byte right = asByte();
        pop();
        final byte left = asByte();
        pop();
        final byte result = (byte) (left - right);
        pushB(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack subC ()
    {
        final char right = asChar();
        pop();
        final char left = asChar();
        pop();
        final char result = (char) (left - right);
        pushC(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack concat ()
    {
        final String right = asString();
        pop();
        final String left = asString();
        pop();
        final String result = left + right;
        pushStr(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack convertD2D ()
    {
        return this; // No Operation Required
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack convertD2F ()
    {
        final double operand = asDouble();
        pop();
        final float result = (float) operand;
        pushF(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack convertD2J ()
    {
        final double operand = asDouble();
        pop();
        final long result = (long) operand;
        pushJ(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack convertD2I ()
    {
        final double operand = asDouble();
        pop();
        final int result = (int) operand;
        pushI(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack convertD2S ()
    {
        final double operand = asDouble();
        pop();
        final short result = (short) operand;
        pushS(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack convertD2B ()
    {
        final double operand = asDouble();
        pop();
        final byte result = (byte) operand;
        pushB(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack convertD2C ()
    {
        final double operand = asDouble();
        pop();
        final char result = (char) operand;
        pushC(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized OperandStack match (final Predicate<OperandStack> functor)
    {
        return pushZ(functor.test(this));
    }

}
