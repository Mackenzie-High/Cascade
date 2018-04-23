package com.mackenziehigh.cascade;

import com.mackenziehigh.cascade.CascadeActor.ActorLifeCycle;
import com.mackenziehigh.cascade.internal.NopExecutor;
import com.mackenziehigh.cascade.internal.ServiceExecutor;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import static junit.framework.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit Test.
 */
public final class CascadeActorTest
{
    private final CascadeToken X = CascadeToken.token("X");

    private final CascadeToken Y = CascadeToken.token("Y");

    private final CascadeToken Z = CascadeToken.token("Z");

    private final CascadePowerSource executor = new ServiceExecutor(Executors.newFixedThreadPool(5));

    private final Cascade cascade = Cascade.newCascade();

    private final CascadeStage stage = cascade.newStage(executor);

    private final CascadeActor actor = stage.newActor();

    @Before
    public void testInitialState ()
    {
        actor.setPowerSource(new NopExecutor());

        assertEquals(cascade, actor.cascade());
        assertEquals(stage, actor.stage());

        assertEquals(cascade, actor.context().cascade());
        assertEquals(stage, actor.context().stage());
        assertEquals(actor, actor.context().actor());
        assertEquals(actor.script(), actor.context().script());

        assertFalse(actor.isOverflowPolicyDropAll());
        assertFalse(actor.isOverflowPolicyDropNewest());
        assertFalse(actor.isOverflowPolicyDropOldest());
        assertFalse(actor.isOverflowPolicyDropPending());
        assertTrue(actor.isOverflowPolicyDropIncoming());

        assertFalse(actor.hasArrayInflowQueue());
        assertTrue(actor.hasLinkedInflowQueue());

        assertEquals(Integer.MAX_VALUE, actor.metrics().getBacklogCapacity());
        assertEquals(0, actor.metrics().getBacklogSize());
        assertEquals(0, actor.metrics().getAcceptedMessageCount());
        assertEquals(0, actor.metrics().getConsumedMessageCount());
        assertEquals(0, actor.metrics().getDroppedMessageCount());
        assertEquals(0, actor.metrics().getUnhandledExceptionCount());

        assertTrue(actor.script().setupScript().isEmpty());
        assertTrue(actor.script().messageScript().isEmpty());
        assertTrue(actor.script().exceptionScript().isEmpty());
        assertTrue(actor.script().closeScript().isEmpty());

        assertEquals(actor.uuid().toString(), actor.getName());
    }

    @After
    public void destroy ()
            throws InterruptedException
    {
        assertTrue(cascade.stages().contains(stage));
        assertTrue(actor.isDead() || stage.actors().contains(actor));

        actor.start(); // TODO

        // TODO: Bug here. Reenable to find it. Had to turn off to test other stuff first.
//        cascade.close().awaitClose(Duration.ofDays(1));
//
//        assertFalse(cascade.stages().contains(stage));
//        assertFalse(stage.actors().contains(actor));
//
//        assertFalse(actor.isStarting());
//        assertTrue(actor.isStarted());
//        assertFalse(actor.isActive());
//        assertFalse(actor.isActing());
//        assertFalse(actor.isClosing());
//        assertTrue(actor.isClosed());
    }

