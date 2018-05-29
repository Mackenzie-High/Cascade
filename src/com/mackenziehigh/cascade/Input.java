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
import java.util.function.Consumer;

/**
 * An <code>Input</code> queues messages that are destined for the enclosing <code>Reactor</code>.
 *
 * <p>
 * An <code>Input</code> can be connected to one <code>Output</code> at a time.
 * The <code>Output</code> will send messages to the <code>Input</code>.
 * </p>
 */
public interface Input<E>
{
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
     * Retrieve the name of this input.
     *
     * @return the name of this input.
     */
    public String name ();

    /**
     * Retrieve the reactor that this input is a part of.
     *
     * @return the enclosing reactor, or empty,
     * if the reactor is not fully constructed yet.
     */
    public Optional<Reactor> reactor ();

    /**
     * Connect this input to the given output.
     *
     * <p>
     * This method is a no-op, if the connection exists.
     * </p>
     *
     * @param output will subsequently be connected hereto.
     * @return this.
     */
    public Input<E> connect (Output<E> output);

    /**
     * Disconnect this input from the output that it is connected-to.
     *
     * <p>
     * This method is a no-op, if the connection already exists.
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
     * Determine whether any messages are currently enqueued herein.
     *
     * @return <code>size() > 0</code>
     */
    public boolean isEmpty ();

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
    public E peekOrNull ();

    /**
     * Retrieve, but do not remove, the message that has
     * been enqueued in this input for the longest time.
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
    public Optional<E> peek ();

    /**
     * Apply the given function to each message enqueued in this input.
     *
     * @param functor will be executed given each message herein.
     * @return this.
     */
    public Input<E> forEach (Consumer<E> functor);

    /**
     * Send the given message to this input.
     *
     * @param value is the message to send.
     * @return this.
     */
    public Input<E> send (E value);

}
