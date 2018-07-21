package com.mackenziehigh.cascade;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.function.Supplier;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit Test.
 */
public final class MessageTest
{
    /**
     * Test: 20180715183233310372
     *
     * <p>
     * Case: Pushing and peeking booleans.
     * </p>
     */
    @Test
    public void test20180715183233310372 ()
    {
        System.out.println("Test: 20180715183233310372");

        Message msg = Message.create();
        assertEquals(0, msg.size());

        msg = msg.pushBoolean(true);
        assertEquals(1, msg.size());
        assertEquals(true, msg.peekAsBoolean());
        assertEquals((char) 1, msg.peekAsChar());
        assertEquals((byte) 1, msg.peekAsByte());
        assertEquals((short) 1, msg.peekAsShort());
        assertEquals((int) 1, msg.peekAsInt());
        assertEquals((long) 1, msg.peekAsLong());
        assertEquals((float) 1, msg.peekAsFloat(), 0.001);
        assertEquals((double) 1, msg.peekAsDouble(), 0.001);
        assertEquals("true", msg.peekAsString());
        assertEquals(Boolean.TRUE, msg.peekAsObject());
        assertEquals(Boolean.TRUE, msg.peekAsObject(Boolean.class));

        msg = msg.pushBoolean(false);
        assertEquals(2, msg.size());
        assertEquals(false, msg.peekAsBoolean());
        assertEquals((char) 0, msg.peekAsChar());
        assertEquals((byte) 0, msg.peekAsByte());
        assertEquals((short) 0, msg.peekAsShort());
        assertEquals((int) 0, msg.peekAsInt());
        assertEquals((long) 0, msg.peekAsLong());
        assertEquals((float) 0, msg.peekAsFloat(), 0.001);
        assertEquals((double) 0, msg.peekAsDouble(), 0.001);
        assertEquals("false", msg.peekAsString());
        assertEquals(Boolean.FALSE, msg.peekAsObject());
        assertEquals(Boolean.FALSE, msg.peekAsObject(Boolean.class));
    }

    private <T> void checkPeek (final T expected,
                                final Supplier<T> actual,
                                final boolean inrange)
    {
        try
        {
            if (expected instanceof Float)
            {
                assertEquals((Float) expected, (Float) actual.get(), 0.001);
            }
            else if (expected instanceof Double)
            {
                assertEquals((Double) expected, (Double) actual.get(), 0.001);
            }
            else
            {
                assertEquals(expected, actual.get());
            }
        }
        catch (IllegalStateException ex)
        {
            if (inrange)
            {
                fail();
            }
        }
    }

    /**
     * Test: 20180715183233310453
     *
     * <p>
     * Case: Pushing and peeking chars.
     * </p>
     */
    @Test
    public void test20180715183233310453 ()
    {
        System.out.println("Test: 20180715183233310453");

        for (char x = Character.MIN_VALUE; x < Character.MAX_VALUE; x++)
        {
            final Message msg = Message.create().pushChar(x);
            assertEquals(1, msg.size());

            assertEquals(x != 0, msg.peekAsBoolean());

            checkPeek((char) x, () -> msg.peekAsChar(), Character.MIN_VALUE <= x && x <= Character.MAX_VALUE);
            checkPeek((byte) x, () -> msg.peekAsByte(), Byte.MIN_VALUE <= x && x <= Byte.MAX_VALUE);
            checkPeek((short) x, () -> msg.peekAsShort(), Short.MIN_VALUE <= x && x <= Short.MAX_VALUE);
            checkPeek((int) x, () -> msg.peekAsInt(), Integer.MIN_VALUE <= x && x <= Integer.MAX_VALUE);
            checkPeek((long) x, () -> msg.peekAsLong(), Long.MIN_VALUE <= x && x <= Long.MAX_VALUE);
            checkPeek((float) x, () -> msg.peekAsFloat(), Float.MIN_VALUE <= x && x <= Float.MAX_VALUE);
            checkPeek((double) x, () -> msg.peekAsDouble(), Double.MIN_VALUE <= x && x <= Double.MAX_VALUE);

            assertEquals(Character.toString(x), msg.peekAsString());
            assertEquals((Character) x, msg.peekAsObject());
            assertEquals((Character) x, msg.peekAsObject(Character.class));
        }
    }

