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

/**
 * An <code>Output</code> with additional methods that should
 * only be used from within a reactor object itself.
 *
 * @param <E>
 */
public interface PrivateOutput<E>
        extends Output<E>
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
    public String name ();

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<E> type ();

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Reactor> reactor ();

    /**
     * {@inheritDoc}
     */
    @Override
    public PrivateOutput<E> connect (Input<E> input);

    /**
     * {@inheritDoc}
     */
    @Override
    public PrivateOutput<E> disconnect ();

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Input<E>> connection ();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFull ();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty ();

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
    public PrivateOutput<E> send (E value);
}
