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

import com.mackenziehigh.cascade.Cascade.AbstractStage.ActorTask;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Micro Actor Framework.
 */
public interface Cascade
{
    /**
     * A group of <code>Actor</code>s with a common power supply.
     */
    public interface Stage
    {
        /**
         * Actor.
         *
         * @param <I> is the type of objects the actor will consume.
         * @param <O> is the type of objects the actor will produce.
         */
        public interface Actor<I, O>
                extends Consumer<I>
        {
            /**
             * Actor Builder.
             *
             * @param <I> is the type of objects the actor will consume.
             * @param <O> is the type of objects the actor will produce.
             */
            public interface Builder<I, O>
            {
                /**
                 * Define the normal behavior of the actor.
                 *
                 * @param <X> is the type of objects the actor will consume.
                 * @param <Y> is the type of objects the actor will produce.
                 * @param script defines the message-handling behavior of the actor.
                 * @return a modified copy of this builder.
                 */
                public <X, Y> Builder<X, Y> withScript (FunctionScript<X, Y> script);

                /**
                 * Define the normal behavior of the actor.
                 *
                 * @param <X> is the type of objects the actor will consume.
                 * @param script defines the message-handling behavior of the actor.
                 * @return a modified copy of this builder.
                 */
                public default <X> Builder<X, X> withScript (ConsumerScript<X> script)
                {
                    return withScript(x ->
                    {
                        script.execute(x);
                        return null;
                    });
                }

                /**
                 * Define how the actor responds to unhandled exceptions.
                 *
                 * @param script defines the error-handling behavior of the actor.
                 * @return a modified copy of this builder.
                 */
                public Builder<I, O> withErrorHandler (Consumer<Throwable> script);

                /**
                 * Cause the actor to use the given queue to store incoming messages.
                 *
                 * <p>
                 * <b>Warning:</b> The queue must ensure thread-safety.
                 * </p>
                 *
                 * @param queue will store incoming messages as they await processing.
                 * @return a modified copy of this builder.
                 */
                public Builder<I, O> withInflowQueue (Queue<I> queue);

                /**
                 * Cause the actor to use a <code>ConcurrentLinkedQueue</code> to store incoming messages.
                 *
                 * @return this.
                 */
                public default Builder<I, O> withConcurrentInflowQueue ()
                {
                    return withInflowQueue(new ConcurrentLinkedQueue<>());
                }

                /**
                 * Cause the actor to use a <code>LinkedBlockingQueue</code> to store incoming messages.
                 *
                 * @return a modified copy of this builder.
                 */
                public default Builder<I, O> withLinkedInflowQueue ()
                {
                    return withInflowQueue(new LinkedBlockingQueue<>());
                }

                /**
                 * Cause the actor to use a <code>LinkedBlockingQueue</code> to store incoming messages.
                 *
                 * @param capacity is the maximum number of simultaneously pending messages.
                 * @return a modified copy of this builder.
                 */
                public default Builder<I, O> withLinkedInflowQueue (int capacity)
                {
                    return withInflowQueue(new LinkedBlockingQueue<>(capacity));
                }

                /**
                 * Cause the actor to use a <code>ArrayBlockingQueue</code> to store incoming messages.
                 *
                 * @param capacity is the maximum number of simultaneously pending messages.
                 * @return a modified copy of this builder.
                 */
                public default Builder<I, O> withArrayInflowQueue (int capacity)
                {
                    return withInflowQueue(new ArrayBlockingQueue<>(capacity));
                }

                /**
                 * Construct the actor and add it to the stage.
                 *
                 * @return the newly created actor.
                 */
                public Actor<I, O> create ();
            }

            /**
             * Input to an Actor.
             *
             * @param <T> is the type of objects that the actor will consume.
             */
            public interface Input<T>
            {

                /**
                 * Get the actor that this input pertains to.
                 *
                 * @return the enclosing actor.
                 */
                public Actor<T, ?> actor ();

                /**
                 * Connect this input to the given output of another actor.
                 *
                 * <p>
                 * This method is a no-op, if the connection already exists.
                 * </p>
                 *
                 * @param output will send messages to this input.
                 * @return this.
                 */
                public default Input<T> connect (final Output<T> output)
                {
                    Objects.requireNonNull(output, "output");
                    output.connect(this);
                    return this;
                }

                /**
                 * Disconnect this input from the given output.
                 *
                 * <p>
                 * This method is a no-op, if the connection does not exist.
                 * </p>
                 *
                 * @param output will no longer be connected.
                 * @return this.
                 */
                public default Input<T> disconnect (final Output<T> output)
                {
                    Objects.requireNonNull(output, "output");
                    output.disconnect(this);
                    return this;
                }

                /**
                 * Send a message to the actor via this input, silently dropping the message,
                 * if this input is does not have sufficient capacity to enqueue the message.
                 *
                 * @param message will be processed by the actor, eventually,
                 * if the message is not dropped due to capacity restrictions.
                 * @return true, if the message was enqueued.
                 */
                public boolean offer (T message);

                /**
                 * Send a message to the actor via this input.
                 *
                 * @param message will be processed by the actor, eventually,
                 * if the message was not dropped due to capacity restrictions.
                 * @return this.
                 */
                public default Input<T> send (T message)
                {
                    offer(message);
                    return this;
                }

                /**
                 * Determine whether this input is connected to the given output.
                 *
                 * @param output may be connected to this input.
                 * @return true, if this input is currently connected to the output.
                 */
                public default boolean isConnected (final Output<?> output)
                {
                    return output.isConnected(this);
                }

            }

            /**
             * Output to an Actor.
             *
             * @param <T> is the type of objects that the actor will produce.
             */
            public interface Output<T>
            {
                /**
                 * Get the actor that this input pertains to.
                 *
                 * @return the enclosing actor.
                 */
                public Actor<?, T> actor ();

                /**
                 * Connect this output to the given input of another actor.
                 *
                 * <p>
                 * This method is a no-op, if the connection already exists.
                 * </p>
                 *
                 * @param input will be sent messages from this output.
                 * @return this.
                 */
                public Output<T> connect (Input<T> input);

                /**
                 * Disconnect this output from the given input.
                 *
                 * <p>
                 * This method is a no-op, if the connection does not exist.
                 * </p>
                 *
                 * @param input will no longer be connected.
                 * @return this.
                 */
                public Output<T> disconnect (Input<T> input);

                /**
                 * Determine whether this output is connected to the given input.
                 *
                 * @param input may be connected to this output.
                 * @return true, if this output is currently connected to the input.
                 */
                public boolean isConnected (final Input<?> input);
            }

            /**
             * Actor Behavior.
             *
             * @param <I> is the type of objects that the actor will consume.
             * @param <O> is the type of objects that the actor will produce.
             */
            @FunctionalInterface
            public interface FunctionScript<I, O>
            {
                public O execute (I input)
                        throws Throwable;
            }

            /**
             * Actor Behavior.
             *
             * @param <I> is the type of objects that the actor will consume.
             */
            @FunctionalInterface
            public interface ConsumerScript<I>
            {
                public void execute (I input)
                        throws Throwable;
            }

            /**
             * Get the <code>Stage</code> that contains this actor.
             *
             * @return the enclosing stage.
             */
            public Stage stage ();

            /**
             * Get the <code>Input</code> that supplies messages to this actor.
             *
             * @return the input to the actor.
             */
            public Input<I> input ();

            /**
             * Get the <code>Output</code> that receives messages from this actor.
             *
             * @return the output from the actor.
             */
            public Output<O> output ();

        }

