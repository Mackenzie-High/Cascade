package com.mackenziehigh.cascade;

import com.google.common.base.Preconditions;
import java.util.LinkedList;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

/**
 * An instance of this class is a pointer to an operand-stack.
 *
 * <p>
 * Methods herein that create new messages will use
 * the dynamic allocator to allocate them.
 * </p>
 */
public final class CascadePtr
{
    private final CascadeOperand value;

    private final CascadePtr below;

    private final int size;

    public CascadePtr (final CascadeOperand value,
                       final CascadePtr below)
    {
        this.value = value;
        this.below = below;
        this.size = 1 + (below == null ? 0 : below.size());
    }

    public CascadePtr ()
    {
        this.size = 0;
        this.value = null;
        this.below = this;
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public CascadePtr push (final CascadeOperand value)
    {
        Preconditions.checkNotNull(value, "value");
        return new CascadePtr(value, this);
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public CascadePtr pushZ (boolean value)
    {
        return push(CascadeAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public CascadePtr pushC (char value)
    {
        return push(CascadeAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public CascadePtr pushB (byte value)
    {
        return push(CascadeAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public CascadePtr pushS (short value)
    {
        return push(CascadeAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public CascadePtr pushI (int value)
    {
        return push(CascadeAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public CascadePtr pushJ (long value)
    {
        return push(CascadeAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public CascadePtr pushF (float value)
    {
        return push(CascadeAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public CascadePtr pushD (double value)
    {
        return push(CascadeAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public CascadePtr pushStr (String value)
    {
        return push(CascadeAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public CascadePtr pushZA (boolean[] value)
    {
        return push(CascadeAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public CascadePtr pushCA (char[] value)
    {
        return push(CascadeAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public CascadePtr pushBA (byte[] value)
    {
        return push(CascadeAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public CascadePtr pushSA (short[] value)
    {
        return push(CascadeAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public CascadePtr pushIA (int[] value)
    {
        return push(CascadeAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public CascadePtr pushJA (long[] value)
    {
        return push(CascadeAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public CascadePtr pushFA (float[] value)
    {
        return push(CascadeAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public CascadePtr pushDA (double[] value)
    {
        return push(CascadeAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public CascadePtr pushStrA (String[] value)
    {
        return push(CascadeAllocator.instance().from(value));
    }

    public CascadeOperand peek ()
    {
        return peek(0);
    }

    public CascadeOperand peek (int index)
    {
        Preconditions.checkArgument(index < size, "index >= size()");

        CascadePtr p = this;

        for (int i = 0; i < index; i++)
        {
            p = p.below;
        }

        return p.value;
    }

    public CascadePtr pop ()
    {
        return pop(1);
    }

    public CascadePtr pop (final int count)
    {
        Preconditions.checkArgument(count <= size, "count > size()");

        CascadePtr p = this;

        for (int i = 0; i < count; i++)
        {
            p = p.below;
        }

        return p;
    }

    public CascadePtr top (final int count)
    {
        return null;
    }

    public CascadePtr clear ()
    {
        return new CascadePtr();
    }

    public int size ()
    {
        return size;
    }

    public boolean isEmpty ()
    {
        return size() == 0;
    }

    /**
     * Note: LinkedList implements Deque.
     *
     * @return
     */
    public LinkedList<CascadeOperand> toLinkedList ()
    {
        final LinkedList<CascadeOperand> result = new LinkedList<>();

        CascadePtr p = this;

        for (int i = 0; i < size; i++)
        {
            result.add(p.value);
            p = p.below;
        }

        return result;
    }

    public CascadePtr dup ()
    {
        return push(peek());
    }

    public CascadePtr dup (final int count)
    {
        return null;
    }

    public CascadePtr swap ()
    {
        CascadePtr p = this;
        final CascadeOperand x = peek();
        p = p.pop();
        final CascadeOperand y = peek();
        p = p.pop();
        return p.push(y).push(x);
    }

    /**
     * Arithmetic Operation: DIVIDE.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Divide the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type double.
     * </p>
     *
     * <p>
     * The result will be of primitive-type double.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr divD ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final double result = left.asDouble() / right.asDouble();
        return p.pushD(result);
    }

    /**
     * Arithmetic Operation: DIVIDE.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Divide the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type float.
     * </p>
     *
     * <p>
     * The result will be of primitive-type float.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr divF ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final float result = left.asFloat() / right.asFloat();
        return p.pushF(result);
    }

    /**
     * Arithmetic Operation: DIVIDE.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Divide the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type long.
     * </p>
     *
     * <p>
     * The result will be of primitive-type long.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr divJ ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final long result = left.asLong() / right.asLong();
        return p.pushJ(result);
    }

    /**
     * Arithmetic Operation: DIVIDE.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Divide the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type int.
     * </p>
     *
     * <p>
     * The result will be of primitive-type int.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr divI ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final int result = left.asInt() / right.asInt();
        return p.pushI(result);
    }

    /**
     * Arithmetic Operation: DIVIDE.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Divide the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type short.
     * </p>
     *
     * <p>
     * The result will be of primitive-type short.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr divS ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final short result = (short) (left.asShort() / right.asShort());
        return p.pushS(result);
    }

    /**
     * Arithmetic Operation: DIVIDE.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Divide the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type byte.
     * </p>
     *
     * <p>
     * The result will be of primitive-type byte.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr divB ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final byte result = (byte) (left.asByte() / right.asByte());
        return p.pushB(result);
    }

    /**
     * Arithmetic Operation: DIVIDE.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Divide the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type char.
     * </p>
     *
     * <p>
     * The result will be of primitive-type char.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr divC ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final char result = (char) (left.asChar() / right.asChar());
        return p.pushC(result);
    }

    /**
     * Arithmetic Operation: MODULO.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Modulo the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type double.
     * </p>
     *
     * <p>
     * The result will be of primitive-type double.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr modD ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final double result = left.asDouble() % right.asDouble();
        return p.pushD(result);
    }

    /**
     * Arithmetic Operation: MODULO.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Modulo the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type float.
     * </p>
     *
     * <p>
     * The result will be of primitive-type float.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr modF ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final float result = left.asFloat() % right.asFloat();
        return p.pushF(result);
    }

    /**
     * Arithmetic Operation: MODULO.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Modulo the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type long.
     * </p>
     *
     * <p>
     * The result will be of primitive-type long.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr modJ ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final long result = left.asLong() % right.asLong();
        return p.pushJ(result);
    }

    /**
     * Arithmetic Operation: MODULO.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Modulo the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type int.
     * </p>
     *
     * <p>
     * The result will be of primitive-type int.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr modI ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final int result = left.asInt() % right.asInt();
        return p.pushI(result);
    }

    /**
     * Arithmetic Operation: MODULO.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Modulo the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type short.
     * </p>
     *
     * <p>
     * The result will be of primitive-type short.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr modS ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final short result = (short) (left.asShort() % right.asShort());
        return p.pushS(result);
    }

    /**
     * Arithmetic Operation: MODULO.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Modulo the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type byte.
     * </p>
     *
     * <p>
     * The result will be of primitive-type byte.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr modB ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final byte result = (byte) (left.asByte() % right.asByte());
        return p.pushB(result);
    }

    /**
     * Arithmetic Operation: MODULO.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Modulo the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type char.
     * </p>
     *
     * <p>
     * The result will be of primitive-type char.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr modC ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final char result = (char) (left.asChar() % right.asChar());
        return p.pushC(result);
    }

    /**
     * Arithmetic Operation: MULTIPLY.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Multiply the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type double.
     * </p>
     *
     * <p>
     * The result will be of primitive-type double.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr mulD ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final double result = left.asDouble() * right.asDouble();
        return p.pushD(result);
    }

    /**
     * Arithmetic Operation: MULTIPLY.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Multiply the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type float.
     * </p>
     *
     * <p>
     * The result will be of primitive-type float.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr mulF ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final float result = left.asFloat() * right.asFloat();
        return p.pushF(result);
    }

    /**
     * Arithmetic Operation: MULTIPLY.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Multiply the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type long.
     * </p>
     *
     * <p>
     * The result will be of primitive-type long.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr mulJ ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final long result = left.asLong() * right.asLong();
        return p.pushJ(result);
    }

    /**
     * Arithmetic Operation: MULTIPLY.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Multiply the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type int.
     * </p>
     *
     * <p>
     * The result will be of primitive-type int.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr mulI ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final int result = left.asInt() * right.asInt();
        return p.pushI(result);
    }

    /**
     * Arithmetic Operation: MULTIPLY.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Multiply the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type short.
     * </p>
     *
     * <p>
     * The result will be of primitive-type short.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr mulS ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final short result = (short) (left.asShort() * right.asShort());
        return p.pushS(result);
    }

    /**
     * Arithmetic Operation: MULTIPLY.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Multiply the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type byte.
     * </p>
     *
     * <p>
     * The result will be of primitive-type byte.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr mulB ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final byte result = (byte) (left.asByte() * right.asByte());
        return p.pushB(result);
    }

    /**
     * Arithmetic Operation: MULTIPLY.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Multiply the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type char.
     * </p>
     *
     * <p>
     * The result will be of primitive-type char.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr mulC ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final char result = (char) (left.asChar() * right.asChar());
        return p.pushC(result);
    }

    /**
     * Arithmetic Operation: ADD.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Add the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type double.
     * </p>
     *
     * <p>
     * The result will be of primitive-type double.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr addD ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final double result = left.asDouble() + right.asDouble();
        return p.pushD(result);
    }

    /**
     * Arithmetic Operation: ADD.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Add the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type float.
     * </p>
     *
     * <p>
     * The result will be of primitive-type float.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr addF ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final float result = left.asFloat() + right.asFloat();
        return p.pushF(result);
    }

    /**
     * Arithmetic Operation: ADD.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Add the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type long.
     * </p>
     *
     * <p>
     * The result will be of primitive-type long.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr addJ ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final long result = left.asLong() + right.asLong();
        return p.pushJ(result);
    }

    /**
     * Arithmetic Operation: ADD.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Add the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type int.
     * </p>
     *
     * <p>
     * The result will be of primitive-type int.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr addI ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final int result = left.asInt() + right.asInt();
        return p.pushI(result);
    }

    /**
     * Arithmetic Operation: ADD.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Add the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type short.
     * </p>
     *
     * <p>
     * The result will be of primitive-type short.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr addS ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final short result = (short) (left.asShort() + right.asShort());
        return p.pushS(result);
    }

    /**
     * Arithmetic Operation: ADD.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Add the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type byte.
     * </p>
     *
     * <p>
     * The result will be of primitive-type byte.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr addB ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final byte result = (byte) (left.asByte() + right.asByte());
        return p.pushB(result);
    }

    /**
     * Arithmetic Operation: ADD.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Add the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type char.
     * </p>
     *
     * <p>
     * The result will be of primitive-type char.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr addC ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final char result = (char) (left.asChar() + right.asChar());
        return p.pushC(result);
    }

    /**
     * Arithmetic Operation: SUBTRACT.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Subtract the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type double.
     * </p>
     *
     * <p>
     * The result will be of primitive-type double.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr subD ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final double result = left.asDouble() - right.asDouble();
        return p.pushD(result);
    }

    /**
     * Arithmetic Operation: SUBTRACT.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Subtract the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type float.
     * </p>
     *
     * <p>
     * The result will be of primitive-type float.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr subF ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final float result = left.asFloat() - right.asFloat();
        return p.pushF(result);
    }

    /**
     * Arithmetic Operation: SUBTRACT.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Subtract the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type long.
     * </p>
     *
     * <p>
     * The result will be of primitive-type long.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr subJ ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final long result = left.asLong() - right.asLong();
        return p.pushJ(result);
    }

    /**
     * Arithmetic Operation: SUBTRACT.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Subtract the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type int.
     * </p>
     *
     * <p>
     * The result will be of primitive-type int.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr subI ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final int result = left.asInt() - right.asInt();
        return p.pushI(result);
    }

    /**
     * Arithmetic Operation: SUBTRACT.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Subtract the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type short.
     * </p>
     *
     * <p>
     * The result will be of primitive-type short.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr subS ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final short result = (short) (left.asShort() - right.asShort());
        return p.pushS(result);
    }

    /**
     * Arithmetic Operation: SUBTRACT.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Subtract the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type byte.
     * </p>
     *
     * <p>
     * The result will be of primitive-type byte.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr subB ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final byte result = (byte) (left.asByte() - right.asByte());
        return p.pushB(result);
    }

    /**
     * Arithmetic Operation: SUBTRACT.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Subtract the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of primitive-type char.
     * </p>
     *
     * <p>
     * The result will be of primitive-type char.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr subC ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final char result = (char) (left.asChar() - right.asChar());
        return p.pushC(result);
    }

    /**
     * Concatenate Two Strings.
     *
     * <p>
     * Pop the right operand off the stack.
     * Pop the left operand off the stack.
     * Divide the operands.
     * Push the result onto the stack.
     * </p>
     *
     * <p>
     * Both operands must be of type String.
     * </p>
     *
     * <p>
     * The result will be of type String.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr concat ()
    {
        CascadePtr p = this;
        final CascadeOperand right = p.peek();
        p = p.pop();
        final CascadeOperand left = p.peek();
        p = p.pop();
        final String result = left.asString() + right.asString();
        return p.pushStr(result);
    }

    /**
     * Conversion: double TO double.
     *
     * <p>
     * Pop and operand off of the stack.
     * Perform the conversion.
     * Push the result onto the stack.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr convertD2D ()
    {
        return this; // No Operation Required
    }

    /**
     * Conversion: double TO float.
     *
     * <p>
     * Pop and operand off of the stack.
     * Perform the conversion.
     * Push the result onto the stack.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr convertD2F ()
    {
        CascadePtr p = this;
        final CascadeOperand operand = p.peek();
        p = p.pop();
        final float result = (float) operand.asDouble();
        return p.pushF(result);
    }

    /**
     * Conversion: double TO long.
     *
     * <p>
     * Pop and operand off of the stack.
     * Perform the conversion.
     * Push the result onto the stack.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr convertD2J ()
    {
        CascadePtr p = this;
        final CascadeOperand operand = p.peek();
        p = p.pop();
        final long result = (long) operand.asDouble();
        return p.pushJ(result);
    }

    /**
     * Conversion: double TO int.
     *
     * <p>
     * Pop and operand off of the stack.
     * Perform the conversion.
     * Push the result onto the stack.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr convertD2I ()
    {
        CascadePtr p = this;
        final CascadeOperand operand = p.peek();
        p = p.pop();
        final int result = (int) operand.asDouble();
        return p.pushI(result);
    }

    /**
     * Conversion: double TO short.
     *
     * <p>
     * Pop and operand off of the stack.
     * Perform the conversion.
     * Push the result onto the stack.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr convertD2S ()
    {
        CascadePtr p = this;
        final CascadeOperand operand = p.peek();
        p = p.pop();
        final short result = (short) operand.asDouble();
        return p.pushS(result);
    }

    /**
     * Conversion: double TO byte.
     *
     * <p>
     * Pop and operand off of the stack.
     * Perform the conversion.
     * Push the result onto the stack.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr convertD2B ()
    {
        CascadePtr p = this;
        final CascadeOperand operand = p.peek();
        p = p.pop();
        final byte result = (byte) operand.asDouble();
        return p.pushB(result);
    }

    /**
     * Conversion: double TO char.
     *
     * <p>
     * Pop and operand off of the stack.
     * Perform the conversion.
     * Push the result onto the stack.
     * </p>
     *
     * @return a modified copy of this stack.
     */
    public CascadePtr convertD2C ()
    {
        CascadePtr p = this;
        final CascadeOperand operand = p.peek();
        p = p.pop();
        final char result = (char) operand.asDouble();
        return p.pushC(result);
    }

    public CascadePtr match (final Predicate<CascadeOperand> functor)
    {
        return pushZ(matches(functor));
    }

    public CascadePtr matchI (final IntPredicate functor)
    {
        return pushZ(matchesI(functor));
    }

    public boolean matches (final Predicate<CascadeOperand> functor)
    {
        return functor.test(peek());
    }

    public boolean matchesI (final IntPredicate functor)
    {
        return functor.test(peek().asInt());
    }

    public static void main (String[] args)
    {
        CascadePtr s = new CascadePtr();

        s = s.pushI(1).pushI(2).addI().pushI(7).addI().pushI(4).subI();

        System.out.println("R = " + s.peek().asInt());
    }
}
