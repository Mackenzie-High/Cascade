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
     * (abstract) or (final + synchronized). Since the implementation
     * is obviously non-abstract itself, each (abstract) method has
     * a corresponding method that must be (final + synchronized).
     * Thus, this test is really ensuring that every non-implicit
     * method implementation is (final + synchronized).
     * </p>
     *
     * <p>
     * Each method is required to be (synchronized) in order to easily
     * ensure that the operand-stack implementation is thread-safe.
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
        stk.pushZ(false); // Method Under Test
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
        stk.pushZ(true); // Method Under Test
        assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
        assertEquals(true, stk.asBoolean()); // Verify the value that was pushed onto the stack.
        assertEquals(1, (byte) stk.asByte());
        assertEquals(1, stk.asByteArray().length);
        assertEquals(1, stk.operandSize());
        assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
        stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
        assertEquals(0, object.anon().size()); // Verify that the operand was freed.

        /**
         * Method: pushC
         */
        for (char x : IntStream.range(Character.MIN_VALUE, Character.MAX_VALUE).mapToObj(x -> (char) x).collect(Collectors.toList()))
        {
            assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
            stk.pushC(x); // Method Under Test
            assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
            assertTrue(x == stk.asChar()); // Verify the value that was pushed onto the stack.
            assertEquals(2, stk.asByteArray().length);
            assertEquals(2, stk.operandSize());
            assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
            stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
            assertEquals(0, object.anon().size()); // Verify that the operand was freed.
        }

        /**
         * Method: pushB
         */
        for (byte x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (byte) x).collect(Collectors.toList()))
        {
            assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
            stk.pushB(x); // Method Under Test
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
            stk.pushS(x); // Method Under Test
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
            stk.pushI(x); // Method Under Test
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
            stk.pushJ(x); // Method Under Test
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
            stk.pushF(x); // Method Under Test
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
            stk.pushD(x); // Method Under Test
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
        stk.pushStr("Vulcan"); // Method Under Test
        assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
        assertEquals("Vulcan", stk.asString()); // Verify the value that was pushed onto the stack.
        assertEquals(6, stk.asByteArray().length);
        assertEquals(6, stk.operandSize());
        assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
        stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
        assertEquals(0, object.anon().size()); // Verify that the operand was freed.

        /**
         * Method: divC
         */
        for (char x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (char) x).collect(Collectors.toList()))
        {
            for (char y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (char) y).collect(Collectors.toList()))
            {
                if (y == 0)
                {
                    continue;
                }

                final char expected = (char) (x / y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushC(x);
                stk.pushC(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.divC(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asChar()); // Verify the value that was pushed onto the stack.
                assertEquals(2, stk.asByteArray().length);
                assertEquals(2, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: divB
         */
        for (byte x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (byte) x).collect(Collectors.toList()))
        {
            for (byte y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (byte) y).collect(Collectors.toList()))
            {
                if (y == 0)
                {
                    continue;
                }

                final byte expected = (byte) (x / y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushB(x);
                stk.pushB(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.divB(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asByte()); // Verify the value that was pushed onto the stack.
                assertEquals(1, stk.asByteArray().length);
                assertEquals(1, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: divS
         */
        for (short x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (short) x).collect(Collectors.toList()))
        {
            for (short y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (short) y).collect(Collectors.toList()))
            {
                if (y == 0)
                {
                    continue;
                }

                final short expected = (short) (x / y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushS(x);
                stk.pushS(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.divS(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asShort()); // Verify the value that was pushed onto the stack.
                assertEquals(2, stk.asByteArray().length);
                assertEquals(2, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: divI
         */
        for (int x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (int) x).collect(Collectors.toList()))
        {
            for (int y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (int) y).collect(Collectors.toList()))
            {
                if (y == 0)
                {
                    continue;
                }

                final int expected = (int) (x / y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushI(x);
                stk.pushI(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.divI(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asInt()); // Verify the value that was pushed onto the stack.
                assertEquals(4, stk.asByteArray().length);
                assertEquals(4, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: divJ
         */
        for (long x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (long) x).collect(Collectors.toList()))
        {
            for (long y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (long) y).collect(Collectors.toList()))
            {
                if (y == 0)
                {
                    continue;
                }

                final long expected = (long) (x / y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushJ(x);
                stk.pushJ(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.divJ(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asLong()); // Verify the value that was pushed onto the stack.
                assertEquals(8, stk.asByteArray().length);
                assertEquals(8, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: divF
         */
        for (float x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (float) x).collect(Collectors.toList()))
        {
            for (float y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (float) y).collect(Collectors.toList()))
            {
                if (y == 0)
                {
                    continue;
                }

                final float expected = (float) (x / y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushF(x);
                stk.pushF(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.divF(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asFloat()); // Verify the value that was pushed onto the stack.
                assertEquals(4, stk.asByteArray().length);
                assertEquals(4, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: divD
         */
        for (double x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (double) x).collect(Collectors.toList()))
        {
            for (double y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (double) y).collect(Collectors.toList()))
            {
                if (y == 0)
                {
                    continue;
                }

                final double expected = (double) (x / y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushD(x);
                stk.pushD(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.divD(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asDouble()); // Verify the value that was pushed onto the stack.
                assertEquals(8, stk.asByteArray().length);
                assertEquals(8, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: modC
         */
        for (char x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (char) x).collect(Collectors.toList()))
        {
            for (char y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (char) y).collect(Collectors.toList()))
            {
                if (y == 0)
                {
                    continue;
                }

                final char expected = (char) (x % y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushC(x);
                stk.pushC(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.modC(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asChar()); // Verify the value that was pushed onto the stack.
                assertEquals(2, stk.asByteArray().length);
                assertEquals(2, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: modB
         */
        for (byte x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (byte) x).collect(Collectors.toList()))
        {
            for (byte y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (byte) y).collect(Collectors.toList()))
            {
                if (y == 0)
                {
                    continue;
                }

                final byte expected = (byte) (x % y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushB(x);
                stk.pushB(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.modB(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asByte()); // Verify the value that was pushed onto the stack.
                assertEquals(1, stk.asByteArray().length);
                assertEquals(1, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: modS
         */
        for (short x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (short) x).collect(Collectors.toList()))
        {
            for (short y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (short) y).collect(Collectors.toList()))
            {
                if (y == 0)
                {
                    continue;
                }

                final short expected = (short) (x % y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushS(x);
                stk.pushS(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.modS(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asShort()); // Verify the value that was pushed onto the stack.
                assertEquals(2, stk.asByteArray().length);
                assertEquals(2, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: modI
         */
        for (int x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (int) x).collect(Collectors.toList()))
        {
            for (int y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (int) y).collect(Collectors.toList()))
            {
                if (y == 0)
                {
                    continue;
                }

                final int expected = (int) (x % y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushI(x);
                stk.pushI(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.modI(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asInt()); // Verify the value that was pushed onto the stack.
                assertEquals(4, stk.asByteArray().length);
                assertEquals(4, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: modJ
         */
        for (long x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (long) x).collect(Collectors.toList()))
        {
            for (long y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (long) y).collect(Collectors.toList()))
            {
                if (y == 0)
                {
                    continue;
                }

                final long expected = (long) (x % y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushJ(x);
                stk.pushJ(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.modJ(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asLong()); // Verify the value that was pushed onto the stack.
                assertEquals(8, stk.asByteArray().length);
                assertEquals(8, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: modF
         */
        for (float x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (float) x).collect(Collectors.toList()))
        {
            for (float y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (float) y).collect(Collectors.toList()))
            {
                if (y == 0)
                {
                    continue;
                }

                final float expected = (float) (x % y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushF(x);
                stk.pushF(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.modF(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asFloat()); // Verify the value that was pushed onto the stack.
                assertEquals(4, stk.asByteArray().length);
                assertEquals(4, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: modD
         */
        for (double x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (double) x).collect(Collectors.toList()))
        {
            for (double y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (double) y).collect(Collectors.toList()))
            {
                if (y == 0)
                {
                    continue;
                }

                final double expected = (double) (x % y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushD(x);
                stk.pushD(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.modD(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asDouble()); // Verify the value that was pushed onto the stack.
                assertEquals(8, stk.asByteArray().length);
                assertEquals(8, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: mulC
         */
        for (char x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (char) x).collect(Collectors.toList()))
        {
            for (char y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (char) y).collect(Collectors.toList()))
            {
                final char expected = (char) (x * y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushC(x);
                stk.pushC(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.mulC(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asChar()); // Verify the value that was pushed onto the stack.
                assertEquals(2, stk.asByteArray().length);
                assertEquals(2, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: mulB
         */
        for (byte x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (byte) x).collect(Collectors.toList()))
        {
            for (byte y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (byte) y).collect(Collectors.toList()))
            {
                final byte expected = (byte) (x * y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushB(x);
                stk.pushB(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.mulB(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asByte()); // Verify the value that was pushed onto the stack.
                assertEquals(1, stk.asByteArray().length);
                assertEquals(1, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: mulS
         */
        for (short x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (short) x).collect(Collectors.toList()))
        {
            for (short y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (short) y).collect(Collectors.toList()))
            {
                final short expected = (short) (x * y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushS(x);
                stk.pushS(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.mulS(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asShort()); // Verify the value that was pushed onto the stack.
                assertEquals(2, stk.asByteArray().length);
                assertEquals(2, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: mulI
         */
        for (int x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (int) x).collect(Collectors.toList()))
        {
            for (int y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (int) y).collect(Collectors.toList()))
            {
                final int expected = (int) (x * y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushI(x);
                stk.pushI(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.mulI(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asInt()); // Verify the value that was pushed onto the stack.
                assertEquals(4, stk.asByteArray().length);
                assertEquals(4, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: mulJ
         */
        for (long x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (long) x).collect(Collectors.toList()))
        {
            for (long y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (long) y).collect(Collectors.toList()))
            {
                final long expected = (long) (x * y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushJ(x);
                stk.pushJ(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.mulJ(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asLong()); // Verify the value that was pushed onto the stack.
                assertEquals(8, stk.asByteArray().length);
                assertEquals(8, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: mulF
         */
        for (float x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (float) x).collect(Collectors.toList()))
        {
            for (float y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (float) y).collect(Collectors.toList()))
            {
                final float expected = (float) (x * y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushF(x);
                stk.pushF(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.mulF(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asFloat()); // Verify the value that was pushed onto the stack.
                assertEquals(4, stk.asByteArray().length);
                assertEquals(4, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: mulD
         */
        for (double x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (double) x).collect(Collectors.toList()))
        {
            for (double y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (double) y).collect(Collectors.toList()))
            {
                final double expected = (double) (x * y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushD(x);
                stk.pushD(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.mulD(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asDouble()); // Verify the value that was pushed onto the stack.
                assertEquals(8, stk.asByteArray().length);
                assertEquals(8, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: addC
         */
        for (char x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (char) x).collect(Collectors.toList()))
        {
            for (char y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (char) y).collect(Collectors.toList()))
            {
                final char expected = (char) (x + y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushC(x);
                stk.pushC(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.addC(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asChar()); // Verify the value that was pushed onto the stack.
                assertEquals(2, stk.asByteArray().length);
                assertEquals(2, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: addB
         */
        for (byte x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (byte) x).collect(Collectors.toList()))
        {
            for (byte y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (byte) y).collect(Collectors.toList()))
            {
                final byte expected = (byte) (x + y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushB(x);
                stk.pushB(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.addB(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asByte()); // Verify the value that was pushed onto the stack.
                assertEquals(1, stk.asByteArray().length);
                assertEquals(1, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: addS
         */
        for (short x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (short) x).collect(Collectors.toList()))
        {
            for (short y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (short) y).collect(Collectors.toList()))
            {
                final short expected = (short) (x + y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushS(x);
                stk.pushS(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.addS(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asShort()); // Verify the value that was pushed onto the stack.
                assertEquals(2, stk.asByteArray().length);
                assertEquals(2, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: addI
         */
        for (int x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (int) x).collect(Collectors.toList()))
        {
            for (int y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (int) y).collect(Collectors.toList()))
            {
                final int expected = (int) (x + y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushI(x);
                stk.pushI(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.addI(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asInt()); // Verify the value that was pushed onto the stack.
                assertEquals(4, stk.asByteArray().length);
                assertEquals(4, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: addJ
         */
        for (long x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (long) x).collect(Collectors.toList()))
        {
            for (long y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (long) y).collect(Collectors.toList()))
            {
                final long expected = (long) (x + y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushJ(x);
                stk.pushJ(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.addJ(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asLong()); // Verify the value that was pushed onto the stack.
                assertEquals(8, stk.asByteArray().length);
                assertEquals(8, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: addF
         */
        for (float x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (float) x).collect(Collectors.toList()))
        {
            for (float y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (float) y).collect(Collectors.toList()))
            {
                final float expected = (float) (x + y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushF(x);
                stk.pushF(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.addF(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asFloat()); // Verify the value that was pushed onto the stack.
                assertEquals(4, stk.asByteArray().length);
                assertEquals(4, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: addD
         */
        for (double x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (double) x).collect(Collectors.toList()))
        {
            for (double y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (double) y).collect(Collectors.toList()))
            {
                final double expected = (double) (x + y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushD(x);
                stk.pushD(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.addD(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asDouble()); // Verify the value that was pushed onto the stack.
                assertEquals(8, stk.asByteArray().length);
                assertEquals(8, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: subC
         */
        for (char x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (char) x).collect(Collectors.toList()))
        {
            for (char y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (char) y).collect(Collectors.toList()))
            {
                final char expected = (char) (x - y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushC(x);
                stk.pushC(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.subC(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asChar()); // Verify the value that was pushed onto the stack.
                assertEquals(2, stk.asByteArray().length);
                assertEquals(2, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: subB
         */
        for (byte x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (byte) x).collect(Collectors.toList()))
        {
            for (byte y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (byte) y).collect(Collectors.toList()))
            {
                final byte expected = (byte) (x - y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushB(x);
                stk.pushB(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.subB(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asByte()); // Verify the value that was pushed onto the stack.
                assertEquals(1, stk.asByteArray().length);
                assertEquals(1, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: subS
         */
        for (short x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (short) x).collect(Collectors.toList()))
        {
            for (short y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (short) y).collect(Collectors.toList()))
            {
                final short expected = (short) (x - y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushS(x);
                stk.pushS(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.subS(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asShort()); // Verify the value that was pushed onto the stack.
                assertEquals(2, stk.asByteArray().length);
                assertEquals(2, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: subI
         */
        for (int x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (int) x).collect(Collectors.toList()))
        {
            for (int y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (int) y).collect(Collectors.toList()))
            {
                final int expected = (int) (x - y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushI(x);
                stk.pushI(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.subI(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asInt()); // Verify the value that was pushed onto the stack.
                assertEquals(4, stk.asByteArray().length);
                assertEquals(4, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: subJ
         */
        for (long x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (long) x).collect(Collectors.toList()))
        {
            for (long y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (long) y).collect(Collectors.toList()))
            {
                final long expected = (long) (x - y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushJ(x);
                stk.pushJ(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.subJ(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asLong()); // Verify the value that was pushed onto the stack.
                assertEquals(8, stk.asByteArray().length);
                assertEquals(8, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: subF
         */
        for (float x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (float) x).collect(Collectors.toList()))
        {
            for (float y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (float) y).collect(Collectors.toList()))
            {
                final float expected = (float) (x - y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushF(x);
                stk.pushF(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.subF(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asFloat()); // Verify the value that was pushed onto the stack.
                assertEquals(4, stk.asByteArray().length);
                assertEquals(4, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }

        /**
         * Method: subD
         */
        for (double x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (double) x).collect(Collectors.toList()))
        {
            for (double y : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(y -> (double) y).collect(Collectors.toList()))
            {
                final double expected = (double) (x - y);
                assertEquals(0, object.anon().size()); // No operands are currently in the allocation-pool.
                stk.pushD(x);
                stk.pushD(y);
                assertEquals(2, object.anon().size()); // (2) operands currently in the allocation-pool.
                stk.subD(); // Method Under Test
                assertEquals(1, object.anon().size()); // (1) operand currently in the allocation-pool.
                assertTrue(expected == stk.asDouble()); // Verify the value that was pushed onto the stack.
                assertEquals(8, stk.asByteArray().length);
                assertEquals(8, stk.operandSize());
                assertEquals(1, stk.stackSize()); // Verify that the operation changed the size of the stack.
                stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
                assertEquals(0, object.anon().size()); // Verify that the operand was freed.
            }
        }
    }
}