    /**
     * Test: 20180415032107415710
     *
     * <p>
     * Method: <code></code>
     * </p>
     *
     * <p>
     * Case: Actor Life-cycle
     * </p>
     */
    @Test
    public void test20180415032107415710 ()
    {
        System.out.println("Test: 20180415032107415710");

        assertEquals(NopExecutor.class, actor.getPowerSource().getClass());

        final List<String> family = new CopyOnWriteArrayList<>();
        actor.setArrayInflowQueue(99);
        assertEquals(99, actor.metrics().getBacklogCapacity());
        actor.script().appendToOnMessage(X, (ctx, evt, msg) -> family.add(msg.peekAsString()));

        /**
         * Send a couple of messages before the actor starts.
         */
        assertEquals(ActorLifeCycle.EGG, actor.getLifeCyclePhase());
        assertEquals(0, actor.metrics().getBacklogSize());
        actor.tell(X, CascadeStack.newStack().pushObject("Chicky"));
        actor.tell(X, CascadeStack.newStack().pushObject("Picky"));
        assertEquals(2, actor.metrics().getBacklogSize());
        IntStream.range(0, 10).forEach(i -> actor.crank());
        assertEquals(2, actor.metrics().getBacklogSize());

        /**
         * Begin starting the actor.
         */
        assertEquals(ActorLifeCycle.EGG, actor.getLifeCyclePhase());
        actor.start();
        assertEquals(ActorLifeCycle.STARTING, actor.getLifeCyclePhase());

        /**
         * Send a couple of messages while the actor is starting.
         */
        assertEquals(2, actor.metrics().getBacklogSize());
        actor.tell(X, CascadeStack.newStack().pushObject("Sikorsky"));
        actor.tell(X, CascadeStack.newStack().pushObject("Lucky"));
        assertEquals(4, actor.metrics().getBacklogSize());
        assertEquals(ActorLifeCycle.STARTING, actor.getLifeCyclePhase());

        /**
         * Finish starting the actor.
         */
        IntStream.range(0, 10).forEach(i -> actor.crank());
        assertEquals(ActorLifeCycle.ACTIVE, actor.getLifeCyclePhase());

        /**
         * Send a couple of messages before the actor begins to close.
         */
        assertEquals(0, actor.metrics().getBacklogSize());
        actor.tell(X, CascadeStack.newStack().pushObject("Eyeball"));
        actor.tell(X, CascadeStack.newStack().pushObject("Molly"));
        assertEquals(2, actor.metrics().getBacklogSize());
        IntStream.range(0, 10).forEach(i -> actor.crank());
        assertEquals(0, actor.metrics().getBacklogSize());
        assertEquals(ActorLifeCycle.ACTIVE, actor.getLifeCyclePhase());

        /**
         * Begin closing the actor.
         */
        assertEquals(ActorLifeCycle.ACTIVE, actor.getLifeCyclePhase());
        actor.close();
        assertEquals(ActorLifeCycle.CLOSING, actor.getLifeCyclePhase());

        /**
         * Send a couple of messages as the actor is closing.
         * The messages should be dropped immediately, since the actor is closing.
         * We do not want to let them set in the inflow-queue at all.
         */
        assertEquals(0, actor.metrics().getBacklogSize());
        actor.tell(X, CascadeStack.newStack().pushObject("Jet"));
        actor.tell(X, CascadeStack.newStack().pushObject("Fluffy"));
        assertEquals(0, actor.metrics().getBacklogSize());

        /**
         * Finish closing the actor.
         */
        assertEquals(ActorLifeCycle.CLOSING, actor.getLifeCyclePhase());
        IntStream.range(0, 10).forEach(i -> actor.crank());
        assertEquals(ActorLifeCycle.DEAD, actor.getLifeCyclePhase());

        /**
         * Send a couple of messages after the actor closed completely.
         */
        assertEquals(0, actor.metrics().getBacklogSize());
        actor.tell(X, CascadeStack.newStack().pushObject("Michael"));
        actor.tell(X, CascadeStack.newStack().pushObject("Daffy"));
        assertEquals(0, actor.metrics().getBacklogSize());
        IntStream.range(0, 10).forEach(i -> actor.crank());
        assertEquals(0, actor.metrics().getBacklogSize());
        assertEquals(ActorLifeCycle.DEAD, actor.getLifeCyclePhase());

        assertEquals(6, family.size());
        assertEquals("Chicky", family.get(0));
        assertEquals("Picky", family.get(1));
        assertEquals("Sikorsky", family.get(2));
        assertEquals("Lucky", family.get(3));
        assertEquals("Eyeball", family.get(4));
        assertEquals("Molly", family.get(5));

        assertEquals(10, actor.metrics().getOfferedMessageCount());
        assertEquals(6, actor.metrics().getAcceptedMessageCount());
        assertEquals(4, actor.metrics().getDroppedMessageCount());
        assertEquals(6, actor.metrics().getConsumedMessageCount());
    }

