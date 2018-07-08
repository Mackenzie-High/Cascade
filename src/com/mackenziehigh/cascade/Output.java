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

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * An <code>Output</code> transmits messages from the enclosing
 * <code>Reactor</code> to the connected <code>Input</code>.
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
     * @param input will subsequently be connected hereto.
     * @return this.
     */
    public Output<E> connect (Input<E> input);

    /**
     * Disconnect this output from the input that it is connected-to.
     *
     * <p>
     * This method is a no-op, if the connection already exists.
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
    public boolean isEmpty ();

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
     * @param value is the message to send.
     * @return this.
     */
    public Output<E> send (E value);

}
