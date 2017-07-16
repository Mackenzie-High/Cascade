package com.mackenziehigh.loader;

import java.util.UUID;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit Test.
 */
public class TopicKeyTest
{
    private String random ()
    {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Test: 20170614191519793805
     *
     * <p>
     * Method: <code>get(String)</code>
     * </p>
     *
     * <p>
     * Case: normal
     * </p>
     */
    @Test
    public void test20170614191519793805 ()
    {
        System.out.println("Test: 20170614191519793805");

        // Identity Equality
        assertTrue(TopicKey.get("X") == TopicKey.get("X"));

        // Case Sensitivity
        assertNotEquals(TopicKey.get("X"), TopicKey.get("x"));

        // Valid Identifier Characters
        final String name = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz._$0123456789";
        assertEquals(TopicKey.get(name), TopicKey.get(name));
    }

    /**
     * Test: 20170614191519793899
     *
     * <p>
     * Method: <code>get(String)</code>
     * </p>
     *
     * <p>
     * Case: null
     * </p>
     */
    @Test (expected = NullPointerException.class)
    public void test20170614191519793899 ()
    {
        System.out.println("Test: 20170614191519793899");
        TopicKey.get(null);
    }

    /**
     * Test: 20170614191519793927
     *
     * <p>
     * Method: <code>name()</code>
     * </p>
     *
     * <p>
     * Case: normal
     * </p>
     */
    @Test
    public void test20170614191519793927 ()
    {
        System.out.println("Test: 20170614191519793927");
        assertEquals("X", TopicKey.get("X").name());
    }

    /**
     * Test: 20170614191519793954
     *
     * <p>
     * Method: <code>toString()</code>
     * </p>
     *
     * <p>
     * Case: normal
     * </p>
     */
    @Test
    public void test20170614191519793954 ()
    {
        System.out.println("Test: 20170614191519793954");
        assertEquals("X", TopicKey.get("X").toString());
    }

    /**
     * Test: 20170614191519793975
     *
     * <p>
     * Method: <code>get(String)</code>
     * </p>
     *
     * <p>
     * Case: invalid characters
     * </p>
     */
    @Test (expected = IllegalArgumentException.class)
    public void test20170614191519793975 ()
    {
        System.out.println("Test: 20170614191519793975");
        TopicKey.get("@123");
    }

    /**
     * Test: 20170614194847007565
     *
     * <p>
     * Method: <code>equals(Object)</code>
     * </p>
     *
     * <p>
     * Case: normal
     * </p>
     */
    @Test
    public void test20170614194847007565 ()
    {
        System.out.println("Test: 20170614194847007565");

        final TopicKey key1 = TopicKey.get(random());
        final TopicKey key2 = TopicKey.get(random());
        final TopicKey key3 = TopicKey.get(random());

        assertTrue(key1.hashCode() == key2.hashCode() - 1);
        assertTrue(key1.hashCode() == key3.hashCode() - 2);
    }

    /**
     * Test: 20170614194847007654
     *
     * <p>
     * Method: <code>hashCode()</code>
     * </p>
     *
     * <p>
     * Case: normal
     * </p>
     */
    @Test
    public void test20170614194847007654 ()
    {
        System.out.println("Test: 20170614194847007654");

        assertEquals(TopicKey.get("X").hashCode(), TopicKey.get("X").hashCode());
    }

    /**
     * Test: 20170614194847007755
     *
     * <p>
     * Method: <code>compareTo(TopicKey)</code>
     * </p>
     *
     * <p>
     * Case: normal
     * </p>
     */
    @Test
    public void test20170614194847007755 ()
    {
        System.out.println("Test: 20170614194847007755");

        final TopicKey key1 = TopicKey.get(random());
        final TopicKey key2 = TopicKey.get(random());

        assertTrue(key1.compareTo(key2) < 0); // less
        assertTrue(key1.compareTo(key1) == 0); // equals
        assertTrue(key2.compareTo(key1) > 0); // greater
    }

    /**
     * Test: 20170614194847007801
     *
     * <p>
     * Method: <code>get(String)</code>
     * </p>
     *
     * <p>
     * Case: empty name
     * </p>
     */
    @Test (expected = IllegalArgumentException.class)
    public void test20170614194847007801 ()
    {
        System.out.println("Test: 20170614194847007801");
        final String empty = "";
        TopicKey.get(empty);
    }
}