    /**
     * Test: 20180415021649285230
     *
     * <p>
     * Case: Long Pipeline of Actors.
     * </p>
     */
    @Test
    public void test20180415021649285230 ()
            throws InterruptedException
    {
        System.out.println("Test: 20180415021649285230");

        final BlockingQueue<CascadeStack> list = new LinkedBlockingDeque<>();

        final CascadeToken front = CascadeToken.random();

        CascadeToken input = front;

        final CascadeActor frontend = stage.newActor().start();

        CascadeActor middleman = frontend;

        final int k = 1000;
        for (int i = 0; i < k; i++)
        {
            final CascadeToken output = CascadeToken.random();
            middleman.script().appendToOnMessage(input, (ctx, evt, msg) -> ctx.send(output, msg.pushObject(ctx.actor())));
            middleman = stage.newActor().start();
            input = output;
        }

        final CascadeActor backend = middleman;
        backend.script().appendToOnMessage(input, (ctx, evt, msg) -> list.add(msg));

        frontend.tell(front, CascadeStack.newStack());

        final CascadeStack stack = list.poll(1, TimeUnit.DAYS);

        assertEquals(k, stack.size());
        assertEquals(1, backend.metrics().getAcceptedMessageCount());
        assertEquals(1, backend.metrics().getConsumedMessageCount());
    }

    /**
     * Test: 20180415033519005791
     *
     * <p>
     * Case: New Overflow Policy (Drop All).
     * </p>
     */
    @Test
    public void test20180415033519005791 ()
    {
        System.out.println("Test: 20180415033519005791");

        assertEquals(NopExecutor.class, actor.getPowerSource().getClass());

        actor.setOverflowPolicyDropIncoming();

        assertFalse(actor.isOverflowPolicyDropAll());
        assertTrue(actor.isOverflowPolicyDropIncoming());
        assertFalse(actor.isOverflowPolicyDropNewest());
        assertFalse(actor.isOverflowPolicyDropOldest());
        assertFalse(actor.isOverflowPolicyDropPending());

        actor.setOverflowPolicyDropAll();

        assertTrue(actor.isOverflowPolicyDropAll());
        assertFalse(actor.isOverflowPolicyDropIncoming());
        assertFalse(actor.isOverflowPolicyDropNewest());
        assertFalse(actor.isOverflowPolicyDropOldest());
        assertFalse(actor.isOverflowPolicyDropPending());

        /**
         * Now we are going to test whether the overflow-policy works as expected.
         */
        final List<String> family = new CopyOnWriteArrayList<>();
        actor.setArrayInflowQueue(3);
        assertEquals(3, actor.metrics().getBacklogCapacity());
        actor.script().appendToOnMessage(X, (ctx, evt, msg) -> family.add(msg.peekAsString()));

        assertEquals(0, actor.metrics().getBacklogSize());
        actor.tell(X, CascadeStack.newStack().pushObject("Chicky"));
        actor.tell(X, CascadeStack.newStack().pushObject("Picky"));
        actor.tell(X, CascadeStack.newStack().pushObject("Fluffy"));
        assertEquals(3, actor.metrics().getBacklogSize());
        actor.tell(X, CascadeStack.newStack().pushObject("Sikorsky"));
        assertEquals(0, actor.metrics().getBacklogSize());

        actor.start();
        IntStream.range(0, 10).forEach(i -> actor.crank());

        assertTrue(family.isEmpty());

        assertEquals(4, actor.metrics().getOfferedMessageCount());
        assertEquals(3, actor.metrics().getAcceptedMessageCount());
        assertEquals(4, actor.metrics().getDroppedMessageCount());
        assertEquals(0, actor.metrics().getConsumedMessageCount());
    }

