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

import com.google.common.base.Preconditions;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/**
 * A reaction defines how a reactor behaves in response to inputs.
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
        public void run ()
                throws Throwable;

        /**
         * Combine this task and the given task into a single task.
         *
         * @param next will be executed after this task.
         * @return the combined task.
         */
        public default ReactionTask andThen (final ReactionTask next)
        {
            Preconditions.checkNotNull(next, "next");
            return () ->
            {
                this.run();
                next.run();
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
     * @param condition is true whenever the reaction shall occur.
     * @return this.
     */
    public Reaction require (BooleanSupplier condition);

    /**
     * Specify the minimum number of messages that must be enqueued
     * in the given input in order for the reaction to execute.
     *
     * @param input must contain at least <code>count</code> messages;
     * otherwise, the reaction will not occur.
     * @param count is the number of messages enqueued in the <code>input</code>.
     * @return this.
     */
    public Reaction require (Input<?> input,
                             int count);

    /**
     * Specify that the given input must have at least one message
     * enqueued in order for the reaction to occur.
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
     * and that the head of the input must math the given predicate
     * in order for the reaction to occur.
     *
     * @param <T>
     * @param input must contain at least one message.
     * @param head must be true given the head of the <code>input</code>.
     * @return this.
     */
    public <T> Reaction require (Input<T> input,
                                 Predicate<T> head);

    /**
     * Specify that the given output must contain at least
     * one message in order for the reaction to occur.
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
     * @param output must have at least <code>count</code> remaining capacity;
     * otherwise, the reaction will not occur.
     * @param count is the number of messages that the output must be able to accept.
     * @return this.
     */
    public Reaction require (Output<?> output,
                             int count);

    /**
     * Specify a task to perform whenever this reaction is allowed to execute.
     *
     * <p>
     * This method may be invoked repeatedly in order to define a series of tasks.
     * </p>
     *
     * @param task defines the behavior of this reaction.
     * @return this.
     */
    public Reaction onMatch (ReactionTask task);

    /**
     * Specify a task to execute whenever an exception is thrown within this reaction.
     *
     * TODO: Should the reaction task be given the exception has an argument???
     *
     * @param handler shall attempt to handle any unhandled exceptions.
     * @return this.
     */
    public Reaction onError (ReactionTask handler);
}