    /**
     * Test: 20180715183233310481
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
    public void test20180715183233310481 ()
    {
        System.out.println("Test: 20180715183233310481");

        for (byte x = Byte.MIN_VALUE; x < Byte.MAX_VALUE; x++)
        {
            final Message msg = Message.create().pushByte(x);
            assertEquals(1, msg.size());

            assertEquals(x != 0, msg.peekAsBoolean());

            checkPeek((char) x, () -> msg.peekAsChar(), Character.MIN_VALUE <= x && x <= Character.MAX_VALUE);
            checkPeek((byte) x, () -> msg.peekAsByte(), Byte.MIN_VALUE <= x && x <= Byte.MAX_VALUE);
            checkPeek((short) x, () -> msg.peekAsShort(), Short.MIN_VALUE <= x && x <= Short.MAX_VALUE);
            checkPeek((int) x, () -> msg.peekAsInt(), Integer.MIN_VALUE <= x && x <= Integer.MAX_VALUE);
            checkPeek((long) x, () -> msg.peekAsLong(), Long.MIN_VALUE <= x && x <= Long.MAX_VALUE);
            checkPeek((float) x, () -> msg.peekAsFloat(), Float.MIN_VALUE <= x && x <= Float.MAX_VALUE);
            checkPeek((double) x, () -> msg.peekAsDouble(), Double.MIN_VALUE <= x && x <= Double.MAX_VALUE);

            assertEquals(Byte.toString(x), msg.peekAsString());
            assertEquals((Byte) x, msg.peekAsObject());
            assertEquals((Byte) x, msg.peekAsObject(Byte.class));
        }
    }

    /**
     * Test: 20180715183233310506
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
    public void test20180715183233310506 ()
    {
        System.out.println("Test: 20180715183233310506");

        for (short x = Short.MIN_VALUE; x < Short.MAX_VALUE; x++)
        {
            final Message msg = Message.create().pushShort(x);
            assertEquals(1, msg.size());

            assertEquals(x != 0, msg.peekAsBoolean());

            checkPeek((char) x, () -> msg.peekAsChar(), Character.MIN_VALUE <= x && x <= Character.MAX_VALUE);
            checkPeek((byte) x, () -> msg.peekAsByte(), Byte.MIN_VALUE <= x && x <= Byte.MAX_VALUE);
            checkPeek((short) x, () -> msg.peekAsShort(), Short.MIN_VALUE <= x && x <= Short.MAX_VALUE);
            checkPeek((int) x, () -> msg.peekAsInt(), Integer.MIN_VALUE <= x && x <= Integer.MAX_VALUE);
            checkPeek((long) x, () -> msg.peekAsLong(), Long.MIN_VALUE <= x && x <= Long.MAX_VALUE);
            checkPeek((float) x, () -> msg.peekAsFloat(), Float.MIN_VALUE <= x && x <= Float.MAX_VALUE);
            checkPeek((double) x, () -> msg.peekAsDouble(), Double.MIN_VALUE <= x && x <= Double.MAX_VALUE);

            assertEquals(Short.toString(x), msg.peekAsString());
            assertEquals((Short) x, msg.peekAsObject());
            assertEquals((Short) x, msg.peekAsObject(Short.class));
        }
    }

    /**
     * Test: 20180715183233310536
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
    public void test20180715183233310536 ()
    {
        System.out.println("Test: 20180715183233310536");

        for (Integer element : Lists.newArrayList(Integer.MIN_VALUE, Integer.MAX_VALUE, -3, -2, -1, 0, 1, 2, 3))
        {
            final int x = (int) element;
            final Message msg = Message.create().pushInt(x);
            assertEquals(1, msg.size());

            assertEquals(x != 0, msg.peekAsBoolean());

            checkPeek((char) x, () -> msg.peekAsChar(), Character.MIN_VALUE <= x && x <= Character.MAX_VALUE);
            checkPeek((byte) x, () -> msg.peekAsByte(), Byte.MIN_VALUE <= x && x <= Byte.MAX_VALUE);
            checkPeek((short) x, () -> msg.peekAsShort(), Short.MIN_VALUE <= x && x <= Short.MAX_VALUE);
            checkPeek((int) x, () -> msg.peekAsInt(), Integer.MIN_VALUE <= x && x <= Integer.MAX_VALUE);
            checkPeek((long) x, () -> msg.peekAsLong(), Long.MIN_VALUE <= x && x <= Long.MAX_VALUE);
            checkPeek((float) x, () -> msg.peekAsFloat(), Float.MIN_VALUE <= x && x <= Float.MAX_VALUE);
            checkPeek((double) x, () -> msg.peekAsDouble(), Double.MIN_VALUE <= x && x <= Double.MAX_VALUE);

            assertEquals(Integer.toString(x), msg.peekAsString());
            assertEquals((Integer) x, msg.peekAsObject());
            assertEquals((Integer) x, msg.peekAsObject(Integer.class));
        }
    }

    /**
     * Test: 20180715183233310561
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
    public void test20180715183233310561 ()
    {
        System.out.println("Test: 20180715183233310561");

        for (Long element : Lists.newArrayList(Long.MIN_VALUE, Long.MAX_VALUE, -3L, -2L, -1L, 0L, 1L, 2L, 3L))
        {
            final long x = (long) element;
            final Message msg = Message.create().pushLong(x);
            assertEquals(1, msg.size());

            assertEquals(x != 0, msg.peekAsBoolean());

            checkPeek((char) x, () -> msg.peekAsChar(), Character.MIN_VALUE <= x && x <= Character.MAX_VALUE);
            checkPeek((byte) x, () -> msg.peekAsByte(), Byte.MIN_VALUE <= x && x <= Byte.MAX_VALUE);
            checkPeek((short) x, () -> msg.peekAsShort(), Short.MIN_VALUE <= x && x <= Short.MAX_VALUE);
            checkPeek((int) x, () -> msg.peekAsInt(), Integer.MIN_VALUE <= x && x <= Integer.MAX_VALUE);
            checkPeek((long) x, () -> msg.peekAsLong(), Long.MIN_VALUE <= x && x <= Long.MAX_VALUE);
            checkPeek((float) x, () -> msg.peekAsFloat(), Float.MIN_VALUE <= x && x <= Float.MAX_VALUE);
            checkPeek((double) x, () -> msg.peekAsDouble(), Double.MIN_VALUE <= x && x <= Double.MAX_VALUE);

            assertEquals(Long.toString(x), msg.peekAsString());
            assertEquals((Long) x, msg.peekAsObject());
            assertEquals((Long) x, msg.peekAsObject(Long.class));
        }
    }

    /**
     * Test: 20180715183233310584
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
    public void test20180715183233310584 ()
    {
        System.out.println("Test: 20180715183233310584");

        for (Float element : ImmutableList.<Float>of(Float.MIN_VALUE, Float.MAX_VALUE, -3.0F, -2.0F, -1.0F, 0.0F, 1.0F, 2.0F, 3.0F))
        {
            final float x = (float) element;
            final Message msg = Message.create().pushFloat(x);
            assertEquals(1, msg.size());

            assertEquals(((int) x) != 0, msg.peekAsBoolean());

            checkPeek((char) x, () -> msg.peekAsChar(), Character.MIN_VALUE <= x && x <= Character.MAX_VALUE);
            checkPeek((byte) x, () -> msg.peekAsByte(), Byte.MIN_VALUE <= x && x <= Byte.MAX_VALUE);
            checkPeek((short) x, () -> msg.peekAsShort(), Short.MIN_VALUE <= x && x <= Short.MAX_VALUE);
            checkPeek((int) x, () -> msg.peekAsInt(), Integer.MIN_VALUE <= x && x <= Integer.MAX_VALUE);
            checkPeek((long) x, () -> msg.peekAsLong(), Long.MIN_VALUE <= x && x <= Long.MAX_VALUE);
            checkPeek((float) x, () -> msg.peekAsFloat(), Float.MIN_VALUE <= x && x <= Float.MAX_VALUE);
            checkPeek((double) x, () -> msg.peekAsDouble(), Double.MIN_VALUE <= x && x <= Double.MAX_VALUE);

            assertEquals(Float.toString(x), msg.peekAsString());
            assertEquals((Float) x, msg.peekAsObject());
            assertEquals((Float) x, msg.peekAsObject(Float.class));
        }
    }

    /**
     * Test: 20180715183233310607
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
    public void test20180715183233310607 ()
    {
        System.out.println("Test: 20180715183233310607");
        fail();
    }

    /**
     * Test: 20180715183233310634
     *
     * <p>
     * Method: <code>peekObject</code>
     * </p>
     *
     * <p>
     * Case: Wrong Type.
     * </p>
     */
    @Test (expected = ClassCastException.class)
    public void test20180715183233310634 ()
    {
        System.out.println("Test: 20180715183233310634");

        Message.create().pushObject("Vulcan").peekAsObject(Integer.class);
    }

