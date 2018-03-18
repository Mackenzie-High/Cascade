package com.mackenziehigh.cascade;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

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

    private final boolean primitive;

    /**
     * Constructor for Empty Stacks.
     */
    private CascadeStack ()
    {
        this.below = null;
        this.size = 0;
        this.type = Object.class;
        this.valueLong = 0;
        this.valueObject = null;
        this.hash = 0;
        this.primitive = false;
    }

    /**
     * Constructor for Primitive Types.
     *
     * @param below is the next node in this pasta-stack.
     * @param type identifies the Java primitive-type.
     * @param value is the (possibly encoded) primitive-value that this node will store.
     */
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
        this.primitive = true;
    }

    /**
     * Constructor for Reference Types.
     *
     * @param below is the next node in this pasta-stack.
     * @param value is the object that this node will store.
     */
    private CascadeStack (final CascadeStack below,
                          final Object value)
    {
        this.below = below;
        this.size = below.size + 1;
        this.valueLong = 0;
        this.valueObject = Objects.requireNonNull(value, "value");
        this.type = value.getClass();
        this.hash = (int) (3181 * value.hashCode()) + (3331 * below.hash);
        this.primitive = false;
    }

    /**
     * Factory Method.
     *
     * @return the empty-stack constant.
     */
    public static CascadeStack newStack ()
    {
        return EMPTY;
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

    /**
     * Push an operand onto the top of the stack.
     *
     * @param value is the operand to push.
     * @return the resultant operand-stack.
     * @throws NullPointerException if the value is null.
     */
    public CascadeStack pushObject (final Object value)
    {
        if (value == null)
        {
            throw new NullPointerException("Refusing to push null onto the stack!");
        }
        else
        {
            return new CascadeStack(this, value);
        }
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * <p>
     * If the given value is an empty stack,
     * then this method is a no-op.
     * </p>
     *
     * @param value is the operand to push.
     * @return the resultant operand-stack.
     */
    public CascadeStack pushTop (final CascadeStack value)
    {
        if (value.isEmpty())
        {
            return this;
        }
        else if (value.primitive)
        {
            return new CascadeStack(this, value.type, value.valueLong);
        }
        else
        {
            return new CascadeStack(this, value.valueObject);
        }
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * @param value is the operand to push.
     * @return the resultant operand-stack.
     */
    public CascadeStack pushBoolean (final boolean value)
    {
        return new CascadeStack(this, boolean.class, !value ? 0 : 1);
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * @param value is the operand to push.
     * @return the resultant operand-stack.
     */
    public CascadeStack pushChar (final char value)
    {
        return new CascadeStack(this, char.class, value);
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * @param value is the operand to push.
     * @return the resultant operand-stack.
     */
    public CascadeStack pushByte (final byte value)
    {
        return new CascadeStack(this, byte.class, value);
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * @param value is the operand to push.
     * @return the resultant operand-stack.
     */
    public CascadeStack pushShort (final short value)
    {
        return new CascadeStack(this, short.class, value);
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * @param value is the operand to push.
     * @return the resultant operand-stack.
     */
    public CascadeStack pushInt (final int value)
    {
        return new CascadeStack(this, int.class, value);
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * @param value is the operand to push.
     * @return the resultant operand-stack.
     */
    public CascadeStack pushLong (final long value)
    {
        return new CascadeStack(this, long.class, value);
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * @param value is the operand to push.
     * @return the resultant operand-stack.
     */
    public CascadeStack pushFloat (final float value)
    {
        final long bytes = Float.floatToRawIntBits(value);
        final float expected = Float.intBitsToFloat((int) bytes);
        Verify.verify(expected == value);
        return new CascadeStack(this, float.class, bytes);
    }

    /**
     * Push an operand onto the top of the stack.
     *
     * @param value is the operand to push.
     * @return the resultant operand-stack.
     */
    public CascadeStack pushDouble (final double value)
    {
        final long bytes = Double.doubleToRawLongBits(value);
        final double expected = Double.longBitsToDouble((long) bytes);
        Verify.verify(expected == value);
        return new CascadeStack(this, double.class, bytes);
    }

    /**
     * Pop a single operand off the top of the operand-stack.
     *
     * @return the resultant operand-stack.
     * @throws IllegalStateException if size() is zero.
     */
    public CascadeStack pop ()
    {
        Preconditions.checkState(!isEmpty(), "Empty Stack");
        return below;
    }

    /**
     * Pop zero-or-more operands off the top of the operand-stack.
     *
     * @param count is the number of operands to pop.
     * @return the resultant operand-stack.
     * @throws IllegalStateException if size() is less than count.
     */
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

    /**
     * Retrieve, but do not remove, the topmost value on the operand-stack,
     * if the value can be converted to a boolean.
     *
     * @return the converted value.
     * @throws IllegalStateException if the operand-stack is empty.
     * @throws ClassCastException if the value cannot be converted.
     */
    public boolean peekAsBoolean ()
    {
        if (type == boolean.class)
        {
            return valueLong != 0;
        }
        else if (type == char.class)
        {
            return peekAsChar() != 0;
        }
        else if (type == byte.class)
        {
            return peekAsByte() != 0;
        }
        else if (type == short.class)
        {
            return peekAsShort() != 0;
        }
        else if (type == int.class)
        {
            return peekAsInt() != 0;
        }
        else if (type == long.class)
        {
            return peekAsLong() != 0;
        }
        else if (type == float.class)
        {
            return ((int) peekAsFloat()) != 0;
        }
        else if (type == double.class)
        {
            return ((int) peekAsDouble()) != 0;
        }
        else if (valueObject instanceof Number)
        {
            return ((Number) valueObject).byteValue() != 0;
        }
        else
        {
            throw new IllegalStateException("Cannot Convert To Boolean");
        }
    }

    /**
     * Retrieve, but do not remove, the topmost value on the operand-stack,
     * if the value can be converted to a char.
     *
     * @return the converted value.
     * @throws IllegalStateException if the operand-stack is empty.
     * @throws ClassCastException if the value cannot be converted.
     */
    public char peekAsChar ()
    {
        if (type == boolean.class)
        {
            return peekAsBoolean() ? (char) 1 : (char) 0;
        }
        else if (type == char.class)
        {
            return (char) checkCast(peekAsLong(), Character.MIN_VALUE, Character.MAX_VALUE);
        }
        else if (type == byte.class)
        {
            return (char) checkCast(peekAsLong(), Character.MIN_VALUE, Character.MAX_VALUE);
        }
        else if (type == short.class)
        {
            return (char) checkCast(peekAsLong(), Character.MIN_VALUE, Character.MAX_VALUE);
        }
        else if (type == int.class)
        {
            return (char) checkCast(peekAsLong(), Character.MIN_VALUE, Character.MAX_VALUE);
        }
        else if (type == long.class)
        {
            return (char) checkCast(peekAsLong(), Character.MIN_VALUE, Character.MAX_VALUE);
        }
        else if (type == float.class)
        {
            return (char) checkCast(peekAsLong(), Character.MIN_VALUE, Character.MAX_VALUE);
        }
        else if (type == double.class)
        {
            return (char) checkCast(peekAsLong(), Character.MIN_VALUE, Character.MAX_VALUE);
        }
        else if (valueObject instanceof Number)
        {
            return (char) checkCast(peekAsLong(), Character.MIN_VALUE, Character.MAX_VALUE);
        }
        else
        {
            throw new IllegalStateException("Cannot Convert To Char");
        }
    }

    /**
     * Retrieve, but do not remove, the topmost value on the operand-stack,
     * if the value can be converted to a byte.
     *
     * @return the converted value.
     * @throws IllegalStateException if the operand-stack is empty.
     * @throws ClassCastException if the value cannot be converted.
     */
    public byte peekAsByte ()
    {
        if (type == boolean.class)
        {
            return peekAsBoolean() ? (byte) 1 : (byte) 0;
        }
        else if (type == char.class)
        {
            return (byte) checkCast(peekAsChar(), Byte.MIN_VALUE, Byte.MAX_VALUE);
        }
        else if (type == byte.class)
        {
            return (byte) valueLong;
        }
        else if (type == short.class)
        {
            return (byte) checkCast(peekAsShort(), Byte.MIN_VALUE, Byte.MAX_VALUE);
        }
        else if (type == int.class)
        {
            return (byte) checkCast(peekAsInt(), Byte.MIN_VALUE, Byte.MAX_VALUE);
        }
        else if (type == long.class)
        {
            return (byte) checkCast(peekAsLong(), Byte.MIN_VALUE, Byte.MAX_VALUE);
        }
        else if (type == float.class)
        {
            return (byte) checkCast(peekAsFloat(), Byte.MIN_VALUE, Byte.MAX_VALUE);
        }
        else if (type == double.class)
        {
            return (byte) checkCast(peekAsDouble(), Byte.MIN_VALUE, Byte.MAX_VALUE);
        }
        else if (valueObject instanceof Number)
        {
            return ((Number) CascadeStack.this.peekAsObject()).byteValue();
        }
        else
        {
            throw new IllegalStateException("Cannot Convert To Byte");
        }
    }

    /**
     * Retrieve, but do not remove, the topmost value on the operand-stack,
     * if the value can be converted to a short.
     *
     * @return the converted value.
     * @throws IllegalStateException if the operand-stack is empty.
     * @throws ClassCastException if the value cannot be converted.
     */
    public short peekAsShort ()
    {
        if (type == boolean.class)
        {
            return peekAsBoolean() ? (byte) 1 : (byte) 0;
        }
        else if (type == char.class)
        {
            return (short) checkCast(peekAsChar(), Short.MIN_VALUE, Short.MAX_VALUE);
        }
        else if (type == byte.class)
        {
            return (short) valueLong;
        }
        else if (type == short.class)
        {
            return (short) valueLong;
        }
        else if (type == int.class)
        {
            return (short) checkCast(peekAsInt(), Short.MIN_VALUE, Short.MAX_VALUE);
        }
        else if (type == long.class)
        {
            return (short) checkCast(peekAsLong(), Short.MIN_VALUE, Short.MAX_VALUE);
        }
        else if (type == float.class)
        {
            return (short) checkCast(peekAsFloat(), Short.MIN_VALUE, Short.MAX_VALUE);
        }
        else if (type == double.class)
        {
            return (short) checkCast(peekAsDouble(), Short.MIN_VALUE, Short.MAX_VALUE);
        }
        else if (valueObject instanceof Number)
        {
            return ((Number) valueObject).shortValue();
        }
        else
        {
            throw new IllegalStateException("Cannot Convert To Short");
        }
    }

    /**
     * Retrieve, but do not remove, the topmost value on the operand-stack,
     * if the value can be converted to an int.
     *
     * @return the converted value.
     * @throws IllegalStateException if the operand-stack is empty.
     * @throws ClassCastException if the value cannot be converted.
     */
    public int peekAsInt ()
    {
        if (type == boolean.class)
        {
            return peekAsBoolean() ? (byte) 1 : (byte) 0;
        }
        else if (type == char.class)
        {
            return (int) valueLong;
        }
        else if (type == byte.class)
        {
            return (int) valueLong;
        }
        else if (type == short.class)
        {
            return (int) valueLong;
        }
        else if (type == int.class)
        {
            return (int) valueLong;
        }
        else if (type == long.class)
        {
            return (int) checkCast(peekAsLong(), Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        else if (type == float.class)
        {
            return (int) checkCast(peekAsFloat(), Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        else if (type == double.class)
        {
            return (int) checkCast(peekAsDouble(), Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        else if (valueObject instanceof Number)
        {
            return ((Number) valueObject).intValue();
        }
        else
        {
            throw new IllegalStateException("Cannot Convert To Integer");
        }
    }

    /**
     * Retrieve, but do not remove, the topmost value on the operand-stack,
     * if the value can be converted to a long.
     *
     * @return the converted value.
     * @throws IllegalStateException if the operand-stack is empty.
     * @throws ClassCastException if the value cannot be converted.
     */
    public long peekAsLong ()
    {
        if (type == boolean.class)
        {
            return peekAsBoolean() ? (byte) 1 : (byte) 0;
        }
        else if (type == char.class)
        {
            return valueLong;
        }
        else if (type == byte.class)
        {
            return valueLong;
        }
        else if (type == short.class)
        {
            return valueLong;
        }
        else if (type == int.class)
        {
            return valueLong;
        }
        else if (type == long.class)
        {
            return valueLong;
        }
        else if (type == float.class)
        {
            return (long) checkCast(peekAsFloat(), Long.MIN_VALUE, Long.MAX_VALUE);
        }
        else if (type == double.class)
        {
            return (long) checkCast(peekAsDouble(), Long.MIN_VALUE, Long.MAX_VALUE);
        }
        else if (valueObject instanceof Number)
        {
            return ((Number) valueObject).longValue();
        }
        else
        {
            throw new IllegalStateException("Cannot Convert To Long");
        }
    }

    /**
     * Retrieve, but do not remove, the topmost value on the operand-stack,
     * if the value can be converted to a float.
     *
     * @return the converted value.
     * @throws IllegalStateException if the operand-stack is empty.
     * @throws ClassCastException if the value cannot be converted.
     */
    public float peekAsFloat ()
    {
        if (type == boolean.class)
        {
            return peekAsBoolean() ? (byte) 1 : (byte) 0;
        }
        else if (type == char.class)
        {
            return (float) peekAsChar();
        }
        else if (type == byte.class)
        {
            return (float) peekAsByte();
        }
        else if (type == short.class)
        {
            return (float) peekAsShort();
        }
        else if (type == int.class)
        {
            return (float) peekAsInt();
        }
        else if (type == long.class)
        {
            return (float) peekAsLong();
        }
        else if (type == float.class)
        {
            return (float) peekAsDouble();
        }
        else if (type == double.class)
        {
            return (float) peekAsDouble();
        }
        else if (valueObject instanceof Number)
        {
            return ((Number) valueObject).floatValue();
        }
        else
        {
            throw new IllegalStateException("Cannot Convert To Float");
        }
    }

    /**
     * Retrieve, but do not remove, the topmost value on the operand-stack,
     * if the value can be converted to a double.
     *
     * @return the converted value.
     * @throws IllegalStateException if the operand-stack is empty.
     * @throws ClassCastException if the value cannot be converted.
     */
    public double peekAsDouble ()
    {
        if (type == boolean.class)
        {
            return peekAsBoolean() ? (byte) 1 : (byte) 0;
        }
        else if (type == char.class)
        {
            return (double) peekAsChar();
        }
        else if (type == byte.class)
        {
            return (double) peekAsByte();
        }
        else if (type == short.class)
        {
            return (double) peekAsShort();
        }
        else if (type == int.class)
        {
            return (double) peekAsInt();
        }
        else if (type == long.class)
        {
            return (double) peekAsLong();
        }
        else if (type == float.class)
        {
            return (double) Double.longBitsToDouble(valueLong);
        }
        else if (type == double.class)
        {
            return (double) Double.longBitsToDouble(valueLong);
        }
        else if (valueObject instanceof Number)
        {
            return ((Number) valueObject).doubleValue();
        }
        else
        {
            throw new IllegalStateException("Cannot Convert To Double");
        }
    }

    /**
     * Retrieve, but do not remove, the topmost value on the operand-stack, as a String.
     *
     * @return the converted value.
     * @throws IllegalStateException if the operand-stack is empty.
     */
    public String peekAsString ()
    {
        if (isEmpty())
        {
            return "";
        }
        else if (valueObject != null)
        {
            return Objects.toString(valueObject);
        }
        else if (type == boolean.class)
        {
            return Boolean.toString(peekAsBoolean());
        }
        else if (type == char.class)
        {
            return new StringBuilder().append(peekAsChar()).toString();
        }
        else if (type == byte.class)
        {
            return Byte.toString(peekAsByte());
        }
        else if (type == short.class)
        {
            return Short.toString(peekAsShort());
        }
        else if (type == int.class)
        {
            return Integer.toString(peekAsInt());
        }
        else if (type == long.class)
        {
            return Long.toString(peekAsLong());
        }
        else if (type == float.class)
        {
            return Float.toString(peekAsFloat());
        }
        else if (type == double.class)
        {
            return Double.toString(peekAsDouble());
        }
        else
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Return the topmost value on the operand-stack, as an Object.
     *
     * @return the value as an object.
     * @throws IllegalStateException if the stack is empty.
     */
    public Object peekAsObject ()
    {
        if (valueObject != null)
        {
            return valueObject;
        }
        else if (type == boolean.class)
        {
            return peekAsBoolean();
        }
        else if (type == char.class)
        {
            return peekAsChar();
        }
        else if (type == byte.class)
        {
            return peekAsByte();
        }
        else if (type == short.class)
        {
            return peekAsShort();
        }
        else if (type == int.class)
        {
            return peekAsInt();
        }
        else if (type == long.class)
        {
            return peekAsLong();
        }
        else if (type == float.class)
        {
            return peekAsFloat();
        }
        else if (type == double.class)
        {
            return peekAsDouble();
        }
        else
        {
            Verify.verify(isEmpty());
            throw new IllegalStateException("Empty Stack");
        }
    }

    /**
     * Return the topmost value on the operand-stack, as an Object of the given type.
     *
     * @param type is the expected type of the value.
     * @return the value as an object.
     * @throws IllegalStateException if the stack is empty.
     * @throws ClassCastException if the value cannot be cast
     */
    public <T> T peekAsObject (final Class<T> type)
    {
        Preconditions.checkNotNull(type, "type");
        final Object operand = CascadeStack.this.peekAsObject();
        final T result = type.cast(operand);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode ()
    {
        return hash;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString ()
    {
        return toDeque().toString();
    }

    /**
     * Create a copy of this operand-stack as a Deque.
     *
     * @return the new equivalent Deque object.
     */
    public Deque<Object> toDeque ()
    {
        final Deque<Object> stack = new ArrayDeque<>(size());
        forEach(x -> stack.addLast(x));
        return stack;
    }

    /**
     * Apply the given function to each operand on the operand-stack,
     * while traversing the stack from top-to-bottom.
     *
     * @param functor is the function to apply.
     * @return this.
     */
    public CascadeStack forEach (final Consumer<Object> functor)
    {
        CascadeStack p = this;

        while (p.isEmpty() == false)
        {
            functor.accept(p);
        }

        return this;
    }

    /**
     * Create an iterator that traverses the operand-stack from top-to-bottom.
     *
     * <p>
     * The remove() operation is not supported, since this stack is immutable.
     * </p>
     *
     * @return the new iterator.
     */
    public Iterator<Object> iterator ()
    {
        final CascadeStack self = this;

        return new Iterator<Object>()
        {
            private volatile CascadeStack ptr = self;

            @Override
            public boolean hasNext ()
            {
                return !ptr.isEmpty();
            }

            @Override
            public Object next ()
            {
                final Object result = ptr.peekAsObject();
                ptr = ptr.pop();
                return result;
            }

            @Override
            public void remove ()
            {
                throw new UnsupportedOperationException("remove() is not supported.");
            }
        };
    }

    private long checkCast (final long value,
                            final long minimum,
                            final long maximum)
    {
        if (value < minimum || value > maximum)
        {
            throw new ClassCastException();
        }
        else
        {
            return value;
        }
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

        stack = stack.pushInt(100).pushInt(200);

        System.out.println(stack.peekAsDouble());
    }
}
