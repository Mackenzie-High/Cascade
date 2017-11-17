package com.mackenziehigh.cascade.internal.messages;

import com.google.common.collect.ImmutableList;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
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
     * Test: 20171105205239920186
     *
     *
     * <p>
     * Case: Normal cases for OperandStack the methods.
     * </p>
     */
    @Test
    public void test20171105205239920186 ()
    {
        System.out.println("Test: 20171105205239920186");

        final ConcreteAllocator object = new ConcreteAllocator();
        final OperandStack stk = object.newOperandStack();

        /**
         * Method: pushZ, Case: false
         */
        assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
        stk.push(false); // Method Under Test
        assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
        assertEquals(false, stk.asBoolean()); // Verify the value that was pushed onto the stack.
        assertEquals(0, (byte) stk.asByte());
        assertEquals(1, stk.asByteArray().length);
        assertEquals(1, stk.operandSize());
        assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
        stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
        assertEquals(0, object.anon().size()); // Verify that the operand was freed.

        /**
         * Method: pushZ, Case: true
         */
        assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
        stk.push(true); // Method Under Test
        assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
        assertEquals(true, stk.asBoolean()); // Verify the value that was pushed onto the stack.
        assertEquals(1, (byte) stk.asByte());
        assertEquals(1, stk.asByteArray().length);
        assertEquals(1, stk.operandSize());
        assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
        stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
        assertEquals(0, object.anon().size()); // Verify that the operand was freed.

        /**
         * Method: pushB
         */
        for (byte x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (byte) x).collect(Collectors.toList()))
        {
            assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
            stk.push(x); // Method Under Test
            assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
            assertTrue(x == stk.asByte()); // Verify the value that was pushed onto the stack.
            assertEquals(1, stk.asByteArray().length);
            assertEquals(1, stk.operandSize());
            assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
            stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
            assertEquals(0, object.anon().size()); // Verify that the operand was freed.
        }

        /**
         * Method: pushS
         */
        for (short x : IntStream.range(Short.MIN_VALUE, Short.MAX_VALUE).mapToObj(x -> (short) x).collect(Collectors.toList()))
        {
            assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
            stk.push(x); // Method Under Test
            assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
            assertTrue(x == stk.asShort()); // Verify the value that was pushed onto the stack.
            assertEquals(2, stk.asByteArray().length);
            assertEquals(2, stk.operandSize());
            assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
            stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
            assertEquals(0, object.anon().size()); // Verify that the operand was freed.
        }

        /**
         * Method: pushI
         */
        for (int x : IntStream.range(Integer.MAX_VALUE - 1000, Integer.MIN_VALUE + 1000).mapToObj(x -> (short) x).collect(Collectors.toList()))
        {
            assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
            stk.push(x); // Method Under Test
            assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
            assertTrue(x == stk.asInt()); // Verify the value that was pushed onto the stack.
            assertEquals(4, stk.asByteArray().length);
            assertEquals(4, stk.operandSize());
            assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
            stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
            assertEquals(0, object.anon().size()); // Verify that the operand was freed.
        }

        /**
         * Method: pushJ
         */
        for (long x : LongStream.range(Long.MAX_VALUE - 1000, Long.MIN_VALUE + 1000).mapToObj(x -> (short) x).collect(Collectors.toList()))
        {
            assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
            stk.push(x); // Method Under Test
            assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
            assertTrue(x == stk.asLong()); // Verify the value that was pushed onto the stack.
            assertEquals(8, stk.asByteArray().length);
            assertEquals(8, stk.operandSize());
            assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
            stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
            assertEquals(0, object.anon().size()); // Verify that the operand was freed.
        }

        /**
         * Method: pushF.
         *
         * Note: NaN == NaN, will always return false, per IEEE-754 specification.
         */
        for (float x : ImmutableList.of(Float.MAX_VALUE, Float.MIN_VALUE, -3.0F, -2.0F, -1.0F, 0.0F, 1.0F, 2.0F, 3.0F, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NaN))
        {
            assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
            stk.push(x); // Method Under Test
            assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
            assertTrue(Float.floatToIntBits(x) == Float.floatToIntBits(stk.asFloat())); // Verify the value that was pushed onto the stack.
            assertEquals(x, (Float) stk.asFloat());
            assertEquals(4, stk.asByteArray().length);
            assertEquals(4, stk.operandSize());
            assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
            stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
            assertEquals(0, object.anon().size()); // Verify that the operand was freed.
        }

        /**
         * Method: pushD.
         *
         * Note: NaN == NaN, will always return false, per IEEE-754 specification.
         */
        for (double x : ImmutableList.of(Double.MAX_VALUE, Double.MIN_VALUE, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NaN))
        {
            assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
            stk.push(x); // Method Under Test
            assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
            assertTrue(Double.doubleToLongBits(x) == Double.doubleToLongBits(stk.asDouble())); // Verify the value that was pushed onto the stack.
            assertEquals(x, (Double) stk.asDouble());
            assertEquals(8, stk.asByteArray().length);
            assertEquals(8, stk.operandSize());
            assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
            stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
            assertEquals(0, object.anon().size()); // Verify that the operand was freed.
        }

        /**
         * Method: pushStr
         */
        assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
        stk.push("Vulcan"); // Method Under Test
        assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
        assertEquals("Vulcan", stk.asString()); // Verify the value that was pushed onto the stack.
        assertEquals(6, stk.asByteArray().length);
        assertEquals(6, stk.operandSize());
        assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
        stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
        assertEquals(0, object.anon().size()); // Verify that the operand was freed.
    }
}
