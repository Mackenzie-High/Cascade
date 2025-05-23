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

import com.mackenziehigh.cascade.Cascade.Stage.Actor.Builder;
import com.mackenziehigh.cascade.Cascade.Stage.Actor.ConsumerErrorHandler;
import com.mackenziehigh.cascade.Cascade.Stage.Actor.Context;
import com.mackenziehigh.cascade.Cascade.Stage.Actor.ContextErrorHandler;
import com.mackenziehigh.cascade.Cascade.Stage.Actor.ContextScript;
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
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

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
         * @param <I> is the type of messages the actor will consume.
         * @param <O> is the type of messages the actor will produce.
         */
        public interface Actor<I, O>
        {
            /**
             * Actor Builder.
             *
             * @param <I> is the type of messages the actor will consume.
             * @param <O> is the type of messages the actor will produce.
             */
            public interface Builder<I, O>
            {
                /**
                 * Define the normal behavior of the actor.
                 *
                 * <p>
                 * If a script was already defined, then the given
                 * script will replace the previously defined one.
                 * </p>
                 *
                 * <p>
                 * If the only instance of the script is held by a single actor,
                 * then the script will only ever handle one exception at a time.
                 * Thus, the code contained in the script is intrinsically thread-safe.
                 * </p>
                 *
                 * <p>
                 * <b>Warning:</b> If two actors share the same script object,
                 * then that script may be executed concurrently by the independent
                 * actors in order to process messages received independently by each.
                 * Thus, in that case, the script is <b>not</b> intrinsically thread-safe.
                 * </p>
                 *
                 * @param <X> is the type of messages the actor will consume.
                 * @param <Y> is the type of messages the actor will produce.
                 * @param script defines the message-handling behavior of the actor.
                 * @return a modified copy of this builder.
                 */
                public <X, Y> Builder<X, Y> withContextScript (ContextScript<X, Y> script);

                /**
                 * Define the normal behavior of the actor.
                 *
                 * <p>
                 * If a script was already defined, then the given
                 * script will replace the previously defined one.
                 * </p>
                 *
                 * <p>
                 * If the only instance of the script is held by a single actor,
                 * then the script will only ever handle one exception at a time.
                 * Thus, the code contained in the script is intrinsically thread-safe.
                 * </p>
                 *
                 * <p>
                 * <b>Warning:</b> If two actors share the same script object,
                 * then that script may be executed concurrently by the independent
                 * actors in order to process messages received independently by each.
                 * Thus, in that case, the script is <b>not</b> intrinsically thread-safe.
                 * </p>
                 *
                 * @param <X> is the type of messages the actor will consume.
                 * @param <Y> is the type of messages the actor will produce.
                 * @param script defines the message-handling behavior of the actor.
                 * @return a modified copy of this builder.
                 */
                public default <X, Y> Builder<X, Y> withFunctionScript (FunctionScript<X, Y> script)
                {
                    return withContextScript((ctx, input) ->
                    {
                        ctx.sendFrom(script.onInput(input));
                    });
                }

                /**
                 * Define the normal behavior of the actor.
                 *
                 * <p>
                 * If a script was already defined, then the given
                 * script will replace the previously defined one.
                 * </p>
                 *
                 * <p>
                 * If the only instance of the script is held by a single actor,
                 * then the script will only ever handle one exception at a time.
                 * Thus, the code contained in the script is intrinsically thread-safe.
                 * </p>
                 *
                 * <p>
                 * <b>Warning:</b> If two actors share the same script object,
                 * then that script may be executed concurrently by the independent
                 * actors in order to process messages received independently by each.
                 * Thus, in that case, the script is <b>not</b> intrinsically thread-safe.
                 * </p>
                 *
                 * @param <X> is the type of messages the actor will consume.
                 * @param script defines the message-handling behavior of the actor.
                 * @return a modified copy of this builder.
                 */
                public default <X> Builder<X, X> withConsumerScript (ConsumerScript<X> script)
                {
                    return withFunctionScript(x ->
                    {
                        script.onInput(x);
                        return null;
                    });
                }

                /**
                 * Define how the actor responds to unhandled exceptions.
                 *
                 * <p>
                 * If an error-handler was already defined, then that error-handler
                 * and the given error-handler will be composed to form a new (third)
                 * error-handler that executes both error-handlers in sequence.
                 * In effect, this method appends the given error-handler onto
                 * the list of error-handlers that the actor will use.
                 * When an unhandled exception occurs, all of the handlers will execute.
                 * </p>
                 *
                 * <p>
                 * If the only instance of the error-handler is held by a single actor,
                 * then the error-handler will only ever handle one exception at a time.
                 * Thus, the code contained in the error-handler is intrinsically thread-safe.
                 * </p>
                 *
                 * <p>
                 * <b>Warning:</b> If two actors share the same error-handler object,
                 * then that error-handler may be executed concurrently by
                 * the independent actors in order to handle distinct exceptions.
                 * Thus, in that case, the error-handler is <b>not</b> intrinsically thread-safe.
                 * </p>
                 *
                 * <p>
                 * If the error-handler itself throws an exception,
                 * then that exception will be silently dropped.
                 * </p>
                 *
                 * @param handler defines the error-handling behavior of the actor.
                 * @return a modified copy of this builder.
                 */
                public Builder<I, O> withContextErrorHandler (ContextErrorHandler<I, O> handler);

                /**
                 * Define how the actor responds to unhandled exceptions.
                 *
                 * <p>
                 * Equivalent: <code>withContextErrorHandler((context, message, cause) -&gt; handler.onError(cause))</code>
                 * </p>
                 *
                 * @param handler defines the error-handling behavior of the actor.
                 * @return a modified copy of this builder.
                 */
                public default Builder<I, O> withConsumerErrorHandler (final ConsumerErrorHandler handler)
                {
                    return withContextErrorHandler((context, message, cause) -> handler.onError(cause));
                }

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
             * A queue-like (FIFO) data-structure that stores incoming messages.
             *
             * @param <I> is the type of messages that the actor will consume.
             */
            public interface Mailbox<I>
            {
                /**
                 * Add a message to the mailbox.
                 *
                 * @param message will be added to the mailbox, if possible.
                 * @return true, only if the message was in-fact added to the mailbox.
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
             * @param <T> is the type of messages that the actor will consume.
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
                 * <p>
                 * Implementations should <b>not</b> override the default
                 * behavior of this method as defined in this interface.
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
                 * <p>
                 * Implementations should <b>not</b> override the default
                 * behavior of this method as defined in this interface.
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
                 * Determine whether this input is connected to the given output.
                 *
                 * <p>
                 * Implementations should <b>not</b> override the default
                 * behavior of this method as defined in this interface.
                 * </p>
                 *
                 * @param output may be connected to this input.
                 * @return true, if this input is currently connected to the output.
                 */
                public default boolean isConnected (final Output<?> output)
                {
                    return output.isConnected(this);
                }

                /**
                 * Send a message to the actor via this input, silently dropping the message,
                 * if this input does not have sufficient capacity to enqueue the message.
                 *
                 * <p>
                 * Equivalent: <code>return actor().context().offerTo(message);</code>
                 * </p>
                 *
                 * @param message will be processed by the actor, eventually,
                 * if the message is not dropped due to capacity restrictions.
                 * @return true, if the message was successfully added to the underlying mailbox.
                 * @throws NullPointerException if the <code>message</code> is null.
                 */
                public default boolean offer (T message)
                {
                    return actor().context().offerTo(message);
                }

                /**
                 * Send a message to the actor via this input.
                 *
                 * <p>
                 * Equivalent: <code>offer(message); return this;</code>
                 * </p>
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
            }

            /**
             * Output to an Actor.
             *
             * @param <T> is the type of messages that the actor will produce.
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
                 * <p>
                 * When implementing this method, care must be taken to ensure
                 * that concurrent connections and/or disconnection do not
                 * lead to incorrect states, such as duplicate connections.
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
                 * <p>
                 * When implementing this method, care must be taken to ensure
                 * that concurrent connections and/or disconnection do not
                 * lead to incorrect states, such as duplicate connections.
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
             * @param <I> is the type of messages that the enclosing actor consumes.
             * @param <O> is the type of messages that the enclosing actor produces.
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
                 * @param message is the message to send to the actor.
                 * @return true, if the message was successfully added to the underlying mailbox.
                 */
                public boolean offerTo (I message);

                /**
                 * Offer a message <b>from</b> the enclosing actor.
                 *
                 * @param message is the message to send from the actor.
                 * @return true, if the message was sent to every connected output.
                 */
                public boolean offerFrom (O message);

                /**
                 * Send a message <b>to</b> the enclosing actor.
                 *
                 * @param message is the message to send to the actor.
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
                 * @param message is the message to send from the actor.
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
             * @param <I> is the type of messages that the actor will consume.
             * @param <O> is the type of messages that the actor will produce.
             */
            @FunctionalInterface
            public interface ContextScript<I, O>
            {

                /**
                 * This method will be invoked by the enclosing actor
                 * in order to process all incoming messages.
                 *
                 * @param context can be used to send messages from the actor, etc.
                 * @param input is being processed by the actor using this script.
                 * @throws Throwable or a sub-class thereof, at the discretion of the implementation.
                 */
                public void onInput (Context<I, O> context,
                                     I input)
                        throws Throwable;
            }

            /**
             * Actor Behavior.
             *
             * @param <I> is the type of messages that the actor will consume.
             * @param <O> is the type of messages that the actor will produce.
             */
            @FunctionalInterface
            public interface FunctionScript<I, O>
            {
                /**
                 * This method will be invoked by the enclosing actor
                 * in order to process all incoming messages.
                 *
                 * @param input is being processed by the actor using this script.
                 * @return the output message to send from the actor, or null,
                 * if the actor shall not produce an output for the given input.
                 * @throws Throwable or a sub-class thereof, at the discretion of the implementation.
                 */
                public O onInput (I input)
                        throws Throwable;
            }

            /**
             * Actor Behavior.
             *
             * @param <I> is the type of messages that the actor will consume.
             */
            @FunctionalInterface
            public interface ConsumerScript<I>
            {
                /**
                 * This method will be invoked by the enclosing actor
                 * in order to process all incoming messages.
                 *
                 * @param input is being processed by the actor using this script.
                 * @throws Throwable or a sub-class thereof, at the discretion of the implementation.
                 */
                public void onInput (I input)
                        throws Throwable;
            }

            /**
             * Actor Error Handler.
             *
             * @param <I> is the type of messages that the actor will consume.
             * @param <O> is the type of messages that the actor will produce.
             */
            @FunctionalInterface
            public interface ContextErrorHandler<I, O>
            {
                /**
                 * This method will be invoked by the enclosing actor in order to
                 * handle any unhandled exceptions that are thrown by the script.
                 *
                 * <p>
                 * The <code>message</code> is not available, in particular,
                 * if the exception occurred due to a <code>Mailbox.poll()</code>.
                 * </p>
                 *
                 * @param context can be used to send messages from the actor, etc.
                 * @param message was being processed when the exception occurred, if available.
                 * @param cause was thrown by the script and unhandled elsewhere.
                 * @throws Throwable if something goes unexpectedly wrong.
                 */
                public void onError (Context<I, O> context,
                                     I message,
                                     Throwable cause)
                        throws Throwable;

                /**
                 * Compose this script within another script, such that any
                 * exceptions thrown by this script will be silently ignored.
                 *
                 * @return the new script that contains this script.
                 */
                public default ContextErrorHandler<I, O> silent ()
                {
                    return (context, message, cause) ->
                    {
                        try
                        {
                            onError(context, message, cause);
                        }
                        catch (Throwable ex)
                        {
                            // Pass.
                        }
                    };
                }

                /**
                 * Compose this script and the given script into a single script.
                 *
                 * <p>
                 * If either script throws an exception, then the exception will be silently dropped.
                 * </p>
                 *
                 * @param after will come after this script inside of the new script.
                 * @return the new script.
                 */
                public default ContextErrorHandler<I, O> andThen (final ContextErrorHandler<I, O> after)
                {
                    final ContextErrorHandler<I, O> first = silent();
                    final ContextErrorHandler<I, O> second = after.silent();
                    return (context, message, cause) ->
                    {
                        first.onError(context, message, cause);
                        second.onError(context, message, cause);
                    };
                }

                /**
                 * Compose this script and the given script into a single script.
                 *
                 * <p>
                 * If either script throws an exception, then the exception will be silently dropped.
                 * </p>
                 *
                 * @param after will come after this script inside of the new script.
                 * @return the new script.
                 */
                public default ContextErrorHandler<I, O> andThen (final ConsumerErrorHandler after)
                {
                    final ContextErrorHandler<I, O> first = silent();
                    final ConsumerErrorHandler second = after.silent();
                    return (context, message, cause) ->
                    {
                        first.onError(context, message, cause);
                        second.onError(cause);
                    };
                }
            }

            /**
             * Actor Error Handler.
             */
            @FunctionalInterface
            public interface ConsumerErrorHandler
            {
                /**
                 * This method will be invoked by the enclosing actor in order to
                 * handle any unhandled exceptions that are thrown by the script.
                 *
                 * @param cause was thrown by the script and unhandled elsewhere.
                 * @throws Throwable if something goes unexpectedly wrong.
                 */
                public void onError (Throwable cause)
                        throws Throwable;

                /**
                 * Compose this script within another script, such that any
                 * exceptions thrown by this script will be silently ignored.
                 *
                 * @return the new script that contains this script.
                 */
                public default ConsumerErrorHandler silent ()
                {
                    return (cause) ->
                    {
                        try
                        {
                            onError(cause);
                        }
                        catch (Throwable ex)
                        {
                            // Pass.
                        }
                    };
                }

                /**
                 * Compose this script and the given script into a single script.
                 *
                 * <p>
                 * If either script throws an exception, then the exception will be silently dropped.
                 * </p>
                 *
                 * @param after will come after this script inside of the new script.
                 * @return the new script.
                 */
                public default ConsumerErrorHandler andThen (final ConsumerErrorHandler after)
                {
                    final ConsumerErrorHandler first = silent();
                    final ConsumerErrorHandler second = after.silent();
                    return (cause) ->
                    {
                        first.onError(cause);
                        second.onError(cause);
                    };
                }
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
            public Context<I, O> context ();

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
         * <p>
         * This method returns a builder, rather than an actor itself,
         * so that further configuration of the actor can be performed,
         * if the calling code so desires.
         * </p>
         *
         * @param <I> is the type of messages that the actor will consume.
         * @param <O> is the type of messages that the actor will produce.
         * @return the new builder.
         */
        public <I, O> Actor.Builder<I, O> newActor ();

        /**
         * Asynchronously shutdown this stage, as soon as reasonably possible.
         *
         * <p>
         * Subsequent invocations of this method are idempotent.
         * </p>
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
    public static abstract class AbstractStage
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
         * <p>
         * Implementations of this method should never throw exceptions.
         * If an exception or error is thrown, then the stage will be closed.
         * </p>
         *
         * @param actor needs to be <code>run()</code> at some point in the future.
         */
        protected abstract void onRunnable (DefaultActor<?, ?> actor);

        /**
         * This method will be invoked when this stage closes.
         */
        protected abstract void onClose ();

        /**
         * {@inheritDoc}
         */
        @Override
        public final <I, O> Actor.Builder<I, O> newActor ()
        {
            return new DefaultActorBuilder<>();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final void close ()
        {
            if (stageClosed.compareAndSet(false, true))
            {
                onClose();
            }
        }

        /**
         * This method protects against exceptions thrown in the overridden <code>onSubmit()</code> method.
         * If an exception is thrown in that method, then the stage must be shutdown,
         * since we would be unable to ensure that all pending tasks get executed.
         *
         * @param actor needs scheduled for execution.
         */
        private void safelySchedule (final DefaultActor<?, ?> actor)
        {
            try
            {
                onRunnable(actor);
            }
            catch (Throwable ex)
            {
                close();
            }
        }

        /**
         * Default Implementation of the <code>Actor.Builder</code> interface.
         *
         * @param <I> is the type of messages that the actor will consume.
         * @param <O> is the type of messages that the actor will produce.
         */
        private final class DefaultActorBuilder<I, O>
                implements Cascade.Stage.Actor.Builder<I, O>
        {
            private final Mailbox<I> mailbox;

            private final ContextScript<I, O> script;

            private final ContextErrorHandler<I, O> errorHandler;

            private DefaultActorBuilder ()
            {
                this.mailbox = ConcurrentLinkedQueueMailbox.create();

                this.script = (context, message) ->
                {
                    // Pass.
                };

                this.errorHandler = (context, message, cause) ->
                {
                    // Pass.
                };
            }

            private DefaultActorBuilder (final Mailbox<I> mailbox,
                                         final ContextScript<I, O> script,
                                         final ContextErrorHandler<I, O> errorHandler)
            {
                this.mailbox = mailbox;
                this.script = script;
                this.errorHandler = errorHandler;
            }

            @Override
            public <X, Y> Actor.Builder<X, Y> withContextScript (final Stage.Actor.ContextScript<X, Y> script)
            {
                Objects.requireNonNull(script, "script");
                return new DefaultActorBuilder(mailbox, script, errorHandler);
            }

            @Override
            public Actor.Builder<I, O> withContextErrorHandler (final ContextErrorHandler<I, O> handler)
            {
                Objects.requireNonNull(handler, "handler");

                /**
                 * Combine the given handler and any previously defined handlers.
                 * Execute each of the handlers in sequence, even if one fails.
                 * If any handler throws an exception, simply ignore it.
                 * In general, an error-handler should not cause an error itself.
                 */
                final ContextErrorHandler<I, O> combined = errorHandler.andThen(handler);
                return new DefaultActorBuilder(mailbox, script, combined);
            }

            @Override
            public Actor.Builder<I, O> withMailbox (final Mailbox<I> mailbox)
            {
                Objects.requireNonNull(mailbox, "mailbox");
                return new DefaultActorBuilder(mailbox, script, errorHandler);
            }

            @Override
            public Actor<I, O> create ()
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
            /**
             * This reference just makes 'this' more explicit in
             * order to avoid confusion caused by nested classes.
             */
            private final DefaultActor<I, O> ACTOR = this;

            /**
             * This mailbox stores the backlog of messages that
             * need to be processed by this actor one at a time.
             */
            private final Mailbox<I> mailbox;

            /**
             * This script will be used to process those messages.
             */
            private final ContextScript<I, O> script;

            /**
             * If that script throws an unhandled exception,
             * then this error-handler will be invoked in
             * order to handle the exception.
             */
            private final ContextErrorHandler<I, O> errorHandler;

            /**
             * This object provides the ability to send messages to
             * and from this actor and will be passed-in to the script.
             */
            private final DefaultContext context = new DefaultContext();

            /**
             * This object provides the input-connector API and wraps the mailbox.
             */
            private final DefaultInput input = new DefaultInput();

            /**
             * This object provides the output-connector API.
             */
            private final DefaultOutput output = new DefaultOutput();

            /**
             * This is the number of messages that are in the mailbox.
             */
            private final AtomicLong pendingCranks = new AtomicLong();

            /**
             * This flag is simply used as a sanity check to detect bugs,
             * if the run() method is executed concurrently; therefore,
             * this may be removed at some point in the future.
             */
            private final AtomicBoolean inProgress = new AtomicBoolean(false);

            /**
             * This field can be used by custom stage implementations
             * to store implementation-specific information.
             */
            private volatile Object meta = null;

            private DefaultActor (final DefaultActorBuilder<I, O> builder)
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
                    /**
                     * This should never actually happen, period; however, the likely cause is either:
                     * (1) the AbstractStage implementation called run() twice for one onRunnable() call,
                     * (2) the scheduling algorithm in this class is fundamentally broken.
                     * In the case of custom stages, case (1) is the most likely cause.
                     */
                    throw new IllegalStateException("concurrent run()");
                }

                I message = null;

                try
                {
                    /**
                     * Pull the next message from the mailbox and
                     * then process the message using the script.
                     */
                    message = mailbox.poll();

                    if (message != null)
                    {
                        script.onInput(context, message);
                    }
                }
                catch (Throwable cause)
                {
                    /**
                     * Invoke the error-handler given the message and exception,
                     * but do not allow the error-handler to throw an exception.
                     * If the poll() threw the exception, then the message is null.
                     */
                    handleException(message, cause);
                }
                finally
                {
                    /**
                     * Now that the processing of the message is complete,
                     * go ahead and schedule the next message, if any.
                     */
                    inProgress.set(false);
                    scheduleSubsequentMessage();
                }
            }

            private void handleException (final I message,
                                          final Throwable cause)
            {
                try
                {
                    errorHandler.onError(context, message, cause);
                }
                catch (Throwable ignored)
                {
                    // Pass, because errors from error-handlers cannot be reasonably handled.
                }
            }

            private void scheduleInitialMessage ()
            {
                if (pendingCranks.incrementAndGet() == 1)
                {
                    safelySchedule(ACTOR);
                }
            }

            private void scheduleSubsequentMessage ()
            {
                if (pendingCranks.decrementAndGet() != 0)
                {
                    safelySchedule(ACTOR);
                }
            }

            @Override
            public Stage stage ()
            {
                return STAGE;
            }

            @Override
            public Context<I, O> context ()
            {
                return context;
            }

            @Override
            public Input<I> input ()
            {
                return input;
            }

            @Override
            public Output<O> output ()
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

            private final class DefaultContext
                    implements Context<I, O>
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
                        final List<Input<O>> outputs = output.connectionList;
                        final int length = outputs.size();

                        // Using for instead of for-each avoids creating an iterator object.
                        for (int i = 0; i < length; i++)
                        {
                            sentToAll &= outputs.get(i).offer(message);
                        }
                    }

                    return sentToAll;
                }

                @Override
                public boolean offerTo (final I message)
                {
                    Objects.requireNonNull(message, "message");

                    if (mailbox.offer(message))
                    {
                        scheduleInitialMessage();
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
            };

            /**
             * Default Implementation of <code>Actor.Input</code>.
             */
            private final class DefaultInput
                    implements Actor.Input<I>
            {
                @Override
                public Actor<I, ?> actor ()
                {
                    return ACTOR;
                }
            }

            /**
             * Default Implementation of <code>Actor.Output</code>.
             */
            private final class DefaultOutput
                    implements Actor.Output<O>
            {
                /**
                 * This lock is used to prevent concurrent connections and disconnections;
                 * however, this lock is not in the critical-path of message processing.
                 */
                private final Object outputLock = new Object();

                /**
                 * This is an immutable list containing the inputs that this output is connected to.
                 */
                private volatile List<Input<O>> connectionList = newImmutableList(Collections.EMPTY_LIST);

                @Override
                public Actor<?, O> actor ()
                {
                    return ACTOR;
                }

                @Override
                public Output<O> connect (final Stage.Actor.Input<O> input)
                {
                    Objects.requireNonNull(input, "input");

                    synchronized (outputLock)
                    {
                        if (isConnected(input) == false)
                        {
                            final List<Input<O>> modified = new ArrayList<>(connectionList);
                            modified.add(input);
                            connectionList = newImmutableList(modified);
                        }
                    }

                    return this;
                }

                @Override
                public Output<O> disconnect (final Stage.Actor.Input<O> input)
                {
                    Objects.requireNonNull(input, "input");

                    synchronized (outputLock)
                    {
                        if (isConnected(input))
                        {
                            final List<Input<O>> modified = new ArrayList<>(connectionList);
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
     * <p>
     * The stage will use a non-daemon thread.
     * </p>
     *
     * @return the new stage.
     */
    public static Stage newStage ()
    {
        return newStage(1);
    }

    /**
     * Create a new multi-threaded stage.
     *
     * <p>
     * The stage will use non-daemon threads.
     * </p>
     *
     * @param threadCount is the number of worker threads that the stage will use.
     * @return the new stage.
     */
    public static Stage newStage (final int threadCount)
    {
        return newStage(threadCount, false);
    }

    /**
     * Create a new multi-threaded stage.
     *
     * @param threadCount is the number of worker threads that the stage will use.
     * @param daemon is true, if the threads will be daemon threads.
     * @return the new stage.
     */
    public static Stage newStage (final int threadCount,
                                  final boolean daemon)
    {
        final ThreadFactory factory = (Runnable task) ->
        {
            final Thread thread = new Thread(task);
            thread.setDaemon(daemon);
            return thread;
        };

        final ExecutorService service = Executors.newFixedThreadPool(threadCount, factory);
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
            protected void onRunnable (final DefaultActor<?, ?> actor)
            {
                /**
                 * Schedule the actor to run at some point in the future.
                 * The actor itself, without blocking, will guarantee that
                 * it is only being run() by one thread at a time.
                 */
                service.execute(actor);
            }

            @Override
            protected void onClose ()
            {
                service.shutdown();
            }
        };
    }
}
