package com.mackenziehigh.cascade;

import static junit.framework.Assert.*;
import org.junit.Test;

/**
 * Unit Test.
 */
public final class CascadeStackTest
{
    /**
     * Test: 20180311170500978012
     *
     * <p>
     * Case: Push and Peek (boolean)
     * </p>
     */
    @Test
    public void test20180311170500978012 ()
    {
        System.out.println("Test: 20180311170500978012");

        CascadeStack stack = CascadeStack.newStack();

        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());

        stack = stack.pushBoolean(true);

        assertFalse(stack.isEmpty());
        assertEquals(1, stack.size());

        assertTrue(stack.peekAsBoolean());
        assertEquals((char) 1, stack.peekAsChar());
        assertEquals((byte) 1, stack.peekAsByte());
        assertEquals((short) 1, stack.peekAsShort());
        assertEquals((int) 1, stack.peekAsInt());
        assertEquals((long) 1, stack.peekAsLong());
        assertEquals((float) 1, stack.peekAsFloat());
        assertEquals((double) 1, stack.peekAsDouble());
        assertEquals(Boolean.TRUE, stack.peekAsObject());
        assertEquals(Boolean.TRUE, stack.peekAsObject(Boolean.class));
        assertEquals("true", stack.peekAsString());

        assertFalse(stack.isEmpty());
        assertEquals(1, stack.size());

        stack = stack.pop();

        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());

        stack = stack.pushBoolean(false);

        assertFalse(stack.peekAsBoolean());
        assertEquals((char) 0, stack.peekAsChar());
        assertEquals((byte) 0, stack.peekAsByte());
        assertEquals((short) 0, stack.peekAsShort());
        assertEquals((int) 0, stack.peekAsInt());
        assertEquals((long) 0, stack.peekAsLong());
        assertEquals((float) 0, stack.peekAsFloat());
        assertEquals((double) 0, stack.peekAsDouble());
        assertEquals(Boolean.FALSE, stack.peekAsObject());
        assertEquals(Boolean.FALSE, stack.peekAsObject(Boolean.class));
        assertEquals("false", stack.peekAsString());

        assertFalse(stack.isEmpty());
        assertEquals(1, stack.size());

        stack = stack.pop();

        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());
    }

    /**
     * Test: 20180311170500978085
     *
     * <p>
     * Case: Push and Peek (char)
     * </p>
     */
    @Test
    public void test20180311170500978085 ()
    {
        System.out.println("Test: 20180311170500978085");
        fail();
    }

    /**
     * Test: 20180311170500978113
     *
     * <p>
     * Case: Push and Peek (byte)
     * </p>
     */
    @Test
    public void test20180311170500978113 ()
    {
        System.out.println("Test: 20180311170500978113");

        CascadeStack stack = CascadeStack.newStack();

        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++)
        {
            final byte k = (byte) i;

            assertTrue(stack.isEmpty());
            assertEquals(0, stack.size());

            stack = stack.pushByte(k);

            assertFalse(stack.isEmpty());
            assertEquals(1, stack.size());

            assertEquals(k != 0, stack.peekAsBoolean());
            assertEquals((byte) i, stack.peekAsByte());
            assertEquals((short) i, stack.peekAsShort());
            assertEquals((int) i, stack.peekAsInt());
            assertEquals((long) i, stack.peekAsLong());
            assertEquals((float) i, stack.peekAsFloat());
            assertEquals((double) i, stack.peekAsDouble());
            assertEquals(k, stack.peekAsObject());
            assertEquals(Byte.valueOf(k), stack.peekAsObject(Byte.class));
            assertEquals(Byte.toString(k), stack.peekAsString());

            if (i >= 0 && i <= Character.MAX_VALUE)
            {
                assertEquals((char) i, stack.peekAsChar());
            }

            assertFalse(stack.isEmpty());
            assertEquals(1, stack.size());

            stack = stack.pop();
        }
    }

    /**
     * Test: 20180311170500978139
     *
     * <p>
     * Case: Push and Peek (short)
     * </p>
     */
    @Test
    public void test20180311170500978139 ()
    {
        System.out.println("Test: 20180311170500978139");

        CascadeStack stack = CascadeStack.newStack();

        for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++)
        {
            final short k = (short) i;

            assertTrue(stack.isEmpty());
            assertEquals(0, stack.size());

            stack = stack.pushShort(k);

            assertFalse(stack.isEmpty());
            assertEquals(1, stack.size());

            assertEquals(k != 0, stack.peekAsBoolean());
//            assertEquals((byte) i, stack.peekAsByte());
            assertEquals((short) i, stack.peekAsShort());
            assertEquals((int) i, stack.peekAsInt());
            assertEquals((long) i, stack.peekAsLong());
            assertEquals((float) i, stack.peekAsFloat());
            assertEquals((double) i, stack.peekAsDouble());
            assertEquals(k, stack.peekAsObject());
            assertEquals(Short.valueOf(k), stack.peekAsObject(Short.class));
            assertEquals(Short.toString(k), stack.peekAsString());

            if (i >= 0 && i <= Character.MAX_VALUE)
            {
                assertEquals((char) i, stack.peekAsChar());
            }

            assertFalse(stack.isEmpty());
            assertEquals(1, stack.size());

            stack = stack.pop();
        }
    }

    /**
     * Test: 20180311170500978163
     *
     * <p>
     * Case: Push and Peek (int)
     * </p>
     */
    @Test
    public void test20180311170500978163 ()
    {
        System.out.println("Test: 20180311170500978163");
        fail();
    }

    /**
     * Test: 20180311170500978187
     *
     * <p>
     * Case: Push and Peek (long)
     * </p>
     */
    @Test
    public void test20180311170500978187 ()
    {
        System.out.println("Test: 20180311170500978187");
        fail();
    }

    /**
     * Test: 20180311170500978210
     *
     * <p>
     * Case: Push and Peek (float)
     * </p>
     */
    @Test
    public void test20180311170500978210 ()
    {
        System.out.println("Test: 20180311170500978210");
        fail();
    }

    /**
     * Test: 20180311170500978232
     *
     * <p>
     * Case: Push and Peek (double)
     * </p>
     */
    @Test
    public void test20180311170500978232 ()
    {
        System.out.println("Test: 20180311170500978232");
        fail();
    }

    /**
     * Test: 20180311170500978255
     *
     * <p>
     * Method: <code></code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180311170500978255 ()
    {
        System.out.println("Test: 20180311170500978255");
        fail();
    }

    /**
     * Test: 20180311170500978277
     *
     * <p>
     * Method: <code></code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180311170500978277 ()
    {
        System.out.println("Test: 20180311170500978277");
        fail();
    }
}
