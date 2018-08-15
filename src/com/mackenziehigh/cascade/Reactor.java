/*
 * Copyright 2018 Michael Mackenzie High
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

import com.mackenziehigh.cascade.Reactor.Input.OverflowPolicy;
import com.mackenziehigh.internal.cascade.ArrayInflowDeque;
import com.mackenziehigh.internal.cascade.LinkedInflowDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A reactor responds to inputs by producing outputs and/or side-effects via reactions.
 */
public interface Reactor
{

    /**
     * An <code>Input</code> queues messages that are destined for the enclosing <code>Reactor</code>.
     *
     * <p>
     * An <code>Input</code> can be connected to zero-or-one <code>Output</code> at a time.
     * The connected <code>Output</code> will send messages to the <code>Input</code>.
     * </p>
     */
    public interface Input<E>
    {
        /**
         * An <code>OverflowPolicy</code> defines what to do when a queue overflow occurs.
         */
        public enum OverflowPolicy
        {
            /**
             * The overflow-policy depends on the queue implementation.
             */
            UNSPECIFIED,

            /**
             * Drop the oldest message that is already in the queue.
             */
            DROP_OLDEST,

            /**
             * Drop the newest message that is already in the queue.
             */
            DROP_NEWEST,

            /**
             * Drop the message that is being inserted into the queue,
             * rather than removing an element that is already in the queue.
             */
            DROP_INCOMING,

            /**
             * Drop everything already in the queue, but accept the incoming message.
             */
            DROP_PENDING,

            /**
             * Drop everything already in the queue and the incoming message.
             */
            DROP_ALL,
            /**
             * Throw an <code>IllegalStateException</code>.
             */
            THROW,
        }

        /**
         * An <code>InflowDeque</code> provides the actual storage needed by an <code>Input</code>.
         *
         * <p>
         * Instances of this interface do <b>not</b> need to ensure thread-safety themselves.
         * </p>
         *
         * <p>
         * Instances of this interface do <b>not</b> need to implement the <code>OverflowPolicy</code>.
         * Rather, the instances merely need to identify the <code>OverflowPolicy</code>.
         * </p>
         *
         * @param <E> is the type of the messages bound for the reactor.
         */
        public interface InflowDeque<E>
                extends Deque<E>
        {
            /**
             * Getter.
             *
             * @return the maximum allowed capacity of the queue.
             */
            public int capacity ();

            /**
             * Getter.
             *
             * @return what <i>should</i> happen when this queue overflows.
             */
            public OverflowPolicy overflowPolicy ();
        }

        /**
         * Specify a verification-check that will be performed
         * whenever a message is enqueued in this input.
         *
         * <p>
         * The verification-check will be performed by the sender
         * on whatever thread the sender is executing on.
         * Moreover, the verification-check may be executed concurrently
         * by different senders given different messages.
         * </p>
         *
         * <p>
         * The verification-check will never receive null as a message.
         * </p>
         *
         * <p>
         * Exceptions thrown by the condition will be propagated.
         * </p>
         *
         * <p>
         * This method causes the enclosing reactor to be signaled.
         * </p>
         *
         * @param condition must be true given the incoming message.
         * @return this.
         */
        public Input<E> verify (Predicate<E> condition);

        /**
         * Retrieve a UUID that uniquely identifies this input in space-time.
         *
         * @return the unique identifier of this input.
         */
        public UUID uuid ();

        /**
         * Retrieve the type of the messages that are enqueued in this object.
         *
         * @return the type of the messages herein.
         */
        public Class<E> type ();

        /**
         * Get the policy that dictates what happens, if this queue overflows.
         *
         * @return the overflow-policy of this input.
         */
        public OverflowPolicy overflowPolicy ();

        /**
         * Retrieve the name of this input.
         *
         * @return the name of this input.
         */
        public String name ();

        /**
         * Specify the name of the input.
         *
         * <p>
         * This method causes the enclosing reactor to be signaled.
         * </p>
         *
         * @param name will be the name of the input.
         * @return this.
         */
        public Input<E> named (String name);

        /**
         * Retrieve the reactor that this input is a part of.
         *
         * @return the enclosing reactor.
         */
        public Reactor reactor ();

