package com.mackenziehigh.cascade.internal;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.mackenziehigh.cascade.CascadeAllocator.AllocationPool;
import com.mackenziehigh.cascade.CascadeAllocator.AllocatorMismatchException;
import com.mackenziehigh.cascade.CascadeAllocator.ExhaustedAllocationPoolException;
import com.mackenziehigh.cascade.CascadeAllocator.OperandArray;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.internal.ConcreteAllocator.FixedAllocationPool;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import static junit.framework.Assert.*;
import org.junit.Test;

/**
 * Unit Test.
 */
public final class ConcreteAllocatorTest
{
    private interface ConsumerEx<T>
            extends Consumer<T>
    {
        // Pass
    }

    private void testThrow (final Class<? extends Throwable> expected,
                            final ConsumerEx<OperandStack> action)
    {
        final ConcreteAllocator allocator = new ConcreteAllocator();
        allocator.addFixedPool("default", 0, 128, 8);
        final OperandStack stk = allocator.newOperandStack();

        try
        {
            action.accept(stk);
            fail();
        }
        catch (Throwable ex)
        {
            assertEquals(expected, ex.getClass());
        }
    }

    /**
     * Test: 20171105195618895437
     *
     * <p>
     * Case: All of the methods in the OperandStack implementation,
     * except those implicitly inherited from Object, must be either
     * (abstract), (default) or (final + synchronized).
     * Since the implementation is obviously non-abstract itself,
     * each (abstract) method has a corresponding method that must
     * be (final + synchronized). Thus, this test is really ensuring
     * that every non-implicit method implementation is either
     * (final + synchronized) or (default).
     * </p>
     *
     * <p>
     * Each method is required to be (synchronized) or (default)
     * in order to easily ensure that the operand-stack
     * implementation is thread-safe.
     * </p>
     *
     * <p>
     * This test is making the assumption that the (default) methods
     * are thread-safe; otherwise, they should not be (default)!!!
     * </p>
     */
    @Test
    public void test20171105195618895437 ()
    {
        System.out.println("Test: 20171105195618895437");
        final ConcreteAllocator object = new ConcreteAllocator();
        final OperandStack stack = object.newOperandStack();
        final List<Method> methods = Arrays.asList(stack.getClass().getMethods())
                .stream()
                .filter(x -> !x.getDeclaringClass().equals(Object.class))
                .filter(x -> !Modifier.isAbstract(x.getModifiers()))
                .filter(x -> !x.isDefault())
                .filter(x -> !(Modifier.isSynchronized(x.getModifiers()) && Modifier.isFinal(x.getModifiers())))
                .collect(Collectors.toList());

        if (methods.isEmpty() == false)
        {
            fail("Wrong Modifiers: " + methods.get(0));
        }
    }

