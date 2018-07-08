package com.mackenziehigh.internal.cascade;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.Input;
import com.mackenziehigh.cascade.Output;
import com.mackenziehigh.cascade.Reactor;
import static junit.framework.Assert.*;
import org.junit.Test;

/**
 *
 * TODO: Test pings
 */
public final class InternalReactionTest
{
    private final Reactor reactor = Cascade.newReactor();

    private final InternalReaction reaction = new InternalReaction(reactor);

    private final Input<String> input = reactor.newLinkedInput(String.class);

    private final Output<String> output = reactor.newOutput(String.class);

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
     * Case: Empty Input.
     * </p>
     */
    @Test
    public void test20180529201504798643 ()
    {
        reaction.require(input);

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
     * Case: Non-Empty Input.
     * </p>
     */
    @Test
    public void test20180529201504798668 ()
    {
        reaction.require(input);

        input.send("X");
        assertFalse(input.isEmpty());

        assertTrue(reaction.isReady());
    }
}