    /**
     * Test: 20180415033519005861
     *
     * <p>
     * Case: New Overflow Policy (Drop Incoming).
     * </p>
     */
    @Test
    public void test20180415033519005861 ()
    {
        System.out.println("Test: 20180415033519005861");

        assertEquals(NopExecutor.class, actor.getPowerSource().getClass());

        actor.setOverflowPolicyDropAll();

        assertTrue(actor.isOverflowPolicyDropAll());
        assertFalse(actor.isOverflowPolicyDropIncoming());
        assertFalse(actor.isOverflowPolicyDropNewest());
        assertFalse(actor.isOverflowPolicyDropOldest());
        assertFalse(actor.isOverflowPolicyDropPending());

        actor.setOverflowPolicyDropIncoming();

        assertFalse(actor.isOverflowPolicyDropAll());
        assertTrue(actor.isOverflowPolicyDropIncoming());
        assertFalse(actor.isOverflowPolicyDropNewest());
        assertFalse(actor.isOverflowPolicyDropOldest());
        assertFalse(actor.isOverflowPolicyDropPending());

        /**
         * Now we are going to test whether the overflow-policy works as expected.
         */
        final List<String> family = new CopyOnWriteArrayList<>();
        actor.setArrayInflowQueue(3);
        assertEquals(3, actor.metrics().getBacklogCapacity());
        actor.script().appendToOnMessage(X, (ctx, evt, msg) -> family.add(msg.peekAsString()));

        assertEquals(0, actor.metrics().getBacklogSize());
        actor.tell(X, CascadeStack.newStack().pushObject("Chicky"));
        actor.tell(X, CascadeStack.newStack().pushObject("Picky"));
        actor.tell(X, CascadeStack.newStack().pushObject("Fluffy"));
        assertEquals(3, actor.metrics().getBacklogSize());
        actor.tell(X, CascadeStack.newStack().pushObject("Sikorsky"));
        assertEquals(3, actor.metrics().getBacklogSize());

        actor.start();
        IntStream.range(0, 10).forEach(i -> actor.crank());

        assertEquals(0, actor.metrics().getBacklogSize());
        assertEquals(3, family.size());
        assertEquals("Chicky", family.get(0));
        assertEquals("Picky", family.get(1));
        assertEquals("Fluffy", family.get(2));

        assertEquals(4, actor.metrics().getOfferedMessageCount());
        assertEquals(3, actor.metrics().getAcceptedMessageCount());
        assertEquals(1, actor.metrics().getDroppedMessageCount());
        assertEquals(3, actor.metrics().getConsumedMessageCount());
    }

    /**
     * Test: 20180415033519005889
     *
     * <p>
     * Case: New Overflow Policy (Drop Newest).
     * </p>
     */
    @Test
    public void test20180415033519005889 ()
    {
        System.out.println("Test: 20180415033519005889");

        assertEquals(NopExecutor.class, actor.getPowerSource().getClass());

        actor.setOverflowPolicyDropAll();

        assertTrue(actor.isOverflowPolicyDropAll());
        assertFalse(actor.isOverflowPolicyDropIncoming());
        assertFalse(actor.isOverflowPolicyDropNewest());
        assertFalse(actor.isOverflowPolicyDropOldest());
        assertFalse(actor.isOverflowPolicyDropPending());

        actor.setOverflowPolicyDropNewest();

        assertFalse(actor.isOverflowPolicyDropAll());
        assertFalse(actor.isOverflowPolicyDropIncoming());
        assertTrue(actor.isOverflowPolicyDropNewest());
        assertFalse(actor.isOverflowPolicyDropOldest());
        assertFalse(actor.isOverflowPolicyDropPending());

        /**
         * Now we are going to test whether the overflow-policy works as expected.
         */
        final List<String> family = new CopyOnWriteArrayList<>();
        actor.setArrayInflowQueue(3);
        assertEquals(3, actor.metrics().getBacklogCapacity());
        actor.script().appendToOnMessage(X, (ctx, evt, msg) -> family.add(msg.peekAsString()));

        assertEquals(0, actor.metrics().getBacklogSize());
        actor.tell(X, CascadeStack.newStack().pushObject("Chicky"));
        actor.tell(X, CascadeStack.newStack().pushObject("Picky"));
        actor.tell(X, CascadeStack.newStack().pushObject("Fluffy"));
        assertEquals(3, actor.metrics().getBacklogSize());
        actor.tell(X, CascadeStack.newStack().pushObject("Sikorsky"));
        assertEquals(3, actor.metrics().getBacklogSize());

        actor.start();
        IntStream.range(0, 10).forEach(i -> actor.crank());

        assertEquals(0, actor.metrics().getBacklogSize());
        assertEquals(3, family.size());
        assertEquals("Chicky", family.get(0));
        assertEquals("Picky", family.get(1));
        assertEquals("Sikorsky", family.get(2));

        assertEquals(4, actor.metrics().getOfferedMessageCount());
        assertEquals(4, actor.metrics().getAcceptedMessageCount());
        assertEquals(1, actor.metrics().getDroppedMessageCount());
        assertEquals(3, actor.metrics().getConsumedMessageCount());
    }

