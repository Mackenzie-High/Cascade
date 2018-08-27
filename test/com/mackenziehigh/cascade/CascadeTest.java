package com.mackenziehigh.cascade;

import com.mackenziehigh.cascade.Cascade.AbstractStage;
import com.mackenziehigh.cascade.Cascade.Stage.Actor;
import com.mackenziehigh.cascade.Cascade.Stage.Actor.Input;
import com.mackenziehigh.cascade.Cascade.Stage.Actor.Output;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit Test.
 */
public final class CascadeTest
{
    private static class CrankedStage
            extends AbstractStage
    {
        public final List<Entry<String, Object>> whatHappened = new LinkedList<>();

        private final Queue<ActorTask> tasks = new LinkedBlockingQueue<>();

        @Override
        protected void onActorSubmit (final ActorTask state)
        {
            whatHappened.add(new AbstractMap.SimpleImmutableEntry<>("SUBMIT", state));
            tasks.add(state);
        }

        @Override
        protected void onStageClose ()
        {
            whatHappened.add(new AbstractMap.SimpleImmutableEntry<>("CLOSE", null));
        }

        public ActorTask crank ()
        {
            final ActorTask task = tasks.poll();

            if (task != null)
            {
                task.crank();
            }

            return task;
        }
    }

    private final CrankedStage stage = new CrankedStage();

