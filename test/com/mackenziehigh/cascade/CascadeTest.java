package com.mackenziehigh.cascade;

import static junit.framework.Assert.*;
import org.junit.Test;

/**
 * Unit Test.
 */
public final class CascadeTest
{
    private final Cascade cascade = Cascades.newCascade();

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

}