    /**
     * Test: 20180415033519005913
     *
     * <p>
     * Case: New Overflow Policy (Drop Oldest).
     * </p>
     */
    @Test
    public void test20180415033519005913 ()
    {
        System.out.println("Test: 20180415033519005913");

        assertEquals(NopExecutor.class, actor.getPowerSource().getClass());

        actor.setOverflowPolicyDropAll();

        assertTrue(actor.isOverflowPolicyDropAll());
        assertFalse(actor.isOverflowPolicyDropIncoming());
        assertFalse(actor.isOverflowPolicyDropNewest());
        assertFalse(actor.isOverflowPolicyDropOldest());
        assertFalse(actor.isOverflowPolicyDropPending());

        actor.setOverflowPolicyDropOldest();

        assertFalse(actor.isOverflowPolicyDropAll());
        assertFalse(actor.isOverflowPolicyDropIncoming());
        assertFalse(actor.isOverflowPolicyDropNewest());
        assertTrue(actor.isOverflowPolicyDropOldest());
        assertFalse(actor.isOverflowPolicyDropPending());

        /**
         * Now we are going to test whether the overflow-policy works as expected.
         */
        final List<String> family = new CopyOnWriteArrayList<>();
        actor.setArrayInflowQueue(3);
        assertEquals(3, actor.metrics().getBacklogCapacity());
        actor.script().appendToOnMessage(X, (ctx, evt, msg) -> family.add(msg.peekAsString()));

        assertEquals(0, actor.metrics().getBacklogSize());
        actor.tell(X, CascadeStack.newStack().pushObject("Chicky"));
        actor.tell(X, CascadeStack.newStack().pushObject("Picky"));
        actor.tell(X, CascadeStack.newStack().pushObject("Fluffy"));
        assertEquals(3, actor.metrics().getBacklogSize());
        actor.tell(X, CascadeStack.newStack().pushObject("Sikorsky"));
        assertEquals(3, actor.metrics().getBacklogSize());

        actor.start();
        IntStream.range(0, 10).forEach(i -> actor.crank());

        assertEquals(0, actor.metrics().getBacklogSize());
        assertEquals(3, family.size());
        assertEquals("Picky", family.get(0));
        assertEquals("Fluffy", family.get(1));
        assertEquals("Sikorsky", family.get(2));

        assertEquals(4, actor.metrics().getOfferedMessageCount());
        assertEquals(4, actor.metrics().getAcceptedMessageCount());
        assertEquals(1, actor.metrics().getDroppedMessageCount());
        assertEquals(3, actor.metrics().getConsumedMessageCount());
    }

