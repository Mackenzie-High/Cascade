package com.mackenziehigh.cascade;

import com.mackenziehigh.cascade.Message;
import com.google.common.base.Preconditions;
import com.mackenziehigh.cascade.internal.messages.DynamicAllocator;
import java.util.LinkedList;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

/**
 * An immutable stack of messages.
 *
 * <p>
 * Methods herein that create new messages will use
 * the dynamic allocator to allocate them.
 * </p>
 */
public final class MessageStack
{
    private final Message value;

    private final MessageStack below;

    private final int size;

    public MessageStack (final Message value,
                         final MessageStack below)
    {
        this.value = value;
        this.below = below;
        this.size = 1 + (below == null ? 0 : below.size());
    }

    public MessageStack ()
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
    public MessageStack push (final Message value)
    {
        Preconditions.checkNotNull(value, "value");
        return new MessageStack(value, this);
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public MessageStack pushZ (boolean value)
    {
        return push(DynamicAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public MessageStack pushC (char value)
    {
        return push(DynamicAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public MessageStack pushB (byte value)
    {
        return push(DynamicAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public MessageStack pushS (short value)
    {
        return push(DynamicAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public MessageStack pushI (int value)
    {
        return push(DynamicAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public MessageStack pushJ (long value)
    {
        return push(DynamicAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public MessageStack pushF (float value)
    {
        return push(DynamicAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public MessageStack pushD (double value)
    {
        return push(DynamicAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public MessageStack pushStr (String value)
    {
        return push(DynamicAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public MessageStack pushZA (boolean[] value)
    {
        return push(DynamicAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public MessageStack pushCA (char[] value)
    {
        return push(DynamicAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public MessageStack pushBA (byte[] value)
    {
        return push(DynamicAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public MessageStack pushSA (short[] value)
    {
        return push(DynamicAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public MessageStack pushIA (int[] value)
    {
        return push(DynamicAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public MessageStack pushJA (long[] value)
    {
        return push(DynamicAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public MessageStack pushFA (float[] value)
    {
        return push(DynamicAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public MessageStack pushDA (double[] value)
    {
        return push(DynamicAllocator.instance().from(value));
    }

    /**
     * Push a message onto the top of the stack.
     *
     * @param value is the message to push.
     * @return a modified copy of this stack.
     */
    public MessageStack pushStrA (String[] value)
    {
        return push(DynamicAllocator.instance().from(value));
    }

    public Message peek ()
    {
        return peek(0);
    }

    public Message peek (int index)
    {
        Preconditions.checkArgument(index < size, "index >= size()");

        MessageStack p = this;

        for (int i = 0; i < index; i++)
        {
            p = p.below;
        }

        return p.value;
    }

    public MessageStack pop ()
    {
        return pop(1);
    }

    public MessageStack pop (final int count)
    {
        Preconditions.checkArgument(count <= size, "count > size()");

        MessageStack p = this;

        for (int i = 0; i < count; i++)
        {
            p = p.below;
        }

        return p;
    }

    public MessageStack top (final int count)
    {
        return null;
    }

    public MessageStack clear ()
    {
        return new MessageStack();
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
    public LinkedList<Message> toLinkedList ()
    {
        final LinkedList<Message> result = new LinkedList<>();

        MessageStack p = this;

        for (int i = 0; i < size; i++)
        {
            result.add(p.value);
            p = p.below;
        }

        return result;
    }

    public MessageStack dup ()
    {
        return push(peek());
    }

    public MessageStack dup (final int count)
    {
        return null;
    }

    public MessageStack swap ()
    {
        MessageStack p = this;
        final Message x = peek();
        p = p.pop();
        final Message y = peek();
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
    public MessageStack divD ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack divF ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack divJ ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack divI ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack divS ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack divB ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack divC ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack modD ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack modF ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack modJ ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack modI ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack modS ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack modB ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack modC ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack mulD ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack mulF ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack mulJ ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack mulI ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack mulS ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack mulB ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack mulC ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack addD ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack addF ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack addJ ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack addI ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack addS ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack addB ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack addC ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack subD ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack subF ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack subJ ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack subI ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack subS ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack subB ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack subC ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack concat ()
    {
        MessageStack p = this;
        final Message right = p.peek();
        p = p.pop();
        final Message left = p.peek();
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
    public MessageStack convertD2D ()
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
    public MessageStack convertD2F ()
    {
        MessageStack p = this;
        final Message operand = p.peek();
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
    public MessageStack convertD2J ()
    {
        MessageStack p = this;
        final Message operand = p.peek();
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
    public MessageStack convertD2I ()
    {
        MessageStack p = this;
        final Message operand = p.peek();
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
    public MessageStack convertD2S ()
    {
        MessageStack p = this;
        final Message operand = p.peek();
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
    public MessageStack convertD2B ()
    {
        MessageStack p = this;
        final Message operand = p.peek();
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
    public MessageStack convertD2C ()
    {
        MessageStack p = this;
        final Message operand = p.peek();
        p = p.pop();
        final char result = (char) operand.asDouble();
        return p.pushC(result);
    }

    public MessageStack match (final Predicate<Message> functor)
    {
        return pushZ(matches(functor));
    }

    public MessageStack matchI (final IntPredicate functor)
    {
        return pushZ(matchesI(functor));
    }

    public boolean matches (final Predicate<Message> functor)
    {
        return functor.test(peek());
    }

    public boolean matchesI (final IntPredicate functor)
    {
        return functor.test(peek().asInt());
    }

    public static void main (String[] args)
    {
        MessageStack s = new MessageStack();

        s = s.pushI(1).pushI(2).addI().pushI(7).addI().pushI(4).subI();

        System.out.println("R = " + s.peek().asInt());
    }
}