        /**
         * Connect this input to the given output.
         *
         * <p>
         * This method is a no-op, if the connection exists.
         * </p>
         *
         * <p>
         * This method causes the newly connected reactor to be signaled.
         * This method causes the enclosing reactor to be signaled.
         * </p>
         *
         * @param output will subsequently be connected hereto.
         * @return this.
         * @throws IllegalStateException if this input is already connect to a different output.
         */
        public Input<E> connect (Output<E> output);

        /**
         * Disconnect this input from the output that it is connected-to.
         *
         * <p>
         * This method is a no-op, if this input is already disconnected.
         * </p>
         *
         * <p>
         * This method causes the newly disconnected reactor, if any, to be signaled.
         * This method causes the enclosing reactor to be signaled.
         * </p>
         *
         * @return this.
         */
        public Input<E> disconnect ();

        /**
         * Retrieve the output that feeds messages to this input,
         * if this input is currently connected to an output.
         *
         * @return the connected output, if connected; otherwise, return empty.
         */
        public Optional<Output<E>> connection ();

        /**
         * Determine whether this input is connected to an output.
         *
         * @return true, if a <code>connection()</code> is present.
         */
        public default boolean isConnected ()
        {
            return connection().isPresent();
        }

        /**
         * Retrieve the maximum number of messages that can
         * be enqueued in this input at a single time.
         *
         * @return the capacity of this input.
         */
        public int capacity ();

        /**
         * Retrieve the number of messages currently enqueued herein.
         *
         * @return the number of enqueued messages.
         */
        public int size ();

        /**
         * Calculate the number of messages that can be enqueued herein
         * before the maximum capacity of this input is reached.
         *
         * @return <code>capacity() - size()</code>
         */
        public int remainingCapacity ();

        /**
         * Determine whether no messages are currently enqueued herein.
         *
         * @return <code>size() > 0</code>
         */
        public default boolean isEmpty ()
        {
            return size() == 0;
        }

        /**
         * Determine whether the maximum number of messages that can
         * be simultaneously enqueued in this input are enqueued.
         *
         * @return <code>size() == capacity()</code>
         */
        public boolean isFull ();

        /**
         * Retrieve, but do not remove, the message that has
         * been enqueued in this input for the longest time.
         *
         * @return the first element in the FIFO queue,
         * or null, if the queue is empty.
         */
        public default E peekOrNull ()
        {
            return peekOrDefault(null);
        }

        /**
         * Retrieve, but do not remove, the message that has
         * been enqueued in this input for the longest time.
         *
         * <p>
         * The default-value is allowed to be null.
         * </p>
         *
         * @param defaultValue will be returned, if the queue is empty.
         * @return the first element in the FIFO queue,
         * or the default value, if the queue is empty.
         */
        public E peekOrDefault (E defaultValue);

        /**
         * Retrieve, but do not remove, the message that has
         * been enqueued in this input for the longest time.
         *
         * @return the first element in the FIFO queue,
         * or empty, if the queue is empty.
         */
        public default Optional<E> peek ()
        {
            final E head = peekOrNull();
            return Optional.ofNullable(head);
        }

        /**
         * Remove all enqueued messages from this input.
         *
         * <p>
         * This method causes the connected reactor, if any, to be signaled.
         * </p>
         *
         * @return this.
         */
        public Input<E> clear ();

        /**
         * Retrieve and remove the message that has been
         * enqueued in this input for the longest time.
         *
         * <p>
         * This method causes the connected reactor, if any, to be signaled.
         * </p>
         *
         * @return the first element in the FIFO queue,
         * or null, if the queue is empty.
         */
        public default E pollOrNull ()
        {
            return pollOrDefault(null);
        }

        /**
         * Retrieve and remove the message that has been
         * enqueued in this input for the longest time.
         *
         * <p>
         * This method causes the connected reactor, if any, to be signaled.
         * </p>
         *
         * @param defaultValue will returned, if the queue is empty.
         * @return the first element in the FIFO queue,
         * or the default value, if the queue is empty.
         */
        public E pollOrDefault (E defaultValue);

        /**
         * Retrieve and remove the message that has been
         * enqueued in this input for the longest time.
         *
         * <p>
         * This method causes the connected reactor, if any, to be signaled.
         * </p>
         *
         * @return the first element in the FIFO queue,
         * or empty, if the queue is empty.
         */
        public default Optional<E> poll ()
        {
            final E head = pollOrDefault(null);
            return Optional.ofNullable(head);
        }