    /**
     * Test: 20180415033519005935
     *
     * <p>
     * Case: New Overflow Policy (Drop Pending).
     * </p>
     */
    @Test
    public void test20180415033519005935 ()
    {
        System.out.println("Test: 20180415033519005935");

        assertEquals(NopExecutor.class, actor.getPowerSource().getClass());

        actor.setOverflowPolicyDropAll();

        assertTrue(actor.isOverflowPolicyDropAll());
        assertFalse(actor.isOverflowPolicyDropIncoming());
        assertFalse(actor.isOverflowPolicyDropNewest());
        assertFalse(actor.isOverflowPolicyDropOldest());
        assertFalse(actor.isOverflowPolicyDropPending());

        actor.setOverflowPolicyDropPending();

        assertFalse(actor.isOverflowPolicyDropAll());
        assertFalse(actor.isOverflowPolicyDropIncoming());
        assertFalse(actor.isOverflowPolicyDropNewest());
        assertFalse(actor.isOverflowPolicyDropOldest());
        assertTrue(actor.isOverflowPolicyDropPending());

        /**
         * Now we are going to test whether the overflow-policy works as expected.
         */
        final List<String> family = new CopyOnWriteArrayList<>();
        actor.setArrayInflowQueue(3);
        actor.script().appendToOnMessage(X, (ctx, evt, msg) -> family.add(msg.peekAsString()));

        assertEquals(0, actor.metrics().getBacklogSize());
        actor.tell(X, CascadeStack.newStack().pushObject("Chicky"));
        actor.tell(X, CascadeStack.newStack().pushObject("Picky"));
        actor.tell(X, CascadeStack.newStack().pushObject("Fluffy"));
        assertEquals(3, actor.metrics().getBacklogSize());
        actor.tell(X, CascadeStack.newStack().pushObject("Sikorsky"));
        assertEquals(1, actor.metrics().getBacklogSize());

        actor.start();
        IntStream.range(0, 10).forEach(i -> actor.crank());

        assertEquals(0, actor.metrics().getBacklogSize());
        assertEquals(1, family.size());
        assertTrue(family.contains("Sikorsky"));

        assertEquals(4, actor.metrics().getOfferedMessageCount());
        assertEquals(4, actor.metrics().getAcceptedMessageCount());
        assertEquals(3, actor.metrics().getDroppedMessageCount());
        assertEquals(1, actor.metrics().getConsumedMessageCount());
    }

    /**
     * Test: 20180415033519005957
     *
     * <p>
     * Case: close() after closed.
     * </p>
     */
    @Test
    public void test20180415033519005957 ()
    {
        System.out.println("Test: 20180415033519005957");
        fail();
    }

    /**
     * Test: 20180415033519005982
     *
     * <p>
     * Case: close() before start().
     * </p>
     */
    @Test
    public void test20180415033519005982 ()
    {
        System.out.println("Test: 20180415033519005982");
        fail();
    }

    /**
     * Test: 20180415033519006005
     *
     * <p>
     * Case: start() after start().
     * </p>
     */
    @Test
    public void test20180415033519006005 ()
    {
        System.out.println("Test: 20180415033519006005");
        fail();
    }

    /**
     * Test: 20180415033519006026
     *
     * <p>
     * Case: Unhandled Exception in script(), uses explicit handler from script.
     * </p>
     */
    @Test
    public void test20180415033519006026 ()
    {
        System.out.println("Test: 20180415033519006026");
        fail();
    }

    /**
     * Test: 20180415033519006048
     *
     * <p>
     * Case: Unhandled Exception in script(), uses implicit handler from actor.
     * </p>
     */
    @Test
    public void test20180415033519006048 ()
    {
        System.out.println("Test: 20180415033519006048");
        fail();
    }

    /**
     * Test: 20180415034324882820
     *
     * <p>
     * Case: Unhandled Exception in script(), uses implicit handler from stage.
     * </p>
     */
    @Test
    public void test20180415034324882820 ()
    {
        System.out.println("Test: 20180415034324882820");
        fail();
    }

    /**
     * Test: 20180415034324882911
     *
     * <p>
     * Case: Unhandled Exception in script(), uses implicit handler from cascade.
     * </p>
     */
    @Test
    public void test20180415034324882911 ()
    {
        System.out.println("Test: 20180415034324882911");
        fail();
    }

