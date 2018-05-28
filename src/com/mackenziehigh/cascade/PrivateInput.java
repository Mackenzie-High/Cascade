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
public interface PrivateInput<E>
        extends Input<E>
{
    /**
     * {@inheritDoc}
     */
    @Override
    public UUID uuid ();

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<E> type ();

    /**
     * {@inheritDoc}
     */
    @Override
    public String name ();

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Reactor> reactor ();

    /**
     * {@inheritDoc}
     */
    @Override
    public PrivateInput<E> connect (Output<E> output);

    /**
     * {@inheritDoc}
     */
    @Override
    public PrivateInput<E> disconnect ();

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Output<E>> connection ();

    /**
     * {@inheritDoc}
     */
    @Override
    public int capacity ();

    /**
     * {@inheritDoc}
     */
    @Override
    public int size ();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty ();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFull ();

    /**
     * {@inheritDoc}
     */
    @Override
    public E peekOrNull ();

    /**
     * {@inheritDoc}
     */
    @Override
    public E peekOrDefault (E defaultValue);

    /**
     * {@inheritDoc}
     */
    @Override
    public default Optional<E> peek ()
    {
        final E head = peekOrDefault(null);
        return Optional.ofNullable(head);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrivateInput<E> forEach (Consumer<E> functor);

    /**
     * {@inheritDoc}
     */
    @Override
    public PrivateInput<E> send (E value);

    public PrivateInput<E> clear ();

    public default E pollOrNull ()
    {
        return pollOrDefault(null);
    }

    public E pollOrDefault (E defaultValue);

    public default Optional<E> poll ()
    {
        final E head = pollOrDefault(null);
        return Optional.ofNullable(head);
    }

}
