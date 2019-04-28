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
import com.mackenziehigh.cascade.Cascade.ArrayBlockingQueueMailbox;
import com.mackenziehigh.cascade.Cascade.ArrayDequeMailbox;
import com.mackenziehigh.cascade.Cascade.LinkedBlockingQueueMailbox;
import com.mackenziehigh.cascade.Cascade.PriorityBlockingQueueMailbox;
import com.mackenziehigh.cascade.Cascade.Stage;
import com.mackenziehigh.cascade.Cascade.Stage.Actor;
import com.mackenziehigh.cascade.Cascade.Stage.Actor.ConsumerScript;
import com.mackenziehigh.cascade.Cascade.Stage.Actor.FunctionScript;
import com.mackenziehigh.cascade.Cascade.Stage.Actor.Mailbox;
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
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
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
     * Test: 20190428003635593065
     *
     * <p>
     * Class: <code>ConcurrentLinkedQueueMailbox</code>
     * </p>
     *
     * <p>
     * Case: Basic Functionality.
     * </p>
     */
    @Test
    public void test20190428003635593065 ()
    {
        final Mailbox<String> mailbox = Cascade.ConcurrentLinkedQueueMailbox.create();
        assertNull(mailbox.poll());
        assertTrue(mailbox.offer("A"));
        assertTrue(mailbox.offer("B"));
        assertTrue(mailbox.offer("C"));
        assertEquals("A", mailbox.poll());
        assertEquals("B", mailbox.poll());
        assertEquals("C", mailbox.poll());
        assertNull(mailbox.poll());
    }

    /**
     * Test: 20190428003635593065
     *
     * <p>
     * Class: <code>LinkedBlockingQueueMailbox</code>
     * </p>
     *
     * <p>
     * Case: Basic Functionality.
     * </p>
     */
    @Test
    public void test20190428003808056212 ()
    {
        final Mailbox<String> mailbox1 = LinkedBlockingQueueMailbox.create();
        assertNull(mailbox1.poll());
        assertTrue(mailbox1.offer("A"));
        assertTrue(mailbox1.offer("B"));
        assertTrue(mailbox1.offer("C"));
        assertEquals("A", mailbox1.poll());
        assertEquals("B", mailbox1.poll());
        assertEquals("C", mailbox1.poll());
        assertNull(mailbox1.poll());

        final Mailbox<String> mailbox2 = LinkedBlockingQueueMailbox.create(3);
        assertNull(mailbox2.poll());
        assertTrue(mailbox2.offer("A"));
        assertTrue(mailbox2.offer("B"));
        assertTrue(mailbox2.offer("C"));
        assertFalse(mailbox2.offer("D"));
        assertFalse(mailbox2.offer("E"));
        assertEquals("A", mailbox2.poll());
        assertEquals("B", mailbox2.poll());
        assertEquals("C", mailbox2.poll());
        assertNull(mailbox2.poll());
    }

    /**
     * Test: 20190428003635593065
     *
     * <p>
     * Class: <code>ArrayBlockingQueueMailbox</code>
     * </p>
     *
     * <p>
     * Case: Basic Functionality.
     * </p>
     */
    @Test
    public void test20190428003808056283 ()
    {
        final Mailbox<String> mailbox = ArrayBlockingQueueMailbox.create(3);
        assertNull(mailbox.poll());
        assertTrue(mailbox.offer("A"));
        assertTrue(mailbox.offer("B"));
        assertTrue(mailbox.offer("C"));
        assertFalse(mailbox.offer("D"));
        assertFalse(mailbox.offer("E"));
        assertEquals("A", mailbox.poll());
        assertEquals("B", mailbox.poll());
        assertEquals("C", mailbox.poll());
        assertNull(mailbox.poll());
    }

    /**
     * Test: 20190428003635593065
     *
     * <p>
     * Class: <code>ArrayDequeMailbox</code>
     * </p>
     *
     * <p>
     * Case: Basic Functionality.
     * </p>
     */
    @Test
    public void test20190428003808056307 ()
    {
        final Mailbox<String> mailbox = ArrayDequeMailbox.create(0, 3);
        assertNull(mailbox.poll());
        assertTrue(mailbox.offer("A"));
        assertTrue(mailbox.offer("B"));
        assertTrue(mailbox.offer("C"));
        assertFalse(mailbox.offer("D"));
        assertFalse(mailbox.offer("E"));
        assertEquals("A", mailbox.poll());
        assertEquals("B", mailbox.poll());
        assertEquals("C", mailbox.poll());
        assertNull(mailbox.poll());
    }

    /**
     * Test: 20190428003635593065
     *
     * <p>
     * Class: <code>CircularArrayDequeMailbox</code>
     * </p>
     *
     * <p>
     * Case: Basic Functionality.
     * </p>
     */
    @Test
    public void test20190428003808056329 ()
    {
        final Mailbox<String> mailbox = Cascade.CircularArrayDequeMailbox.create(0, 3);
        assertNull(mailbox.poll());
        assertTrue(mailbox.offer("A"));
        assertTrue(mailbox.offer("B"));
        assertTrue(mailbox.offer("C"));
        assertTrue(mailbox.offer("D"));
        assertTrue(mailbox.offer("E"));
        assertEquals("C", mailbox.poll());
        assertEquals("D", mailbox.poll());
        assertEquals("E", mailbox.poll());
        assertNull(mailbox.poll());
    }

    /**
     * Test: 20190428003635593065
     *
     * <p>
     * Class: <code>PriorityBlockingQueueMailbox</code>
     * </p>
     *
     * <p>
     * Case: Basic Functionality.
     * </p>
     */
    @Test
    public void test20190428003808056347 ()
    {
        final Mailbox<String> mailbox = PriorityBlockingQueueMailbox.create(1, String.CASE_INSENSITIVE_ORDER);
        assertNull(mailbox.poll());
        assertTrue(mailbox.offer("A"));
        assertTrue(mailbox.offer("X"));
        assertTrue(mailbox.offer("B"));
        assertTrue(mailbox.offer("Y"));
        assertTrue(mailbox.offer("C"));
        assertTrue(mailbox.offer("Z"));
        assertTrue(mailbox.offer("D"));
        assertTrue(mailbox.offer("X"));
        assertTrue(mailbox.offer("E"));
        assertEquals("A", mailbox.poll());
        assertEquals("B", mailbox.poll());
        assertEquals("C", mailbox.poll());
        assertEquals("D", mailbox.poll());
        assertEquals("E", mailbox.poll());
        assertEquals("X", mailbox.poll());
        assertEquals("X", mailbox.poll());
        assertEquals("Y", mailbox.poll());
        assertEquals("Z", mailbox.poll());
        assertNull(mailbox.poll());
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
        final Actor<Integer, Integer> source = stage.newActor().withFunctionScript(sourceScript).create();
        final Actor<Integer, Integer> expr = stage.newActor().withFunctionScript(exprScript).create();
        final Actor<Integer, Integer> sink = stage.newActor().withConsumerScript(sinkScript).create();
        final Actor<Integer, Integer> bug = stage.newActor().withConsumerScript(bugScript).create();
        source.output().connect(expr.input());
        expr.output().connect(sink.input());
        sink.output().connect(bug.input());

        /**
         * Send ten messages through the network of actors.
         */
        IntStream.rangeClosed(1, 10).forEach(i -> source.context().sendTo(i));

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
     * Case: By default, the actor will use a ConcurrentLinkedQueue to store inputs.
     * </p>
     *
     * @throws java.lang.Throwable
     */
    @Test
    public void test20180908022610095209 ()
            throws Throwable
    {
        final Actor<Object, Object> actor = stage.newActor().create();

        assertEquals(Cascade.ConcurrentLinkedQueueMailbox.class, getField(actor, "mailbox", Mailbox.class).getClass());
    }

    /**
     * Test: 20190427204049214123
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Case: By default, the actor will use a script that does not forward messages.
     * </p>
     */
    @Test
    public void test20190427204049214123 ()
    {
        final Queue<Object> queue = new LinkedBlockingQueue<>();

        /**
         * This is the actor under test.
         */
        final Actor<Object, Object> actor1 = stage.newActor().create();

        /**
         * If this actor does not receive any outputs from the previous actor,
         * then the script did not forward any messages.
         */
        final Actor<Object, Object> actor2 = stage.newActor().withConsumerScript(x -> queue.add(x)).create();
        actor1.output().connect(actor2.input());

        /**
         * Run Test.
         */
        actor2.input().offer("A");
        stage.crank();
        actor1.input().offer("B");
        stage.crank();
        actor2.input().offer("C");
        stage.crank();

        assertEquals(2, queue.size());
        assertTrue(queue.contains("A"));
        assertFalse(queue.contains("B"));
        assertTrue(queue.contains("C"));
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
                .withFunctionScript((Integer x) -> x / 0) // div by zero
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
                .withFunctionScript((Integer x) -> (x / x) * x) // potential div by zero
                .withErrorHandler(ex -> System.out.println(1 / 0)) // another div by zero
                .create();

        /**
         * This actor will receive messages from the previous actor,
         * unless the previous actor experienced an exception.
         */
        final List<Integer> results = new LinkedList<>();
        final Actor<Integer, ?> sink = stage.newActor().withFunctionScript((Integer x) -> results.add(x)).create();
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
                .withFunctionScript((Integer x) -> x / 0) // div by zero
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
                .withFunctionScript((Integer x) -> (x / x) * x) // potential div by zero
                .create();

        /**
         * This actor will receive messages from the previous actor,
         * unless the previous actor experienced an exception.
         */
        final List<Integer> results = new LinkedList<>();
        final Actor<Integer, ?> sink = stage.newActor().withFunctionScript((Integer x) -> results.add(x)).create();
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
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Method: <code>withCircularArrayMailbox(int)</code>
     * </p>
     *
     * <p>
     * Case: The mailbox serves as a ring-buffer.
     * </p>
     *
     * @throws java.lang.Exception
     */
    @Test
    public void test20190427214843891381 ()
            throws Exception
    {
        final List<String> log = new ArrayList<>();

        final Mailbox<String> mailbox = Cascade.CircularArrayDequeMailbox.create(0, 3);
        final Queue<String> mailQueue = getField(mailbox, "queue", Queue.class);

        final Actor<String, Boolean> actor = stage
                .newActor()
                .withFunctionScript((String x) -> log.add(x))
                .withMailbox(mailbox)
                .create();

        assertSame(mailbox, getField(actor, "mailbox", Mailbox.class));

        assertTrue(mailQueue.isEmpty());

        assertTrue(actor.input().offer("A"));
        assertTrue(actor.input().offer("B"));
        assertTrue(actor.input().offer("C"));
        assertTrue(actor.input().offer("D"));
        assertTrue(actor.input().offer("E"));

        assertEquals(3, mailQueue.size());
        assertTrue(mailQueue.contains("C"));
        assertTrue(mailQueue.contains("D"));
        assertTrue(mailQueue.contains("E"));
        assertEquals(0, log.size());

        stage.crank();
        stage.crank();
        stage.crank();
        stage.crank();
        stage.crank();
        stage.crank();
        stage.crank();
        stage.crank();
        stage.crank();
        stage.crank();

        assertTrue(mailQueue.isEmpty());
        assertEquals(3, log.size());
        assertEquals("C", log.get(0));
        assertEquals("D", log.get(1));
        assertEquals("E", log.get(2));
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Method: <code>withCircularArrayMailbox(int)</code>
     * </p>
     *
     * <p>
     * Case: Stress Test of Ring Buffer.
     * </p>
     *
     * @throws java.lang.Exception
     */
    @Test
    public void test20190427223450580290 ()
            throws Exception
    {
        final int capacity = 3;

        final Mailbox<Object> mailbox = Cascade.CircularArrayDequeMailbox.create(1, capacity);

        final Queue<Object> expected = new ArrayDeque<>(1024);

        /**
         * Perform the test multiple times, so that the head/tail pointers
         * are forced to move through the backing array and then wrap around.
         */
        for (int k = 0; k < 100; k++)
        {
            /**
             * Perform varying numbers of insertions.
             */
            for (int i = 0; i < 25; i++)
            {
                /**
                 * Perform removals.
                 * Sometimes perform more removals than insertions.
                 */
                for (int r = 0; r < 25; r++)
                {
                    /**
                     * Ensure both the queue and mailbox are empty.
                     */
                    expected.clear();
                    for (int n = 0; n < capacity; n++)
                    {
                        mailbox.poll(); // clear
                    }

                    /**
                     * Perform the insertions.
                     */
                    for (int x = 0; x < i; x++)
                    {
                        final Object msg = new Object();

                        assertTrue(mailbox.offer(msg));

                        if (expected.size() < capacity)
                        {
                            expected.offer(msg);
                        }
                        else
                        {
                            expected.poll();
                            expected.offer(msg);
                        }
                    }

                    assertEquals(Math.min(capacity, i), expected.size());

                    /**
                     * Perform the removals.
                     */
                    for (int x = 0; x < r; x++)
                    {
                        assertEquals(expected.poll(), mailbox.poll());
                    }
                }
            }
        }
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Method: <code>withPriorityQueueMailbox(int)</code>
     * </p>
     *
     * <p>
     * Case: Ordering is based on priority.
     * </p>
     *
     * @throws java.lang.Exception
     */
    @Test
    public void test20190427212635644446 ()
            throws Exception
    {
        final List<String> log = new ArrayList<>();

        final Mailbox<String> mailbox = PriorityBlockingQueueMailbox.create(1, String.CASE_INSENSITIVE_ORDER.reversed());
        final Queue<String> mailQueue = getField(mailbox, "queue", Queue.class);

        final Actor<String, Boolean> actor = stage
                .newActor()
                .withFunctionScript((String x) -> log.add(x))
                .withMailbox(mailbox)
                .create();

        assertSame(mailbox, getField(actor, "mailbox", Mailbox.class));

        assertTrue(mailQueue.isEmpty());

        assertTrue(actor.input().offer("A"));
        assertTrue(actor.input().offer("B"));
        assertTrue(actor.input().offer("C"));
        assertTrue(actor.input().offer("D"));
        assertTrue(actor.input().offer("E"));

        assertEquals(5, mailQueue.size());
        assertTrue(mailQueue.contains("A"));
        assertTrue(mailQueue.contains("B"));
        assertTrue(mailQueue.contains("C"));
        assertTrue(mailQueue.contains("D"));
        assertTrue(mailQueue.contains("E"));
        assertEquals(0, log.size());

        stage.crank();
        stage.crank();
        stage.crank();
        stage.crank();
        stage.crank();
        stage.crank();
        stage.crank();
        stage.crank();
        stage.crank();
        stage.crank();

        assertEquals(0, mailQueue.size());
        assertEquals(5, log.size());
        assertEquals("E", log.get(0));
        assertEquals("D", log.get(1));
        assertEquals("C", log.get(2));
        assertEquals("B", log.get(3));
        assertEquals("A", log.get(4));
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Method: <code>withArrayDequeMailbox(int)</code>
     * </p>
     *
     * <p>
     * Case: Capacity Overflow.
     * </p>
     *
     * @throws java.lang.Exception
     */
    @Test
    public void test20190427211309547456 ()
            throws Exception
    {
        final List<String> log = new ArrayList<>();

        final Mailbox<String> mailbox = ArrayDequeMailbox.create(0, 3);
        final Queue<String> mailQueue = getField(mailbox, "queue", Queue.class);

        final Actor<String, Boolean> actor = stage
                .newActor()
                .withFunctionScript((String x) -> log.add(x))
                .withMailbox(mailbox)
                .create();

        assertSame(mailbox, getField(actor, "mailbox", Mailbox.class));

        assertTrue(mailQueue.isEmpty());

        assertTrue(actor.input().offer("A"));
        assertTrue(actor.input().offer("B"));
        assertTrue(actor.input().offer("C"));
        assertFalse(actor.input().offer("D"));
        assertFalse(actor.input().offer("E"));

        assertEquals(3, mailQueue.size());
        assertTrue(mailQueue.contains("A"));
        assertTrue(mailQueue.contains("B"));
        assertTrue(mailQueue.contains("C"));
        assertEquals(0, log.size());

        stage.crank();
        stage.crank();
        stage.crank();
        stage.crank();
        stage.crank();

        assertEquals(0, mailQueue.size());
        assertEquals(3, log.size());
        assertEquals("A", log.get(0));
        assertEquals("B", log.get(1));
        assertEquals("C", log.get(2));
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Method: <code>withLinkedBlockingQueueMailbox(int)</code>
     * </p>
     *
     * <p>
     * Case: Capacity Overflow.
     * </p>
     *
     * @throws java.lang.Exception
     */
    @Test
    public void test20190427211116986712 ()
            throws Exception
    {
        final List<String> log = new ArrayList<>();

        final Mailbox<String> mailbox = LinkedBlockingQueueMailbox.create(3);
        final Queue<String> mailQueue = getField(mailbox, "queue", Queue.class);

        final Actor<String, Boolean> actor = stage
                .newActor()
                .withFunctionScript((String x) -> log.add(x))
                .withMailbox(mailbox)
                .create();

        assertSame(mailbox, getField(actor, "mailbox", Mailbox.class));

        assertTrue(mailQueue.isEmpty());

        assertTrue(actor.input().offer("A"));
        assertTrue(actor.input().offer("B"));
        assertTrue(actor.input().offer("C"));
        assertFalse(actor.input().offer("D"));
        assertFalse(actor.input().offer("E"));

        assertEquals(3, mailQueue.size());
        assertTrue(mailQueue.contains("A"));
        assertTrue(mailQueue.contains("B"));
        assertTrue(mailQueue.contains("C"));
        assertEquals(0, log.size());

        stage.crank();
        stage.crank();
        stage.crank();
        stage.crank();
        stage.crank();

        assertEquals(0, mailQueue.size());
        assertEquals(3, log.size());
        assertEquals("A", log.get(0));
        assertEquals("B", log.get(1));
        assertEquals("C", log.get(2));
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Method: <code>withArrayBlockingQueueMailbox(int)</code>
     * </p>
     *
     * <p>
     * Case: Capacity Overflow.
     * </p>
     *
     * @throws java.lang.Exception
     */
    @Test
    public void test20190427210552304276 ()
            throws Exception
    {
        final List<String> log = new ArrayList<>();

        final Mailbox<String> mailbox = ArrayBlockingQueueMailbox.create(3);
        final Queue<String> queue = getField(mailbox, "queue", Queue.class);

        final Actor<String, Boolean> actor = stage
                .newActor()
                .withFunctionScript((String x) -> log.add(x))
                .withMailbox(mailbox)
                .create();

        assertSame(mailbox, getField(actor, "mailbox", Mailbox.class));

        assertTrue(queue.isEmpty());
        assertTrue(actor.input().offer("A"));
        assertTrue(actor.input().offer("B"));
        assertTrue(actor.input().offer("C"));
        assertFalse(actor.input().offer("D"));
        assertFalse(actor.input().offer("E"));

        assertEquals(3, queue.size());
        assertTrue(queue.contains("A"));
        assertTrue(queue.contains("B"));
        assertTrue(queue.contains("C"));
        assertEquals(0, log.size());

        stage.crank();
        stage.crank();
        stage.crank();
        stage.crank();
        stage.crank();

        assertEquals(0, queue.size());
        assertEquals(3, log.size());
        assertEquals("A", log.get(0));
        assertEquals("B", log.get(1));
        assertEquals("C", log.get(2));
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
                .withFunctionScript((String x) -> x)
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
                .withFunctionScript((String x) -> x)
                .create();

        final Actor<String, String> actor2 = stage
                .newActor()
                .withFunctionScript((String x) -> x)
                .create();

        final Actor<String, String> actor3 = stage
                .newActor()
                .withFunctionScript((String x) -> x)
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
                .withFunctionScript((String x) -> x)
                .create();

        final Actor<String, String> actor2 = stage
                .newActor()
                .withFunctionScript((String x) -> x)
                .create();

        final Actor<String, String> actor3 = stage
                .newActor()
                .withFunctionScript((String x) -> x)
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

        final Mailbox<String> mailbox = ArrayBlockingQueueMailbox.create(3);
        final Queue<String> mailQueue = getField(mailbox, "queue", Queue.class);

        final Actor<String, Boolean> actor = stage
                .newActor()
                .withFunctionScript((String x) -> log.add(x))
                .withMailbox(mailbox)
                .create();

        assertSame(mailbox, getField(actor, "mailbox", Mailbox.class));

        assertTrue(mailQueue.isEmpty());

        assertEquals(actor.input(), actor.input().send("A"));
        assertEquals(actor.input(), actor.input().send("B"));
        assertEquals(actor.input(), actor.input().send("C"));

        assertEquals(3, mailQueue.size());
        assertTrue(mailQueue.contains("A"));
        assertTrue(mailQueue.contains("B"));
        assertTrue(mailQueue.contains("C"));
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

        final Mailbox<String> mailbox = LinkedBlockingQueueMailbox.create(3);
        final Queue<String> mailQueue = getField(mailbox, "queue", Queue.class);

        final Actor<String, Boolean> actor = stage
                .newActor()
                .withFunctionScript((String x) -> log.add(x))
                .withMailbox(mailbox)
                .create();

        assertSame(mailbox, getField(actor, "mailbox", Mailbox.class));

        assertTrue(mailQueue.isEmpty());
        assertTrue(actor.input().offer("A"));
        assertTrue(actor.input().offer("B"));
        assertTrue(actor.input().offer("C"));
        assertFalse(actor.input().offer("D"));
        assertFalse(actor.input().offer("E"));

        assertEquals(3, mailQueue.size());
        assertTrue(mailQueue.contains("A"));
        assertTrue(mailQueue.contains("B"));
        assertTrue(mailQueue.contains("C"));
        assertEquals(0, log.size());

        stage.crank();
        stage.crank();
        stage.crank();

        assertEquals(0, mailQueue.size());
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
                .withFunctionScript((String x) -> String.format("(X %s)", x))
                .create();

        final Actor<String, String> actor2 = stage
                .newActor()
                .withFunctionScript((String y) -> String.format("(Y %s)", y))
                .create();

        final Actor<String, Boolean> actor3 = stage
                .newActor()
                .withFunctionScript((String z) -> log.add(String.format("(Z %s)", z)))
                .create();

        actor1.output().connect(actor2.input());
        actor2.output().connect(actor3.input());

        actor1.input().send("A");
        actor1.input().send("B");
        actor1.input().send("C");

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
                .withFunctionScript((String x) -> x)
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
                .withFunctionScript((String x) -> x)
                .create();

        final Actor<String, String> actor2 = stage
                .newActor()
                .withFunctionScript((String x) -> x)
                .create();

        final Actor<String, String> actor3 = stage
                .newActor()
                .withFunctionScript((String x) -> x)
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
                .withFunctionScript((String x) -> x)
                .create();

        final Actor<String, String> actor2 = stage
                .newActor()
                .withFunctionScript((String x) -> x)
                .create();

        final Actor<String, String> actor3 = stage
                .newActor()
                .withFunctionScript((String x) -> x)
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
                .withFunctionScript((String x) -> x)
                .create();

        final Actor<String, String> actor2 = stage
                .newActor()
                .withFunctionScript((String x) -> x)
                .create();

        final Actor<String, String> actor3 = stage
                .newActor()
                .withFunctionScript((String x) -> x)
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
                .withFunctionScript((String x) -> x)
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
                .withFunctionScript((String x) -> x)
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
                .withFunctionScript((String x) -> x)
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
        final Actor<Integer, Integer> actor = stage.newActor().withFunctionScript((Integer x) -> x).create();

        /**
         * Send ten messages through the actor.
         * The first message will cause an execution-counter to be added to the stage, for the actor.
         * Each subsequent message will cause th execution-counter to be incremented.
         * The counter is stored in the "meta" slot contained in the actor object.
         * The usage of the "meta" slot is what we are trying to test here.
         */
        IntStream.rangeClosed(1, 10).forEach(i -> actor.input().send(i));
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
        final Stage stageToTest = Cascade.newStage(service);
        assertFalse(service.isShutdown());
        testStage(stageToTest);
        assertTrue(service.isShutdown());
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
        final Actor<Integer, Integer> source = stageToTest.newActor().withFunctionScript((Integer x) -> x).create();
        final Actor<Integer, Integer> square = stageToTest.newActor().withFunctionScript((Integer x) -> 10301 + (x * x)).create();
        final Actor<Integer, Integer> cube = stageToTest.newActor().withFunctionScript((Integer x) -> 12421 + (x * x * x)).create();
        final Actor<Integer, Boolean> sink = stageToTest.newActor().withFunctionScript((Integer x) -> results.add(x)).create();
        final Actor<Boolean, Boolean> term = stageToTest.newActor().withConsumerScript((Boolean x) -> permits.countDown()).create();
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
