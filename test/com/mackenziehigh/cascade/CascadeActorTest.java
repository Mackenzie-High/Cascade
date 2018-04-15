package com.mackenziehigh.cascade;

import com.mackenziehigh.cascade.internal.ServiceExecutor;
import java.time.Duration;
import java.util.concurrent.Executors;
import static junit.framework.Assert.*;
import org.junit.After;
import org.junit.Test;

/**
 * Unit Test.
 */
public final class CascadeActorTest
{
    private final CascadeExecutor executor = new ServiceExecutor(Executors.newFixedThreadPool(1));

    private final Cascade cascade = Cascade.newCascade();

    private final CascadeStage stage = cascade.newStage(executor);

    private final CascadeActor actor = stage.newActor();

    @After
    public void destroy ()
            throws InterruptedException
    {
        assertTrue(cascade.stages().contains(stage));
        assertTrue(stage.actors().contains(actor));

        cascade.close().awaitClose(Duration.ofDays(1));

        assertFalse(cascade.stages().contains(stage));
        assertFalse(stage.actors().contains(actor));

        assertFalse(actor.isStarting());
        assertTrue(actor.isStarted());
        assertFalse(actor.isActive());
        assertFalse(actor.isActing());
        assertFalse(actor.isClosing());
        assertTrue(actor.isClosed());
    }

    @Test
    public void testInitialState ()
    {
        assertEquals(cascade, actor.cascade());
        assertEquals(stage, actor.stage());

        assertEquals(cascade, actor.context().cascade());
        assertEquals(stage, actor.context().stage());
        assertEquals(actor, actor.context().actor());
        assertEquals(actor.script(), actor.context().script());

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