        /**
         * Apply the given function to each message enqueued in this input.
         *
         * <p>
         * The messages will be traversed in FIFO order.
         * </p>
         *
         * @param functor will be executed given each message herein.
         * @return this.
         */
        public Input<E> forEach (Consumer<E> functor);

        /**
         * Send the given message to this input.
         *
         * <p>
         * This method causes the enclosing reactor to be signaled.
         * </p>
         *
         * <p>
         * If this input is connected to an output, then do not call this method!
         * Otherwise, a race-condition could develop between you and the connected reactor.
         * The connection reactor may be monitoring this input for available space,
         * before attempting to send a message. If you send messages concurrently,
         * then an inadvertent overflow may occur.
         * </p>
         *
         * @param message will be sent.
         * @return this.
         * @throws NullPointerException if the message is null.
         */
        public Input<E> send (E message);

        /**
         * Re/configure this input to use the given queue as the backing storage provider.
         *
         * <p>
         * If this input already contains enqueued messages,
         * then an attempt will be made to enqueue them in the new queue;
         * however, the operation is subject to the capacity and overflow
         * limitations of the new queue.
         * </p>
         *
         * <p>
         * This method causes the connected reactor, if any, to be signaled.
         * This method causes the enclosing reactor to be signaled.
         * </p>
         *
         * @param queue will be used to store the messages enqueued herein.
         * @return this.
         */
        public Input<E> useInflowDeque (InflowDeque<E> queue);

        /**
         * Re/configure this input to use array-based storage.
         *
         * <p>
         * This method causes the connected reactor, if any, to be signaled.
         * This method causes the enclosing reactor to be signaled.
         * </p>
         *
         * @param capacity will be the maximum capacity of this queue.
         * @param policy defines how queue overflows will be handled.
         * @return this.
         */
        public default Input<E> useArrayInflowDeque (int capacity,
                                                     OverflowPolicy policy)
        {
            final InflowDeque<E> queue = new ArrayInflowDeque<>(capacity, policy);
            return useInflowDeque(queue);
        }

        /**
         * Re/configure this input to use array-based storage.
         *
         * <p>
         * The overflow-policy will be <i>Drop Incoming</i>.
         * </p>
         *
         * <p>
         * This method causes the connected reactor, if any, to be signaled.
         * This method causes the enclosing reactor to be signaled.
         * </p>
         *
         * @param capacity will be the maximum capacity of this queue.
         * @return this.
         */
        public default Input<E> useArrayInflowDeque (int capacity)
        {
            return useArrayInflowDeque(capacity, OverflowPolicy.DROP_INCOMING);
        }

        /**
         * Re/configure this input to use linked-list based storage.
         *
         * <p>
         * This method causes the connected reactor, if any, to be signaled.
         * This method causes the enclosing reactor to be signaled.
         * </p>
         *
         * @param capacity will be the maximum capacity of this queue.
         * @param policy defines how queue overflows will be handled.
         * @return this.
         */
        public default Input<E> useLinkedInflowDeque (int capacity,
                                                      OverflowPolicy policy)
        {
            final InflowDeque<E> queue = new LinkedInflowDeque<>(capacity, policy);
            return useInflowDeque(queue);
        }

        /**
         * Re/configure this input to use linked-list based storage.
         *
         * <p>
         * The overflow-policy will be <i>Drop Incoming</i>.
         * </p>
         *
         * <p>
         * This method causes the connected reactor, if any, to be signaled.
         * This method causes the enclosing reactor to be signaled.
         * </p>
         *
         * @param capacity will be the maximum capacity of this queue.
         * @return this.
         */
        public default Input<E> useLinkedInflowDeque (int capacity)
        {
            return useLinkedInflowDeque(capacity, OverflowPolicy.DROP_INCOMING);
        }

        /**
         * Re/configure this input to use linked-list based storage.
         *
         * <p>
         * The overflow-policy will be <i>Drop Incoming</i>.
         * </p>
         *
         * <p>
         * The queue will have a maximal capacity.
         * </p>
         *
         * <p>
         * This method causes the connected reactor, if any, to be signaled.
         * This method causes the enclosing reactor to be signaled.
         * </p>
         *
         * @return this.
         */
        public default Input<E> useLinkedInflowDeque ()
        {
            return useLinkedInflowDeque(Integer.MAX_VALUE);
        }
    }