    private static <T> T getField (final Object object,
                                   final String name,
                                   final Class<T> type)
            throws Exception
    {
        final Field field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return type.cast(field.get(object));
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Method: <code>withScript(Script)</code>
     * </p>
     *
     * @throws java.lang.Exception
     */
    @Test
    public void test20180826010008846278 ()
            throws Exception
    {
        fail();
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Method: <code>withScript(ConsumerScript)</code>
     * </p>
     */
    @Test
    public void test20180826232658962695 ()
    {
        System.out.println("Test: 20180826232658962695");
        fail();
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Method: <code>withErrorHandler(Consumer)</code>
     * </p>
     */
    @Test
    public void test20180826232927857314 ()
    {
        System.out.println("Test: 20180826232927857314");
        fail();
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Method: <code>withInflowQueue(Queue)</code>
     * </p>
     *
     * @throws java.lang.Exception
     */
    @Test
    public void test20180826232927857343 ()
            throws Exception
    {
        final Queue<String> queue = new LinkedList();

        final Actor<String, String> actor = stage
                .newActor()
                .withScript((String x) -> x)
                .withInflowQueue(queue)
                .create();

        // Identity Equality
        assertTrue(queue == getField(actor, "mailbox", Queue.class));
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Method: <code>withConcurrentInflowQueue()</code>
     * </p>
     *
     * @throws java.lang.Exception
     */
    @Test
    public void test20180826232927857371 ()
            throws Exception
    {
        final Actor<String, String> actor = stage
                .newActor()
                .withScript((String x) -> x)
                .withConcurrentInflowQueue()
                .create();

        final ConcurrentLinkedQueue<String> queue = getField(actor, "mailbox", ConcurrentLinkedQueue.class);
        assertEquals(0, queue.size());
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Method: <code>withLinkedInflowQueue()</code>
     * </p>
     *
     * @throws java.lang.Exception
     */
    @Test
    public void test20180826232927857400 ()
            throws Exception
    {
        final Actor<String, String> actor = stage
                .newActor()
                .withScript((String x) -> x)
                .withLinkedInflowQueue()
                .create();

        final LinkedBlockingQueue<String> queue = getField(actor, "mailbox", LinkedBlockingQueue.class);
        assertEquals(0, queue.size());
        assertEquals(Integer.MAX_VALUE, queue.remainingCapacity());
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Method: <code>withLinkedInflowQueue(int)</code>
     * </p>
     *
     * @throws java.lang.Exception
     */
    @Test
    public void test20180826234257533692 ()
            throws Exception
    {
        final int capacity = 109;

        final Actor<String, String> actor = stage
                .newActor()
                .withScript((String x) -> x)
                .withLinkedInflowQueue(capacity)
                .create();

        final LinkedBlockingQueue<String> queue = getField(actor, "mailbox", LinkedBlockingQueue.class);
        assertEquals(0, queue.size());
        assertEquals(capacity, queue.remainingCapacity());
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Method: <code>withArrayInflowQueue(int)</code>
     * </p>
     *
     * @throws java.lang.Exception
     */
    @Test
    public void test20180826234257533759 ()
            throws Exception
    {
        final int capacity = 109;

        final Actor<String, String> actor = stage
                .newActor()
                .withScript((String x) -> x)
                .withArrayInflowQueue(capacity)
                .create();

        final ArrayBlockingQueue<String> queue = getField(actor, "mailbox", ArrayBlockingQueue.class);
        assertEquals(0, queue.size());
        assertEquals(capacity, queue.remainingCapacity());
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Input</code>
     * </p>
     *
     * <p>
     * Method: <code>actor()</code>
     * </p>
     */
    @Test
    public void test20180826234257533787 ()
    {
        final Actor<String, String> actor = stage
                .newActor()
                .withScript((String x) -> x)
                .create();

        assertEquals(actor, actor.input().actor());
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Input</code>
     * </p>
     *
     * <p>
     * Method: <code>connections()</code>
     * </p>
     */
    @Test
    public void test20180826234452148065 ()
    {
        final Actor<String, String> actor1 = stage
                .newActor()
                .withScript((String x) -> x)
                .create();

        final Actor<String, String> actor2 = stage
                .newActor()
                .withScript((String x) -> x)
                .create();

        final Actor<String, String> actor3 = stage
                .newActor()
                .withScript((String x) -> x)
                .create();

        final Set<Output<String>> connections0 = actor1.input().connections();
        actor1.input().connect(actor2.output());
        final Set<Output<String>> connections1 = actor1.input().connections();
        actor1.input().connect(actor3.output());
        final Set<Output<String>> connections2 = actor1.input().connections();

        /**
         * The collections are immutable; therefore, different ones are returned.
         */
        assertFalse(connections0 == connections1); // Identity Equality
        assertFalse(connections1 == connections2); // Identity Equality

        /**
         * Verify Results.
         */
        assertEquals(0, connections0.size());
        assertEquals(1, connections1.size());
        assertEquals(2, connections2.size());
        assertTrue(connections1.contains(actor2.output()));
        assertTrue(connections2.contains(actor3.output()));
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Input</code>
     * </p>
     *
     * <p>
     * Method: <code>connect(Output)</code>
     * </p>
     */
    @Test
    public void test20180826234452148142 ()
    {
        final Actor<String, String> actor1 = stage
                .newActor()
                .withScript((String x) -> x)
                .create();

        final Actor<String, String> actor2 = stage
                .newActor()
                .withScript((String x) -> x)
                .create();

        final Actor<String, String> actor3 = stage
                .newActor()
                .withScript((String x) -> x)
                .create();

        /**
         * Method Under Test.
         */
        assertEquals(actor1.input(), actor1.input().connect(actor2.output()));
        assertEquals(actor1.input(), actor1.input().connect(actor3.output()));

        /**
         * Verify the input is connected to the outputs.
         */
        assertEquals(2, actor1.input().connections().size());
        assertTrue(actor1.input().connections().contains(actor2.output()));
        assertTrue(actor1.input().connections().contains(actor3.output()));

        /**
         * Verify that the outputs are connected to the input.
         */
        assertEquals(1, actor2.output().connections().size());
        assertTrue(actor2.output().connections().contains(actor1.input()));
        assertEquals(1, actor3.output().connections().size());
        assertTrue(actor3.output().connections().contains(actor1.input()));
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Input</code>
     * </p>
     *
     * <p>
     * Method: <code>disconnect(Output)</code>
     * </p>
     */
    @Test
    public void test20180826234452148170 ()
    {
        final Actor<String, String> actor1 = stage
                .newActor()
                .withScript((String x) -> x)
                .create();

        final Actor<String, String> actor2 = stage
                .newActor()
                .withScript((String x) -> x)
                .create();

        final Actor<String, String> actor3 = stage
                .newActor()
                .withScript((String x) -> x)
                .create();

        /**
         * Establish the connections.
         */
        assertEquals(actor1.input(), actor1.input().connect(actor2.output()));
        assertEquals(actor1.input(), actor1.input().connect(actor3.output()));

        /**
         * Method Under Test.
         */
        assertEquals(actor1.input(), actor1.input().disconnect(actor2.output()));
        assertEquals(actor1.input(), actor1.input().disconnect(actor3.output()));

        /**
         * Verify the input is connected to the outputs.
         */
        assertEquals(0, actor1.input().connections().size());
        assertFalse(actor1.input().connections().contains(actor2.output()));
        assertFalse(actor1.input().connections().contains(actor3.output()));

        /**
         * Verify that the outputs are connected to the input.
         */
        assertEquals(0, actor2.output().connections().size());
        assertFalse(actor2.output().connections().contains(actor1.input()));
        assertEquals(0, actor3.output().connections().size());
        assertFalse(actor3.output().connections().contains(actor1.input()));
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Input</code>
     * </p>
     *
     * <p>
     * Method: <code>send(Object)</code>
     * </p>
     *
     * @throws java.lang.Exception
     */
    @Test
    public void test20180826235159104619 ()
            throws Exception
    {
        final List<String> log = new ArrayList<>();

        final Actor<String, Boolean> actor = stage
                .newActor()
                .withScript((String x) -> log.add(x))
                .create();

        final Queue<String> mailbox = getField(actor, "mailbox", Queue.class);

        assertTrue(mailbox.isEmpty());

        assertEquals(actor.input(), actor.input().send("A"));
        assertEquals(actor.input(), actor.input().send("B"));
        assertEquals(actor.input(), actor.input().send("C"));

        assertEquals(3, mailbox.size());
        assertTrue(mailbox.contains("A"));
        assertTrue(mailbox.contains("B"));
        assertTrue(mailbox.contains("C"));
        assertEquals(0, log.size());

        stage.crank();
        stage.crank();
        stage.crank();

        assertEquals(3, log.size());
        assertTrue(log.contains("A"));
        assertTrue(log.contains("B"));
        assertTrue(log.contains("C"));
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Output</code>
     * </p>
     *
     * <p>
     * Method: <code>actor()</code>
     * </p>
     */
    @Test
    public void test20180826234816988944 ()
    {
        final Actor<String, String> actor = stage
                .newActor()
                .withScript((String x) -> x)
                .create();

        assertEquals(actor, actor.output().actor());
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Output</code>
     * </p>
     *
     * <p>
     * Method: <code>connections()</code>
     * </p>
     */
    @Test
    public void test20180826234816989016 ()
    {
        final Actor<String, String> actor1 = stage
                .newActor()
                .withScript((String x) -> x)
                .create();

        final Actor<String, String> actor2 = stage
                .newActor()
                .withScript((String x) -> x)
                .create();

        final Actor<String, String> actor3 = stage
                .newActor()
                .withScript((String x) -> x)
                .create();

        final Set<Input<String>> connections0 = actor1.output().connections();
        actor1.output().connect(actor2.input());
        final Set<Input<String>> connections1 = actor1.output().connections();
        actor1.output().connect(actor3.input());
        final Set<Input<String>> connections2 = actor1.output().connections();

        /**
         * The collections are immutable; therefore, different ones are returned.
         */
        assertFalse(connections0 == connections1); // Identity Equality
        assertFalse(connections1 == connections2); // Identity Equality

        /**
         * Verify Results.
         */
        assertEquals(0, connections0.size());
        assertEquals(1, connections1.size());
        assertEquals(2, connections2.size());
        assertTrue(connections1.contains(actor2.input()));
        assertTrue(connections2.contains(actor3.input()));
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Output</code>
     * </p>
     *
     * <p>
     * Method: <code>connect(Input)</code>
     * </p>
     */
    @Test
    public void test20180826234816989043 ()
    {
        final Actor<String, String> actor1 = stage
                .newActor()
                .withScript((String x) -> x)
                .create();

        final Actor<String, String> actor2 = stage
                .newActor()
                .withScript((String x) -> x)
                .create();

        final Actor<String, String> actor3 = stage
                .newActor()
                .withScript((String x) -> x)
                .create();

        /**
         * Method Under Test.
         */
        assertEquals(actor1.output(), actor1.output().connect(actor2.input()));
        assertEquals(actor1.output(), actor1.output().connect(actor3.input()));

        /**
         * Verify the output is connected to the inputs.
         */
        assertEquals(2, actor1.output().connections().size());
        assertTrue(actor1.output().connections().contains(actor2.input()));
        assertTrue(actor1.output().connections().contains(actor3.input()));

        /**
         * Verify that the inputs are connected to the output.
         */
        assertEquals(1, actor2.input().connections().size());
        assertTrue(actor2.input().connections().contains(actor1.output()));
        assertEquals(1, actor3.input().connections().size());
        assertTrue(actor3.input().connections().contains(actor1.output()));
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Output</code>
     * </p>
     *
     * <p>
     * Method: <code>disconnect(Input)</code>
     * </p>
     */
    @Test
    public void test20180827000217370665 ()
    {
        final Actor<String, String> actor1 = stage
                .newActor()
                .withScript((String x) -> x)
                .create();

        final Actor<String, String> actor2 = stage
                .newActor()
                .withScript((String x) -> x)
                .create();

        final Actor<String, String> actor3 = stage
                .newActor()
                .withScript((String x) -> x)
                .create();

        /**
         * Establish the connections.
         */
        assertEquals(actor1.output(), actor1.output().connect(actor2.input()));
        assertEquals(actor1.output(), actor1.output().connect(actor3.input()));

        /**
         * Method Under Test.
         */
        assertEquals(actor1.output(), actor1.output().disconnect(actor2.input()));
        assertEquals(actor1.output(), actor1.output().disconnect(actor3.input()));

        /**
         * Verify the output is connected to the inputs.
         */
        assertEquals(0, actor1.output().connections().size());
        assertFalse(actor1.output().connections().contains(actor2.input()));
        assertFalse(actor1.output().connections().contains(actor3.input()));

        /**
         * Verify that the inputs are connected to the output.
         */
        assertEquals(0, actor2.input().connections().size());
        assertFalse(actor2.input().connections().contains(actor1.output()));
        assertEquals(0, actor3.input().connections().size());
        assertFalse(actor3.input().connections().contains(actor1.output()));
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Actor</code>
     * </p>
     *
     * <p>
     * Method: <code>input()</code>
     * </p>
     */
    @Test
    public void test20180827000217370732 ()
    {
        final Actor<String, String> actor = stage
                .newActor()
                .withScript((String x) -> x)
                .create();

        /**
         * The exact same object is always returned.
         */
        assertNotNull(actor.input());
        assertTrue(actor.input() == actor.input());
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Actor</code>
     * </p>
     *
     * <p>
     * Method: <code>output()</code>
     * </p>
     */
    @Test
    public void test20180827000217370760 ()
    {
        final Actor<String, String> actor = stage
                .newActor()
                .withScript((String x) -> x)
                .create();

        /**
         * The exact same object is always returned.
         */
        assertNotNull(actor.input());
        assertTrue(actor.output() == actor.output());
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Actor</code>
     * </p>
     *
     * <p>
     * Method: <code>stage()</code>
     * </p>
     */
    @Test
    public void test20180827000807769120 ()
    {
        final Actor<String, String> actor = stage
                .newActor()
                .withScript((String x) -> x)
                .create();

        /**
         * The exact same object is always returned.
         */
        assertNotNull(actor.input());
        assertTrue(actor.stage() == actor.stage());

        /**
         * The object that is returned is the stage that created the actor.
         */
        assertEquals(stage, actor.stage());
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Stage</code>
     * </p>
     *
     * <p>
     * Method: <code>setErrorHandler</code>
     * </p>
     */
    @Test
    public void test20180827001307123327 ()
    {
        System.out.println("Test: 20180827001307123327");
        fail();
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Stage</code>
     * </p>
     *
     * <p>
     * Method: <code>close</code>
     * </p>
     */
    @Test
    public void test20180827001353357631 ()
    {
        stage.close();

        assertEquals(1, stage.whatHappened.size());
        assertEquals("CLOSE", stage.whatHappened.get(0).getKey());
    }

}