    /**
     * Test: 20180415034428512866
     *
     * <p>
     * Case: Name Change
     * </p>
     */
    @Test
    public void test20180415034428512866 ()
    {
        System.out.println("Test: 20180415034428512866");
        fail();
    }

    /**
     * Test: 20180415034556318104
     *
     * <p>
     * Case: subscribe() after start().
     * </p>
     */
    @Test
    public void test20180415034556318104 ()
    {
        System.out.println("Test: 20180415034556318104");
        fail();
    }

    /**
     * Test: 20180415034646600185
     *
     * <p>
     * Case: unsubscribe() after start().
     * </p>
     */
    @Test
    public void test20180415034646600185 ()
    {
        System.out.println("Test: 20180415034646600185");
        fail();
    }

    /**
     * Test: 20180415034646600270
     *
     * <p>
     * Case: subscribe() after close().
     * </p>
     */
    @Test
    public void test20180415034646600270 ()
    {
        System.out.println("Test: 20180415034646600270");
        fail();
    }

    /**
     * Test: 20180415034646600300
     *
     * <p>
     * Case: unsubscribe() after close().
     * </p>
     */
    @Test
    public void test20180415034646600300 ()
    {
        System.out.println("Test: 20180415034646600300");
        fail();
    }

    /**
     * Test: 20180415034646600325
     *
     * <p>
     * Case: Await Start
     * </p>
     */
    @Test
    public void test20180415034646600325 ()
    {
        System.out.println("Test: 20180415034646600325");
        fail();
    }

    /**
     * Test: 20180415034646600346
     *
     * <p>
     * Case: Await Close
     * </p>
     */
    @Test
    public void test20180415034646600346 ()
    {
        System.out.println("Test: 20180415034646600346");
        fail();
    }

    /**
     * Test: 20180415034913880483
     *
     * <p>
     * Case: Unhandled Exception in Setup-Handler.
     * </p>
     */
    @Test
    public void test20180415034913880483 ()
    {
        System.out.println("Test: 20180415034913880483");
        fail();
    }

    /**
     * Test: 20180415034913880567
     *
     * <p>
     * Case: Unhandled Exception in Message-Handler.
     * </p>
     */
    @Test
    public void test20180415034913880567 ()
    {
        System.out.println("Test: 20180415034913880567");
        fail();
    }

    /**
     * Test: 20180415035121777621
     *
     * <p>
     * Case: Unhandled Exception in Exception-Handler.
     * </p>
     */
    @Test
    public void test20180415035121777621 ()
    {
        System.out.println("Test: 20180415035121777621");
        fail();
    }

    /**
     * Test: 20180415035121777700
     *
     * <p>
     * Case: Unhandled Exception in Close-Handler.
     * </p>
     */
    @Test
    public void test20180415035121777700 ()
    {
        System.out.println("Test: 20180415035121777700");
        fail();
    }

    /**
     * Test: 20180415035121777728
     *
     * <p>
     * Method: <code>toString()</code>
     * </p>
     */
    @Test
    public void test20180415035121777728 ()
    {
        System.out.println("Test: 20180415035121777728");
        fail();
    }

    /**
     * Test: 20180415035557863742
     *
     * <p>
     * Case: Send to non-existent channel via context().
     * </p>
     */
    @Test
    public void test20180415035557863742 ()
    {
        System.out.println("Test: 20180415035557863742");
        fail();
    }

    /**
     * Test: 20180415035557863807
     *
     * <p>
     * Case: Send to existent channel via context.
     * </p>
     */
    @Test
    public void test20180415035557863807 ()
    {
        System.out.println("Test: 20180415035557863807");
        fail();
    }

    /**
     * Test: 20180415035751297263
     *
     * <p>
     * Case: Send via tell().
     * </p>
     */
    @Test
    public void test20180415035751297263 ()
    {
        System.out.println("Test: 20180415035751297263");
        fail();
    }
}