    /**
     * Test: 20180715183233310657
     *
     * <p>
     * Method: <code>type</code>
     * </p>
     *
     * <p>
     * Case: All Cases.
     * </p>
     */
    @Test
    public void test20180715183233310657 ()
    {
        System.out.println("Test: 20180715183233310657");

        /**
         * Case: Empty Stack
         */
        assertEquals(void.class, Message.create().type());

        /**
         * Case: Primitive Types
         */
        assertEquals(boolean.class, Message.create().pushBoolean(true).type());
        assertEquals(char.class, Message.create().pushChar((char) 1).type());
        assertEquals(byte.class, Message.create().pushByte((byte) 1).type());
        assertEquals(short.class, Message.create().pushShort((short) 1).type());
        assertEquals(int.class, Message.create().pushInt((int) 1).type());
        assertEquals(long.class, Message.create().pushLong((long) 1).type());
        assertEquals(float.class, Message.create().pushFloat((float) 1).type());
        assertEquals(double.class, Message.create().pushDouble((double) 1).type());

        /**
         * Case: Boxed Types (auto-converted to primitives)
         */
        assertEquals(boolean.class, Message.create().pushObject(Boolean.TRUE).type());
        assertEquals(char.class, Message.create().pushChar((char) 1).type());
        assertEquals(byte.class, Message.create().pushByte((byte) 1).type());
        assertEquals(short.class, Message.create().pushShort((short) 1).type());
        assertEquals(int.class, Message.create().pushInt((int) 1).type());
        assertEquals(long.class, Message.create().pushLong((long) 1).type());
        assertEquals(float.class, Message.create().pushFloat((float) 1).type());
        assertEquals(double.class, Message.create().pushDouble((double) 1).type());

        /**
         * Case: Objects
         */
        assertEquals(String.class, Message.create().pushObject("Mars").type());
        assertEquals(ArrayList.class, Message.create().pushObject(Lists.newArrayList()).type());
    }

