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
package com.mackenziehigh.internal.cascade;

import com.mackenziehigh.cascade.Reactor;
import com.mackenziehigh.cascade.Reactor.Input;
import com.mackenziehigh.cascade.Reactor.Output;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Implementation of <code>Output</code>.
 */
final class InternalOutput<E>
        implements Output<E>
{
    private final Reactor reactor;

    private final UUID uuid = UUID.randomUUID();

    private final Class<E> type;

    private volatile String name = uuid.toString();

    private volatile Optional<Input<E>> connection = Optional.empty();

    private volatile UnaryOperator<E> verifications = UnaryOperator.identity();

    public InternalOutput (final Reactor reactor,
                           final Class<E> type)
    {
        this.reactor = Objects.requireNonNull(reactor, "reactor");
        this.type = Objects.requireNonNull(type, "type");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Class<E> type ()
    {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Output<E> named (final String name)
    {
        this.name = Objects.requireNonNull(name, "name");
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Output<E> verify (final Predicate<E> condition)
    {
        final UnaryOperator<E> checker = x ->
        {
            final boolean test = condition.test(x);
            Utils.verify(test); // TODO: Correct exception type?
            return x;
        };

        final UnaryOperator<E> op = verifications;
        verifications = x -> checker.apply(op.apply(x));
        reactor.signal();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized UUID uuid ()
    {
        return uuid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized String name ()
    {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Reactor reactor ()
    {
        return reactor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Output<E> connect (final Input<E> input)
    {
        Objects.requireNonNull(input, "input");

        if (connection.map(x -> x.equals(input)).orElse(false))
        {
            return this;
        }
        else if (connection.isPresent())
        {
            throw new IllegalStateException("Already Connected!");
        }
        else
        {
            connection = Optional.of(input);
            input.connect(this);
            input.reactor().signal();
            reactor.signal();
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Output<E> disconnect ()
    {
        if (connection.isPresent())
        {
            final Input<E> input = connection.get();
            connection = Optional.empty();
            input.disconnect();
            input.reactor().signal();
            reactor.signal();
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Optional<Input<E>> connection ()
    {
        return connection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean isFull ()
    {
        return connection.isPresent() ? remainingCapacity() == 0 : false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Output<E> send (final E value)
    {
        final Input<E> connectedInput = connection.orElse(null);

        if (connectedInput != null)
        {
            connectedInput.send(value);
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean isEmpty ()
    {
        return connection.map(x -> x.isEmpty()).orElse(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized int capacity ()
    {
        return connection.map(x -> x.capacity()).orElse(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized int size ()
    {
        return connection.map(x -> x.size()).orElse(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized int remainingCapacity ()
    {
        final int result = connection.isPresent() ? capacity() - connection.get().size() : 0;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized String toString ()
    {
        return name;
    }
}
