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

import com.mackenziehigh.cascade.Cascade.Stage.Actor.Builder;
import com.mackenziehigh.cascade.Cascade.Stage.Actor.Context;
import com.mackenziehigh.cascade.Cascade.Stage.Actor.ContextScript;
import com.mackenziehigh.cascade.Cascade.Stage.Actor.ErrorHandler;
import com.mackenziehigh.cascade.Cascade.Stage.Actor.Mailbox;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Micro Actor Framework.
 */
public interface Cascade
{

    /**
     * A creator of <code>Actor</code> objects.
     */
    @FunctionalInterface
    public interface ActorFactory
    {
        /**
         * Create a builder that can build new <code>Actor</code> object(s).
         *
         * <p>
         * This method returns a builder, rather than an actor itself,
         * so that further configuration of the actor can be performed,
         * if the calling code so desires.
         * </p>
         *
         * @param <I> is the type of objects the actor(s) will consume.
         * @param <O> is the type of objects the actor(s) will produce.
         * @return a new builder of actors.
         */
        public <I, O> Stage.Actor.Builder<I, O> newActor ();
    }

    /**
     * A group of <code>Actor</code>s with a common power supply.
     */
    public interface Stage
            extends ActorFactory
    {

        /**
         * Actor.
         *
         * @param <I> is the type of objects the actor will consume.
         * @param <O> is the type of objects the actor will produce.
         */
        public interface Actor<I, O>
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
                public <X, Y> Builder<X, Y> withContextScript (ContextScript<X, Y> script);

                /**
                 * Define the normal behavior of the actor.
                 *
                 * @param <X> is the type of objects the actor will consume.
                 * @param <Y> is the type of objects the actor will produce.
                 * @param script defines the message-handling behavior of the actor.
                 * @return a modified copy of this builder.
                 */
                public default <X, Y> Builder<X, Y> withFunctionScript (FunctionScript<X, Y> script)
                {
                    return withContextScript((ctx, input) ->
                    {
                        ctx.sendFrom(script.execute(input));
                    });
                }

                /**
                 * Define the normal behavior of the actor.
                 *
                 * @param <X> is the type of objects the actor will consume.
                 * @param script defines the message-handling behavior of the actor.
                 * @return a modified copy of this builder.
                 */
                public default <X> Builder<X, X> withConsumerScript (ConsumerScript<X> script)
                {
                    return withFunctionScript(x ->
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
                public Builder<I, O> withErrorHandler (ErrorHandler<I, O> script);

                /**
                 * Cause the actor to use the given mailbox to store incoming messages.
                 *
                 * <p>
                 * <b>Warning:</b> The mailbox must ensure thread-safety.
                 * </p>
                 *
                 * @param queue will store incoming messages as they await processing.
                 * @return a modified copy of this builder.
                 */
                public Builder<I, O> withMailbox (Mailbox<I> queue);

                /**
                 * Construct the actor and add it to the stage.
                 *
                 * @return the newly created actor.
                 */
                public Actor<I, O> create ();
            }

            /**
             * A queue-like data-structure that stores incoming messages.
             *
             * @param <I> is the type of objects that the actor will consume.
             */
            public interface Mailbox<I>
            {
                /**
                 * Add a message to the mailbox.
                 *
                 * @param message will be added to the mailbox, if possible.
                 * @return true, if the message was in-fact added to the mailbox.
                 */
                public boolean offer (I message);

                /**
                 * Remove a message from the mailbox.
                 *
                 * <p>
                 * If any message is in the mailbox, then this method <b>must</b> return non-null.
                 * In other words, a mailbox <b>cannot</b> choose to delay the removal
                 * of a message by forcing the caller to call this method again at a later time.
                 * </p>
                 *
                 * <p>
                 * A mailbox can choose to unilaterally drop messages.
                 * In other words, a message that was successfully <code>offer()</code>-ed
                 * to this mailbox may never be returned by <code>poll()</code>,
                 * at the sole discretion of the mailbox itself.
                 * </p>
                 *
                 * @return the message that was removed, or null, if no message was available.
                 */
                public I poll ();
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
                public default Input<T> send (final T message)
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
             * Script Execution Context.
             *
             * @param <I> is the type of objects that the enclosing actor consumes.
             * @param <O> is the type of objects that the enclosing actor produces.
             */
            public interface Context<I, O>
            {
                /**
                 * Get the enclosing actor.
                 *
                 * @return the actor that owns this context.
                 */
                public Actor<I, O> actor ();

                /**
                 * Offer a message <b>to</b> the enclosing actor.
                 *
                 * @param message is the message to send.
                 * @return true, if the message was enqueued.
                 */
                public default boolean offerTo (final I message)
                {
                    return actor().input().offer(message);
                }

                /**
                 * Offer a message <b>from</b> the enclosing actor.
                 *
                 * @param message is the message to send.
                 * @return true, if the message was enqueued in every connected output.
                 */
                public boolean offerFrom (final O message);

                /**
                 * Send a message <b>to</b> the enclosing actor.
                 *
                 * @param message is the message to send.
                 * @return this.
                 */
                public default Context<I, O> sendTo (final I message)
                {
                    offerTo(message);
                    return this;
                }

                /**
                 * Send a message <b>from</b> the enclosing actor.
                 *
                 * @param message is the message to send.
                 * @return this.
                 */
                public default Context<I, O> sendFrom (final O message)
                {
                    offerFrom(message);
                    return this;
                }
            }

            /**
             * Actor Behavior.
             *
             * @param <I> is the type of objects that the actor will consume.
             * @param <O> is the type of objects that the actor will produce.
             */
            @FunctionalInterface
            public interface ContextScript<I, O>
            {
                public void execute (Context<I, O> context,
                                     I input)
                        throws Throwable;
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
             * Actor Behavior.
             *
             * @param <I> is the type of objects that the actor will consume.
             * @param <O> is the type of objects that the actor will produce.
             */
            @FunctionalInterface
            public interface ErrorHandler<I, O>
            {
                public void execute (Context<I, O> context,
                                     Throwable input)
                        throws Throwable;
            }

            /**
             * Get the <code>Stage</code> that contains this actor.
             *
             * @return the enclosing stage.
             */
            public Stage stage ();

            /**
             * Get the <code>Context</code> that is passed into <code>ContextScript</code>s.
             *
             * @return the context used when executing scripts.
             */
            public Context context ();

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
         * Create a builder that can be used to add a new actor to this stage.
         *
         * @param <I> is the type of objects that the actor will consume.
         * @param <O> is the type of objects that the actor will produce.
         * @return the new builder.
         */
        @Override
        public <I, O> Actor.Builder<I, O> newActor ();

        /**
         * Asynchronously shutdown this stage, as soon as reasonably possible.
         */
        public void close ();
    }

    /**
     * A <code>Mailbox</code> implementation based on a <code>ConcurrentLinkedQueue</code>.
     *
     * @param <I> is the type of messages that will be stored in the mailbox.
     */
    public static final class ConcurrentLinkedQueueMailbox<I>
            implements Mailbox<I>
    {
        private final ConcurrentLinkedQueue<I> queue;

        private ConcurrentLinkedQueueMailbox (final ConcurrentLinkedQueue<I> queue)
        {
            this.queue = queue;
        }

        /**
         * Create a new mailbox.
         *
         * @param <I> is the type of messages that will be stored in the mailbox.
         * @return the new mailbox.
         */
        public static <I> Mailbox<I> create ()
        {
            return new ConcurrentLinkedQueueMailbox<>(new ConcurrentLinkedQueue<>());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean offer (final I message)
        {
            return queue.offer(message);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public I poll ()
        {
            return queue.poll();
        }
    }

    /**
     * A <code>Mailbox</code> implementation based on a <code>LinkedBlockingQueue</code>.
     *
     * @param <I> is the type of messages that will be stored in the mailbox.
     */
    public static final class LinkedBlockingQueueMailbox<I>
            implements Mailbox<I>
    {
        private final LinkedBlockingQueue<I> queue;

        private LinkedBlockingQueueMailbox (final LinkedBlockingQueue<I> queue)
        {
            this.queue = queue;
        }

        /**
         * Create a new mailbox.
         *
         * @param <I> is the type of messages that will be stored in the mailbox.
         * @return the new mailbox.
         */
        public static <I> Mailbox<I> create ()
        {
            return new LinkedBlockingQueueMailbox<>(new LinkedBlockingQueue<>());
        }

        /**
         * Create a new mailbox.
         *
         * @param <I> is the type of messages that will be stored in the mailbox.
         * @param capacity is the maximum number of messages that can be stored simultaneously.
         * @return the new mailbox.
         */
        public static <I> Mailbox<I> create (final int capacity)
        {
            return new LinkedBlockingQueueMailbox<>(new LinkedBlockingQueue<>(capacity));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean offer (final I message)
        {
            return queue.offer(message);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public I poll ()
        {
            return queue.poll();
        }
    }

    /**
     * A <code>Mailbox</code> implementation based on a <code>ArrayBlockingQueue</code>.
     *
     * @param <I> is the type of messages that will be stored in the mailbox.
     */
    public static final class ArrayBlockingQueueMailbox<I>
            implements Mailbox<I>
    {
        private final ArrayBlockingQueue<I> queue;

        private ArrayBlockingQueueMailbox (final ArrayBlockingQueue<I> queue)
        {
            this.queue = queue;
        }

        /**
         * Create a new mailbox.
         *
         * @param <I> is the type of messages that will be stored in the mailbox.
         * @param capacity is the maximum number of messages that can be stored simultaneously.
         * @return the new mailbox.
         */
        public static <I> Mailbox<I> create (final int capacity)
        {
            return new ArrayBlockingQueueMailbox<>(new ArrayBlockingQueue<>(capacity));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean offer (final I message)
        {
            return queue.offer(message);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public I poll ()
        {
            return queue.poll();
        }
    }

    /**
     * A <code>Mailbox</code> implementation based on a <code>ArrayDeque</code>.
     *
     * @param <I> is the type of messages that will be stored in the mailbox.
     */
    public static final class ArrayDequeMailbox<I>
            implements Mailbox<I>
    {
        private final ArrayDeque<I> queue;

        private final int capacity;

        private ArrayDequeMailbox (final ArrayDeque<I> queue,
                                   final int capacity)
        {
            this.queue = queue;
            this.capacity = capacity;
        }

        /**
         * Create a new mailbox.
         *
         * @param <I> is the type of messages that will be stored in the mailbox.
         * @param initial is the initial size of the backing data-structure.
         * @param capacity is the maximum number of messages that can be stored simultaneously.
         * @return the new mailbox.
         */
        public static <I> Mailbox<I> create (final int initial,
                                             final int capacity)
        {
            return new ArrayDequeMailbox<>(new ArrayDeque<>(initial), capacity);
        }

        /**
         * {@inheritDoc}
         *
         * <p>
         * Notice that this method is synchronized.
         * </p>
         */
        @Override
        public synchronized boolean offer (final I message)
        {
            if (queue.size() == capacity)
            {
                return false;
            }
            else
            {
                return queue.offer(message);
            }
        }

        /**
         * {@inheritDoc}
         *
         * <p>
         * Notice that this method is synchronized.
         * </p>
         */
        @Override
        public synchronized I poll ()
        {
            return queue.poll();
        }
    }

    /**
     * A <code>Mailbox</code> implementation based on a <code>PriorityBlockingQueue</code>.
     *
     * @param <I> is the type of messages that will be stored in the mailbox.
     */
    public static final class PriorityBlockingQueueMailbox<I>
            implements Mailbox<I>
    {
        private final PriorityBlockingQueue<I> queue;

        private PriorityBlockingQueueMailbox (final PriorityBlockingQueue<I> queue)
        {
            this.queue = queue;
        }

        /**
         * Create a new mailbox.
         *
         * @param <I> is the type of messages that will be stored in the mailbox.
         * @param initial is the initial size of the backing data-structure.
         * @param ordering assigns priorities to messages in the mailbox.
         * @return the new mailbox.
         */
        public static <I> Mailbox<I> create (final int initial,
                                             final Comparator<I> ordering)
        {
            return new PriorityBlockingQueueMailbox<>(new PriorityBlockingQueue<>(initial, ordering));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean offer (final I message)
        {
            return queue.offer(message);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public I poll ()
        {
            return queue.poll();
        }
    }

    /**
     * A <code>Mailbox</code> implementation based on an <code>ArrayDeque</code>,
     * which behaves like a ring-buffer data-structure.
     *
     * @param <I> is the type of messages that will be stored in the mailbox.
     */
    public static final class CircularArrayDequeMailbox<I>
            implements Mailbox<I>
    {
        private final ArrayDeque<I> queue;

        private final int capacity;

        private CircularArrayDequeMailbox (final ArrayDeque<I> queue,
                                           final int capacity)
        {
            this.queue = queue;
            this.capacity = capacity;
        }

        /**
         * Create a new mailbox.
         *
         * @param <I> is the type of messages that will be stored in the mailbox.
         * @param initial is the initial size of the backing data-structure.
         * @param capacity is the maximum number of messages that can be stored simultaneously.
         * @return the new mailbox.
         */
        public static <I> Mailbox<I> create (final int initial,
                                             final int capacity)
        {
            return new CircularArrayDequeMailbox<>(new ArrayDeque<>(initial), capacity);
        }

        /**
         * {@inheritDoc}
         *
         * <p>
         * Notice that this method is synchronized.
         * </p>
         */
        @Override
        public synchronized boolean offer (final I message)
        {
            if (queue.size() == capacity)
            {
                queue.poll();
                queue.offer(message);
                return true;
            }
            else
            {
                queue.offer(message);
                return true;
            }
        }

        /**
         * {@inheritDoc}
         *
         * <p>
         * Notice that this method is synchronized.
         * </p>
         */
        @Override
        public synchronized I poll ()
        {
            return queue.poll();
        }
    }

    /**
     * Partial Implementation of <code>Stage</code>.
     */
    public abstract class AbstractStage
            implements Cascade.Stage
    {
        private final Stage STAGE = this;

        private final AtomicBoolean stageClosed = new AtomicBoolean(false);

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
         * @param actor needs to be <code>run()</code> at some point in the future.
         */
        protected abstract void onSubmit (DefaultActor<?, ?> actor);

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
            final ErrorHandler<I, O> errorHandler = (ctx, ex) ->
            {
                // Pass.
            };

            final Stage.Actor.Builder<I, O> builder = new ActorBuilder<I, O>()
                    .withMailbox(ConcurrentLinkedQueueMailbox.create())
                    .withErrorHandler(errorHandler)
                    .withFunctionScript(msg -> null);

            return builder;
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

        private final class ActorBuilder<I, O>
                implements Cascade.Stage.Actor.Builder<I, O>
        {
            private final Mailbox<I> mailbox;

            private final Stage.Actor.ContextScript<I, O> script;

            private final ErrorHandler<I, O> errorHandler;

            private ActorBuilder ()
            {
                this.errorHandler = null;
                this.script = null;
                this.mailbox = null;
            }

            private ActorBuilder (final Mailbox<I> mailbox,
                                  final Stage.Actor.ContextScript<I, O> script,
                                  final ErrorHandler<I, O> errorHandler)
            {
                this.mailbox = mailbox;
                this.script = script;
                this.errorHandler = errorHandler;
            }

            @Override
            public <X, Y> Stage.Actor.Builder<X, Y> withContextScript (final Stage.Actor.ContextScript<X, Y> script)
            {
                Objects.requireNonNull(script, "script");
                return new ActorBuilder(mailbox, script, errorHandler);
            }

            @Override
            public Stage.Actor.Builder<I, O> withErrorHandler (final ErrorHandler<I, O> handler)
            {
                Objects.requireNonNull(handler, "handler");

                /**
                 * Combine the given handler and any previously defined handlers.
                 * Execute each of the handlers in sequence, even if one fails.
                 * If any handler throws an exception, simply ignore it.
                 * In general, an error-handler should not cause an error itself.
                 */
                final ErrorHandler<I, O> safeConsumer = (context, cause) ->
                {
                    if (errorHandler != null)
                    {
                        errorHandler.execute(context, cause);
                    }

                    try
                    {
                        handler.execute(context, cause);
                    }
                    catch (Throwable ignored)
                    {
                        // Pass.
                    }
                };

                return new ActorBuilder(mailbox, script, safeConsumer);
            }

            @Override
            public Stage.Actor.Builder<I, O> withMailbox (final Mailbox<I> mailbox)
            {
                Objects.requireNonNull(mailbox, "mailbox");
                return new ActorBuilder(mailbox, script, errorHandler);
            }

            @Override
            public Stage.Actor<I, O> create ()
            {
                final DefaultActor<I, O> actor = new DefaultActor<>(this);
                return actor;
            }
        }

        /**
         * Default Actor Implementation.
         *
         * <p>
         * A (meta) object is stored herein, which is intended
         * for use by implementing sub-classes, so that they
         * can store actor specific information.
         * </p>
         *
         * @param <I> is the type of the messages incoming to the actor.
         * @param <O> is the type of the messages outgoing from the actor.
         */
        public final class DefaultActor<I, O>
                implements Cascade.Stage.Actor<I, O>,
                           Runnable
        {
            private final DefaultActor<I, O> ACTOR = this;

            private final Mailbox<I> mailbox;

            private final ContextScript<I, O> script;

            private final ErrorHandler<I, O> errorHandler;

            private final InternalInput input = new InternalInput();

            private final InternalOutput output = new InternalOutput();

            private final AtomicLong pendingCranks = new AtomicLong();

            private final AtomicBoolean inProgress = new AtomicBoolean(false);

            private volatile Object meta = null;

            private final Context<I, O> context = new Context<I, O>()
            {
                @Override
                public Actor<I, O> actor ()
                {
                    return ACTOR;
                }

                @Override
                public boolean offerFrom (final O message)
                {
                    boolean sentToAll = true;

                    if (message != null)
                    {
                        final List<Stage.Actor.Input<O>> outputs = output.connectionList;
                        final int length = outputs.size();

                        for (int i = 0; i < length; i++)
                        {
                            sentToAll &= outputs.get(i).offer(message);
                        }
                    }

                    return sentToAll;
                }
            };

            private DefaultActor (final ActorBuilder<I, O> builder)
            {
                this.errorHandler = builder.errorHandler;
                this.mailbox = builder.mailbox;
                this.script = builder.script;
            }

            @Override
            public void run ()
            {
                if (inProgress.compareAndSet(false, true) == false)
                {
                    throw new IllegalStateException("concurrent run()");
                }

                try
                {
                    processMessage();
                }
                catch (Throwable ex)
                {
                    handle(ex);
                }
                finally
                {
                    inProgress.set(false);

                    if (pendingCranks.decrementAndGet() != 0)
                    {
                        onSubmit(ACTOR);
                    }
                }
            }

            private void processMessage ()
                    throws Throwable
            {
                final I msgIn = mailbox.poll();

                if (msgIn != null)
                {
                    script.execute(context, msgIn);
                }
            }

            private void submit ()
            {
                if (pendingCranks.incrementAndGet() == 1)
                {
                    onSubmit(ACTOR);
                }
            }

            private void handle (final Throwable cause)
            {
                try
                {
                    errorHandler.execute(context, cause);
                }
                catch (Throwable ignored)
                {
                    // Pass, because errors from error-handlers cannot be reasonably handled.
                }
            }

            @Override
            public Cascade.Stage stage ()
            {
                return STAGE;
            }

            @Override
            public Stage.Actor.Context<I, O> context ()
            {
                return context;
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

            public Object meta ()
            {
                return meta;
            }

            public void meta (final Object value)
            {
                meta = value;
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

                    if (mailbox.offer(message))
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
            return List.copyOf(collection);
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
        return newStage(service);
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
        return newStage(service);
    }

    /**
     * Create a new stage based on a given <code>ExecutorService</code>.
     *
     * @param service will power the new stage.
     * @return the new stage.
     */
    public static Stage newStage (final ExecutorService service)
    {
        Objects.requireNonNull(service, "service");

        return new AbstractStage()
        {
            @Override
            protected void onSubmit (final DefaultActor<?, ?> actor)
            {
                actor.run();
            }

            @Override
            protected void onStageClose ()
            {
                service.shutdown();
            }
        };
    }
}
