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
 *
 */
public interface Input<E>
{
    /**
     * Retrieve a UUID that uniquely identifies this input in space-time.
     *
     * @return the unique identifier of this input.
     */
    public UUID uuid ();

    public Class<E> type ();

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

    public Input<E> disconnect ();

    public Optional<Output<E>> connection ();

    public int capacity ();

    public int size ();

    public int remainingCapacity ();

    public boolean isEmpty ();

    public boolean isFull ();

    public E peekOrNull ();

    public E peekOrDefault (E defaultValue);

    public Optional<E> peek ();

    public Input<E> forEach (Consumer<E> functor);

    public Input<E> send (E value);

}
