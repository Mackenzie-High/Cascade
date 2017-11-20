package com.mackenziehigh.cascade.internal.messages;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.mackenziehigh.cascade.CascadeAllocator.AllocationPool;
import com.mackenziehigh.cascade.CascadeAllocator.OperandArray;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.internal.messages.ConcreteAllocator.FixedAllocationPool;
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
        allocator.addFixedPool(ConcreteAllocator.ANON, 0, 128, 8);
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
     * Test: 20171105205239920186
     *
     *
     * <p>
     * Case: Normal cases for the OperandStack methods.
     * </p>
     */
    @Test
    public void test20171105205239920186 ()
    {
        System.out.println("Test: 20171105205239920186");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        allocator.addFixedPool(ConcreteAllocator.ANON, 0, 128, 8);
        final FixedAllocationPool pool = (FixedAllocationPool) allocator.anon();
        final OperandStack stk = allocator.newOperandStack();

        /**
         * Method: pushZ
         */
        for (byte x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (byte) x).collect(Collectors.toList()))
        {
            assertEquals(0, pool.size()); // No operands are currently in the allocation-pool.
            stk.push(x != 0); // Method Under Test
            assertEquals(1, pool.size()); // (1) operand currently in the allocation-pool.
            assertTrue((x != 0) == stk.asBoolean()); // Verify the value that was pushed onto the stack.
            assertEquals(1, stk.asByteArray().length);
            assertEquals(1, stk.operandSize());
            assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
            stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
            assertEquals(0, pool.size()); // Verify that the operand was freed.
        }

        /**
         * Method: pushB
         */
        for (byte x : IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE).mapToObj(x -> (byte) x).collect(Collectors.toList()))
        {
            assertEquals(0, pool.size()); // No operands are currently in the allocation-pool.
            stk.push(x); // Method Under Test
            assertEquals(1, pool.size()); // (1) operand currently in the allocation-pool.
            assertTrue(x == stk.asByte()); // Verify the value that was pushed onto the stack.
            assertEquals(1, stk.asByteArray().length);
            assertEquals(1, stk.operandSize());
            assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
            stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
            assertEquals(0, pool.size()); // Verify that the operand was freed.
        }

        /**
         * Method: pushS
         */
        for (short x : IntStream.range(Short.MIN_VALUE, Short.MAX_VALUE).mapToObj(x -> (short) x).collect(Collectors.toList()))
        {
            assertEquals(0, pool.size()); // No operands are currently in the allocation-pool.
            stk.push(x); // Method Under Test
            assertEquals(1, pool.size()); // (1) operand currently in the allocation-pool.
            assertTrue(x == stk.asShort()); // Verify the value that was pushed onto the stack.
            assertEquals(2, stk.asByteArray().length);
            assertEquals(2, stk.operandSize());
            assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
            stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
            assertEquals(0, pool.size()); // Verify that the operand was freed.
        }

        /**
         * Method: pushI
         */
        for (int x : IntStream.range(Integer.MAX_VALUE - 1000, Integer.MIN_VALUE + 1000).mapToObj(x -> (short) x).collect(Collectors.toList()))
        {
            assertEquals(0, pool.size()); // No operands are currently in the allocation-pool.
            stk.push(x); // Method Under Test
            assertEquals(1, pool.size()); // (1) operand currently in the allocation-pool.
            assertTrue(x == stk.asInt()); // Verify the value that was pushed onto the stack.
            assertEquals(4, stk.asByteArray().length);
            assertEquals(4, stk.operandSize());
            assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
            stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
            assertEquals(0, pool.size()); // Verify that the operand was freed.
        }

        /**
         * Method: pushJ
         */
        for (long x : LongStream.range(Long.MAX_VALUE - 1000, Long.MIN_VALUE + 1000).mapToObj(x -> (short) x).collect(Collectors.toList()))
        {
            assertEquals(0, pool.size()); // No operands are currently in the allocation-pool.
            stk.push(x); // Method Under Test
            assertEquals(1, pool.size()); // (1) operand currently in the allocation-pool.
            assertTrue(x == stk.asLong()); // Verify the value that was pushed onto the stack.
            assertEquals(8, stk.asByteArray().length);
            assertEquals(8, stk.operandSize());
            assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
            stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
            assertEquals(0, pool.size()); // Verify that the operand was freed.
        }

        /**
         * Method: pushF.
         *
         * Note: NaN == NaN, will always return false, per IEEE-754 specification.
         */
        for (float x : ImmutableList.of(Float.MAX_VALUE, Float.MIN_VALUE, -3.0F, -2.0F, -1.0F, 0.0F, 1.0F, 2.0F, 3.0F, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NaN))
        {
            assertEquals(0, pool.size()); // No operands are currently in the allocation-pool.
            stk.push(x); // Method Under Test
            assertEquals(1, pool.size()); // (1) operand currently in the allocation-pool.
            assertTrue(Float.floatToIntBits(x) == Float.floatToIntBits(stk.asFloat())); // Verify the value that was pushed onto the stack.
            assertEquals(x, (Float) stk.asFloat());
            assertEquals(4, stk.asByteArray().length);
            assertEquals(4, stk.operandSize());
            assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
            stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
            assertEquals(0, pool.size()); // Verify that the operand was freed.
        }

        /**
         * Method: pushD.
         *
         * Note: NaN == NaN, will always return false, per IEEE-754 specification.
         */
        for (double x : ImmutableList.of(Double.MAX_VALUE, Double.MIN_VALUE, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NaN))
        {
            assertEquals(0, pool.size()); // No operands are currently in the allocation-pool.
            stk.push(x); // Method Under Test
            assertEquals(1, pool.size()); // (1) operand currently in the allocation-pool.
            assertTrue(Double.doubleToLongBits(x) == Double.doubleToLongBits(stk.asDouble())); // Verify the value that was pushed onto the stack.
            assertEquals(x, (Double) stk.asDouble());
            assertEquals(8, stk.asByteArray().length);
            assertEquals(8, stk.operandSize());
            assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
            stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
            assertEquals(0, pool.size()); // Verify that the operand was freed.
        }

        /**
         * Method: push(OperandStack)
         */
        Verify.verify(true);

        /**
         * Method: pushStr
         */
        assertEquals(0, pool.size()); // No operands are currently in the allocation-pool.
        stk.push("Vulcan"); // Method Under Test
        assertEquals(1, pool.size()); // (1) operand currently in the allocation-pool.
        assertEquals("Vulcan", stk.asString()); // Verify the value that was pushed onto the stack.
        assertEquals(6, stk.asByteArray().length);
        assertEquals(6, stk.operandSize());
        assertEquals(1, stk.stackSize()); // Verify that the push changed the size of the stack.
        stk.pop(); // Pop the value off the stack *and* implicitly cause it to be freed.
        assertEquals(0, pool.size()); // Verify that the operand was freed.

        /**
         * Method: asBoolean()
         */
        testThrow(NullPointerException.class, x -> x.push((OperandStack) null));
        Verify.verify(true);

        /**
         * Method: asByte()
         */
        Verify.verify(true);

        /**
         * Method: asShort()
         */
        Verify.verify(true);

        /**
         * Method: asInt()
         */
        Verify.verify(true);

        /**
         * Method: asLong()
         */
        Verify.verify(true);

        /**
         * Method: asFloat()
         */
        Verify.verify(true);

        /**
         * Method: asDouble()
         */
        Verify.verify(true);

        /**
         * Method: asString()
         */
        Verify.verify(true);

        /**
         * Method: asByteArray()
         */
        Verify.verify(true);

        /**
         * Method: stackSize()
         */
        Verify.verify(true);

        /**
         * Method: operandSize()
         */
        Verify.verify(true);

        /**
         * Method: operandCapacity()
         */
        Verify.verify(true);

        /**
         * Method: isStackEmpty()
         */
        Verify.verify(true);

        /**
         * Method: isOperandEmpty()
         */
        Verify.verify(true);

        /**
         * Method: pop()
         */
        Verify.verify(true);

        /**
         * Method: set(OperandStack)
         */
        Verify.verify(true);

        /**
         * Method: set(int, OperandArray)
         */
        Verify.verify(true);

        /**
         * Method: byteAt()
         */
        Verify.verify(true);

        /**
         * Method: copyTo(byte[])
         */
        Verify.verify(true);

        /**
         * Method: copyTo(byte[], int)
         */
        Verify.verify(true);

        /**
         * Method: copyTo(byte[], int, int)
         */
        Verify.verify(true);

        /**
         * Method: clear()
         */
        Verify.verify(true);

        /**
         * Method: copy()
         */
        Verify.verify(true);

        /**
         * Method: close()
         */
        Verify.verify(true);

        /**
         * Method: allocator()
         */
        Verify.verify(true);

        /**
         * Method: pool()
         */
        Verify.verify(true);
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
        allocator.addFixedPool(ConcreteAllocator.ANON, 0, 128, 8);

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
         * Method: asString()
         */
        Verify.verify(true);

        /**
         * Method: pop()
         */
        testThrow(IllegalStateException.class, x -> x.pop()); // pop() from empty stack.

        /**
         * Method: set(OperandStack)
         */
        testThrow(IllegalArgumentException.class, x -> x.set(new ConcreteAllocator().newOperandStack()));

        /**
         * Method: set(int, OperandArray)
         */
        final OperandArray array1 = allocator.newOperandArray(8);
        final OperandArray array2 = new ConcreteAllocator().newOperandArray(8);
        testThrow(NullPointerException.class, x -> x.set((OperandArray) null, 0));
        testThrow(IndexOutOfBoundsException.class, x -> x.set(array1, -1)); // index too small
        testThrow(IndexOutOfBoundsException.class, x -> x.set(array1, 8)); // index too large
        testThrow(IndexOutOfBoundsException.class, x -> x.set(array1, 9)); // index too large
        testThrow(IllegalArgumentException.class, x -> x.set(array2, 0)); // wrong allocator

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
        final AllocationPool pool1 = allocator.addDynamicPool(ConcreteAllocator.ANON, 3, 6);
        assertEquals(ConcreteAllocator.ANON, pool1.name());
        assertEquals(3, pool1.minimumAllocationSize());
        assertEquals(6, pool1.maximumAllocationSize());
        assertTrue(pool1.isAnon());
        assertFalse(pool1.isPreallocated());

        // Must Allow Exact Size
        // Must Allow Unique Name
        final AllocationPool pool2 = allocator.addDynamicPool("Cities", 7, 7);
        assertEquals("Cities", pool2.name());
        assertEquals(7, pool2.minimumAllocationSize());
        assertEquals(7, pool2.maximumAllocationSize());
        assertFalse(pool2.isAnon());
        assertFalse(pool2.isPreallocated());

        assertEquals(pool1, allocator.anon());
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
        final AllocationPool pool1 = allocator.addFixedPool(ConcreteAllocator.ANON, 3, 6, 8);
        assertEquals(ConcreteAllocator.ANON, pool1.name());
        assertEquals(3, pool1.minimumAllocationSize());
        assertEquals(6, pool1.maximumAllocationSize());
        assertTrue(pool1.isAnon());
        assertTrue(pool1.isPreallocated());

        // Must Allow Exact Size
        // Must Allow Unique Name
        final AllocationPool pool2 = allocator.addFixedPool("Cities", 7, 7, 16);
        assertEquals("Cities", pool2.name());
        assertEquals(7, pool2.minimumAllocationSize());
        assertEquals(7, pool2.maximumAllocationSize());
        assertFalse(pool2.isAnon());
        assertTrue(pool2.isPreallocated());

        assertEquals(pool1, allocator.anon());
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
        final AllocationPool pool1 = allocator.addCompositePool(ConcreteAllocator.ANON, null, delegates);
        assertEquals(ConcreteAllocator.ANON, pool1.name());
        assertEquals(101, pool1.minimumAllocationSize());
        assertEquals(300, pool1.maximumAllocationSize());
        assertTrue(pool1.isAnon());
        assertFalse(pool1.isPreallocated());

        // Fallback Present
        // Must Allow Unique Name
        final AllocationPool pool2 = allocator.addCompositePool("Cities", fallback, delegates);
        assertEquals("Cities", pool2.name());
        assertEquals(50, pool2.minimumAllocationSize());
        assertEquals(375, pool2.maximumAllocationSize());
        assertFalse(pool2.isAnon());
        assertFalse(pool2.isPreallocated());

        assertEquals(pool1, allocator.anon());
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
        final AllocationPool pool1 = allocator.addFixedPool(ConcreteAllocator.ANON, 2, 4, 2);
        final AllocationPool pool2 = allocator.addFixedPool("Cities", 2, 6, 2);

        assertEquals(2, allocator.pools().size());
        assertTrue(allocator.pools() == allocator.pools()); // Exact Same Object
        assertEquals(pool1, allocator.pools().get(ConcreteAllocator.ANON));
        assertEquals(pool2, allocator.pools().get("Cities"));
    }

    /**
     * Test: 20171120052958992560
     *
     * <p>
     * Method: <code>anon()</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171120052958992560 ()
    {
        System.out.println("Test: 20171120052958992560");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final AllocationPool pool = allocator.addFixedPool(ConcreteAllocator.ANON, 2, 4, 2);

        // Exact Same Object
        assertTrue(pool == allocator.anon());
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
        final AllocationPool pool = allocator.addFixedPool(ConcreteAllocator.ANON, 2, 4, 2);
        final OperandStack stack = allocator.newOperandStack();

        assertTrue(stack.isStackEmpty());
        assertEquals(0, stack.operandSize());
        assertEquals(0, stack.operandCapacity());
        assertEquals(allocator, stack.allocator());
        assertEquals(allocator.anon(), stack.pool());
        assertEquals(pool, stack.pool());
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
}
