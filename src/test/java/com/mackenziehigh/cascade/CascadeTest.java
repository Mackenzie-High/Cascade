/*
 * Copyright 2017 Mackenzie High
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
import com.mackenziehigh.cascade.Cascade.AbstractStage.DefaultActor;
import com.mackenziehigh.cascade.Cascade.ArrayBlockingQueueMailbox;
import com.mackenziehigh.cascade.Cascade.ArrayDequeMailbox;
import com.mackenziehigh.cascade.Cascade.ConcurrentLinkedQueueMailbox;
import com.mackenziehigh.cascade.Cascade.LinkedBlockingQueueMailbox;
import com.mackenziehigh.cascade.Cascade.PriorityBlockingQueueMailbox;
import com.mackenziehigh.cascade.Cascade.Stage;
import com.mackenziehigh.cascade.Cascade.Stage.Actor;
import com.mackenziehigh.cascade.Cascade.Stage.Actor.ConsumerErrorHandler;
import com.mackenziehigh.cascade.Cascade.Stage.Actor.ConsumerScript;
import com.mackenziehigh.cascade.Cascade.Stage.Actor.Context;
import com.mackenziehigh.cascade.Cascade.Stage.Actor.ContextErrorHandler;
import com.mackenziehigh.cascade.Cascade.Stage.Actor.ContextScript;
import com.mackenziehigh.cascade.Cascade.Stage.Actor.FunctionScript;
import com.mackenziehigh.cascade.Cascade.Stage.Actor.Mailbox;
import java.lang.reflect.Field;
import java.util.AbstractMap;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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

        private final Queue<DefaultActor<?, ?>> tasks = new LinkedBlockingQueue<>();

        @Override
        protected void onRunnable (final DefaultActor<?, ?> actor)
        {
            if (actor.meta() == null)
            {
                final AtomicInteger counter = new AtomicInteger(1);
                actor.meta(counter);
                executionCounters.put(actor, counter);
            }
            else
            {
                ((AtomicInteger) actor.meta()).incrementAndGet();
            }

            whatHappened.add(new AbstractMap.SimpleImmutableEntry<>("SUBMIT", actor));
            tasks.add(actor);
        }

        @Override
        protected void onClose ()
        {
            whatHappened.add(new AbstractMap.SimpleImmutableEntry<>("CLOSE", null));
        }

        public DefaultActor<?, ?> crank ()
        {
            DefaultActor<?, ?> task = tasks.poll();

            while (task != null)
            {
                task.run();
                task = tasks.poll();
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
     * Test: 20190606234154824066
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Method: Default Error Handler.
     * </p>
     *
     * <p>
     * Case: Basic Functionality.
     * </p>
     */
    @Test
    public void test20190606234154824066 ()
    {
        /**
         * This actor will always cause an exception due to a div by zero.
         */
        final Actor<Integer, Integer> actor = stage
                .newActor()
                .withFunctionScript((Integer x) -> x / 0) // div by zero
                .create();

        /**
         * Cause an exception, which will be silently dropped.
         */
        actor.input().send(1);
        stage.crank();
    }

    /**
     * Test: 20190606215647910314
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Method: <code>withConsumerErrorHandler()</code>
     * </p>
     *
     * <p>
     * Case: Basic Functionality.
     * </p>
     */
    @Test
    public void test20190606215647910314 ()
    {
        final BlockingQueue<Throwable> errors = new LinkedBlockingQueue<>();

        final ConsumerErrorHandler errorHandler = (cause) ->
        {
            errors.add(cause);
        };

        /**
         * This actor will always cause an exception due to a div by zero.
         */
        final Actor<Integer, Integer> actor = stage
                .newActor()
                .withFunctionScript((Integer x) -> x / 0) // div by zero
                .withConsumerErrorHandler(errorHandler)
                .create();

        /**
         * Cause an exception.
         */
        actor.input().send(1);
        stage.crank();

        /**
         * The actor caught the exception.
         */
        final Throwable error = errors.poll();
        assertTrue(error instanceof ArithmeticException);

        /**
         * Verify that the error-handler was given the actor context as an argument.
         */
        assertNotNull(actor.context());
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Method: <code>withContextErrorHandler()</code>
     * </p>
     *
     * <p>
     * Case: Basic Functionality.
     * </p>
     */
    @Test
    public void test20180826232927857314 ()
    {
        final AtomicReference<Context<Integer, Integer>> context = new AtomicReference<>();

        final AtomicReference<Integer> message = new AtomicReference<>();

        final BlockingQueue<Throwable> errors = new LinkedBlockingQueue<>();

        final ContextErrorHandler<Integer, Integer> errorHandler = (ctx, msg, cause) ->
        {
            context.set(ctx);
            message.set(msg);
            errors.add(cause);
        };

        /**
         * This actor will always cause an exception due to a div by zero.
         */
        final Actor<Integer, Integer> actor = stage
                .newActor()
                .withFunctionScript((Integer x) -> x / 0) // div by zero
                .withContextErrorHandler(errorHandler)
                .create();

        /**
         * Cause an exception.
         */
        actor.input().send(42);
        stage.crank();

        /**
         * The actor caught the exception.
         */
        final Throwable error = errors.poll();
        assertTrue(error instanceof ArithmeticException);

        /**
         * Verify that the error-handler was given the actor context as an argument.
         */
        assertNotNull(actor.context());
        assertSame(actor.context(), context.get());

        /**
         * Verify that the error-handler was given the message as an argument.
         */
        assertEquals((Object) 42, message.get());
    }

    /**
     * Test: 20180908031439953144
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Method: <code>withContextErrorHandler()</code>
     * </p>
     *
     * <p>
     * Case: Exception in Error-Handler.
     * </p>
     */
    @Test
    public void test20180908031439953144 ()
    {
        final BlockingQueue<String> errors = new LinkedBlockingQueue<>();

        final ConsumerScript<Integer> script = msg ->
        {
            throw new Throwable("T0");
        };

        final ContextErrorHandler<Integer, Integer> errorHandler1 = (context, message, cause) ->
        {
            assertNotNull(context);
            errors.add("H1" + cause.getMessage()); // H1T0
            throw new Throwable("T1");
        };

        final ContextErrorHandler<Integer, Integer> errorHandler2 = (context, message, cause) ->
        {
            assertNotNull(context);
            errors.add("H2" + cause.getMessage()); // H2T0
            throw new Throwable("T2");
        };

        final ContextErrorHandler<Integer, Integer> errorHandler3 = (context, message, cause) ->
        {
            assertNotNull(context);
            errors.add("H3" + cause.getMessage()); // H3T0
            throw new Throwable("T3");
        };

        final ContextErrorHandler<Integer, Integer> errorHandler4 = (context, message, cause) ->
        {
            assertNotNull(context);
            errors.add("H4" + cause.getMessage()); // H4T0
        };

        /**
         * This actor will always cause an exception due to a div by zero.
         */
        final Actor<Integer, Integer> actor = stage
                .newActor()
                .withConsumerScript(script)
                .withContextErrorHandler(errorHandler1)
                .withContextErrorHandler(errorHandler2)
                .withContextErrorHandler(errorHandler3)
                .withContextErrorHandler(errorHandler4)
                .create();

        /**
         * Cause an exception.
         */
        actor.input().send(1);
        stage.crank();

        /**
         * The handlers all receive the original exception as input.
         * The exceptions thrown by handlers are simply ignored.
         */
        assertEquals("H1T0", errors.poll());
        assertEquals("H2T0", errors.poll());
        assertEquals("H3T0", errors.poll());
        assertEquals("H4T0", errors.poll());
        assertTrue(errors.isEmpty());
    }

    /**
     * Test: 20190606220439730858
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Method: <code>withConsumerErrorHandler()</code>
     * </p>
     *
     * <p>
     * Case: Exception in Error-Handler.
     * </p>
     */
    @Test
    public void test20190606220439730858 ()
    {
        final BlockingQueue<String> errors = new LinkedBlockingQueue<>();

        final ConsumerScript<Integer> script = msg ->
        {
            throw new Throwable("T0");
        };

        final ConsumerErrorHandler errorHandler1 = (cause) ->
        {
            errors.add("H1" + cause.getMessage()); // H1T0
            throw new Throwable("T1");
        };

        final ConsumerErrorHandler errorHandler2 = (cause) ->
        {
            errors.add("H2" + cause.getMessage()); // H2T0
            throw new Throwable("T2");
        };

        final ConsumerErrorHandler errorHandler3 = (cause) ->
        {
            errors.add("H3" + cause.getMessage()); // H3T0
            throw new Throwable("T3");
        };

        final ConsumerErrorHandler errorHandler4 = (cause) ->
        {
            errors.add("H4" + cause.getMessage()); // H4T0
        };

        /**
         * This actor will always cause an exception due to a div by zero.
         */
        final Actor<Integer, Integer> actor = stage
                .newActor()
                .withConsumerScript(script)
                .withConsumerErrorHandler(errorHandler1)
                .withConsumerErrorHandler(errorHandler2)
                .withConsumerErrorHandler(errorHandler3)
                .withConsumerErrorHandler(errorHandler4)
                .create();

        /**
         * Cause an exception.
         */
        actor.input().send(1);
        stage.crank();

        /**
         * The handlers all receive the original exception as input.
         * The exceptions thrown by handlers are simply ignored.
         */
        assertEquals("H1T0", errors.poll());
        assertEquals("H2T0", errors.poll());
        assertEquals("H3T0", errors.poll());
        assertEquals("H4T0", errors.poll());
        assertTrue(errors.isEmpty());
    }

    /**
     * Test: 20180826010008846278
     *
     * <p>
     * Class: <code>CircularArrayDequeMailbox</code>
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
     * Class: <code>CircularArrayDequeMailbox</code>
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
     * Class: <code>LinkedBlockingQueueMailbox</code>
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
     * Class: <code>ArrayBlockingQueueMailbox</code>
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

        assertEquals(3, log.size());
        assertTrue(log.contains("A"));
        assertTrue(log.contains("B"));
        assertTrue(log.contains("C"));
    }

    /**
     * Test: 20190428010710826349
     *
     * <p>
     * Class: <code>Output</code>
     * </p>
     *
     * <p>
     * Case: Connection and Disconnection.
     * </p>
     */
    @Test
    public void test20190428010710826349 ()
    {
        final Actor<Object, Object> actor1 = stage.newActor().create();
        final Actor<Object, Object> actor2 = stage.newActor().create();

        /**
         * Not connected.
         */
        assertFalse(actor1.output().isConnected(actor2.input()));
        assertFalse(actor2.input().isConnected(actor1.output()));

        /**
         * Connect.
         */
        actor1.output().connect(actor2.input());
        assertTrue(actor1.output().isConnected(actor2.input()));
        assertTrue(actor2.input().isConnected(actor1.output()));

        /**
         * Connect (ignored).
         */
        actor1.output().connect(actor2.input());
        assertTrue(actor1.output().isConnected(actor2.input()));
        assertTrue(actor2.input().isConnected(actor1.output()));

        /**
         * Disconnect.
         */
        actor1.output().disconnect(actor2.input());
        assertFalse(actor1.output().isConnected(actor2.input()));
        assertFalse(actor2.input().isConnected(actor1.output()));

        /**
         * Disconnect (ignored).
         */
        actor1.output().disconnect(actor2.input());
        assertFalse(actor1.output().isConnected(actor2.input()));
        assertFalse(actor2.input().isConnected(actor1.output()));
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

        stage.crank();

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
     * Test: 20190606210829294732
     *
     * <p>
     * Class: <code>Context</code>
     * </p>
     *
     * <p>
     * Method: <code>actor()</code>
     * </p>
     *
     * <p>
     * Case: Verify that the method returns the enclosing actor.
     * </p>
     */
    @Test
    public void test20190606210829294732 ()
    {
        final Actor<String, String> actor = stage
                .newActor()
                .withFunctionScript((String x) -> x)
                .create();

        assertNotNull(actor);
        assertNotNull(actor.context());
        assertNotNull(actor.context().actor());
        assertSame(actor, actor.context().actor());
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
     * Class: <code>DefaultActor</code>
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

    /**
     * Test: 20190518225309075324
     *
     * <p>
     * Class: <code>AbstractStage</code>
     * </p>
     *
     * <p>
     * Case: Exception in <code>onRunnable()</code>.
     * </p>
     */
    @Test
    public void test20190518225309075324 ()
    {
        final AtomicInteger closeCount = new AtomicInteger();

        final AbstractStage customStage = new AbstractStage()
        {
            @Override
            protected void onRunnable (final DefaultActor<?, ?> actor)
            {
                throw new RuntimeException();
            }

            @Override
            protected void onClose ()
            {
                closeCount.incrementAndGet();
            }
        };

        final Actor<String, String> actor = customStage.newActor().withFunctionScript((String x) -> x).create();

        assertEquals(0, closeCount.get());
        actor.input().send("X");
        assertEquals(1, closeCount.get());
        actor.input().send("X");
        assertEquals(1, closeCount.get());
    }

    /**
     * Test: 20190606211312619428
     *
     * <p>
     * Class: <code>ContextErrorHandler</code>
     * </p>
     *
     * <p>
     * Method: <code>silent()</code>
     * </p>
     *
     * <p>
     * Case: Suppress Exceptions.
     * </p>
     */
    @Test
    public void test20190606211312619428 ()
    {
        final ContextErrorHandler<String, String> handler = (context, message, cause) ->
        {
            throw new Throwable();
        };

        try
        {
            handler.silent().onError(null, null, null);
        }
        catch (Throwable ex)
        {
            fail();
        }
    }

    /**
     * Test: 20190606211312619471
     *
     * <p>
     * Class: <code>ConsumerErrorHandler</code>
     * </p>
     *
     * <p>
     * Method: <code>silent()</code>
     * </p>
     *
     * <p>
     * Case: Suppress Exceptions.
     * </p>
     */
    @Test
    public void test20190606211312619471 ()
    {
        final ConsumerErrorHandler handler = (cause) ->
        {
            throw new Throwable();
        };

        try
        {
            handler.silent().onError(null);
        }
        catch (Throwable ex)
        {
            fail();
        }
    }

    /**
     * Test: 20190606211312619489
     *
     * <p>
     * Class: <code>ContextErrorHandler</code>
     * </p>
     *
     * <p>
     * Method: <code>andThen(ContextErrorHandler)</code>
     * </p>
     *
     * <p>
     * Case: Chaining and Exception Suppression.
     * </p>
     */
    @Test
    public void test20190606211312619489 ()
    {
        final List<String> list = new CopyOnWriteArrayList<>();

        final ContextErrorHandler<String, String> handler1 = (context, message, cause) ->
        {
            list.add("A");
            throw new Throwable();
        };

        final ContextErrorHandler<String, String> handler2 = (context, message, cause) ->
        {
            list.add("B"); // No Error
        };

        final ContextErrorHandler<String, String> handler3 = (context, message, cause) ->
        {
            list.add("C");
            throw new Throwable();
        };

        final ContextErrorHandler<String, String> handler = handler1.andThen(handler2).andThen(handler3);

        try
        {
            handler.onError(null, null, null);
        }
        catch (Throwable ex)
        {
            fail();
        }

        assertEquals(List.of("A", "B", "C"), list);
    }

    /**
     * Test: 20190606211312619505
     *
     * <p>
     * Class: <code>ContextErrorHandler</code>
     * </p>
     *
     * <p>
     * Method: <code>andThen(ConsumerErrorHandler)</code>
     * </p>
     *
     * <p>
     * Case: Chaining and Exception Suppression.
     * </p>
     */
    @Test
    public void test20190606211312619505 ()
    {
        final List<String> list = new CopyOnWriteArrayList<>();

        final ContextErrorHandler<String, String> handler1 = (context, message, cause) ->
        {
            list.add("A");
            throw new Throwable();
        };

        final ConsumerErrorHandler handler2 = (cause) ->
        {
            list.add("B");
            throw new Throwable();
        };

        final ContextErrorHandler<String, String> handler = handler1.andThen(handler2);

        try
        {
            handler.onError(null, null, null);
        }
        catch (Throwable ex)
        {
            fail();
        }

        assertEquals(List.of("A", "B"), list);
    }

    /**
     * Test: 20190606211312619522
     *
     * <p>
     * Class: <code>ConsumerErrorHandler</code>
     * </p>
     *
     * <p>
     * Method: <code>andThen(ConsumerErrorHandler)</code>
     * </p>
     *
     * <p>
     * Case: Chaining and Exception Suppression.
     * </p>
     */
    @Test
    public void test20190606211312619522 ()
    {
        final List<String> list = new CopyOnWriteArrayList<>();

        final ConsumerErrorHandler handler1 = (cause) ->
        {
            list.add("A");
            throw new Throwable();
        };

        final ConsumerErrorHandler handler2 = (cause) ->
        {
            list.add("B"); // No Error
        };

        final ConsumerErrorHandler handler3 = (cause) ->
        {
            list.add("C");
            throw new Throwable();
        };

        final ConsumerErrorHandler handler = handler1.andThen(handler2).andThen(handler3);

        try
        {
            handler.onError(null);
        }
        catch (Throwable ex)
        {
            fail();
        }

        assertEquals(List.of("A", "B", "C"), list);
    }

    /**
     * Test: 20190606220842719380
     *
     * <p>
     * Class: <code>DefaultActor</code>
     * </p>
     *
     * <p>
     * Method: <code>run()</code>
     * </p>
     *
     * <p>
     * Case: An actor never runs concurrently, unless there is a bug in Cascade.
     * </p>
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void test20190606220842719380 ()
            throws InterruptedException
    {
        final BlockingQueue<Object> queue1 = new LinkedBlockingQueue<>();
        final BlockingQueue<Object> queue2 = new LinkedBlockingQueue<>();

        final ConsumerScript<Object> script = (message) ->
        {
            queue1.offer("B");
            queue2.take();
        };

        final AbstractStage threadedStage = (AbstractStage) Cascade.newStage();

        final DefaultActor actor = (DefaultActor) threadedStage
                .newActor()
                .withConsumerScript(script)
                .create();

        actor.input().send("A");
        assertEquals("B", queue1.take());

        try
        {
            /**
             * At this point, the actor is blocked waiting on Queue #1 in run().
             * Now, we are going to invoke run() on this thread too.
             * Therefore, run() will be executing concurrently, which is forbidden.
             */
            actor.run();

            /**
             * An IllegalStateException should have been thrown,
             * because run() cannot occur concurrently.
             */
            fail();
        }
        catch (IllegalStateException ex)
        {
            assertEquals("concurrent run()", ex.getMessage());
        }
        finally
        {
            threadedStage.close();
        }
    }

    /**
     * Test: 20190606231957944389
     *
     * <p>
     * Method: <code>newStage()</code>
     * </p>
     *
     * <p>
     * Case: The default stage implementation executes actors on worker threads.
     * </p>
     *
     * <p>
     * Note: A regression introduced a bug where the actors where being
     * executed on the threads submitting messages. That regression bug
     * managed to bypass all of the other unit-tests.
     * Therefore, a dedicated unit-test is warranted.
     * </p>
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void test20190606231957944389 ()
            throws InterruptedException
    {
        final BlockingQueue<Thread> queue1 = new LinkedBlockingQueue<>();
        final BlockingQueue<Object> queue2 = new LinkedBlockingQueue<>();

        final Stage threadedStage = Cascade.newStage(2);

        final ConsumerScript<Object> script = (message) ->
        {
            queue1.add(Thread.currentThread());
            queue2.take();
        };

        final Actor<Object, Object> actor1 = threadedStage
                .newActor()
                .withConsumerScript(script)
                .create();

        final Actor<Object, Object> actor2 = threadedStage
                .newActor()
                .withConsumerScript(script)
                .create();

        /**
         * Cause the actors to start blocking.
         */
        actor1.input().send("A");
        actor2.input().send("B");

        /**
         * Get the threads that the actors were executing on.
         */
        final Thread thread1 = queue1.take();
        final Thread thread2 = queue1.take();

        /**
         * Cause the actors to stop blocking.
         */
        queue2.add("X");
        queue2.add("Y");

        /**
         * Neither actor was executing on this non-stage related thread.
         * The actors were executing on different stage related threads.
         */
        assertNotEquals(Thread.currentThread(), thread1);
        assertNotEquals(Thread.currentThread(), thread2);
        assertNotEquals(thread1, thread2);

        /**
         * The stage uses non-daemon threads by default.
         */
        assertFalse(thread1.isDaemon());
        assertFalse(thread2.isDaemon());

        threadedStage.close();
    }

    /**
     * Test: 20190606233624505125
     *
     * <p>
     * Method: <code>newStage(count, daemon)</code>
     * </p>
     *
     * <p>
     * Case: Daemon Threads
     * </p>
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void test20190606233624505125 ()
            throws InterruptedException
    {
        final BlockingQueue<Thread> queue1 = new LinkedBlockingQueue<>();
        final BlockingQueue<Object> queue2 = new LinkedBlockingQueue<>();

        final boolean daemon = true;

        final Stage threadedStage = Cascade.newStage(2, daemon);

        final ConsumerScript<Object> script = (message) ->
        {
            queue1.add(Thread.currentThread());
            queue2.take();
        };

        final Actor<Object, Object> actor1 = threadedStage
                .newActor()
                .withConsumerScript(script)
                .create();

        final Actor<Object, Object> actor2 = threadedStage
                .newActor()
                .withConsumerScript(script)
                .create();

        /**
         * Cause the actors to start blocking.
         */
        actor1.input().send("A");
        actor2.input().send("B");

        /**
         * Get the threads that the actors were executing on.
         */
        final Thread thread1 = queue1.take();
        final Thread thread2 = queue1.take();

        /**
         * Cause the actors to stop blocking.
         */
        queue2.add("X");
        queue2.add("Y");

        /**
         * The stage uses non-daemon threads by default.
         */
        assertEquals(daemon, thread1.isDaemon());
        assertEquals(daemon, thread2.isDaemon());

        threadedStage.close();
    }

    /**
     * Test: 20190606233624505174
     *
     * <p>
     * Method: <code>newStage(count, daemon)</code>
     * </p>
     *
     * <p>
     * Case: Non-Daemon Threads
     * </p>
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void test20190606233624505174 ()
            throws InterruptedException
    {
        final BlockingQueue<Thread> queue1 = new LinkedBlockingQueue<>();
        final BlockingQueue<Object> queue2 = new LinkedBlockingQueue<>();

        final boolean daemon = false;

        final Stage threadedStage = Cascade.newStage(2, daemon);

        final ConsumerScript<Object> script = (message) ->
        {
            queue1.add(Thread.currentThread());
            queue2.take();
        };

        final Actor<Object, Object> actor1 = threadedStage
                .newActor()
                .withConsumerScript(script)
                .create();

        final Actor<Object, Object> actor2 = threadedStage
                .newActor()
                .withConsumerScript(script)
                .create();

        /**
         * Cause the actors to start blocking.
         */
        actor1.input().send("A");
        actor2.input().send("B");

        /**
         * Get the threads that the actors were executing on.
         */
        final Thread thread1 = queue1.take();
        final Thread thread2 = queue1.take();

        /**
         * Cause the actors to stop blocking.
         */
        queue2.add("X");
        queue2.add("Y");

        /**
         * The stage uses non-daemon threads by default.
         */
        assertEquals(daemon, thread1.isDaemon());
        assertEquals(daemon, thread2.isDaemon());

        threadedStage.close();
    }

    /**
     * Test: 20190606234718947631
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Case: Verify that the default script does nothing.
     * </p>
     *
     * @throws java.lang.Exception
     */
    @Test
    public void test20190606234718947631 ()
            throws Exception,
                   Throwable
    {
        final Actor.Builder actor = stage.newActor();
        final ContextScript script = getField(actor, "script", ContextScript.class);

        final Context context = new Context()
        {
            @Override
            public Actor actor ()
            {
                fail();
                return null;
            }

            @Override
            public boolean offerTo (Object message)
            {
                fail();
                return false;
            }

            @Override
            public boolean offerFrom (Object message)
            {
                fail();
                return false;
            }
        };

        /**
         * The script will not manipulate the context in any way.
         */
        script.onInput(context, "Mons");
    }

    /**
     * Test: 20190606235343993069
     *
     * <p>
     * Class: <code>Builder</code>
     * </p>
     *
     * <p>
     * Case: Verify that the default script does nothing.
     * </p>
     *
     * @throws java.lang.Exception
     */
    @Test
    public void test20190606235343993069 ()
            throws Exception
    {
        final Actor.Builder actor = stage.newActor();
        final Mailbox mailbox = getField(actor, "mailbox", Mailbox.class);
        assertNotNull(mailbox);
        assertTrue(mailbox instanceof ConcurrentLinkedQueueMailbox);
    }
}