    /**
     * An <code>Output</code> transmits messages from the enclosing
     * <code>Reactor</code> to the connected <code>Input</code>.
     *
     * <p>
     * An <code>Output</code> can be connected to zero-or-one <code>Input</code> at a time.
     * </p>
     */
    public interface Output<E>
    {
        /**
         * Specify a verification-check that will be performed
         * whenever a message is via this output.
         *
         * <p>
         * The verification-check will be performed by the sender
         * on whatever thread the sender is executing on.
         * Moreover, the verification-check may be executed concurrently
         * by different senders given different messages.
         * </p>
         *
         * <p>
         * The verification-check will never receive null as a message.
         * </p>
         *
         * <p>
         * Exceptions thrown by the condition will be propagated.
         * </p>
         *
         * <p>
         * This method causes the enclosing reactor to be signaled.
         * </p>
         *
         * @param condition must be true given the outgoing message.
         * @return this.
         */
        public Output<E> verify (Predicate<E> condition);

        /**
         * Retrieve a UUID that uniquely identifies this output in space-time.
         *
         * @return the unique identifier of this output.
         */
        public UUID uuid ();

        /**
         * Retrieve the name of this output.
         *
         * @return the name of this output.
         */
        public String name ();

        /**
         * Specify the name of the output.
         *
         * <p>
         * This method causes the enclosing reactor to be signaled.
         * </p>
         *
         * @param name will be the name of the output.
         * @return this.
         */
        public Output<E> named (String name);

        /**
         * Retrieve the type of messages that can be transmitted via this output.
         *
         * @return the type of messages that this output can send.
         */
        public Class<E> type ();

        /**
         * Retrieve the reactor that this output is a part of.
         *
         * @return the enclosing reactor.
         */
        public Reactor reactor ();

        /**
         * Connect this output to the given put.
         *
         * <p>
         * This method is a no-op, if the connection exists.
         * </p>
         *
         * <p>
         * This method causes the connected reactor, if any, to be signaled.
         * This method causes the enclosing reactor to be signaled.
         * </p>
         *
         * @param input will subsequently be connected hereto.
         * @return this.
         * @throws IllegalStateException if this output is already connected to a different input.
         */
        public Output<E> connect (Input<E> input);

        /**
         * Disconnect this output from the input that it is connected-to.
         *
         * <p>
         * This method is a no-op, if this output is already disconnected.
         * </p>
         *
         * <p>
         * This method causes the newly disconnected reactor, if any, to be signaled.
         * This method causes the enclosing reactor to be signaled.
         * </p>
         *
         * @return this.
         */
        public Output<E> disconnect ();

        /**
         * Retrieve the input that this output will feed messages to,
         * if this output is currently connected to an input.
         *
         * @return the connected input, if connected; otherwise, return empty.
         */
        public Optional<Input<E>> connection ();

        /**
         * Determine whether this output is connected to an input.
         *
         * @return true, if a <code>connection()</code> is present.
         */
        public default boolean isConnected ()
        {
            return connection().isPresent();
        }

        /**
         * Determine whether the maximum number of messages that can
         * be simultaneously enqueued in the connected input are enqueued.
         *
         * @return <code>size() == capacity()</code>
         */
        public boolean isFull ();

        /**
         * Determine whether any messages are currently enqueued in the connected input.
         *
         * @return <code>size() > 0</code>
         */
        public default boolean isEmpty ()
        {
            return size() == 0;
        }

        /**
         * Retrieve the maximum number of messages that can
         * be enqueued in the connected input at a single time.
         *
         * @return the capacity of the connected input.
         */
        public int capacity ();

        /**
         * Retrieve the number of messages currently enqueued in the connected input.
         *
         * @return the number of enqueued messages.
         */
        public int size ();

        /**
         * Calculate the number of messages that can be enqueued in the connected
         * input before the maximum capacity of the input is reached.
         *
         * @return <code>capacity() - size()</code>
         */
        public int remainingCapacity ();

        /**
         * Send a message via this output to the connected input, if any.
         *
         * <p>
         * This method is a no-op, if this output is not connected.
         * </p>
         *
         * <p>
         * This method causes the enclosing reactor to be signaled.
         * </p>
         *
         * @param message will be sent to the connected input.
         * @return this.
         * @throws NullPointerException if the message is null.
         */
        public Output<E> send (E message);
    }

    /**
     * A <code>Reaction</code> defines how a reactor behaves in response to inputs.
     */
    public interface Reaction
    {
        /**
         * A nullary void function that can throw checked exceptions.
         */
        @FunctionalInterface
        public interface ReactionTask
        {
            /**
             * Execute the task.
             *
             * @throws Throwable if something goes wrong.
             */
            public void execute ()
                    throws Throwable;