    /**
     * Test: 20171121040558663391
     *
     * <p>
     * Method: <code>push(boolean)</code> and <code>asBoolean()</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121040558663391 ()
    {
        System.out.println("Test: 20171121040558663391");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final FixedAllocationPool pool = (FixedAllocationPool) allocator.addFixedPool("default", 0, 128, 16);
        final OperandStack stk = allocator.newOperandStack();

        for (byte x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (byte) x).collect(Collectors.toList()))
        {
            assertEquals(0, pool.size().getAsLong()); // No operands are currently in the allocation-pool.
            stk.push(x != 0); // Method Under Test
            assertEquals(1, pool.size().getAsLong()); // (1) operand currently in the allocation-pool.
            assertTrue((x != 0) == stk.asBoolean()); // Verify the value that was pushed onto the stack.
            assertEquals(1, stk.asByteArray().length);
            assertEquals(1, stk.operandSize());
            assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
            stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
            assertEquals(0, pool.size().getAsLong()); // Verify that the operand was freed.
        }
    }

    /**
     * Test: 20171121040558663473
     *
     * <p>
     * Method: <code>push(byte)</code> and <code>asByte()</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121040558663473 ()
    {
        System.out.println("Test: 20171121040558663473");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final FixedAllocationPool pool = (FixedAllocationPool) allocator.addFixedPool("default", 0, 128, 16);
        final OperandStack stk = allocator.newOperandStack();

        for (byte x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (byte) x).collect(Collectors.toList()))
        {
            assertEquals(0, pool.size().getAsLong()); // No operands are currently in the allocation-pool.
            stk.push(x); // Method Under Test
            assertEquals(1, pool.size().getAsLong()); // (1) operand currently in the allocation-pool.
            assertTrue(x == stk.asByte()); // Verify the value that was pushed onto the stack.
            assertEquals(1, stk.asByteArray().length);
            assertEquals(1, stk.operandSize());
            assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
            stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
            assertEquals(0, pool.size().getAsLong()); // Verify that the operand was freed.
        }
    }

    /**
     * Test: 20171121040558663502
     *
     * <p>
     * Method: <code>push(short)</code> and <code>asShort()</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121040558663502 ()
    {
        System.out.println("Test: 20171121040558663502");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final FixedAllocationPool pool = (FixedAllocationPool) allocator.addFixedPool("default", 0, 128, 16);
        final OperandStack stk = allocator.newOperandStack();

        for (short x : IntStream.range(Short.MIN_VALUE, Short.MAX_VALUE).mapToObj(x -> (short) x).collect(Collectors.toList()))
        {
            assertEquals(0, pool.size().getAsLong()); // No operands are currently in the allocation-pool.
            stk.push(x); // Method Under Test
            assertEquals(1, pool.size().getAsLong()); // (1) operand currently in the allocation-pool.
            assertTrue(x == stk.asShort()); // Verify the value that was pushed onto the stack.
            assertEquals(2, stk.asByteArray().length);
            assertEquals(2, stk.operandSize());
            assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
            stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
            assertEquals(0, pool.size().getAsLong()); // Verify that the operand was freed.
        }
    }

    /**
     * Test: 20171121040558663528
     *
     * <p>
     * Method: <code>push(int)</code> and <code>asInt()</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121040558663528 ()
    {
        System.out.println("Test: 20171121040558663528");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final FixedAllocationPool pool = (FixedAllocationPool) allocator.addFixedPool("default", 0, 128, 16);
        final OperandStack stk = allocator.newOperandStack();

        for (int x : IntStream.range(Integer.MAX_VALUE - 1000, Integer.MIN_VALUE + 1000).mapToObj(x -> (short) x).collect(Collectors.toList()))
        {
            assertEquals(0, pool.size().getAsLong()); // No operands are currently in the allocation-pool.
            stk.push(x); // Method Under Test
            assertEquals(1, pool.size().getAsLong()); // (1) operand currently in the allocation-pool.
            assertTrue(x == stk.asInt()); // Verify the value that was pushed onto the stack.
            assertEquals(4, stk.asByteArray().length);
            assertEquals(4, stk.operandSize());
            assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
            stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
            assertEquals(0, pool.size().getAsLong()); // Verify that the operand was freed.
        }
    }

    /**
     * Test: 20171121040558663554
     *
     * <p>
     * Method: <code>push(long)</code> and <code>asLong()</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121040558663554 ()
    {
        System.out.println("Test: 20171121040558663554");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final FixedAllocationPool pool = (FixedAllocationPool) allocator.addFixedPool("default", 0, 128, 16);
        final OperandStack stk = allocator.newOperandStack();

        for (long x : LongStream.range(Long.MAX_VALUE - 1000, Long.MIN_VALUE + 1000).mapToObj(x -> (short) x).collect(Collectors.toList()))
        {
            assertEquals(0, pool.size().getAsLong()); // No operands are currently in the allocation-pool.
            stk.push(x); // Method Under Test
            assertEquals(1, pool.size().getAsLong()); // (1) operand currently in the allocation-pool.
            assertTrue(x == stk.asLong()); // Verify the value that was pushed onto the stack.
            assertEquals(8, stk.asByteArray().length);
            assertEquals(8, stk.operandSize());
            assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
            stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
            assertEquals(0, pool.size().getAsLong()); // Verify that the operand was freed.
        }
    }

    /**
     * Test: 20171121040558663575
     *
     * <p>
     * Method: <code>push(float)</code> and <code>asFloat()</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121040558663575 ()
    {
        System.out.println("Test: 20171121040558663575");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final FixedAllocationPool pool = (FixedAllocationPool) allocator.addFixedPool("default", 0, 128, 16);
        final OperandStack stk = allocator.newOperandStack();

        /**
         * Note: NaN == NaN, will always return false, per IEEE-754 specification.
         */
        for (float x : ImmutableList.of(Float.MAX_VALUE, Float.MIN_VALUE, -3.0F, -2.0F, -1.0F, 0.0F, 1.0F, 2.0F, 3.0F, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NaN))
        {
            assertEquals(0, pool.size().getAsLong()); // No operands are currently in the allocation-pool.
            stk.push(x); // Method Under Test
            assertEquals(1, pool.size().getAsLong()); // (1) operand currently in the allocation-pool.
            assertTrue(Float.floatToIntBits(x) == Float.floatToIntBits(stk.asFloat())); // Verify the value that was pushed onto the stack.
            assertEquals(x, (Float) stk.asFloat());
            assertEquals(4, stk.asByteArray().length);
            assertEquals(4, stk.operandSize());
            assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
            stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
            assertEquals(0, pool.size().getAsLong()); // Verify that the operand was freed.
        }
    }

    /**
     * Test: 20171121040558663596
     *
     * <p>
     * Method: <code>push(double)</code> and <code>asDouble()</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121040558663596 ()
    {
        System.out.println("Test: 20171121040558663596");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final FixedAllocationPool pool = (FixedAllocationPool) allocator.addFixedPool("default", 0, 128, 16);
        final OperandStack stk = allocator.newOperandStack();

        /**
         * Note: NaN == NaN, will always return false, per IEEE-754 specification.
         */
        for (double x : ImmutableList.of(Double.MAX_VALUE, Double.MIN_VALUE, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NaN))
        {
            assertEquals(0, pool.size().getAsLong()); // No operands are currently in the allocation-pool.
            stk.push(x); // Method Under Test
            assertEquals(1, pool.size().getAsLong()); // (1) operand currently in the allocation-pool.
            assertTrue(Double.doubleToLongBits(x) == Double.doubleToLongBits(stk.asDouble())); // Verify the value that was pushed onto the stack.
            assertEquals(x, (Double) stk.asDouble());
            assertEquals(8, stk.asByteArray().length);
            assertEquals(8, stk.operandSize());
            assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
            stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
            assertEquals(0, pool.size().getAsLong()); // Verify that the operand was freed.
        }
    }

    /**
     * Test: 20171121040558663618
     *
     * <p>
     * Method: <code>push(OperandStack)</code> and <code>asByteArray()</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20171121040558663618 ()
    {
        System.out.println("Test: 20171121040558663618");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final FixedAllocationPool pool = (FixedAllocationPool) allocator.addFixedPool("default", 0, 128, 16);
        final OperandStack stack1 = allocator.newOperandStack();
        final OperandStack stack2 = allocator.newOperandStack();
        final byte[] data1 = "Emma".getBytes();
        final byte[] data2 = "Erin".getBytes();

        stack1.push(data1);
        stack1.push(data2);
        stack2.push("Autumn");
        stack2.push(stack1); // Method Under Test

        assertEquals(2, stack1.stackSize());
        assertEquals(2, stack2.stackSize());
        assertEquals(4, pool.size().getAsLong());

        assertTrue(Arrays.equals(data2, stack2.asByteArray())); // Method Under Test
        assertTrue(Arrays.equals(data2, stack1.asByteArray())); // Method Under Test
        stack1.pop();
        assertTrue(Arrays.equals(data1, stack1.asByteArray())); // Method Under Test

        stack1.clear();
        stack2.clear();

        assertEquals(0, pool.size().getAsLong());
    }

    /**
     * Test: 20171121040558663639
     *
     * <p>
     * Method: <code>push(String)</code> and <code>asString()</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121040558663639 ()
    {
        System.out.println("Test: 20171121040558663639");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final FixedAllocationPool pool = (FixedAllocationPool) allocator.addFixedPool("default", 0, 128, 16);
        final OperandStack stk = allocator.newOperandStack();

        assertEquals(0, pool.size().getAsLong()); // No operands are currently in the allocation-pool.
        stk.push("Vulcan"); // Method Under Test
        assertEquals(1, pool.size().getAsLong()); // (1) operand currently in the allocation-pool.
        assertEquals("Vulcan", stk.asString()); // Verify the value that was pushed onto the stack.
        assertEquals(6, stk.asByteArray().length);
        assertEquals(6, stk.operandSize());
        assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
        stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
        assertEquals(0, pool.size().getAsLong()); // Verify that the operand was freed.
    }

    /**
     * Test: 20171121040558663657
     *
     * <p>
     * Method: <code>stackSize()</code> and <code>isStackEmpty()</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121040558663657 ()
    {
        System.out.println("Test: 20171121040558663657");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final FixedAllocationPool pool = (FixedAllocationPool) allocator.addFixedPool("default", 0, 128, 16);
        final OperandStack stk = allocator.newOperandStack();

        for (int k = 0; k <= 16; k++)
        {
            // Push
            for (int i = 0; i < k; i++)
            {
                assertEquals(i, stk.stackSize());
                assertEquals(stk.stackSize() == 0, stk.isStackEmpty());
                assertEquals(pool.size().getAsLong(), stk.stackSize());
                stk.push(1000 * i);
            }

            // Pop
            for (int i = k; i > 0; i--)
            {
                assertEquals(i, stk.stackSize());
                assertEquals(stk.stackSize() == 0, stk.isStackEmpty());
                assertEquals(pool.size().getAsLong(), stk.stackSize());
                stk.pop();
            }

            assertEquals(0, stk.stackSize());
            assertEquals(0, pool.size().getAsLong());
            assertTrue(stk.isStackEmpty());
        }
    }

    /**
     * Test: 20171121041533714861
     *
     * <p>
     * Method: <code>operandSize()</code> and <code>operandCapacity()</code> and <code>isOperandEmpty()</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121041533714861 ()
    {
        System.out.println("Test: 20171121041533714861");

        final int capacity = 128;
        final ConcreteAllocator allocator = new ConcreteAllocator();
        allocator.addFixedPool("default", 0, capacity, 16);
        final OperandStack stk = allocator.newOperandStack();

        stk.push(true);
        assertEquals(1, stk.operandSize());
        assertEquals(capacity, stk.operandCapacity());
        assertFalse(stk.isOperandEmpty());

        stk.push((byte) 0);
        assertEquals(1, stk.operandSize());
        assertEquals(capacity, stk.operandCapacity());
        assertFalse(stk.isOperandEmpty());

        stk.push((short) 0);
        assertEquals(2, stk.operandSize());
        assertEquals(capacity, stk.operandCapacity());
        assertFalse(stk.isOperandEmpty());

        stk.push((int) 0);
        assertEquals(4, stk.operandSize());
        assertEquals(capacity, stk.operandCapacity());
        assertFalse(stk.isOperandEmpty());

        stk.push((long) 0);
        assertEquals(8, stk.operandSize());
        assertEquals(capacity, stk.operandCapacity());
        assertFalse(stk.isOperandEmpty());

        stk.push((float) 0);
        assertEquals(4, stk.operandSize());
        assertEquals(capacity, stk.operandCapacity());
        assertFalse(stk.isOperandEmpty());

        stk.push((double) 0);
        assertEquals(8, stk.operandSize());
        assertEquals(capacity, stk.operandCapacity());
        assertFalse(stk.isOperandEmpty());

        stk.push(new byte[0]);
        assertEquals(0, stk.operandSize());
        assertEquals(capacity, stk.operandCapacity());
        assertTrue(stk.isOperandEmpty());

        stk.push(new byte[57]);
        assertEquals(57, stk.operandSize());
        assertEquals(capacity, stk.operandCapacity());
        assertFalse(stk.isOperandEmpty());
    }

    /**
     * Test: 20171121041637421363
     *
     * <p>
     * Method: <code>pop()</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121041637421363 ()
    {
        System.out.println("Test: 20171121041637421363");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool pool = allocator.addFixedPool("default", 0, 128, 16);
        final OperandStack stack1 = allocator.newOperandStack();
        final OperandStack stack2 = allocator.newOperandStack();

        stack1.push("A").push("B");
        stack2.set(stack1);
        stack1.push("P").push("Q");
        stack2.push("X").push("Y");

        assertEquals(4, stack1.stackSize());
        assertEquals(4, stack2.stackSize());
        assertEquals(6, pool.size().getAsLong());

        /**
         * Verify Stack #1.
         */
        assertEquals("Q", stack1.asString());
        stack1.pop();
        assertEquals(5, pool.size().getAsLong());
        assertEquals(3, stack1.stackSize());

        assertEquals("P", stack1.asString());
        stack1.pop();
        assertEquals(4, pool.size().getAsLong());
        assertEquals(2, stack1.stackSize());

        assertEquals("B", stack1.asString());
        stack1.pop();
        assertEquals(4, pool.size().getAsLong());
        assertEquals(1, stack1.stackSize());

        assertEquals("A", stack1.asString());
        stack1.pop();
        assertEquals(4, pool.size().getAsLong());
        assertEquals(0, stack1.stackSize());

        /**
         * Verify Stack #2.
         */
        assertEquals("Y", stack2.asString());
        stack2.pop();
        assertEquals(3, pool.size().getAsLong());
        assertEquals(3, stack2.stackSize());

        assertEquals("X", stack2.asString());
        stack2.pop();
        assertEquals(2, pool.size().getAsLong());
        assertEquals(2, stack2.stackSize());

        assertEquals("B", stack2.asString());
        stack2.pop();
        assertEquals(1, pool.size().getAsLong());
        assertEquals(1, stack2.stackSize());

        assertEquals("A", stack2.asString());
        stack2.pop();
        assertEquals(0, pool.size().getAsLong());
        assertEquals(0, stack2.stackSize());
    }

    /**
     * Test: 20171121041637421449
     *
     * <p>
     * Method: <code>set(OperandStack)</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121041637421449 ()
    {
        System.out.println("Test: 20171121041637421449");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool pool = allocator.addFixedPool("default", 0, 128, 16);

        final OperandStack empty = allocator.newOperandStack();
        final OperandStack women = allocator.newOperandStack().push("Autumn").push("Emma").push("Erin");
        final OperandStack stack = allocator.newOperandStack();

        /**
         * Case: Empty, Null
         */
        assertTrue(stack.isStackEmpty());
        stack.set(null);
        assertTrue(stack.isStackEmpty());
        assertEquals(3, pool.size().getAsLong());

        /**
         * Case: Empty, Empty
         */
        assertTrue(stack.isStackEmpty());
        stack.set(empty);
        assertTrue(stack.isStackEmpty());
        assertEquals(3, pool.size().getAsLong());

        /**
         * Case: Empty, Not Empty
         */
        assertTrue(stack.isStackEmpty());
        stack.set(women);
        assertEquals(3, stack.stackSize());
        stack.clear();
        assertTrue(stack.isStackEmpty());
        assertEquals(3, pool.size().getAsLong());

        /**
         * Case: Not Empty, Null
         */
        assertTrue(stack.isStackEmpty());
        stack.set(women);
        assertEquals(3, stack.stackSize());
        stack.set(null);
        assertTrue(stack.isStackEmpty());
        assertEquals(3, pool.size().getAsLong());

        /**
         * Case: Not Empty, Empty
         */
        assertTrue(stack.isStackEmpty());
        stack.set(women);
        assertEquals(3, stack.stackSize());
        stack.set(empty);
        assertTrue(stack.isStackEmpty());
        assertEquals(3, pool.size().getAsLong());

        /**
         * Case: Not Empty, Not Empty
         */
        assertTrue(stack.isStackEmpty());
        stack.set(women);
        assertEquals(3, stack.stackSize());
        stack.push("Rachel");
        stack.push("Jenna");
        assertEquals(5, stack.stackSize());
        assertEquals(5, pool.size().getAsLong());
        stack.set(women);
        assertEquals(3, stack.stackSize());
        assertEquals(3, pool.size().getAsLong());
        stack.clear();
        assertTrue(stack.isStackEmpty());
    }

    /**
     * Test: 20171121041637421477
     *
     *
     * <p>
     * Method: <code>set(int, OperandArray)</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121041637421477 ()
    {
        System.out.println("Test: 20171121041637421477");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool pool = allocator.addFixedPool("default", 0, 128, 16);

        final OperandStack emptyStk = allocator.newOperandStack();
        final OperandStack womenStk = allocator.newOperandStack().push("Autumn").push("Emma").push("Erin");
        final OperandArray womenArray = allocator.newOperandArray(1);
        final OperandArray emptyArray = allocator.newOperandArray(1);
        final OperandArray nullArray = allocator.newOperandArray(1);
        womenArray.set(0, womenStk);
        emptyArray.set(0, emptyStk);
        nullArray.set(0, null);
        final OperandStack stack = allocator.newOperandStack();

        /**
         * Case: Empty, Null
         */
        assertTrue(stack.isStackEmpty());
        stack.set(nullArray, 0);
        assertTrue(stack.isStackEmpty());
        assertEquals(3, pool.size().getAsLong());

        /**
         * Case: Empty, Empty
         */
        assertTrue(stack.isStackEmpty());
        stack.set(emptyArray, 0);
        assertTrue(stack.isStackEmpty());
        assertEquals(3, pool.size().getAsLong());

        /**
         * Case: Empty, Not Empty
         */
        assertTrue(stack.isStackEmpty());
        stack.set(womenArray, 0);
        assertEquals(3, stack.stackSize());
        stack.clear();
        assertTrue(stack.isStackEmpty());
        assertEquals(3, pool.size().getAsLong());

        /**
         * Case: Not Empty, Null
         */
        assertTrue(stack.isStackEmpty());
        stack.set(womenStk);
        assertEquals(3, stack.stackSize());
        stack.set(nullArray, 0);
        assertTrue(stack.isStackEmpty());
        assertEquals(3, pool.size().getAsLong());

        /**
         * Case: Not Empty, Empty
         */
        assertTrue(stack.isStackEmpty());
        stack.set(womenStk);
        assertEquals(3, stack.stackSize());
        stack.set(emptyArray, 0);
        assertTrue(stack.isStackEmpty());
        assertEquals(3, pool.size().getAsLong());

        /**
         * Case: Not Empty, Not Empty
         */
        assertTrue(stack.isStackEmpty());
        stack.set(womenStk);
        assertEquals(3, stack.stackSize());
        stack.push("Rachel");
        stack.push("Jenna");
        assertEquals(5, stack.stackSize());
        assertEquals(5, pool.size().getAsLong());
        stack.set(womenArray, 0);
        assertEquals(3, stack.stackSize());
        assertEquals(3, pool.size().getAsLong());
        stack.clear();
        assertTrue(stack.isStackEmpty());
    }

    /**
     * Test: 20171121041637421503
     *
     * <p>
     * Method: <code>byteAt(int)</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121041637421503 ()
    {
        System.out.println("Test: 20171121041637421503");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        allocator.addFixedPool("default", 0, 128, 16);
        final OperandStack stk = allocator.newOperandStack();

        stk.push("Vulcan");
        assertEquals((byte) 'V', stk.byteAt(0));
        assertEquals((byte) 'u', stk.byteAt(1));
        assertEquals((byte) 'l', stk.byteAt(2));
        assertEquals((byte) 'c', stk.byteAt(3));
        assertEquals((byte) 'a', stk.byteAt(4));
        assertEquals((byte) 'n', stk.byteAt(5));
    }

    /**
     * Test: 20171121041637421529
     *
     * <p>
     * Method: <code>copyTo(byte[])</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121041637421529 ()
    {
        System.out.println("Test: 20171121041637421529");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        allocator.addFixedPool("default", 0, 128, 16);
        final OperandStack stk = allocator.newOperandStack();

        stk.push("Mars");

        final byte[] out1 = new byte[4]; // Exact Size
        stk.copyTo(out1);
        assertEquals((byte) 'M', out1[0]);
        assertEquals((byte) 'a', out1[1]);
        assertEquals((byte) 'r', out1[2]);
        assertEquals((byte) 's', out1[3]);

        final byte[] out2 = new byte[6]; // Has Padding
        stk.copyTo(out2);
        assertEquals((byte) 'M', out2[0]);
        assertEquals((byte) 'a', out2[1]);
        assertEquals((byte) 'r', out2[2]);
        assertEquals((byte) 's', out2[3]);
        assertEquals((byte) 0, out2[4]);
        assertEquals((byte) 0, out2[5]);
    }

    /**
     * Test: 20171121042245269924
     *
     * <p>
     * Method: <code>copyTo(byte[], int)</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121042245269924 ()
    {
        System.out.println("Test: 20171121042245269924");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        allocator.addFixedPool("default", 0, 128, 16);
        final OperandStack stk = allocator.newOperandStack();

        stk.push("Mars");

        final byte[] out1 = new byte[4]; // Exact Size
        stk.copyTo(out1, 0);
        assertEquals((byte) 'M', out1[0]);
        assertEquals((byte) 'a', out1[1]);
        assertEquals((byte) 'r', out1[2]);
        assertEquals((byte) 's', out1[3]);

        final byte[] out2 = new byte[5]; // Frontal Padding
        stk.copyTo(out2, 1);
        assertEquals((byte) 0, out2[0]);
        assertEquals((byte) 'M', out2[1]);
        assertEquals((byte) 'a', out2[2]);
        assertEquals((byte) 'r', out2[3]);
        assertEquals((byte) 's', out2[4]);

        final byte[] out3 = new byte[5]; // Rear Padding
        stk.copyTo(out3, 0);
        assertEquals((byte) 'M', out3[0]);
        assertEquals((byte) 'a', out3[1]);
        assertEquals((byte) 'r', out3[2]);
        assertEquals((byte) 's', out3[3]);
        assertEquals((byte) 0, out3[4]);

        final byte[] out4 = new byte[6]; // Dual Padding
        stk.copyTo(out4, 1);
        assertEquals((byte) 0, out4[0]);
        assertEquals((byte) 'M', out4[1]);
        assertEquals((byte) 'a', out4[2]);
        assertEquals((byte) 'r', out4[3]);
        assertEquals((byte) 's', out4[4]);
        assertEquals((byte) 0, out4[5]);
    }

    /**
     * Test: 20171121042309255947
     *
     * <p>
     * Method: <code>copyTo(byte[], int, int)</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121042309255947 ()
    {
        System.out.println("Test: 20171121042309255947");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        allocator.addFixedPool("default", 0, 128, 16);
        final OperandStack stk = allocator.newOperandStack();

        stk.push("Mars");

        final byte[] out1 = new byte[2]; // Exact Size
        stk.copyTo(out1, 0, 2);
        assertEquals((byte) 'M', out1[0]);
        assertEquals((byte) 'a', out1[1]);

        final byte[] out2 = new byte[4]; // Frontal Padding
        stk.copyTo(out2, 1, 3);
        assertEquals((byte) 0, out2[0]);
        assertEquals((byte) 'M', out2[1]);
        assertEquals((byte) 'a', out2[2]);
        assertEquals((byte) 'r', out2[3]);

        final byte[] out3 = new byte[4]; // Rear Padding
        stk.copyTo(out3, 0, 3);
        assertEquals((byte) 'M', out3[0]);
        assertEquals((byte) 'a', out3[1]);
        assertEquals((byte) 'r', out3[2]);
        assertEquals((byte) 0, out3[3]);

        final byte[] out4 = new byte[5]; // Dual Padding
        stk.copyTo(out4, 1, 3);
        assertEquals((byte) 0, out4[0]);
        assertEquals((byte) 'M', out4[1]);
        assertEquals((byte) 'a', out4[2]);
        assertEquals((byte) 'r', out4[3]);
        assertEquals((byte) 0, out4[4]);
    }

    /**
     * Test: 20171121042309256024
     *
     * <p>
     * Method: <code>clear()</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121042309256024 ()
    {
        System.out.println("Test: 20171121042309256024");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final FixedAllocationPool pool = (FixedAllocationPool) allocator.addFixedPool("default", 0, 128, 16);
        final OperandStack stk = allocator.newOperandStack();

        for (int k = 0; k <= 16; k++)
        {
            assertTrue(stk.isStackEmpty());

            for (int i = 0; i < k; i++)
            {
                stk.push(1000 * i);
            }

            assertEquals(k, stk.stackSize());
            assertEquals(k, pool.size().getAsLong());
            stk.clear(); // Method Under Test
            assertTrue(stk.isStackEmpty());
            assertEquals(0, pool.size().getAsLong());
        }
    }

    /**
     * Test: 20171121042309256052
     *
     * <p>
     * Method: <code>copy()</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121042309256052 ()
    {
        System.out.println("Test: 20171121042309256052");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final FixedAllocationPool pool = (FixedAllocationPool) allocator.addFixedPool("default", 0, 128, 16);
        final OperandStack stack1 = allocator.newOperandStack();

        stack1.push("Emma");
        stack1.push("Erin");

        assertEquals(2, stack1.stackSize());
        assertEquals(2, pool.size().getAsLong());

        final OperandStack stack2 = stack1.copy(); // Method Under Test

        assertEquals(2, stack1.stackSize());
        assertEquals(2, stack2.stackSize());
        assertEquals(2, pool.size().getAsLong());

        /**
         * Verify Stack #1.
         */
        assertEquals("Erin", stack1.asString());
        assertEquals(2, stack1.stackSize());
        assertEquals(2, pool.size().getAsLong());
        stack1.pop();

        assertEquals("Emma", stack1.asString());
        assertEquals(1, stack1.stackSize());
        assertEquals(2, pool.size().getAsLong());
        stack1.pop();

        assertTrue(stack1.isStackEmpty());
        assertEquals(0, stack1.stackSize());
        assertEquals(2, stack2.stackSize());
        assertEquals(2, pool.size().getAsLong());

        /**
         * Verify Stack #2.
         */
        assertEquals("Erin", stack2.asString());
        assertEquals(2, stack2.stackSize());
        assertEquals(2, pool.size().getAsLong());
        stack2.pop();

        assertEquals("Emma", stack2.asString());
        assertEquals(1, stack2.stackSize());
        assertEquals(1, pool.size().getAsLong());
        stack2.pop();

        assertTrue(stack2.isStackEmpty());
        assertEquals(0, stack2.stackSize());
        assertEquals(0, pool.size().getAsLong());
    }

    /**
     * Test: 20171121042607008412
     *
     * <p>
     * Method: <code>close()</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121042607008412 ()
    {
        System.out.println("Test: 20171121042607008412");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final FixedAllocationPool pool = (FixedAllocationPool) allocator.addFixedPool("default", 0, 128, 16);
        final OperandStack stk = allocator.newOperandStack();

        for (int k = 0; k <= 16; k++)
        {
            assertTrue(stk.isStackEmpty());

            for (int i = 0; i < k; i++)
            {
                stk.push(1000 * i);
            }

            assertEquals(k, stk.stackSize());
            assertEquals(k, pool.size().getAsLong());
            stk.close();// Method Under Test
            assertTrue(stk.isStackEmpty());
            assertEquals(0, pool.size().getAsLong());
        }
    }

    /**
     * Test: 20171121042607008488
     *
     * <p>
     * Method: <code>allocator()</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121042607008488 ()
    {
        System.out.println("Test: 20171121042607008488");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        allocator.addFixedPool("default", 0, 128, 16);
        final OperandStack stk = allocator.newOperandStack();

        assertSame(allocator, stk.allocator());
    }

    /**
     * Test: 20171121042607008518
     *
     * <p>
     * Method: <code>pool()</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121042607008518 ()
    {
        System.out.println("Test: 20171121042607008518");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool pool1 = allocator.addFixedPool("default", 0, 128, 16);
        final AllocationPool pool2 = allocator.addFixedPool("OlympicPool", 0, 128, 16);
        final OperandStack stk = allocator.newOperandStack();

        assertTrue(stk.isStackEmpty());
        assertSame(allocator.defaultPool(), stk.pool());
        assertSame(allocator.defaultPool(), pool1);

        pool2.alloc(stk, "Water".getBytes(), 0, 5);

        assertFalse(stk.isStackEmpty());
        assertEquals(1, stk.stackSize());
        assertEquals("Water", stk.asString());
        assertSame(pool2, stk.pool());
        assertNotSame(pool1, pool2);
    }

    /**
     * Test: 20171120000935135207
     *
     * <p>
     * Case: Exceptions thrown by methods in OperandStack.
     * </p>
     */
    @Test
    public void test20171120000935135207 ()
    {
        System.out.println("Test: 20171120000935135207");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        allocator.addFixedPool("anon", 0, 128, 8);

        /**
         * Method: push(OperandStack)
         */
        testThrow(NullPointerException.class, x -> x.push((OperandStack) null));
        testThrow(IllegalArgumentException.class, x -> x.push((OperandStack) x.clear()));

        /**
         * Method: pushStr
         */
        testThrow(NullPointerException.class, x -> x.push((String) null));

        /**
         * Method: asBoolean()
         */
        testThrow(IllegalStateException.class, x -> x.push((long) 0).asBoolean()); // Wrong Size, 8 != 1

        /**
         * Method: asByte()
         */
        testThrow(IllegalStateException.class, x -> x.push((long) 0).asByte()); // Wrong Size, 8 != 1

        /**
         * Method: asShort()
         */
        testThrow(IllegalStateException.class, x -> x.push((long) 0).asShort()); // Wrong Size, 8 != 2

        /**
         * Method: asInt()
         */
        testThrow(IllegalStateException.class, x -> x.push((long) 0).asInt()); // Wrong Size, 8 != 4

        /**
         * Method: asLong()
         */
        testThrow(IllegalStateException.class, x -> x.push((int) 0).asLong()); // Wrong Size, 4 != 8

        /**
         * Method: asFloat()
         */
        testThrow(IllegalStateException.class, x -> x.push((long) 0).asFloat()); // Wrong Size, 8 != 4

        /**
         * Method: asDouble()
         */
        testThrow(IllegalStateException.class, x -> x.push((int) 0).asDouble()); // Wrong Size, 4 != 8

        /**
         * Method: asString()
         */
        Verify.verify(true); // TODO: Non UTF-8

        /**
         * Method: pop()
         */
        testThrow(IllegalStateException.class, x -> x.pop()); // pop() from empty stack.

        /**
         * Method: set(OperandStack)
         */
        final ConcreteAllocator otherAllocator = new ConcreteAllocator();
        otherAllocator.addDynamicPool("default", 0, 128);
        testThrow(AllocatorMismatchException.class, x -> x.set(otherAllocator.newOperandStack().push("X")));

        /**
         * Method: set(int, OperandArray)
         */
        final OperandArray arrayAllocMismatch = new ConcreteAllocator().newOperandArray(8);
        testThrow(NullPointerException.class, x -> x.set((OperandArray) null, 0));
        testThrow(IndexOutOfBoundsException.class, x -> x.set(x.allocator().newOperandArray(8), -1)); // index too small
        testThrow(IndexOutOfBoundsException.class, x -> x.set(x.allocator().newOperandArray(8), 8)); // index too large
        testThrow(IndexOutOfBoundsException.class, x -> x.set(x.allocator().newOperandArray(8), 9)); // index too large
        testThrow(AllocatorMismatchException.class, x -> x.set(arrayAllocMismatch, 0)); // wrong allocator

        /**
         * Method: byteAt(int)
         */
        testThrow(IllegalStateException.class, x -> x.byteAt(0)); // Empty Stack
        testThrow(IndexOutOfBoundsException.class, x -> x.push("X").byteAt(-1)); // index too small
        testThrow(IndexOutOfBoundsException.class, x -> x.push("X").byteAt(+2)); // index too large

        /**
         * Method: copyTo(byte[])
         */
        testThrow(NullPointerException.class, x -> x.copyTo(null));

        /**
         * Method: copyTo(byte[], int)
         */
        final byte[] buffer1 = new byte["XYZ".length()];
        testThrow(NullPointerException.class, x -> x.copyTo(null, 0));
        testThrow(IndexOutOfBoundsException.class, x -> x.push("XYZ").copyTo(buffer1, -1)); // offset too small
        testThrow(IndexOutOfBoundsException.class, x -> x.push("XYZ").copyTo(buffer1, 3)); // offset too large

        /**
         * Method: copyTo(byte[], int, int)
         */
        final byte[] buffer2 = new byte["XYZ".length()];
        testThrow(NullPointerException.class, x -> x.copyTo(null, 0, 1));
        testThrow(IndexOutOfBoundsException.class, x -> x.push("XYZ").copyTo(buffer2, -1, 1)); // offset too small
        testThrow(IndexOutOfBoundsException.class, x -> x.push("XYZ").copyTo(buffer2, 3, 1)); // offset too large
        testThrow(IllegalArgumentException.class, x -> x.push("XYZ").copyTo(buffer2, 1, 3)); // (1 + 3) > (3 = buffer length)
    }

    /**
     * Test: 20171120052958992386
     *
     * <p>
     * Method: <code>addDynamicPool</code>
     * </p>
     *
     * <p>
     * Case: Normal Cases
     * </p>
     */
    @Test
    public void test20171120052958992386 ()
    {
        System.out.println("Test: 20171120052958992386");

        final ConcreteAllocator allocator = new ConcreteAllocator();

        // Anonymous
        final AllocationPool pool1 = allocator.addDynamicPool("anon", 3, 6);
        assertEquals("anon", pool1.name());
        assertEquals(3, pool1.minimumAllocationSize());
        assertEquals(6, pool1.maximumAllocationSize());
        assertFalse(pool1.isFixed());

        // Must Allow Exact Size
        // Must Allow Unique Name
        final AllocationPool pool2 = allocator.addDynamicPool("Cities", 7, 7);
        assertEquals("Cities", pool2.name());
        assertEquals(7, pool2.minimumAllocationSize());
        assertEquals(7, pool2.maximumAllocationSize());
        assertFalse(pool2.isFixed());

        assertEquals(pool2, allocator.pools().get("Cities"));
    }

    /**
     * Test: 20171120052958992473
     *
     * <p>
     * Method: <code>addFixedPool</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171120052958992473 ()
    {
        System.out.println("Test: 20171120052958992473");

        final ConcreteAllocator allocator = new ConcreteAllocator();

        // Anonymous
        final AllocationPool pool1 = allocator.addFixedPool("anon", 3, 6, 8);
        assertEquals("anon", pool1.name());
        assertEquals(3, pool1.minimumAllocationSize());
        assertEquals(6, pool1.maximumAllocationSize());
        assertTrue(pool1.isFixed());

        // Must Allow Exact Size
        // Must Allow Unique Name
        final AllocationPool pool2 = allocator.addFixedPool("Cities", 7, 7, 16);
        assertEquals("Cities", pool2.name());
        assertEquals(7, pool2.minimumAllocationSize());
        assertEquals(7, pool2.maximumAllocationSize());
        assertTrue(pool2.isFixed());

        assertEquals(pool2, allocator.pools().get("Cities"));
    }

    /**
     * Test: 20171120052958992504
     *
     * <p>
     * Method: <code>addCompositePool</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171120052958992504 ()
    {
        System.out.println("Test: 20171120052958992504");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool delegate1 = allocator.addFixedPool("Toronto", 101, 200, 4);
        final AllocationPool delegate2 = allocator.addFixedPool("Toronto", 201, 300, 4);
        final List<AllocationPool> delegates = ImmutableList.of(delegate1, delegate2);
        final AllocationPool fallback = allocator.addDynamicPool("backup", 50, 375);

        // Anonymous, No Fallback
        final AllocationPool pool1 = allocator.addCompositePool("anon", null, delegates);
        assertEquals("anon", pool1.name());
        assertEquals(101, pool1.minimumAllocationSize());
        assertEquals(300, pool1.maximumAllocationSize());
        assertFalse(pool1.isFixed());

        // Fallback Present
        // Must Allow Unique Name
        final AllocationPool pool2 = allocator.addCompositePool("Cities", fallback, delegates);
        assertEquals("Cities", pool2.name());
        assertEquals(50, pool2.minimumAllocationSize());
        assertEquals(375, pool2.maximumAllocationSize());
        assertFalse(pool2.isFixed());

        assertEquals(pool2, allocator.pools().get("Cities"));
    }

    /**
     * Test: 20171120052958992531
     *
     * <p>
     * Method: <code>pools()</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171120052958992531 ()
    {
        System.out.println("Test: 20171120052958992531");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool pool0 = allocator.addFixedPool("default", 2, 4, 2);
        final AllocationPool pool1 = allocator.addFixedPool("Planets", 2, 4, 4);
        final AllocationPool pool2 = allocator.addFixedPool("Cities", 2, 6, 8);

        assertEquals(3, allocator.pools().size());
        assertTrue(allocator.pools() == allocator.pools()); // Exact Same Object
        assertEquals(pool1, allocator.pools().get("Planets"));
        assertEquals(pool2, allocator.pools().get("Cities"));

        assertTrue(allocator.pools().containsValue(allocator.defaultPool()));
        assertSame(pool0, allocator.defaultPool());
        assertNotSame(pool1, allocator.defaultPool());
        assertNotSame(pool2, allocator.defaultPool());
    }

    /**
     * Test: 20171120053501742437
     *
     * <p>
     * Method: <code>newOperandStack</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171120053501742437 ()
    {
        System.out.println("Test: 20171120053501742437");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final OperandStack stack = allocator.newOperandStack();

        assertTrue(stack.isStackEmpty());
        assertEquals(0, stack.operandSize());
        assertEquals(0, stack.operandCapacity());
        assertEquals(allocator, stack.allocator());
    }

    /**
     * Test: 20171120053501742512
     *
     * <p>
     * Method: <code>newOperandArray</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171120053501742512 ()
    {
        System.out.println("Test: 20171120053501742512");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final OperandArray array1 = allocator.newOperandArray(0);
        final OperandArray array2 = allocator.newOperandArray(3);

        assertEquals(0, array1.size());
        assertEquals(allocator, array1.allocator());

        assertEquals(3, array2.size());
        assertEquals(allocator, array2.allocator());
    }

    /**
     * Test: 20171120053501742540
     *
     * <p>
     * Method: <code>newOperandArray</code>
     * </p>
     *
     * <p>
     * Case: Negative Size
     * </p>
     */
    @Test (expected = IllegalArgumentException.class)
    public void test20171120053501742540 ()
    {
        System.out.println("Test: 20171120053501742540");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        allocator.newOperandArray(-1);
    }

    /**
     * Test: 20171121033510905405
     *
     * <p>
     * Allocator: Dynamic
     * </p>
     *
     * <p>
     * Method: <code>tryAlloc</code>
     * </p>
     *
     * <p>
     * Case: Allocator Mismatch
     * </p>
     */
    @Test (expected = AllocatorMismatchException.class)
    public void test20171121033510905405 ()
    {
        System.out.println("Test: 20171121033510905405");

        final ConcreteAllocator allocator1 = new ConcreteAllocator();
        final AllocationPool pool = allocator1.addDynamicPool("default", 0, 128);

        final ConcreteAllocator allocator2 = new ConcreteAllocator();
        final OperandStack stk = allocator2.newOperandStack();

        pool.tryAlloc(stk, "Saturn".getBytes(), 0, 6);
    }

    /**
     * Test: 20171121033510905481
     *
     * <p>
     * Allocator: Dynamic
     * </p>
     *
     * <p>
     * Method: <code>tryAlloc</code>
     * </p>
     *
     * <p>
     * Case: Offset Too Small
     * </p>
     */
    @Test (expected = IndexOutOfBoundsException.class)
    public void test20171121033510905481 ()
    {
        System.out.println("Test: 20171121033510905481");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool pool = allocator.addDynamicPool("default", 0, 128);
        final OperandStack stk = allocator.newOperandStack();

        final byte[] data = "Venus".getBytes();

        final int bad = -1; // The Problem
        pool.tryAlloc(stk, data, bad, data.length);
    }

    /**
     * Test: 20171121033510905509
     *
     * <p>
     * Allocator: Dynamic
     * </p>
     *
     * <p>
     * Method: <code>tryAlloc</code>
     * </p>
     *
     * <p>
     * Case: Offset Too Large
     * </p>
     */
    @Test (expected = IndexOutOfBoundsException.class)
    public void test20171121033510905509 ()
    {
        System.out.println("Test: 20171121033510905509");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool pool = allocator.addDynamicPool("default", 0, 128);
        final OperandStack stk = allocator.newOperandStack();

        final byte[] data = "Venus".getBytes();

        final int bad = data.length; // The Problem
        pool.tryAlloc(stk, data, bad, data.length);
    }

    /**
     * Test: 20171121033510905535
     *
     * <p>
     * Allocator: Dynamic
     * </p>
     *
     * <p>
     * Method: <code>tryAlloc</code>
     * </p>
     *
     * <p>
     * Case: Offset Plus Length is Too Large
     * </p>
     */
    @Test (expected = IllegalArgumentException.class)
    public void test20171121033510905535 ()
    {
        System.out.println("Test: 20171121033510905535");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool pool = allocator.addDynamicPool("default", 0, 128);
        final OperandStack stk = allocator.newOperandStack();

        final byte[] data = "Venus".getBytes();

        pool.tryAlloc(stk, data, 2, 3);
    }

    /**
     * Test: 20171121040027311473
     *
     * <p>
     * Allocator: Dynamic
     * </p>
     *
     * <p>
     * Method: <code>tryAlloc</code>
     * </p>
     *
     * <p>
     * Case: Length Too Small
     * </p>
     */
    @Test (expected = IllegalArgumentException.class)
    public void test20171121040027311473 ()
    {
        System.out.println("Test: 20171121040027311473");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool pool = allocator.addDynamicPool("default", 0, 128);
        final OperandStack stk = allocator.newOperandStack();

        final byte[] data = "Venus".getBytes();

        pool.tryAlloc(stk, data, 0, -1);
    }

    /**
     * Test: 20171121040027311554
     *
     * <p>
     * Allocator: Dynamic
     * </p>
     *
     * <p>
     * Method: <code>tryAlloc</code>
     * </p>
     *
     * <p>
     * Case: Length Too Large
     * </p>
     */
    @Test (expected = IllegalArgumentException.class)
    public void test20171121040027311554 ()
    {
        System.out.println("Test: 20171121040027311554");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool pool = allocator.addDynamicPool("default", 0, 128);
        final OperandStack stk = allocator.newOperandStack();

        final byte[] data = "Venus".getBytes();

        pool.tryAlloc(stk, data, 0, data.length + 1);
    }

    /**
     * Test: 20171121033510905580
     *
     * <p>
     * Allocator: Dynamic
     * </p>
     *
     * <p>
     * Method: <code>tryAlloc</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121033510905580 ()
    {
        System.out.println("Test: 20171121033510905580");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool pool = allocator.addDynamicPool("default", 0, 128);
        final OperandStack stk = allocator.newOperandStack();

        assertTrue(pool.tryAlloc(stk, "X".getBytes(), 0, 1));
        assertTrue(pool.tryAlloc(stk, "Y".getBytes(), 0, 1));
        assertTrue(pool.tryAlloc(stk, "Z".getBytes(), 0, 1));

        assertEquals(3, stk.stackSize());
        assertEquals("Z", stk.asString());
        stk.pop();

        assertEquals(2, stk.stackSize());
        assertEquals("Y", stk.asString());
        stk.pop();

        assertEquals(1, stk.stackSize());
        assertEquals("X", stk.asString());
        stk.pop();

        assertTrue(stk.isStackEmpty());
    }

    /**
     * Test: 20171121033510905602
     *
     * <p>
     * Allocator: Fixed
     * </p>
     *
     * <p>
     * Method: <code>tryAlloc</code>
     * </p>
     *
     * <p>
     * Case: Allocator Mismatch
     * </p>
     */
    @Test (expected = AllocatorMismatchException.class)
    public void test20171121033510905602 ()
    {
        System.out.println("Test: 20171121033510905602");

        final ConcreteAllocator allocator1 = new ConcreteAllocator();
        final AllocationPool pool = allocator1.addFixedPool("default", 0, 128, 16);

        final ConcreteAllocator allocator2 = new ConcreteAllocator();
        final OperandStack stk = allocator2.newOperandStack();

        pool.tryAlloc(stk, "Saturn".getBytes(), 0, 6);
    }

    /**
     * Test: 20171121033510905624
     *
     * <p>
     * Allocator: Fixed
     * </p>
     *
     * <p>
     * Method: <code>tryAlloc</code>
     * </p>
     *
     * <p>
     * Case: Offset Too Small
     * </p>
     */
    @Test (expected = IndexOutOfBoundsException.class)
    public void test20171121033510905624 ()
    {
        System.out.println("Test: 20171121033510905624");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool pool = allocator.addFixedPool("default", 0, 128, 16);
        final OperandStack stk = allocator.newOperandStack();

        final byte[] data = "Venus".getBytes();

        pool.tryAlloc(stk, data, -1, data.length);
    }

    /**
     * Test: 20171121033510905646
     *
     * <p>
     * Allocator: Fixed
     * </p>
     *
     * <p>
     * Method: <code>tryAlloc</code>
     * </p>
     *
     * <p>
     * Case: Offset Too Large
     * </p>
     */
    @Test (expected = IndexOutOfBoundsException.class)
    public void test20171121033510905646 ()
    {
        System.out.println("Test: 20171121033510905646");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool pool = allocator.addFixedPool("default", 0, 128, 16);
        final OperandStack stk = allocator.newOperandStack();

        final byte[] data = "Venus".getBytes();

        pool.tryAlloc(stk, data, 6, 0);
    }

    /**
     * Test: 20171121033510905666
     *
     * <p>
     * Allocator: Fixed
     * </p>
     *
     * <p>
     * Method: <code>tryAlloc</code>
     * </p>
     *
     * <p>
     * Case: Offset Plus Length is Too Large
     * </p>
     */
    @Test (expected = IllegalArgumentException.class)
    public void test20171121033510905666 ()
    {
        System.out.println("Test: 20171121033510905666");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool pool = allocator.addFixedPool("default", 0, 128, 16);
        final OperandStack stk = allocator.newOperandStack();

        final byte[] data = "Venus".getBytes();

        pool.tryAlloc(stk, data, 2, 3);
    }

    /**
     * Test: 20171121035558884129
     *
     * <p>
     * Allocator: Fixed
     * </p>
     *
     * <p>
     * Method: <code>tryAlloc</code>
     * </p>
     *
     * <p>
     * Case: Length Too Small
     * </p>
     */
    @Test (expected = IllegalArgumentException.class)
    public void test20171121035558884129 ()
    {
        System.out.println("Test: 20171121035558884129");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool pool = allocator.addFixedPool("default", 0, 128, 16);
        final OperandStack stk = allocator.newOperandStack();

        final byte[] data = "Venus".getBytes();

        pool.tryAlloc(stk, data, 0, -1);
    }

    /**
     * Test: 20171121035558884212
     *
     * <p>
     * Allocator: Fixed
     * </p>
     *
     * <p>
     * Method: <code>tryAlloc</code>
     * </p>
     *
     * <p>
     * Case: Length Too Large
     * </p>
     */
    @Test (expected = IllegalArgumentException.class)
    public void test20171121035558884212 ()
    {
        System.out.println("Test: 20171121035558884212");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool pool = allocator.addFixedPool("default", 0, 128, 16);
        final OperandStack stk = allocator.newOperandStack();

        final byte[] data = "Venus".getBytes();

        pool.tryAlloc(stk, data, 0, data.length + 1);
    }

    /**
     * Test: 20171121033510905685
     *
     * <p>
     * Allocator: Fixed
     * </p>
     *
     * <p>
     * Method: <code>tryAlloc</code>
     * </p>
     *
     * <p>
     * Case: Out of Space
     * </p>
     */
    @Test
    public void test20171121033510905685 ()
    {
        System.out.println("Test: 20171121033510905685");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool pool = allocator.addFixedPool("default", 0, 128, 2);
        final OperandStack stk = allocator.newOperandStack();

        assertEquals(2, pool.capacity().getAsLong());
        assertEquals(0, pool.size().getAsLong());

        assertTrue(pool.tryAlloc(stk, "X".getBytes(), 0, 1));
        assertEquals(1, pool.size().getAsLong());

        assertTrue(pool.tryAlloc(stk, "Y".getBytes(), 0, 1));
        assertEquals(2, pool.size().getAsLong());

        assertFalse(pool.tryAlloc(stk, "Z".getBytes(), 0, 1));
        assertEquals(2, pool.size().getAsLong());
    }

    /**
     * Test: 20171121033510905704
     *
     * <p>
     * Allocator: Fixed
     * </p>
     *
     * <p>
     * Method: <code>tryAlloc</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121033510905704 ()
    {
        System.out.println("Test: 20171121033510905704");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool pool = allocator.addFixedPool("default", 0, 128, 3);
        final OperandStack stk = allocator.newOperandStack();

        assertEquals(3, pool.capacity().getAsLong());
        assertEquals(0, pool.size().getAsLong());

        assertTrue(pool.tryAlloc(stk, "X".getBytes(), 0, 1));
        assertEquals(1, pool.size().getAsLong());

        assertTrue(pool.tryAlloc(stk, "Y".getBytes(), 0, 1));
        assertEquals(2, pool.size().getAsLong());

        assertTrue(pool.tryAlloc(stk, "Z".getBytes(), 0, 1));
        assertEquals(3, pool.size().getAsLong());

        assertEquals(3, stk.stackSize());
        assertEquals("Z", stk.asString());
        stk.pop();

        assertEquals(2, stk.stackSize());
        assertEquals("Y", stk.asString());
        stk.pop();

        assertEquals(1, stk.stackSize());
        assertEquals("X", stk.asString());
        stk.pop();

        assertTrue(stk.isStackEmpty());
    }

    /**
     * Test: 20171121033510905722
     *
     * <p>
     * Allocator: Composite
     * </p>
     *
     * <p>
     * Method: <code>tryAlloc</code>
     * </p>
     *
     * <p>
     * Case: Allocator Mismatch
     * </p>
     */
    @Test (expected = AllocatorMismatchException.class)
    public void test20171121033510905722 ()
    {
        System.out.println("Test: 20171121033510905722");

        final ConcreteAllocator allocator1 = new ConcreteAllocator();
        final AllocationPool delegate1 = allocator1.addDynamicPool("delegate1", 100, 200);
        final AllocationPool delegate2 = allocator1.addDynamicPool("delegate2", 300, 400);
        final AllocationPool pool = allocator1.addCompositePool("default", null, ImmutableList.of(delegate1, delegate2));

        final ConcreteAllocator allocator2 = new ConcreteAllocator();
        final OperandStack stk = allocator2.newOperandStack();

        pool.tryAlloc(stk, "Saturn".getBytes(), 0, 6);
    }

    /**
     * Test: 20171121033510905741
     *
     * <p>
     * Allocator: Composite
     * </p>
     *
     * <p>
     * Method: <code>tryAlloc</code>
     * </p>
     *
     * <p>
     * Case: Offset Too Small
     * </p>
     */
    @Test (expected = IndexOutOfBoundsException.class)
    public void test20171121033510905741 ()
    {
        System.out.println("Test: 20171121033510905741");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool delegate1 = allocator.addDynamicPool("delegate1", 100, 200);
        final AllocationPool delegate2 = allocator.addDynamicPool("delegate2", 300, 400);
        final AllocationPool pool = allocator.addCompositePool("default", null, ImmutableList.of(delegate1, delegate2));
        final OperandStack stk = allocator.newOperandStack();

        final byte[] data = "Venus".getBytes();
        assertEquals(5, data.length);

        pool.tryAlloc(stk, data, -1, 5);
    }

    /**
     * Test: 20171121033510905781
     *
     * <p>
     * Allocator: Composite
     * </p>
     *
     * <p>
     * Method: <code>tryAlloc</code>
     * </p>
     *
     * <p>
     * Case: Offset Too Large
     * </p>
     */
    @Test (expected = IndexOutOfBoundsException.class)
    public void test20171121033510905781 ()
    {
        System.out.println("Test: 20171121033510905781");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool delegate1 = allocator.addDynamicPool("delegate1", 100, 200);
        final AllocationPool delegate2 = allocator.addDynamicPool("delegate2", 300, 400);
        final AllocationPool pool = allocator.addCompositePool("default", null, ImmutableList.of(delegate1, delegate2));
        final OperandStack stk = allocator.newOperandStack();

        final byte[] data = "Venus".getBytes();
        assertEquals(5, data.length);

        pool.tryAlloc(stk, data, 6, 0);
    }

    /**
     * Test: 20171121033510905817
     *
     * <p>
     * Allocator: Composite
     * </p>
     *
     * <p>
     * Method: <code>tryAlloc</code>
     * </p>
     *
     * <p>
     * Case: Offset Plus Length is Too Large
     * </p>
     */
    @Test (expected = IllegalArgumentException.class)
    public void test20171121033510905817 ()
    {
        System.out.println("Test: 20171121033510905817");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool delegate1 = allocator.addDynamicPool("delegate1", 100, 200);
        final AllocationPool delegate2 = allocator.addDynamicPool("delegate2", 300, 400);
        final AllocationPool pool = allocator.addCompositePool("default", null, ImmutableList.of(delegate1, delegate2));
        final OperandStack stk = allocator.newOperandStack();

        final byte[] data = "Venus".getBytes();
        assertEquals(5, data.length);

        pool.tryAlloc(stk, data, 2, 3);
    }

    /**
     * Test: 20171121035200808693
     *
     * <p>
     * Allocator: Composite
     * </p>
     *
     * <p>
     * Method: <code>tryAlloc</code>
     * </p>
     *
     * <p>
     * Case: Length Too Small
     * </p>
     */
    @Test (expected = IllegalArgumentException.class)
    public void test20171121035200808693 ()
    {
        System.out.println("Test: 20171121035200808693");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool delegate1 = allocator.addDynamicPool("delegate1", 100, 200);
        final AllocationPool delegate2 = allocator.addDynamicPool("delegate2", 300, 400);
        final AllocationPool pool = allocator.addCompositePool("default", null, ImmutableList.of(delegate1, delegate2));
        final OperandStack stk = allocator.newOperandStack();

        final byte[] data = "Venus".getBytes();
        assertEquals(5, data.length);

        pool.tryAlloc(stk, data, 0, -1);
    }

    /**
     * Test: 20171121035200808760
     *
     * <p>
     * Allocator: Composite
     * </p>
     *
     * <p>
     * Method: <code>tryAlloc</code>
     * </p>
     *
     * <p>
     * Case: Length Too Large
     * </p>
     */
    @Test (expected = IllegalArgumentException.class)
    public void test20171121035200808760 ()
    {
        System.out.println("Test: 20171121035200808760");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool delegate1 = allocator.addDynamicPool("delegate1", 100, 200);
        final AllocationPool delegate2 = allocator.addDynamicPool("delegate2", 300, 400);
        final AllocationPool pool = allocator.addCompositePool("default", null, ImmutableList.of(delegate1, delegate2));
        final OperandStack stk = allocator.newOperandStack();

        final byte[] data = "Venus".getBytes();
        assertEquals(5, data.length);

        pool.tryAlloc(stk, data, 0, 6);
    }

    /**
     * Test: 20171121033510905836
     *
     * <p>
     * Allocator: Composite
     * </p>
     *
     * <p>
     * Method: <code>tryAlloc</code>
     * </p>
     *
     * <p>
     * Case: Out of Space with No Fallback
     * </p>
     */
    @Test
    public void test20171121033510905836 ()
    {
        System.out.println("Test: 20171121033510905836");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool delegate1 = allocator.addFixedPool("delegate1", 0, 1, 2);
        final AllocationPool delegate2 = allocator.addFixedPool("delegate2", 2, 3, 2);
        final AllocationPool pool = allocator.addCompositePool("default", null, ImmutableList.of(delegate1, delegate2));
        final OperandStack stk = allocator.newOperandStack();

        assertEquals(2, delegate1.capacity().getAsLong());
        assertEquals(2, delegate2.capacity().getAsLong());
        assertEquals(0, delegate1.size().getAsLong());
        assertEquals(0, delegate2.size().getAsLong());

        /**
         * Allocation into Delegate #1.
         */
        assertTrue(pool.tryAlloc(stk, "X".getBytes(), 0, 1));
        assertEquals(1, delegate1.size().getAsLong());
        assertEquals(0, delegate2.size().getAsLong());

        /**
         * Allocation into Delegate #1.
         */
        assertTrue(pool.tryAlloc(stk, "Y".getBytes(), 0, 1));
        assertEquals(2, delegate1.size().getAsLong());
        assertEquals(0, delegate2.size().getAsLong());

        /**
         * Allocation Failure for Delegate #1.
         */
        assertFalse(pool.tryAlloc(stk, "Z".getBytes(), 0, 1));
        assertEquals(2, delegate1.size().getAsLong());
        assertEquals(0, delegate2.size().getAsLong());

        /**
         * Allocation into Delegate #2.
         */
        assertTrue(pool.tryAlloc(stk, "Va".getBytes(), 0, 2));
        assertEquals(2, delegate1.size().getAsLong());
        assertEquals(1, delegate2.size().getAsLong());

        /**
         * Allocation into Delegate #2.
         */
        assertTrue(pool.tryAlloc(stk, "Or".getBytes(), 0, 2));
        assertEquals(2, delegate1.size().getAsLong());
        assertEquals(2, delegate2.size().getAsLong());

        /**
         * Allocation Failure for Delegate #2.
         */
        assertFalse(pool.tryAlloc(stk, "Wa".getBytes(), 0, 2));
        assertEquals(2, delegate1.size().getAsLong());
        assertEquals(2, delegate2.size().getAsLong());

        /**
         * Verify the successful allocations.
         */
        assertEquals(4, stk.stackSize());
        assertEquals("Or", stk.asString());
        assertSame(delegate2, stk.pool());
        stk.pop();
        assertEquals("Va", stk.asString());
        assertSame(delegate2, stk.pool());
        stk.pop();
        assertEquals("Y", stk.asString());
        assertSame(delegate1, stk.pool());
        stk.pop();
        assertEquals("X", stk.asString());
        assertSame(delegate1, stk.pool());
        stk.pop();
        assertTrue(stk.isStackEmpty());
    }

    /**
     * Test: 20171121081453580017
     *
     * <p>
     * Allocator: Composite
     * </p>
     *
     * <p>
     * Method: <code>tryAlloc</code>
     * </p>
     *
     * <p>
     * Case: Out of Space with Fallback
     * </p>
     */
    @Test
    public void test20171121081453580017 ()
    {
        System.out.println("Test: 20171121081453580017");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool fallback = allocator.addFixedPool("delegate1", 0, 8, 2);
        final AllocationPool delegate1 = allocator.addFixedPool("delegate1", 0, 1, 2);
        final AllocationPool delegate2 = allocator.addFixedPool("delegate2", 2, 3, 2);
        final AllocationPool pool = allocator.addCompositePool("default", fallback, ImmutableList.of(delegate1, delegate2));
        final OperandStack stk = allocator.newOperandStack();

        assertEquals(2, fallback.capacity().getAsLong());
        assertEquals(2, delegate1.capacity().getAsLong());
        assertEquals(2, delegate2.capacity().getAsLong());
        assertEquals(0, fallback.size().getAsLong());
        assertEquals(0, delegate1.size().getAsLong());
        assertEquals(0, delegate2.size().getAsLong());

        /**
         * Allocation into Delegate #1.
         */
        assertTrue(pool.tryAlloc(stk, "X".getBytes(), 0, 1));
        assertEquals(0, fallback.size().getAsLong());
        assertEquals(1, delegate1.size().getAsLong());
        assertEquals(0, delegate2.size().getAsLong());

        /**
         * Allocation into Delegate #1.
         */
        assertTrue(pool.tryAlloc(stk, "Y".getBytes(), 0, 1));
        assertEquals(0, fallback.size().getAsLong());
        assertEquals(2, delegate1.size().getAsLong());
        assertEquals(0, delegate2.size().getAsLong());

        /**
         * Allocation Failure for Delegate #1
         * therefore, Allocation into Fallback.
         */
        assertTrue(pool.tryAlloc(stk, "Z".getBytes(), 0, 1));
        assertEquals(1, fallback.size().getAsLong());
        assertEquals(2, delegate1.size().getAsLong());
        assertEquals(0, delegate2.size().getAsLong());

        /**
         * Allocation into Delegate #2.
         */
        assertTrue(pool.tryAlloc(stk, "Va".getBytes(), 0, 2));
        assertEquals(1, fallback.size().getAsLong());
        assertEquals(2, delegate1.size().getAsLong());
        assertEquals(1, delegate2.size().getAsLong());

        /**
         * Allocation into Delegate #2.
         */
        assertTrue(pool.tryAlloc(stk, "Or".getBytes(), 0, 2));
        assertEquals(1, fallback.size().getAsLong());
        assertEquals(2, delegate1.size().getAsLong());
        assertEquals(2, delegate2.size().getAsLong());

        /**
         * Allocation Failure for Delegate #2;
         * therefore, Allocation into Fallback.
         */
        assertTrue(pool.tryAlloc(stk, "Wa".getBytes(), 0, 2));
        assertEquals(2, fallback.size().getAsLong());
        assertEquals(2, delegate1.size().getAsLong());
        assertEquals(2, delegate2.size().getAsLong());

        /**
         * Allocation Failure for Delegate #1 and Fallback.
         */
        assertFalse(pool.tryAlloc(stk, "M".getBytes(), 0, 1));
        assertEquals(2, fallback.size().getAsLong());
        assertEquals(2, delegate1.size().getAsLong());
        assertEquals(2, delegate2.size().getAsLong());

        /**
         * Allocation Failure for Delegate #2 and Fallback.
         */
        assertFalse(pool.tryAlloc(stk, "Ca".getBytes(), 0, 2));
        assertEquals(2, fallback.size().getAsLong());
        assertEquals(2, delegate1.size().getAsLong());
        assertEquals(2, delegate2.size().getAsLong());

        /**
         * Verify the successful allocations.
         */
        assertEquals(6, stk.stackSize());
        assertEquals("Wa", stk.asString());
        assertSame(fallback, stk.pool());
        stk.pop();
        assertEquals("Or", stk.asString());
        assertSame(delegate2, stk.pool());
        stk.pop();
        assertEquals("Va", stk.asString());
        assertSame(delegate2, stk.pool());
        stk.pop();
        assertEquals("Z", stk.asString());
        assertSame(fallback, stk.pool());
        stk.pop();
        assertEquals("Y", stk.asString());
        assertSame(delegate1, stk.pool());
        stk.pop();
        assertEquals("X", stk.asString());
        assertSame(delegate1, stk.pool());
        stk.pop();
        assertTrue(stk.isStackEmpty());
    }

    /**
     * Test: 20171121033510905854
     *
     * <p>
     * Allocator: Composite
     * </p>
     *
     * <p>
     * Method: <code>tryAlloc</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121033510905854 ()
    {
        System.out.println("Test: 20171121033510905854");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool fallback = allocator.addFixedPool("delegate1", 0, 8, 2);
        final AllocationPool delegate1 = allocator.addFixedPool("delegate1", 4, 4, 2);
        final AllocationPool delegate2 = allocator.addFixedPool("delegate2", 6, 6, 2);
        final AllocationPool pool = allocator.addCompositePool("default", fallback, ImmutableList.of(delegate1, delegate2));
        final OperandStack stk = allocator.newOperandStack();

        assertEquals(2, fallback.capacity().getAsLong());
        assertEquals(2, delegate1.capacity().getAsLong());
        assertEquals(2, delegate2.capacity().getAsLong());
        assertEquals(0, fallback.size().getAsLong());
        assertEquals(0, delegate1.size().getAsLong());
        assertEquals(0, delegate2.size().getAsLong());

        /**
         * Allocation into Delegate #1,
         * because length("Mars") is (4).
         */
        assertTrue(pool.tryAlloc(stk, "Mars".getBytes(), 0, 4));
        assertEquals(0, fallback.size().getAsLong());
        assertEquals(1, delegate1.size().getAsLong());
        assertEquals(0, delegate2.size().getAsLong());

        /**
         * Allocation into Fallback,
         * because length("Earth") is (5).
         */
        assertTrue(pool.tryAlloc(stk, "Earth".getBytes(), 0, 5));
        assertEquals(1, fallback.size().getAsLong());
        assertEquals(1, delegate1.size().getAsLong());
        assertEquals(0, delegate2.size().getAsLong());

        /**
         * Allocation into Delegate #2,
         * because length("Saturn") is (6).
         */
        assertTrue(pool.tryAlloc(stk, "Saturn".getBytes(), 0, 6));
        assertEquals(1, fallback.size().getAsLong());
        assertEquals(1, delegate1.size().getAsLong());
        assertEquals(1, delegate2.size().getAsLong());

        /**
         * Allocation into Fallback,
         * because length("Jupiter") is (7).
         */
        assertTrue(pool.tryAlloc(stk, "Jupiter".getBytes(), 0, 7));
        assertEquals(2, fallback.size().getAsLong());
        assertEquals(1, delegate1.size().getAsLong());
        assertEquals(1, delegate2.size().getAsLong());

        /**
         * Verify the successful allocations.
         */
        assertEquals(4, stk.stackSize());
        assertEquals("Jupiter", stk.asString());
        assertSame(fallback, stk.pool());
        stk.pop();
        assertEquals("Saturn", stk.asString());
        assertSame(delegate2, stk.pool());
        stk.pop();
        assertEquals("Earth", stk.asString());
        assertSame(fallback, stk.pool());
        stk.pop();
        assertEquals("Mars", stk.asString());
        assertSame(delegate1, stk.pool());
        stk.pop();
        assertTrue(stk.isStackEmpty());
    }

    /**
     * Test: 20171121035358615320
     *
     * <p>
     * Method: <code>alloc</code>
     * </p>
     *
     * <p>
     * Case: Out of Space
     * </p>
     */
    @Test (expected = ExhaustedAllocationPoolException.class)
    public void test20171121035358615320 ()
    {
        System.out.println("Test: 20171121035358615320");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool pool = allocator.addFixedPool("default", 0, 128, 2);
        final OperandStack stk = allocator.newOperandStack();

        assertEquals(2, pool.capacity().getAsLong());
        assertEquals(0, pool.size().getAsLong());

        assertTrue(pool.tryAlloc(stk, "X".getBytes(), 0, 1));
        assertEquals(1, pool.size().getAsLong());

        assertTrue(pool.tryAlloc(stk, "Y".getBytes(), 0, 1));
        assertEquals(2, pool.size().getAsLong());

        pool.alloc(stk, "Z".getBytes(), 0, 1);
    }

    /**
     * Test: 20171121035432867337
     *
     * <p>
     * Method: <code>alloc</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121035432867337 ()
    {
        System.out.println("Test: 20171121035432867337");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool pool = allocator.addFixedPool("default", 0, 128, 3);
        final OperandStack stk = allocator.newOperandStack();

        assertEquals(3, pool.capacity().getAsLong());
        assertEquals(0, pool.size().getAsLong());

        pool.alloc(stk, "X".getBytes(), 0, 1);
        assertEquals(1, pool.size().getAsLong());

        pool.alloc(stk, "Y".getBytes(), 0, 1);
        assertEquals(2, pool.size().getAsLong());

        pool.alloc(stk, "Z".getBytes(), 0, 1);
        assertEquals(3, pool.size().getAsLong());

        assertEquals(3, stk.stackSize());
        assertEquals("Z", stk.asString());
        stk.pop();

        assertEquals(2, stk.stackSize());
        assertEquals("Y", stk.asString());
        stk.pop();

        assertEquals(1, stk.stackSize());
        assertEquals("X", stk.asString());
        stk.pop();

        assertTrue(stk.isStackEmpty());
    }

    /**
     * Test: 20171121034626049407
     *
     * <p>
     * Method: <code>defaultPool</code>
     * </p>
     *
     * <p>
     * Case: No Default Pool Specified
     * </p>
     */
    @Test (expected = IllegalStateException.class)
    public void test20171121034626049407 ()
    {
        System.out.println("Test: 20171121034626049407");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        allocator.addFixedPool("NonDefault", 0, 128, 3);

        allocator.defaultPool();
    }

    /**
     * Test: 20171121223521973170
     *
     * <p>
     * Allocator: Dynamic
     * </p>
     *
     * <p>
     * Case: Getters
     * </p>
     */
    @Test
    public void test20171121223521973170 ()
    {
        System.out.println("Test: 20171121223521973170");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool pool = allocator.addDynamicPool("BigBangPool", 100, 200);

        assertSame(allocator, pool.allocator());
        assertEquals("BigBangPool", pool.name());
        assertFalse(pool.isFixed());
        assertEquals(100, pool.minimumAllocationSize());
        assertEquals(200, pool.maximumAllocationSize());
        assertFalse(pool.size().isPresent());
        assertFalse(pool.capacity().isPresent());
    }

    /**
     * Test: 20171121223521973256
     *
     * <p>
     * Allocator: Fixed
     * </p>
     *
     * <p>
     * Case: Getters
     * </p>
     */
    @Test
    public void test20171121223521973256 ()
    {
        System.out.println("Test: 20171121223521973256");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool pool = allocator.addFixedPool("SwimmingPool", 100, 200, 300);

        assertSame(allocator, pool.allocator());
        assertEquals("SwimmingPool", pool.name());
        assertTrue(pool.isFixed());
        assertEquals(100, pool.minimumAllocationSize());
        assertEquals(200, pool.maximumAllocationSize());
        assertEquals(0, pool.size().getAsLong());
        assertEquals(300, pool.capacity().getAsLong());
    }

    /**
     * Test: 20171121223521973291
     *
     * <p>
     * Allocator: Composite with Fallback
     * </p>
     *
     * <p>
     * Case: Getters
     * </p>
     */
    @Test
    public void test20171121223521973291 ()
    {
        System.out.println("Test: 20171121223521973291");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool fallback = allocator.addFixedPool("delegate1", 91, 321, 2);
        final AllocationPool delegate1 = allocator.addFixedPool("delegate1", 101, 200, 2);
        final AllocationPool delegate2 = allocator.addFixedPool("delegate2", 201, 300, 2);
        final AllocationPool pool = allocator.addCompositePool("RouterPool", fallback, ImmutableList.of(delegate1, delegate2));

        assertSame(allocator, pool.allocator());
        assertEquals("RouterPool", pool.name());
        assertFalse(pool.isFixed());
        assertEquals(91, pool.minimumAllocationSize());
        assertEquals(321, pool.maximumAllocationSize());
        assertFalse(pool.size().isPresent());
        assertFalse(pool.capacity().isPresent());
    }

    /**
     * Test: 20171121224042640578
     *
     * <p>
     * Allocator: Composite without Fallback
     * </p>
     *
     * <p>
     * Case: Getters
     * </p>
     */
    @Test
    public void test20171121224042640578 ()
    {
        System.out.println("Test: 20171121224042640578");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool fallback = null;
        final AllocationPool delegate1 = allocator.addFixedPool("delegate1", 101, 200, 2);
        final AllocationPool delegate2 = allocator.addFixedPool("delegate2", 201, 300, 2);
        final AllocationPool pool = allocator.addCompositePool("RouterPool", fallback, ImmutableList.of(delegate1, delegate2));

        assertSame(allocator, pool.allocator());
        assertEquals("RouterPool", pool.name());
        assertFalse(pool.isFixed());
        assertEquals(101, pool.minimumAllocationSize());
        assertEquals(300, pool.maximumAllocationSize());
        assertFalse(pool.size().isPresent());
        assertFalse(pool.capacity().isPresent());
    }

    /**
     * Test: 20171121223521973323
     *
     * <p>
     * Method in OperandArray: <code>close()</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171121223521973323 ()
    {
        System.out.println("Test: 20171121223521973323");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool pool = allocator.addFixedPool("default", 0, 200, 300);
        final OperandArray array = allocator.newOperandArray(4);
        final OperandStack stack = allocator.newOperandStack();

        stack.push("Autumn");
        array.set(0, stack);
        stack.clear();

        stack.push("Emma").push("Erin");
        array.set(1, stack);
        stack.clear();

        stack.push("Jenna").push("Rachel");
        array.set(2, stack);

        // Jenna and Rachel are still on the stack.
        assertEquals(2, stack.stackSize());
        stack.push("Kate");
        array.set(3, stack);

        // Remove Kate and Rachel from the stack.
        stack.pop().pop();

        assertEquals(1, stack.stackSize());
        assertEquals(6, pool.size().getAsLong());

        // Method Under Test
        array.close();

        /**
         * All of the operands, except "Jenna", should have been deallocated,
         * because they were not referenced by anything other than the array.
         * "Jenna" was referenced by the array, so that operand was still in-use;
         * therefore, that operand must still be actively allocated.
         */
        assertEquals(1, stack.stackSize());
        assertEquals(1, pool.size().getAsLong());

        // The array is now clear.
        assertTrue(IntStream.range(0, 4).allMatch(i -> stack.set(array, i).isStackEmpty()));
    }
}
