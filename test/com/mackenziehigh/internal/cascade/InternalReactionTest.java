package com.mackenziehigh.internal.cascade;

import static junit.framework.Assert.*;
import org.junit.Test;

/**
 *
 * TODO: Test pings
 */
public final class InternalReactionTest
{
    private final MockReactor reactor = new MockReactor();

    private final InternalReaction reaction = new InternalReaction(reactor);

    private final InternalInput<String> input = new InternalInput<>(reactor, String.class).withCapacity(8).build();

    private final InternalOutput<String> output = new InternalOutput<>(reactor, String.class);

    /**
     * Test: 20180529201504798481
     *
     * <p>
     * Method: <code>name</code>
     * </p>
     *
     * <p>
     * Case: Default.
     * </p>
     */
    @Test
    public void test20180529201504798481 ()
    {
        assertEquals(reaction.uuid().toString(), reaction.name());
    }

    /**
     * Test: 20180529201504798589
     *
     * <p>
     * Method: <code>name</code>
     * </p>
     *
     * <p>
     * Case: After Assignment.
     * </p>
     */
    @Test
    public void test20180529201504798589 ()
    {
        reaction.named("Combustion");
        assertEquals("Combustion", reaction.name());
    }

    /**
     * Test: 20180529201504798616
     *
     * <p>
     * Method: <code>uuid</code>
     * </p>
     *
     * <p>
     * Case: Normal.
     * </p>
     */
    @Test
    public void test20180529201504798616 ()
    {
        assertNotNull(reaction.uuid());
        assertTrue(reaction.uuid() == reaction.uuid()); // Same Identity
    }

    /**
     * Test: 20180529201504798643
     *
     * <p>
     * Method: <code>require(Input)</code>
     * </p>
     *
     * <p>
     * Case: After build(), Empty Input.
     * </p>
     */
    @Test
    public void test20180529201504798643 ()
    {
        reaction.require(input).build();

        assertTrue(input.isEmpty());
        assertFalse(reaction.isReady());
    }

    /**
     * Test: 20180529201504798668
     *
     * <p>
     * Method: <code>require(Input)</code>
     * </p>
     *
     * <p>
     * Case: After build(), Non-Empty Input.
     * </p>
     */
    @Test
    public void test20180529201504798668 ()
    {
        reaction.require(input).build();

        input.send("X");
        assertFalse(input.isEmpty());

        assertTrue(reaction.isReady());
    }
}