            /**
             * Combine this task and the given task into a single task.
             *
             * @param next will be executed after this task.
             * @return the combined task.
             */
            public default ReactionTask andThen (final ReactionTask next)
            {
                Objects.requireNonNull(next, "next");
                return () ->
                {
                    this.execute();
                    next.execute();
                };
            }
        }

        /**
         * A unary void function that consume checked exceptions.
         */
        @FunctionalInterface
        public interface ErrorHandlerTask
        {
            /**
             * Execute the task.
             *
             * @param cause needs to be handled by the task.
             * @throws Throwable if something goes wrong.
             */
            public void accept (final Throwable cause)
                    throws Throwable;

            /**
             * Combine this task and the given task into a single task.
             *
             * @param next will be executed after this task.
             * @return the combined task.
             */
            public default ErrorHandlerTask andThen (final ErrorHandlerTask next)
            {
                Objects.requireNonNull(next, "next");
                return ex ->
                {
                    this.accept(ex);
                    next.accept(ex);
                };
            }
        }

        /**
         * Retrieve a UUID that uniquely identifies this reaction in space-time.
         *
         * @return the unique identifier of this reaction.
         */
        public UUID uuid ();

        /**
         * Retrieve the name of this reaction.
         *
         * @return the name of this reaction.
         */
        public String name ();

        /**
         * Specify the name of the reaction.
         *
         * <p>
         * This method causes the enclosing reactor to be signaled.
         * </p>
         *
         * @param name will be the name of the reaction.
         * @return this.
         */
        public Reaction named (String name);

        /**
         * Retrieve the reactor that this reaction is a part of.
         *
         * @return the enclosing reactor.
         */
        public Reactor reactor ();

        /**
         * Specify a condition that must be true in order for the reaction to execute.
         *
         * <p>
         * This method causes the enclosing reactor to be signaled.
         * </p>
         *
         * @param condition is true whenever the reaction shall occur.
         * @return this.
         */
        public Reaction require (BooleanSupplier condition);

        /**
         * Specify the minimum number of messages that must be enqueued
         * in the given input in order for the reaction to execute.
         *
         * <p>
         * This method causes the enclosing reactor to be signaled.
         * </p>
         *
         * @param input must contain at least <code>count</code> messages;
         * otherwise, the reaction will not occur.
         * @param count is the number of messages enqueued in the <code>input</code>.
         * @return this.
         */
        public default Reaction require (Input<?> input,
                                         int count)
        {
            return require(() -> input.size() >= count);
        }

        /**
         * Specify that the given input must have at least one message
         * enqueued in order for the reaction to occur.
         *
         * <p>
         * This method causes the enclosing reactor to be signaled.
         * </p>
         *
         * @param input must contain at least one message.
         * @return this.
         */
        public default Reaction require (Input<?> input)
        {
            return require(input, 1);
        }

        /**
         * Specify that the given input must contain at least one message
         * and that the head of the input must match the given predicate
         * in order for the reaction to occur.
         *
         * <p>
         * This method causes the enclosing reactor to be signaled.
         * </p>
         *
         * @param <T>
         * @param input must contain at least one message.
         * @param head must be true given the head of the <code>input</code>.
         * @return this.
         */
        public default <T> Reaction require (Input<T> input,
                                             Predicate<T> head)
        {
            return require(() -> input.size() >= 1 && head.test(input.peekOrNull()));
        }

        /**
         * Specify that the given output must contain at least
         * one message in order for the reaction to occur.
         *
         * <p>
         * This method causes the enclosing reactor to be signaled.
         * </p>
         *
         * @param output must contain at least one message.
         * @return this.
         */
        public default Reaction require (Output<?> output)
        {
            return require(output, 1);
        }

        /**
         * Specify the minimum number of messages that must able to be enqueued
         * in the given output in order for the reaction to execute.
         *
         * <p>
         * This method causes the enclosing reactor to be signaled.
         * </p>
         *
         * @param output must have at least <code>count</code> remaining capacity;
         * otherwise, the reaction will not occur.
         * @param count is the number of messages that the output must be able to accept.
         * @return this.
         */
        public default Reaction require (Output<?> output,
                                         int count)
        {
            return require(() -> output.remainingCapacity() >= count || !output.isConnected());
        }

