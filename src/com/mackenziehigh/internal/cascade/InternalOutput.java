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

import com.google.common.base.Preconditions;
import com.mackenziehigh.cascade.Input;
import com.mackenziehigh.cascade.PrivateOutput;
import com.mackenziehigh.cascade.Reactor;
import com.mackenziehigh.cascade.builder.OutputBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Combined Implementation of <code>OutputBuilder</code> and <code>PrivateOutput</code>.
 *
 * TODO: Already built; locks.
 */
public final class InternalOutput<T>
        implements OutputBuilder<T>,
                   PrivateOutput<T>
{
    private final MockableReactor reactor;

    private final UUID uuid = UUID.randomUUID();

    private final Class<T> type;

    private final Object lock = new Object();

    private volatile boolean built = false;

    private volatile String name = uuid.toString();

    private volatile Optional<Input<T>> connection = Optional.empty();

    public InternalOutput (final MockableReactor reactor,
                           final Class<T> type)
    {
        this.reactor = Objects.requireNonNull(reactor, "reactor");
        this.type = Objects.requireNonNull(type, "type");
    }

    private void requireEgg ()
    {
        Preconditions.checkState(!built, "Already Built!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> type ()
    {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputBuilder<T> named (final String name)
    {
        synchronized (lock)
        {
            requireEgg();
            this.name = Objects.requireNonNull(name, "name");
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputBuilder<T> verify (final Predicate<T> condition)
    {
        synchronized (lock)
        {
            requireEgg();
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InternalOutput<T> build ()
    {
        synchronized (lock)
        {
            requireEgg();
            Preconditions.checkState(!built, "Already Built!");
            built = true;
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID uuid ()
    {
        return uuid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name ()
    {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Reactor> reactor ()
    {
        return reactor.reactor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrivateOutput<T> connect (final Input<T> input)
    {
        synchronized (lock)
        {
            Preconditions.checkNotNull(input, "input");
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
                reactor.ping();
                input.reactor().ifPresent(x -> x.ping());
            }
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrivateOutput<T> disconnect ()
    {
        synchronized (lock)
        {
            if (connection.isPresent())
            {
                final Input<T> input = connection.get();
                connection = Optional.empty();
                input.disconnect();
                reactor.ping();
                input.reactor().ifPresent(x -> x.ping());
            }
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Input<T>> connection ()
    {
        return connection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFull ()
    {
        synchronized (lock)
        {
            return remainingCapacity() == 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrivateOutput<T> send (final T value)
    {
        /**
         * TODO: Should this *not* be synchronized???
         * What if we eventually add Lock-Free Inputs?
         * In that case, this is forcing synchronization which would defeat the lock-free gains.
         */
        synchronized (lock)
        {
            if (connection.isPresent())
            {
                connection.get().send(value);
            }
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty ()
    {
        synchronized (lock)
        {
            return connection.map(x -> x.isEmpty()).orElse(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int capacity ()
    {
        synchronized (lock)
        {
            return connection.map(x -> x.capacity()).orElse(0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size ()
    {
        synchronized (lock)
        {
            return connection.map(x -> x.size()).orElse(0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int remainingCapacity ()
    {
        synchronized (lock)
        {
            if (built == false)
            {
                return 0;
            }
            else
            {
                return connection.map(x -> x.remainingCapacity()).orElse(0);
            }
        }
    }
}
