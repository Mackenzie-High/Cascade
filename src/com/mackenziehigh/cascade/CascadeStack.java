package com.mackenziehigh.cascade;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 *
 */
public final class CascadeStack
{

    private static final CascadeStack EMPTY = new CascadeStack();

    private final CascadeStack below;

    private final int size;

    private final Class type;

    private final long valueLong;

    private final Object valueObject;

    private final int hash;

    private CascadeStack ()
    {
        this.below = null;
        this.size = 0;
        this.type = Object.class;
        this.valueLong = 0;
        this.valueObject = this;
        this.hash = 0;
    }

    private CascadeStack (final CascadeStack below,
                          final Class type,
                          final long value)
    {
        this.below = below;
        this.size = below.size + 1;
        this.type = type;
        this.valueLong = value;
        this.valueObject = null;
        this.hash = (int) (3079 * value) + (3257 * below.hash);
    }

    private CascadeStack (final CascadeStack below,
                          final Object value)
    {
        this.below = below;
        this.size = below.size + 1;
        this.valueLong = 0;
        this.valueObject = Objects.requireNonNull(value, "value");
        this.type = value.getClass();
        this.hash = (int) (3181 * value.hashCode()) + (3331 * below.hash);
    }

    /**
     * Getter.
     *
     * @return the number of operands that are on this operand-stack.
     */
    public int size ()
    {
        return size;
    }

    /**
     * Getter.
     *
     * @return true, iff the size() is zero.
     */
    public boolean isEmpty ()
    {
        return size() == 0;
    }

