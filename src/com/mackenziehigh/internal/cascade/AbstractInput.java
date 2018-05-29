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
import com.google.common.base.Verify;
import com.mackenziehigh.cascade.Output;
import com.mackenziehigh.cascade.PrivateInput;
import com.mackenziehigh.cascade.Reactor;
import com.mackenziehigh.cascade.builder.InputBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Abstract Implementation of <code>InputBuilder</code> and <code>PrivateInput</code>.
 */
public abstract class AbstractInput<E, T extends AbstractInput<E, T>>
        implements InputBuilder<E>,
                   PrivateInput<E>
{
    protected abstract T self ();

    protected abstract boolean offer (E value);

    protected final Object lock = new Object();

    protected final MockableReactor reactor;

    protected final AtomicBoolean built = new AtomicBoolean();

    private final UUID uuid = UUID.randomUUID();

    private volatile String name = uuid.toString();

    private volatile UnaryOperator<E> verifications = UnaryOperator.identity();

    private volatile Optional<Output<E>> connection = Optional.empty();

    public AbstractInput (final MockableReactor reactor)
    {
        this.reactor = Objects.requireNonNull(reactor);
    }

    protected void requireEgg ()
    {
        if (built.get())
        {
            throw new IllegalStateException("Already Built!");
        }
    }

    protected void pingInputs ()
    {
        synchronized (lock)
        {
            if (connection.isPresent())
            {
                connection.get().reactor().ifPresent(x -> x.ping());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T named (final String name)
    {
        synchronized (lock)
        {
            requireEgg();
            this.name = Objects.requireNonNull(name, "name");
            return self();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T verify (Predicate<E> condition)
    {
        final UnaryOperator<E> checker = x ->
        {
            final boolean test = condition.test(x);
            Verify.verify(test);
            return x;
        };

        synchronized (lock)
        {
            requireEgg();
            final UnaryOperator<E> op = verifications;
            verifications = x -> checker.apply(op.apply(x));
            return self();
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
    public PrivateInput<E> connect (final Output<E> output)
    {
        synchronized (lock)
        {
            Preconditions.checkNotNull(output, "output");
            if (connection.map(x -> x.equals(output)).orElse(false))
            {
                return this;
            }
            else if (connection.isPresent())
            {
                throw new IllegalStateException("Alreayd Connected!");
            }
            else
            {
                connection = Optional.of(output);
                output.connect(this);
                pingInputs();
                reactor.ping();
            }
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrivateInput<E> disconnect ()
    {
        synchronized (lock)
        {
            final Output<E> output = connection.orElse(null);
            connection = Optional.empty();
            if (output != null)
            {
                output.disconnect();
                pingInputs();
                reactor.ping();
            }
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Output<E>> connection ()
    {
        return connection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrivateInput<E> send (final E value)
    {
        if (value == null)
        {
            throw new NullPointerException("Refusing to send() null!");
        }

        if (built.get())
        {
            final E transformed = verifications.apply(value);
            final boolean inserted = transformed != null && offer(transformed);

            if (inserted)
            {
                reactor.ping();
            }
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString ()
    {
        return name;
    }
}
