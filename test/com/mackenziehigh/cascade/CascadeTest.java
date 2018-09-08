/*
 * Copyright 2017 Michael Mackenzie High
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mackenziehigh.cascade;

import com.mackenziehigh.cascade.Cascade.AbstractStage;
import com.mackenziehigh.cascade.Cascade.AbstractStage.ActorTask;
import com.mackenziehigh.cascade.Cascade.Stage;
import com.mackenziehigh.cascade.Cascade.Stage.Actor;
import com.mackenziehigh.cascade.Cascade.Stage.Actor.ConsumerScript;
import com.mackenziehigh.cascade.Cascade.Stage.Actor.FunctionScript;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
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
        public final Map<Actor, AtomicInteger> executionCounters = new HashMap<>();

        public final List<Entry<String, Object>> whatHappened = new LinkedList<>();

        private final Queue<ActorTask> tasks = new LinkedBlockingQueue<>();

        @Override
        protected void onSubmit (final ActorTask state)
        {
            if (state.meta() == null)
            {
                final AtomicInteger counter = new AtomicInteger(1);
                state.meta(counter);
                executionCounters.put(state.actor(), counter);
            }
            else
            {
                ((AtomicInteger) state.meta()).incrementAndGet();
            }

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
                task.run();
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
     * Method: <code>withScript</code>
     * </p>
     *
     * @throws java.lang.Exception
     */
    @Test
    public void test20180826010008846278 ()
            throws Exception
    {
        /**
         * The sink-actor will append results to this list.
         */
        final List<Integer> results1 = new LinkedList<>();

        /**
         * The bug-actor will append results to this list.
         * This list should remain empty.
         */
        final List<Integer> results2 = new LinkedList<>();

        /**
         * Create an actor network that will test both FunctionScripts and ConsumerScripts.
         * The source-actor tests FunctionScript throughput.
         * The expression-actor tests FunctionScript throughput and filtering ability (null output).
         * The sink-actor tests ConsumerScript throughput.
         * The bug-actor is used to detect messages sent from the sink-actor.
         * The sink-actor should not send any messages, because it is based on a ConsumerScript.
         */
        final FunctionScript<Integer, Integer> sourceScript = (Integer x) -> x;
        final FunctionScript<Integer, Integer> exprScript = (Integer x) -> (x % 3 == 0) ? null : x;
        final ConsumerScript<Integer> sinkScript = (Integer x) -> results1.add(x);
        final ConsumerScript<Integer> bugScript = (Integer x) -> results2.add(x);
        final Actor<Integer, Integer> source = stage.newActor().withScript(sourceScript).create();
        final Actor<Integer, Integer> expr = stage.newActor().withScript(exprScript).create();
        final Actor<Integer, Integer> sink = stage.newActor().withScript(sinkScript).create();
        final Actor<Integer, Integer> bug = stage.newActor().withScript(bugScript).create();
        source.output().connect(expr.input());
        expr.output().connect(sink.input());
        sink.output().connect(bug.input());

        /**
         * Send ten messages through the network of actors.
         */
        IntStream.rangeClosed(1, 10).forEach(i -> source.accept(i));

        /**
         * Apply power, so the actors act.
         */
        IntStream.rangeClosed(1, 100).forEach(i -> stage.crank());

        /**
         * Verify the results.
         */
        assertEquals(Arrays.asList(1, 2, 4, 5, 7, 8, 10), results1);
        assertTrue(results2.isEmpty());
    }

    /**
     * Test: 20180908022610095209
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Case: Defaults.
     * </p>
     *
     * @throws java.lang.Throwable
     */
    @Test
    public void test20180908022610095209 ()
            throws Throwable
    {
        final Actor<Object, Object> actor = stage.newActor().create();

        /**
         * By default, the actor will use a ConcurrentLinkedQueue to store inputs.
         */
        assertTrue(getField(actor, "mailbox", Queue.class) instanceof ConcurrentLinkedQueue);

        /**
         * By default, the actor will use a script that always returns null.
         */
        assertNull(getField(actor, "script", FunctionScript.class).execute(100));
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
     *
     * <p>
     * Case: Basic Functionality.
     * </p>
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void test20180826232927857314 ()
            throws InterruptedException
    {
        /**
         * This queue will contain the exceptions propagated to the stage.
         * This queue should remain empty.
         */
        final BlockingQueue<Throwable> errors1 = new LinkedBlockingQueue<>();
        stage.addErrorHandler(ex -> errors1.add(ex));

        /**
         * This queue will contain the exceptions caught by the actor itself.
         */
        final BlockingQueue<Throwable> errors2 = new LinkedBlockingQueue<>();

        /**
         * This actor will always cause an exception due to a div by zero.
         */
        final Actor<Integer, Integer> actor = stage
                .newActor()
                .withScript((Integer x) -> x / 0) // div by zero
                .withErrorHandler(ex -> errors2.add(ex))
                .create();

        /**
         * Cause an exception.
         */
        actor.input().send(1);
        stage.crank();

        /**
         * The actor caught the exception.
         */
        final Throwable error = errors2.poll(5, TimeUnit.SECONDS);
        assertTrue(error instanceof ArithmeticException);

        /**
         * The exception was *not* propagated to the stage.
         */
        assertTrue(errors1.isEmpty());
    }

    /**
     * Test: 20180908031439953144
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Method: <code>withErrorHandler(Consumer)</code>
     * </p>
     *
     * <p>
     * Case: Exception in Error-Handler.
     * </p>
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void test20180908031439953144 ()
            throws InterruptedException
    {
        /**
         * This queue will contain the exceptions propagated to the stage.
         * This queue should remain empty.
         */
        final List<Throwable> errors = new LinkedList<>();
        stage.addErrorHandler(ex -> errors.add(ex));

        /**
         * This actor can cause an exception due to a div by zero.
         * If that happens, then the error-handler will also cause such an exception.
         */
        final Actor<Integer, Integer> actor = stage
                .newActor()
                .withScript((Integer x) -> (x / x) * x) // potential div by zero
                .withErrorHandler(ex -> System.out.println(1 / 0)) // another div by zero
                .create();

        /**
         * This actor will receive messages from the previous actor,
         * unless the previous actor experienced an exception.
         */
        final List<Integer> results = new LinkedList<>();
        final Actor<Integer, ?> sink = stage.newActor().withScript((Integer x) -> results.add(x)).create();
        actor.output().connect(sink.input());

        /**
         * Cause an exception.
         */
        actor.input().send(1011);
        actor.input().send(0);
        actor.input().send(1012);
        IntStream.rangeClosed(1, 100).forEach(x -> stage.crank());

        /**
         * The exception was *not* propagated to the stage.
         */
        assertTrue(errors.isEmpty());

        /**
         * The non-exception-causing messages should have been processed.
         */
        assertEquals(Arrays.asList(1011, 1012), results);
    }

    /**
     * Test: 20180908012913300766
     *
     * <p>
     * Class: <code>Stage</code>
     * </p>
     *
     * <p>
     * Method: <code>addErrorHandler</code>
     * </p>
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void test20180908012913300766 ()
            throws InterruptedException
    {
        /**
         * This queue will contain the exceptions propagated to the stage by the first handler.
         */
        final BlockingQueue<Throwable> errors1 = new LinkedBlockingQueue<>();
        stage.addErrorHandler(ex -> errors1.add(ex));

        /**
         * This queue will contain the exceptions propagated to the stage by the second handler.
         */
        final BlockingQueue<Throwable> errors2 = new LinkedBlockingQueue<>();
        stage.addErrorHandler(ex -> errors2.add(ex));

        /**
         * This actor will always cause an exception due to a div by zero.
         */
        final Actor<Integer, Integer> actor = stage
                .newActor()
                .withScript((Integer x) -> x / 0) // div by zero
                .create();

        /**
         * Cause an exception.
         */
        actor.input().send(1);
        stage.crank();

        /**
         * The stage caught the exception.
         */
        final Throwable error1 = errors1.poll(5, TimeUnit.SECONDS);
        final Throwable error2 = errors2.poll(5, TimeUnit.SECONDS);
        assertTrue(error1 instanceof ArithmeticException);
        assertTrue(error2 instanceof ArithmeticException);
        assertEquals(error1, error2);
        assertTrue(errors1.isEmpty());
        assertTrue(errors2.isEmpty());
    }

    /**
     * Test: 20180908033001320035
     *
     * <p>
     * Class: <code>Stage</code>
     * </p>
     *
     * <p>
     * Method: <code>addErrorHandler</code>
     * </p>
     *
     * <p>
     * Case: Basic Functionality.
     * </p>
     */
    @Test
    public void test20180908033001320035 ()
    {
        /**
         * The stage error-handler will always cause an exception.
         */
        stage.addErrorHandler(ex -> System.out.println(1 / 0));

        /**
         * This actor can cause an exception due to a div by zero.
         */
        final Actor<Integer, Integer> actor = stage
                .newActor()
                .withScript((Integer x) -> (x / x) * x) // potential div by zero
                .create();

        /**
         * This actor will receive messages from the previous actor,
         * unless the previous actor experienced an exception.
         */
        final List<Integer> results = new LinkedList<>();
        final Actor<Integer, ?> sink = stage.newActor().withScript((Integer x) -> results.add(x)).create();
        actor.output().connect(sink.input());

        /**
         * Cause an exception.
         */
        actor.input().send(1011);
        actor.input().send(0);
        actor.input().send(1012);
        IntStream.rangeClosed(1, 100).forEach(x -> stage.crank());

        /**
         * The non-exception-causing messages should have been processed.
         */
        assertEquals(Arrays.asList(1011, 1012), results);
    }

    /**
     * Test: 20180908014307646248
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Method: <code>withInflowQueue(Queue)</code>
     * </p>
     *
     * <p>
     * Case: Non-Thread-Safe Array-Deque.
     * </p>
     */
    @Test (expected = IllegalArgumentException.class)
    public void test20180908014307646248 ()
    {
        stage.newActor().withInflowQueue(new ArrayDeque<>());
    }

    /**
     * Test: 20180908014307646316
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Method: <code>withInflowQueue(Queue)</code>
     * </p>
     *
     * <p>
     * Case: Non-Thread-Safe Linked-List.
     * </p>
     */
    @Test (expected = IllegalArgumentException.class)
    public void test20180908014307646316 ()
    {
        stage.newActor().withInflowQueue(new LinkedList<>());
    }

    /**
     * Test: 20180908014307646340
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Method: <code>withInflowQueue(Queue)</code>
     * </p>
     *
     * <p>
     * Case: Non-Thread-Safe Priority-Queue.
     * </p>
     */
    @Test (expected = IllegalArgumentException.class)
    public void test20180908014307646340 ()
    {
        stage.newActor().withInflowQueue(new PriorityQueue<>());
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
        final Queue<String> queue = new SynchronousQueue<>();

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
        assertTrue(actor1.input().isConnected(actor2.output()));
        assertTrue(actor1.input().isConnected(actor3.output()));

        /**
         * Verify that the outputs are connected to the input.
         */
        assertTrue(actor2.output().isConnected(actor1.input()));
        assertTrue(actor3.output().isConnected(actor1.input()));
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
         * Verify the input is no longer connected to the outputs.
         */
        assertFalse(actor1.input().isConnected(actor2.output()));
        assertFalse(actor1.input().isConnected(actor3.output()));

        /**
         * Verify that the outputs are no longer connected to the input.
         */
        assertFalse(actor2.output().isConnected(actor1.input()));
        assertFalse(actor3.output().isConnected(actor1.input()));
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
     * Test: 20180908014838917134
     *
     * <p>
     * Class: <code>Input</code>
     * </p>
     *
     * <p>
     * Method: <code>offer(Object)</code>
     * </p>
     *
     * @throws java.lang.Exception
     */
    @Test
    public void test20180908014838917134 ()
            throws Exception
    {
        final List<String> log = new ArrayList<>();

        final Actor<String, Boolean> actor = stage
                .newActor()
                .withLinkedInflowQueue(3)
                .withScript((String x) -> log.add(x))
                .create();

        final Queue<String> mailbox = getField(actor, "mailbox", Queue.class);

        assertTrue(mailbox.isEmpty());

        assertTrue(actor.input().offer("A"));
        assertTrue(actor.input().offer("B"));
        assertTrue(actor.input().offer("C"));
        assertFalse(actor.input().offer("D"));
        assertFalse(actor.input().offer("E"));

        assertEquals(3, mailbox.size());
        assertTrue(mailbox.contains("A"));
        assertTrue(mailbox.contains("B"));
        assertTrue(mailbox.contains("C"));
        assertEquals(0, log.size());

        stage.crank();
        stage.crank();
        stage.crank();

        assertEquals(0, mailbox.size());
        assertEquals(3, log.size());
        assertTrue(log.contains("A"));
        assertTrue(log.contains("B"));
        assertTrue(log.contains("C"));
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Case: Throughput through a chain of actors.
     * </p>
     */
    @Test
    public void test20180827022013426538 ()
    {
        final List<String> log = new LinkedList<>();

        final Actor<String, String> actor1 = stage
                .newActor()
                .withScript((String x) -> String.format("(X %s)", x))
                .create();

        final Actor<String, String> actor2 = stage
                .newActor()
                .withScript((String y) -> String.format("(Y %s)", y))
                .create();

        final Actor<String, Boolean> actor3 = stage
                .newActor()
                .withScript((String z) -> log.add(String.format("(Z %s)", z)))
                .create();

        actor1.output().connect(actor2.input());
        actor2.output().connect(actor3.input());

        actor1.accept("A");
        actor1.accept("B");
        actor1.accept("C");

        for (int i = 0; i < 100; i++)
        {
            stage.crank();
        }

        assertEquals(3, log.size());
        assertEquals("(Z (Y (X A)))", log.get(0));
        assertEquals("(Z (Y (X B)))", log.get(1));
        assertEquals("(Z (Y (X C)))", log.get(2));
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
        assertTrue(actor1.output().isConnected(actor2.input()));
        assertTrue(actor1.output().isConnected(actor3.input()));

        /**
         * Verify that the inputs are connected to the output.
         */
        assertTrue(actor2.input().isConnected(actor1.output()));
        assertTrue(actor3.input().isConnected(actor1.output()));
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
     *
     * <p>
     * Case: Removal of Established Connection.
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
         * Verify the output is no longer connected to the inputs.
         */
        assertFalse(actor1.output().isConnected(actor2.input()));
        assertFalse(actor1.output().isConnected(actor3.input()));

        /**
         * Verify that the inputs are no longer connected to the output.
         */
        assertFalse(actor2.input().isConnected(actor1.output()));
        assertFalse(actor3.input().isConnected(actor1.output()));
    }

    /**
     * Test: 20180908023733492867
     *
     * <p>
     * Class: <code>Output</code>
     * </p>
     *
     * <p>
     * Method: <code>disconnect(Input)</code>
     * </p>
     *
     * <p>
     * Case: Removal of Non-Existent Connection.
     * </p>
     */
    @Test
    public void test20180908023733492867 ()
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
        assertEquals(actor1.output(), actor1.output().disconnect(actor2.input()));
        assertEquals(actor1.output(), actor1.output().disconnect(actor3.input()));

        /**
         * Verify the output is not connected to the inputs.
         */
        assertFalse(actor1.output().isConnected(actor2.input()));
        assertFalse(actor1.output().isConnected(actor3.input()));

        /**
         * Verify that the inputs are not connected to the output.
         */
        assertFalse(actor2.input().isConnected(actor1.output()));
        assertFalse(actor3.input().isConnected(actor1.output()));
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
     * Test: 20180908023934276934
     *
     * <p>
     * Class: <code>ActorTast</code>
     * </p>
     *
     * <p>
     * Case: Meta Object Storage.
     * </p>
     */
    @Test
    public void test20180908023934276934 ()
    {
        /**
         * Create an actor.
         */
        final Actor<Integer, Integer> actor = stage.newActor().withScript((Integer x) -> x).create();

        /**
         * Send ten messages through the actor.
         * The first message will cause an execution-counter to be added to the stage, for the actor.
         * Each subsequent message will cause th execution-counter to be incremented.
         * The counter is stored in the "meta" slot contained in the actor object.
         * The usage of the "meta" slot is what we are trying to test here.
         */
        IntStream.rangeClosed(1, 10).forEach(i -> actor.accept(i));
        IntStream.rangeClosed(1, 10).forEach(i -> stage.crank());

        /**
         * Verify that the counter was only added to the actor object once.
         * If the counter was added more than once, the value would be one probably.
         * Moreover, each time the actor executed, the counter was incremented.
         */
        assertEquals(10, stage.executionCounters.get(actor).get());
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
     *
     * <p>
     * Case: Duplicate Close.
     * </p>
     */
    @Test
    public void test20180827024427414111 ()
    {
        stage.close();
        stage.close();

        assertEquals(1, stage.whatHappened.size());
        assertEquals("CLOSE", stage.whatHappened.get(0).getKey());
    }

    /**
     * Test: 20180907235336724869
     *
     * <p>
     * Method: <code>newExecutorStage</code>
     * </p>
     *
     * <p>
     * Case: Throughput.
     * </p>
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void test20180907235336724869 ()
            throws InterruptedException
    {
        final ExecutorService service = Executors.newFixedThreadPool(4);
        final Stage stageToTest = Cascade.newExecutorStage(service);
        assertFalse(service.isShutdown());
        testStage(stageToTest);
        assertTrue(service.isShutdown());
    }

    /**
     * Test: 20180908003928729787
     *
     * <p>
     * Method: <code>newFixedPoolStage(int)</code>
     * </p>
     *
     * <p>
     * Case: Throughput.
     * </p>
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void test20180908003928729787 ()
            throws InterruptedException
    {
        final Stage stageToTest = Cascade.newFixedPoolStage(4);
        testStage(stageToTest);
    }

    /**
     * Test: 20180908004328143656
     *
     * <p>
     * Method: <code>newStage(int)</code>
     * </p>
     *
     * <p>
     * Case: Throughput.
     * </p>
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void test20180908004328143656 ()
            throws InterruptedException
    {
        final Stage stageToTest = Cascade.newStage(4);
        testStage(stageToTest);
    }

    /**
     * Test: 20180908011412680125
     *
     * <p>
     * Method: <code>newStage()</code>
     * </p>
     *
     * <p>
     * Case: Throughput.
     * </p>
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void test20180908011412680125 ()
            throws InterruptedException
    {
        final Stage stageToTest = Cascade.newStage();
        testStage(stageToTest);
    }

    /**
     * This method provides a common throughput test for <code>Stage</code>s.
     *
     * @param stageToTest will be tested.
     * @throws InterruptedException
     */
    private void testStage (final Stage stageToTest)
            throws InterruptedException
    {
        /**
         * By the end of the test, (2000) messages should be in the set of results.
         */
        final CountDownLatch permits = new CountDownLatch(2000);
        final Set<Integer> results = new ConcurrentSkipListSet<>();

        /**
         * Create a network of actors on the stage.
         * The source-actor feeds integers to the square-actor and the cube-actor.
         * The sink-actor receives integers from the square-actor and the cube-actor.
         * The sink-actor puts each integer into the set of results.
         * The sink-actor sends the integers to the term-actor.
         * The term-actor decrements the latch that prevents the unit-test
         * thread from exiting this method before the test is complete.
         */
        final Actor<Integer, Integer> source = stageToTest.newActor().withScript((Integer x) -> x).create();
        final Actor<Integer, Integer> square = stageToTest.newActor().withScript((Integer x) -> 10301 + (x * x)).create();
        final Actor<Integer, Integer> cube = stageToTest.newActor().withScript((Integer x) -> 12421 + (x * x * x)).create();
        final Actor<Integer, Boolean> sink = stageToTest.newActor().withScript((Integer x) -> results.add(x)).create();
        final Actor<Boolean, Boolean> term = stageToTest.newActor().withScript((Boolean x) -> permits.countDown()).create();
        source.output().connect(square.input());
        source.output().connect(cube.input());
        square.output().connect(sink.input());
        cube.output().connect(sink.input());
        sink.output().connect(term.input());

        /**
         * Cause the source-actor to have (1000) messages.
         * Each message will be duplicated.
         * Thus, a total of (2000) messages will pass through the network.
         */
        IntStream.rangeClosed(1, 1000).forEach(i -> source.input().send(i));

        /**
         * Wait for the actors to finish processing the messages.
         */
        permits.await(5, TimeUnit.SECONDS);

        /**
         * Verify that the actors processed the messages correctly.
         */
        assertEquals(2 * 1000, results.size());
        assertTrue(IntStream.range(1, 1000).allMatch(x -> results.contains(10301 + (x * x)))); // squares
        assertTrue(IntStream.range(1, 1000).allMatch(x -> results.contains(12421 + (x * x * x)))); // cubes

        /**
         * Shutdown any threads that are inside the stage.
         */
        stageToTest.close();
    }
}
