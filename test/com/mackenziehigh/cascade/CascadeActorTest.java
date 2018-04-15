package com.mackenziehigh.cascade;

import static junit.framework.Assert.*;
import org.junit.Test;

/**
 * Unit Test.
 */
public final class CascadeActorTest
{
    private final Cascade cascade = Cascade.newCascade();

    private final CascadeStage stage = cascade.newStage();

    private final CascadeActor actor = stage.newActor();

    @Test
    public void testInitialState ()
    {
        assertFalse(actor.isStarting());
        assertFalse(actor.isStarted());
        assertFalse(actor.isActive());
        assertFalse(actor.isActing());
        assertFalse(actor.isClosing());
        assertFalse(actor.isClosed());

        assertFalse(actor.isOverflowPolicyDropAll());
        assertFalse(actor.isOverflowPolicyDropNewest());
        assertFalse(actor.isOverflowPolicyDropOldest());
        assertFalse(actor.isOverflowPolicyDropPending());
        assertTrue(actor.isOverflowPolicyDropIncoming());

        assertFalse(actor.hasArrayInflowQueue());
        assertTrue(actor.hasLinkedInflowQueue());

        assertEquals(Integer.MAX_VALUE, actor.backlogCapacity());
        assertEquals(0, actor.backlogSize());
        assertEquals(0, actor.acceptedMessages());
        assertEquals(0, actor.consumedMessages());
        assertEquals(0, actor.droppedMessages());
        assertEquals(0, actor.unhandledExceptions());

        assertTrue(actor.script().setupScript().isEmpty());
        assertTrue(actor.script().messageScript().isEmpty());
        assertTrue(actor.script().exceptionScript().isEmpty());
        assertTrue(actor.script().closeScript().isEmpty());

        assertEquals(actor.uuid().toString(), actor.name());

    }
}