    /**
     * Test: 20180715204204703812
     *
     * <p>
     * Method: <code>dup()</code>
     * </p>
     *
     * <p>
     * Case: All Cases.
     * </p>
     */
    @Test
    public void test20180715204204703812 ()
    {
        System.out.println("Test: 20180715204204703812");

        Message p;

        /**
         * Case: Empty Stack.
         */
        p = Message.create().dup();
        assertEquals(0, p.size());

        /**
         * Case: One Element.
         */
        p = Message.create().pushObject("A").dup();
        assertEquals(2, p.size());
        assertEquals(Lists.newArrayList("A", "A"), Lists.newArrayList(p.toDeque()));

        /**
         * Case: Two Elements.
         */
        p = Message.create().pushObject("A").pushObject("B").dup();
        assertEquals(3, p.size());
        assertEquals(Lists.newArrayList("B", "B", "A"), Lists.newArrayList(p.toDeque()));

        /**
         * Case: Three or More Elements.
         */
        p = Message.create().pushObject("A").pushObject("B").pushObject("C").dup();
        assertEquals(4, p.size());
        assertEquals(Lists.newArrayList("C", "C", "B", "A"), Lists.newArrayList(p.toDeque()));
    }

    /**
     * Test: 20180715204204703882
     *
     * <p>
     * Method: <code>dup(int)</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180715204204703882 ()
    {
        System.out.println("Test: 20180715204204703882");
        fail();
    }

    /**
     * Test: 20180715204204703910
     *
     * <p>
     * Method: <code>swap</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180715204204703910 ()
    {
        System.out.println("Test: 20180715204204703910");
        fail();
    }

    /**
     * Test: 20180715204204703935
     *
     * <p>
     * Method: <code>equals</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180715204204703935 ()
    {
        System.out.println("Test: 20180715204204703935");
        fail();
    }

    /**
     * Test: 20180715204204703962
     *
     * <p>
     * Method: <code>hashCode</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180715204204703962 ()
    {
        System.out.println("Test: 20180715204204703962");
        fail();
    }

    /**
     * Test: 20180715205122005741
     *
     * <p>
     * Method: <code>toString</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180715205122005741 ()
    {
        System.out.println("Test: 20180715205122005741");
        fail();
    }

    /**
     * Test: 20180715205122005801
     *
     * <p>
     * Method: <code>forEach</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180715205122005801 ()
    {
        System.out.println("Test: 20180715205122005801");
        fail();
    }

    /**
     * Test: 20180715205122005822
     *
     * <p>
     * Method: <code>toDeque</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180715205122005822 ()
    {
        System.out.println("Test: 20180715205122005822");
        fail();
    }

    /**
     * Test: 20180715205356307954
     *
     * <p>
     * Method: <code>iterator</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180715205356307954 ()
    {
        System.out.println("Test: 20180715205356307954");
        fail();
    }

    /**
     * Test: 20180715205441853029
     *
     * <p>
     * Method: <code>rebase</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180715205441853029 ()
    {
        System.out.println("Test: 20180715205441853029");
        fail();
    }

    /**
     * Test: 20180715205544205871
     *
     * <p>
     * Method: <code>pushTop</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180715205544205871 ()
    {
        System.out.println("Test: 20180715205544205871");
        fail();
    }
}