        /**
         * Add a default error-handler that will receive unhandled exceptions,
         * if no other more specific error-handler is available.
         *
         * @param handler will be used to handle unhandled exceptions.
         * @return this.
         */
        public Stage addErrorHandler (Consumer<Throwable> handler);

        /**
         * Create a builder that can be used to add a new actor to this stage.
         *
         * @param <I> is the type of objects that the actor will consume.
         * @param <O> is the type of objects that the actor will produce.
         * @return the new builder.
         */
        public <I, O> Actor.Builder<I, O> newActor ();

        /**
         * Asynchronously shutdown this stage, as soon as reasonably possible.
         */
        public void close ();
    }

    /**
     * Partial Implementation of <code>Stage</code>.
     */
    public abstract class AbstractStage
            implements Cascade.Stage
    {
        private final Stage STAGE = this;

        private final AtomicBoolean stageClosed = new AtomicBoolean(false);

        private final Set<Consumer<Throwable>> stageErrorHandlers = new CopyOnWriteArraySet<>();

        /**
         * This method will be invoked whenever an actor needs executed.
         *
         * <p>
         * This method will not be re-invoked, until the actor finishes
         * being executed, even if the actor determines that it needs
         * to be executed again. Rather, the actor will invoke this
         * method again, if needed, at the end of its execution.
         * This strategy helps ensure that no two threads will
         * ever power the actor concurrently. Moreover,
         * this strategy lessons the amount of memory used
         * by some implementations in order to schedule actors.
         * </p>
         *
         * @param state provides the methods needed to execute an actor.
         */
        protected abstract void onSubmit (ActorTask state);

        /**
         * This method will be invoked when this stage closes.
         */
        protected abstract void onStageClose ();

        /**
         * {@inheritDoc}
         */
        @Override
        public final <I, O> Stage.Actor.Builder<I, O> newActor ()
        {
            return new ActorBuilder<>();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final Stage addErrorHandler (final Consumer<Throwable> handler)
        {
            Objects.requireNonNull(handler, "handler");

            final Consumer<Throwable> safeConsumer = ex ->
            {
                try
                {
                    handler.accept(ex);
                }
                catch (Throwable ignored)
                {
                    // Pass.
                }
            };

            stageErrorHandlers.add(safeConsumer);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final void close ()
        {
            if (stageClosed.compareAndSet(false, true))
            {
                onStageClose();
            }
        }

        /**
         * An instance of this class facilitates executing an actor.
         *
         * <p>
         * A (meta) object is stored herein, which is intended
         * for use by implementing sub-classes, so that they
         * can store actor specific information.
         * </p>
         */
        protected final class ActorTask
                implements Runnable
        {
            private final InternalActor<?, ?> actor;

            private volatile Object meta;

            private ActorTask (final InternalActor<?, ?> actor)
            {
                this.actor = actor;
            }

            public Stage.Actor<?, ?> actor ()
            {
                return actor;
            }

            public Object meta ()
            {
                return meta;
            }

            public void meta (final Object value)
            {
                meta = value;
            }

            @Override
            public void run ()
            {
                actor.run();
            }
        }

        private final class ActorBuilder<I, O>
                implements Cascade.Stage.Actor.Builder<I, O>
        {
            private final Queue<I> mailbox;

            private final Stage.Actor.FunctionScript<I, O> script;

            private final Consumer<Throwable> errorHandler;

            private ActorBuilder ()
            {
                this.mailbox = new ConcurrentLinkedQueue<>();
                this.script = (I x) -> null;
                this.errorHandler = ex -> stageErrorHandlers.forEach(x -> x.accept(ex));
            }

            private ActorBuilder (final Queue<I> mailbox,
                                  final Stage.Actor.FunctionScript<I, O> script,
                                  final Consumer<Throwable> errorHandler)
            {
                this.mailbox = mailbox;
                this.script = script;
                this.errorHandler = errorHandler;
            }

            @Override
            public <X, Y> Stage.Actor.Builder<X, Y> withScript (final Stage.Actor.FunctionScript<X, Y> script)
            {
                Objects.requireNonNull(script, "script");
                return new ActorBuilder(mailbox, script, errorHandler);
            }

            @Override
            public Stage.Actor.Builder<I, O> withErrorHandler (final Consumer<Throwable> handler)
            {
                Objects.requireNonNull(handler, "handler");

                final Consumer<Throwable> safeConsumer = ex ->
                {
                    try
                    {
                        handler.accept(ex);
                    }
                    catch (Throwable ignored)
                    {
                        // Pass.
                    }
                };

                return new ActorBuilder(mailbox, script, safeConsumer);
            }

            @Override
            public Stage.Actor.Builder<I, O> withInflowQueue (final Queue<I> queue)
            {
                Objects.requireNonNull(queue, "queue");

                /**
                 * Prevent common mistakes.
                 * None of these types of queues are thread-safe.
                 * Therefore, they break the contract of this method.
                 */
                if (queue.getClass().equals(ArrayDeque.class))
                {
                    throw new IllegalArgumentException("The ArrayDeque class is not thread-safe!");
                }
                else if (queue.getClass().equals(LinkedList.class))
                {
                    throw new IllegalArgumentException("The LinkedList class is not thread-safe!");
                }
                else if (queue.getClass().equals(PriorityQueue.class))
                {
                    throw new IllegalArgumentException("The PriorityQueue class is not thread-safe!");
                }

                return new ActorBuilder(queue, script, errorHandler);
            }

            @Override
            public Stage.Actor<I, O> create ()
            {
                final InternalActor<I, O> actor = new InternalActor<>(this);
                return actor;
            }
        }

        private final class InternalActor<I, O>
                implements Cascade.Stage.Actor<I, O>
        {
            private final Stage.Actor<I, O> ACTOR = this;

            private final ActorBuilder<I, O> builder;

            private final Queue<I> mailbox;

            private final FunctionScript<I, O> script;

            private final InternalInput input = new InternalInput();

            private final InternalOutput output = new InternalOutput();

            private final ActorTask state = new ActorTask(this);

            private final AtomicLong pendingCranks = new AtomicLong();

            private final AtomicBoolean inProgress = new AtomicBoolean(false);

            private InternalActor (final ActorBuilder<I, O> builder)
            {
                this.builder = builder;
                this.script = builder.script;
                this.mailbox = builder.mailbox;
            }

            private void run ()
            {
                if (inProgress.compareAndSet(false, true) == false)
                {
                    throw new IllegalStateException("concurrent run()");
                }

                try
                {
                    processMessage();
                }
                catch (Throwable ex1)
                {
                    builder.errorHandler.accept(ex1);
                }
                finally
                {
                    inProgress.set(false);

                    if (pendingCranks.decrementAndGet() != 0)
                    {
                        onSubmit(state);
                    }
                }
            }

            private void processMessage ()
                    throws Throwable
            {
                final I msgIn = mailbox.poll();
                final O msgOut = script.execute(msgIn);

                if (msgOut != null)
                {
                    final List<Stage.Actor.Input<O>> outputs = output.connectionList;
                    final int length = outputs.size();

                    for (int i = 0; i < length; i++)
                    {
                        outputs.get(i).send(msgOut);
                    }
                }
            }

            private void submit ()
            {
                if (pendingCranks.incrementAndGet() == 1)
                {
                    onSubmit(state);
                }
            }

            @Override
            public Cascade.Stage stage ()
            {
                return STAGE;
            }

            @Override
            public Stage.Actor.Input<I> input ()
            {
                return input;
            }

            @Override
            public Stage.Actor.Output<O> output ()
            {
                return output;
            }

            @Override
            public void accept (final I message)
            {
                input().send(message);
            }

            private final class InternalInput
                    implements Cascade.Stage.Actor.Input<I>
            {
                @Override
                public Stage.Actor<I, ?> actor ()
                {
                    return ACTOR;
                }

                @Override
                public boolean offer (final I message)
                {
                    Objects.requireNonNull(message, "message");
                    if (builder.mailbox.offer(message))
                    {
                        submit();
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
            }

            private final class InternalOutput
                    implements Cascade.Stage.Actor.Output<O>
            {
                private final Object outputLock = new Object();

                private volatile List<Stage.Actor.Input<O>> connectionList = newImmutableList(Collections.EMPTY_LIST);

                @Override
                public Stage.Actor<?, O> actor ()
                {
                    return ACTOR;
                }

                @Override
                public Stage.Actor.Output<O> connect (final Stage.Actor.Input<O> input)
                {
                    Objects.requireNonNull(input, "input");

                    synchronized (outputLock)
                    {
                        if (isConnected(input) == false)
                        {
                            final List<Stage.Actor.Input<O>> modified = new ArrayList<>(connectionList);
                            modified.add(input);
                            connectionList = newImmutableList(modified);
                        }
                    }

                    return this;
                }

                @Override
                public Stage.Actor.Output<O> disconnect (final Stage.Actor.Input<O> input)
                {
                    Objects.requireNonNull(input, "input");

                    synchronized (outputLock)
                    {
                        if (isConnected(input))
                        {
                            final List<Stage.Actor.Input<O>> modified = new ArrayList<>(connectionList);
                            modified.remove(input);
                            connectionList = newImmutableList(modified);
                        }
                    }

                    return this;
                }

                @Override
                public boolean isConnected (final Input<?> input)
                {
                    return connectionList.contains(input);
                }
            }
        }

        private static <T> List<T> newImmutableList (final Collection<T> collection)
        {
            return Collections.unmodifiableList(new CopyOnWriteArrayList<>(collection));
        }
    }

    /**
     * Create a new single-threaded stage.
     *
     * @return the new stage.
     */
    public static Stage newStage ()
    {
        final ExecutorService service = Executors.newSingleThreadExecutor();
        return newExecutorStage(service);
    }

    /**
     * Create a new multi-threaded stage.
     *
     * @param threadCount is the number of worker threads that the stage will use.
     * @return the new stage.
     */
    public static Stage newStage (final int threadCount)
    {
        final ExecutorService service = Executors.newFixedThreadPool(threadCount);
        return newExecutorStage(service);
    }

    /**
     * Create a new stage based on a given <code>ExecutorService</code>.
     *
     * @param service will power the new stage.
     * @return the new stage.
     */
    public static Stage newExecutorStage (final ExecutorService service)
    {
        Objects.requireNonNull(service, "service");

        return new AbstractStage()
        {
            @Override
            protected void onSubmit (final AbstractStage.ActorTask task)
            {
                service.submit(task);
            }

            @Override
            protected void onStageClose ()
            {
                service.shutdown();
            }
        };
    }

    /**
     * Create a new stage based on a fixed-size pool of threads.
     *
     * @param threadCount is the number of threads in the pool.
     * @return the new stage.
     */
    public static Stage newFixedPoolStage (final int threadCount)
    {
        final ThreadFactory factory = (final Runnable task) ->
        {
            final String name = String.format("newPooledStage-%d-%s", threadCount, UUID.randomUUID().toString());
            final Thread thread = new Thread(task, name);
            thread.setDaemon(true);
            return thread;
        };

        final LinkedBlockingQueue<ActorTask> queue = new LinkedBlockingQueue<>();

        return newFixedPoolStage(factory, threadCount, queue);
    }

    /**
     * Create a new stage based on a fixed-size pool of threads.
     *
     * @param threadFactory will be used to create the threads in the pool.
     * @param threadCount is the number of threads in the pool.
     * @param taskQueue will be used to feed tasks to the threads in the pool.
     * @return the new stage.
     */
    public static Stage newFixedPoolStage (final ThreadFactory threadFactory,
                                           final int threadCount,
                                           final BlockingQueue<ActorTask> taskQueue)
    {
        Objects.requireNonNull(threadFactory, "threadFactory");
        Objects.requireNonNull(taskQueue, "taskQueue");

        final AtomicBoolean stop = new AtomicBoolean(false);

        final Runnable mainLoop = () ->
        {
            while (stop.get() == false)
            {
                try
                {
                    final ActorTask task = taskQueue.poll(1, TimeUnit.SECONDS);

                    if (task != null)
                    {
                        task.run();
                    }
                }
                catch (Throwable ex)
                {
                    ex.printStackTrace(System.err);
                }
            }
        };

        /**
         * Create the threads.
         */
        for (int i = 0; i < threadCount; i++)
        {
            final Thread thread = threadFactory.newThread(mainLoop);
            thread.start();
        }

        return new AbstractStage()
        {
            @Override
            protected void onSubmit (final AbstractStage.ActorTask state)
            {
                taskQueue.add(state);
            }

            @Override
            protected void onStageClose ()
            {
                stop.set(true);
            }
        };
    }
}
