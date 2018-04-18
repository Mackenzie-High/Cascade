package com.mackenziehigh.cascade;

import static junit.framework.Assert.*;
import org.junit.Test;

/**
 * Unit Test.
 */
public final class CascadeTest
{
    private final Cascade cascade = Cascade.newCascade();

    @Test
    public void testInitialState ()
    {
        assertTrue(cascade.isActive());
        assertFalse(cascade.isClosing());
        assertFalse(cascade.isClosed());
        assertTrue(cascade.stages().isEmpty());

        /**
         * The value should not change across invocations.
         */
        assertEquals(cascade.uuid(), cascade.uuid());
    }

    /**
     * Test: 20180415205229762235
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180415205229762235 ()
    {
        System.out.println("Test: 20180415205229762235");
        fail();
    }

    /**
     * Test: 20180415205229762344
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
    public void test20180415205229762344 ()
    {
        System.out.println("Test: 20180415205229762344");
        fail();
    }

    /**
     * Test: 20180415205229762374
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
    public void test20180415205229762374 ()
    {
        System.out.println("Test: 20180415205229762374");
        fail();
    }

    /**
     * Test: 20180415205229762401
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
    public void test20180415205229762401 ()
    {
        System.out.println("Test: 20180415205229762401");
        fail();
    }

    /**
     * Test: 20180415205229762429
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
    public void test20180415205229762429 ()
    {
        System.out.println("Test: 20180415205229762429");
        fail();
    }

}