        /**
         * Specify a task to perform whenever this reaction is allowed to execute.
         *
         * <p>
         * This method may be invoked repeatedly in order to define a series of tasks.
         * </p>
         *
         * <p>
         * This method causes the enclosing reactor to be signaled.
         * </p>
         *
         * @param task defines the behavior of this reaction.
         * @return this.
         */
        public Reaction onMatch (ReactionTask task);

        /**
         * Specify a task to execute whenever an exception is thrown within this reaction.
         *
         * <p>
         * This method may be invoked repeatedly in order to define a series of error-handlers.
         * </p>
         *
         * <p>
         * This method causes the enclosing reactor to be signaled.
         * </p>
         *
         * @param handler shall attempt to handle any unhandled exceptions.
         * @return this.
         */
        public Reaction onError (ErrorHandlerTask handler);
    }

    /**
     * Add a new input to this reactor.
     *
     * <p>
     * This method causes the reactor to be signaled.
     * </p>
     *
     * @param <T>
     * @param type is the type of messages that the input will receive.
     * @return the new input.
     */
    public <T> Input<T> newInput (Class<T> type);

    /**
     * Add a new output to this reactor.
     *
     * <p>
     * This method causes the reactor to be signaled.
     * </p>
     *
     * @param <T>
     * @param type is the type of messages that the output will send.
     * @return the new output.
     */
    public <T> Output<T> newOutput (Class<T> type);

    /**
     * Create a reaction to execute whenever this reactor is signaled.
     *
     * <p>
     * This method causes the reactor to be signaled.
     * </p>
     *
     * @return the new reaction.
     */
    public Reaction newReaction ();

    /**
     * Get the UUID that uniquely identifies this reactor in space-time.
     *
     * @return the unique identifier of this reactor.
     */
    public UUID uuid ();

    /**
     * Get the name of this reactor.
     *
     * <p>
     * By default, the name is the string representation of the <code>uuid()</code>.
     * </p>
     *
     * @return the name of this reactor.
     */
    public String name ();

    /**
     * Set the name of the reactor.
     *
     * <p>
     * This method causes the reactor to be signaled.
     * </p>
     *
     * @param name will be the name of the reactor.
     * @return this.
     */
    public Reactor named (String name);

    /**
     * Get all of the inputs that feed messages into this reactor.
     *
     * @return the inputs.
     */
    public Set<Input<?>> inputs ();

    /**
     * Get all of the outputs that transmit messages from this reactor.
     *
     * @return the outputs.
     */
    public Set<Output<?>> outputs ();

    /**
     * Get all of the reactions that define the behavior of this reactor.
     *
     * <p>
     * The returned list of reactions are in declaration-order.
     * </p>
     *
     * @return the reactions.
     */
    public List<Reaction> reactions ();

    /**
     * Disconnect the reactor from all of the inputs and outputs.
     *
     * <p>
     * This method causes the reactor to be signaled.
     * This method causes any newly disconnected reactors, if any, to be signaled.
     * </p>
     *
     * @return this.
     */
    public Reactor disconnect ();

    /**
     * Determine whether this reactor is currently reacting to an input.
     *
     * @return true, if a reaction is in-progress.
     */
    public boolean isReacting ();

    /**
     * Get the powerplant that provides power to this reactor whenever necessary.
     *
     * <p>
     * The powerplant may change during the life of the reactor.
     * </p>
     *
     * @return the associated powerplant.
     */
    public Powerplant powerplant ();

    /**
     * Set the powerplant that will power the reactor.
     *
     * <p>
     * This method can be called after the reactor has started.
     * </p>
     *
     * <p>
     * This method causes the reactor to be signaled after the powerplant is changed.
     * </p>
     *
     * @param plant will power the reactor whenever needed.
     * @return this.
     */
    public Reactor poweredBy (Powerplant plant);

    /**
     * Cause this reactor to be scheduled for execution by the powerplant.
     *
     * @return this.
     */
    public Reactor signal ();

    /**
     * Cause this reactor to perform a single unit-of-work, if work is available.
     *
     * <p>
     * A single unit-of-work entails executing each reaction once.
     * The reactions are executed in declaration-order.
     * </p>
     *
     * <p>
     * This method never throws an exception.
     * </p>
     *
     * @return true, if meaningful work was performed.
     */
    public boolean crank ();
}