    public CascadeStack pushObject (final Object value)
    {
        if (value == null)
        {
            throw new NullPointerException("Refusing to push null onto the stack!");
        }
        else if (value.getClass() == boolean.class)
        {
            return pushBoolean((boolean) value);
        }
        else if (value.getClass() == char.class)
        {
            return pushChar((char) value);
        }
        else if (value.getClass() == byte.class)
        {
            return pushByte((byte) value);
        }
        else if (value.getClass() == short.class)
        {
            return pushShort((short) value);
        }
        else if (value.getClass() == int.class)
        {
            return pushInt((int) value);
        }
        else if (value.getClass() == long.class)
        {
            return pushLong((long) value);
        }
        else if (value.getClass() == float.class)
        {
            return pushFloat((float) value);
        }
        else if (value.getClass() == double.class)
        {
            return pushDouble((double) value);
        }
        else
        {
            return new CascadeStack(this, value);
        }
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * @param value is the operand to push.
     * @return this.
     */
    public CascadeStack pushBoolean (final boolean value)
    {
        return new CascadeStack(this, boolean.class, valueLong);
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * @param value is the operand to push.
     * @return this.
     */
    public CascadeStack pushChar (final char value)
    {
        return new CascadeStack(this, char.class, valueLong);
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * @param value is the operand to push.
     * @return this.
     */
    public CascadeStack pushByte (final byte value)
    {
        return new CascadeStack(this, byte.class, valueLong);
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * @param value is the operand to push.
     * @return this.
     */
    public CascadeStack pushShort (final short value)
    {
        return new CascadeStack(this, short.class, valueLong);
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * @param value is the operand to push.
     * @return this.
     */
    public CascadeStack pushInt (final int value)
    {
        return new CascadeStack(this, int.class, valueLong);
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * @param value is the operand to push.
     * @return this.
     */
    public CascadeStack pushLong (final long value)
    {
        return new CascadeStack(this, long.class, valueLong);
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * @param value is the operand to push.
     * @return this.
     */
    public CascadeStack pushFloat (final float value)
    {
        return new CascadeStack(this, float.class, valueLong);
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * @param value is the operand to push.
     * @return this.
     */
    public CascadeStack pushDouble (final double value)
    {
        return new CascadeStack(this, double.class, valueLong);
    }

    /**
     * Pop a single operand off the top of the operand-stack.
     *
     * @return this.
     * @throws IllegalStateException if stackSize() is zero.
     */
    public CascadeStack pop ()
    {
        Preconditions.checkState(!isEmpty(), "Empty Stack");
        return below;
    }

    public CascadeStack pop (final int count)
    {
        Preconditions.checkState(count < size(), "count >= size()");

        CascadeStack p = this;

        for (int i = 0; i < count; i++)
        {
            p = p.below;
        }

        return p;
    }

    public CascadeStack dup ()
    {
        return null;
    }

    public CascadeStack swap ()
    {
        return null;
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
        if (type == boolean.class)
        {
            return asBoolean() ? (char) 1 : (char) 0;
        }
        else if (type == char.class)
        {
            return (char) checkCast(asLong(), Character.MIN_VALUE, Character.MAX_VALUE);
        }
        else if (type == byte.class)
        {
            return (char) checkCast(asLong(), Character.MIN_VALUE, Character.MAX_VALUE);
        }
        else if (type == short.class)
        {
            return (char) checkCast(asLong(), Character.MIN_VALUE, Character.MAX_VALUE);
        }
        else if (type == int.class)
        {
            return (char) checkCast(asLong(), Character.MIN_VALUE, Character.MAX_VALUE);
        }
        else if (type == long.class)
        {
            return (char) checkCast(asLong(), Character.MIN_VALUE, Character.MAX_VALUE);
        }
        else if (type == float.class)
        {
            return (char) checkCast(asLong(), Character.MIN_VALUE, Character.MAX_VALUE);
        }
        else if (type == double.class)
        {
            return (char) checkCast(asLong(), Character.MIN_VALUE, Character.MAX_VALUE);
        }
        else if (valueObject instanceof Number)
        {
            return (char) checkCast(asLong(), Character.MIN_VALUE, Character.MAX_VALUE);
        }
        else
        {
            throw new IllegalStateException("Cannot Convert To Char");
        }
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
        if (type == boolean.class)
        {
            return asBoolean() ? (byte) 1 : (byte) 0;
        }
        else if (type == char.class)
        {
            return (byte) checkCast(asLong(), Byte.MIN_VALUE, Byte.MAX_VALUE);
        }
        else if (type == byte.class)
        {
            return (byte) checkCast(asLong(), Byte.MIN_VALUE, Byte.MAX_VALUE);
        }
        else if (type == short.class)
        {
            return (byte) checkCast(asLong(), Byte.MIN_VALUE, Byte.MAX_VALUE);
        }
        else if (type == int.class)
        {
            return (byte) checkCast(asLong(), Byte.MIN_VALUE, Byte.MAX_VALUE);
        }
        else if (type == long.class)
        {
            return (byte) checkCast(asLong(), Byte.MIN_VALUE, Byte.MAX_VALUE);
        }
        else if (type == float.class)
        {
            return (byte) checkCast(asLong(), Byte.MIN_VALUE, Byte.MAX_VALUE);
        }
        else if (type == double.class)
        {
            return (byte) checkCast(asLong(), Byte.MIN_VALUE, Byte.MAX_VALUE);
        }
        else if (valueObject instanceof Number)
        {
            return ((Number) asObject()).byteValue();
        }
        else
        {
            throw new IllegalStateException("Cannot Convert To Byte");
        }
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
        if (type == boolean.class)
        {
            return asBoolean() ? (byte) 1 : (byte) 0;
        }
        else if (type == char.class)
        {
            return (short) checkCast(asLong(), Short.MIN_VALUE, Short.MAX_VALUE);
        }
        else if (type == byte.class)
        {
            return (short) checkCast(asLong(), Short.MIN_VALUE, Short.MAX_VALUE);
        }
        else if (type == short.class)
        {
            return (short) checkCast(asLong(), Short.MIN_VALUE, Short.MAX_VALUE);
        }
        else if (type == int.class)
        {
            return (short) checkCast(asLong(), Short.MIN_VALUE, Short.MAX_VALUE);
        }
        else if (type == long.class)
        {
            return (short) checkCast(asLong(), Short.MIN_VALUE, Short.MAX_VALUE);
        }
        else if (type == float.class)
        {
            return (short) checkCast(asLong(), Short.MIN_VALUE, Short.MAX_VALUE);
        }
        else if (type == double.class)
        {
            return (short) checkCast(asLong(), Short.MIN_VALUE, Short.MAX_VALUE);
        }
        else if (valueObject instanceof Number)
        {
            return ((Number) asObject()).shortValue();
        }
        else
        {
            throw new IllegalStateException("Cannot Convert To Short");
        }
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
        if (type == boolean.class)
        {
            return asBoolean() ? (byte) 1 : (byte) 0;
        }
        else if (type == char.class)
        {
            return (int) checkCast(asLong(), Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        else if (type == byte.class)
        {
            return (int) checkCast(asLong(), Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        else if (type == short.class)
        {
            return (int) checkCast(asLong(), Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        else if (type == int.class)
        {
            return (int) checkCast(asLong(), Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        else if (type == long.class)
        {
            return (int) checkCast(asLong(), Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        else if (type == float.class)
        {
            return (int) checkCast(asLong(), Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        else if (type == double.class)
        {
            return (int) checkCast(asLong(), Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        else if (valueObject instanceof Number)
        {
            return ((Number) asObject()).intValue();
        }
        else
        {
            throw new IllegalStateException("Cannot Convert To Integer");
        }
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
        if (type == boolean.class)
        {
            return asBoolean() ? (byte) 1 : (byte) 0;
        }
        else if (type == char.class)
        {
            return (long) checkCast(asLong(), Long.MIN_VALUE, Long.MAX_VALUE);
        }
        else if (type == byte.class)
        {
            return (long) checkCast(asLong(), Long.MIN_VALUE, Long.MAX_VALUE);
        }
        else if (type == short.class)
        {
            return (long) checkCast(asLong(), Long.MIN_VALUE, Long.MAX_VALUE);
        }
        else if (type == int.class)
        {
            return (long) checkCast(asLong(), Long.MIN_VALUE, Long.MAX_VALUE);
        }
        else if (type == long.class)
        {
            return valueLong;
        }
        else if (type == float.class)
        {
            return (long) checkCast(asLong(), Long.MIN_VALUE, Long.MAX_VALUE);
        }
        else if (type == double.class)
        {
            return (long) checkCast(asLong(), Long.MIN_VALUE, Long.MAX_VALUE);
        }
        else if (valueObject instanceof Number)
        {
            return ((Number) asObject()).longValue();
        }
        else
        {
            throw new IllegalStateException("Cannot Convert To Long");
        }
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
        if (type == boolean.class)
        {
            return asBoolean() ? (byte) 1 : (byte) 0;
        }
        else if (type == char.class)
        {
            return (float) checkCast(asLong(), Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        else if (type == byte.class)
        {
            return (float) checkCast(asLong(), Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        else if (type == short.class)
        {
            return (float) checkCast(asLong(), Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        else if (type == int.class)
        {
            return (float) checkCast(asLong(), Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        else if (type == long.class)
        {
            return (float) checkCast(asLong(), Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        else if (type == float.class)
        {
            return (float) checkCast(asLong(), Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        else if (type == double.class)
        {
            return (float) checkCast(asLong(), Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        else if (valueObject instanceof Number)
        {
            return ((Number) asObject()).floatValue();
        }
        else
        {
            throw new IllegalStateException("Cannot Convert To FLoat");
        }
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
        return asFloat();
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
        if (valueObject != null)
        {
            return Objects.toString(valueObject);
        }
        else if (type == boolean.class)
        {
            return Byte.toString(asByte());
        }
        else if (type == char.class)
        {
            return new StringBuilder().append(asChar()).toString();
        }
        else if (type == byte.class)
        {
            return Byte.toString(asByte());
        }
        else if (type == short.class)
        {
            return Short.toString(asShort());
        }
        else if (type == int.class)
        {
            return Integer.toString(asInt());
        }
        else if (type == long.class)
        {
            return Long.toString(asLong());
        }
        else if (type == float.class)
        {
            return Float.toString(asFloat());
        }
        else if (type == double.class)
        {
            return Double.toString(asDouble());
        }
        else
        {
            throw new UnsupportedOperationException();
        }
    }

    public Object asObject ()
    {
        if (valueObject != null)
        {
            return valueObject;
        }
        else if (type == boolean.class)
        {
            return asBoolean();
        }
        else if (type == char.class)
        {
            return asChar();
        }
        else if (type == byte.class)
        {
            return asByte();
        }
        else if (type == short.class)
        {
            return asShort();
        }
        else if (type == int.class)
        {
            return asInt();
        }
        else if (type == long.class)
        {
            return asLong();
        }
        else if (type == float.class)
        {
            return asFloat();
        }
        else if (type == double.class)
        {
            return asDouble();
        }
        else
        {
            Verify.verifyNotNull(valueObject);
            return valueObject;
        }
    }

    public <T> T asObject (final Class<T> type)
    {
        Preconditions.checkNotNull(type, "type");
        final Object operand = asObject();
        final T result = type.cast(operand);
        return result;
    }

    @Override
    public int hashCode ()
    {
        return hash;
    }

    @Override
    public boolean equals (final Object other)
    {
        if (other == this)
        {
            return true;
        }
        else if (other == null)
        {
            return false;
        }
        else if (other.getClass() != getClass())
        {
            return false;
        }

        final CascadeStack stack = (CascadeStack) other;

        if (stack.size() != size())
        {
            return false;
        }
        else if (stack.hashCode() != hashCode())
        {
            return false;
        }

        CascadeStack p = this;
        CascadeStack q = stack;

        while (!p.isEmpty()
               && p.hash == q.hash
               && p.valueLong == q.valueLong
               && p.type.equals(q.type)
               && p.valueObject.equals(q.valueObject))
        {
            p = p.pop();
            q = q.pop();
        }

        return p.isEmpty();
    }

    @Override
    public String toString ()
    {
        return asString();
    }

    public CascadeStack negate ()
    {
        return this;
    }

    public CascadeStack divide ()
    {
        return this;
    }

    public CascadeStack remainder ()
    {
        return this;
    }

    public CascadeStack modulo ()
    {
        return this;
    }

    public CascadeStack multiply ()
    {
        return this;
    }

    public CascadeStack add ()
    {
        return this;
    }

    public CascadeStack subtract ()
    {
        return this;
    }

    public CascadeStack test (final Predicate<Object> functor)
    {
        return this;
    }

    public CascadeStack not ()
    {
        return this;
    }

    public CascadeStack and ()
    {
        return this;
    }

    public CascadeStack or ()
    {
        return this;
    }

    public CascadeStack xor ()
    {
        return this;
    }

    public CascadeStack lessThan ()
    {
        return this;
    }

    public CascadeStack lessEquals ()
    {
        return this;
    }

    public CascadeStack equalTo ()
    {
        return this;
    }

    public CascadeStack greaterThan ()
    {
        return this;
    }

    public CascadeStack greaterEquals ()
    {
        return this;
    }

    public CascadeStack convertToBoolean ()
    {
        return this;
    }

    public CascadeStack convertToChar ()
    {
        return this;
    }

    public CascadeStack convertToByte ()
    {
        return this;
    }

    public CascadeStack convertToShort ()
    {
        return this;
    }

    public CascadeStack convertToInt ()
    {
        return this;
    }

    public CascadeStack convertToLong ()
    {
        return this;
    }

    public CascadeStack convertToFloat ()
    {
        return this;
    }

    public CascadeStack convertToDouble ()
    {
        return this;
    }

    public CascadeStack convertToString ()
    {
        return this;
    }

    public CascadeStack println ()
    {
        System.out.println(asString());
        return this;
    }

    public CascadeStack printlns ()
    {
        CascadeStack p = this;

        while (p.isEmpty() == false)
        {
            System.out.println(p.asString());
        }

        return this;
    }

    public Deque<Object> toDeque ()
    {
        final Deque<Object> stack = new ArrayDeque<>(size());
        forEach(x -> stack.addLast(x));
        return stack;
    }

    public CascadeStack forEach (final Consumer<Object> functor)
    {
        CascadeStack p = this;

        while (p.isEmpty() == false)
        {
            functor.accept(p);
        }

        return this;
    }

    private long checkCast (final long value,
                            final long minimum,
                            final long maximum)
    {
        return value;
    }

    private double checkCast (final double value,
                              final double minimum,
                              final double maximum)
    {
        return value;
    }

    public static void main (String[] args)
    {
        CascadeStack stack = CascadeStack.EMPTY;

        System.out.println(stack);
    }
}
