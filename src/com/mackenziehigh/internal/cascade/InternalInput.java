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
import com.mackenziehigh.cascade.Input;
import com.mackenziehigh.cascade.Output;
import com.mackenziehigh.cascade.OverflowPolicy;
import com.mackenziehigh.cascade.Reactor;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Array-based <code>Input</code>.
 *
 * TODO: Default capacity should not be zero!!!
 */
final class InternalInput<E>
        implements Input<E>
{

    private final Class<E> type;

    private volatile Deque<E> queue;

    private volatile OverflowHandler<E> overflowHandler;

    private final Object lock = new Object();

    private final Reactor reactor;

    private final UUID uuid = UUID.randomUUID();

    private volatile String name = uuid.toString();

    private volatile UnaryOperator<E> verifications = UnaryOperator.identity();

    private volatile Optional<Output<E>> connection = Optional.empty();

    private InternalInput (final Reactor reactor,
                           final Class<E> type,
                           final Deque<E> queue,
                           final OverflowHandler<E> handler)
    {
        this.reactor = Objects.requireNonNull(reactor, "reactor");
        this.type = Objects.requireNonNull(type, "type");
        this.queue = queue;
        this.overflowHandler = handler;
    }

    public static <T> InternalInput<T> newArrayInput (final Reactor reactor,
                                                      final Class<T> type,
                                                      final int capacity,
                                                      final OverflowPolicy policy)
    {
        final Deque<T> queue = new ArrayDeque<>(capacity);
        final OverflowHandler<T> handler = new OverflowHandler<>(queue, capacity, policy);
        return new InternalInput<>(reactor, type, queue, handler);
    }

    public static <T> InternalInput<T> newLinkedInput (final Reactor reactor,
                                                       final Class<T> type,
                                                       final int capacity,
                                                       final OverflowPolicy policy)
    {
        final Deque<T> queue = new LinkedList<>();
        final OverflowHandler<T> handler = new OverflowHandler<>(queue, capacity, policy);
        return new InternalInput<>(reactor, type, queue, handler);
    }

    private void pingInputs ()
    {
        synchronized (lock)
        {
            if (connection.isPresent())
            {
                connection.get().reactor().ping();
            }
        }
    }

    private boolean offer (final E value)
    {
        synchronized (lock)
        {
            return overflowHandler.offer(value);
        }
    }

    @Override
    public Class<E> type ()
    {
        return type;
    }

    @Override
    public OverflowPolicy overflowPolicy ()
    {
        return overflowHandler.policy;
    }

    @Override
    public InternalInput<E> clear ()
    {
        synchronized (lock)
        {
            if (queue != null)
            {
                queue.clear();
            }
            return this;
        }
    }

    @Override
    public E peekOrDefault (final E defaultValue)
    {
        synchronized (lock)
        {
            if (queue != null)
            {
                final E head = queue.peek();
                return head == null ? defaultValue : head;
            }
            else
            {
                return defaultValue;
            }
        }
    }

    @Override
    public E pollOrDefault (final E defaultValue)
    {
        synchronized (lock)
        {
            if (queue != null)
            {
                final E head = queue.poll();
                pingInputs();
                return head == null ? defaultValue : head;
            }
            else
            {
                return defaultValue;
            }
        }
    }

    @Override
    public int capacity ()
    {
        return overflowHandler.capacity;
    }

    @Override
    public int size ()
    {
        synchronized (lock)
        {
            final int size = queue == null ? 0 : queue.size();
            return size;
        }
    }

    @Override
    public boolean isEmpty ()
    {
        synchronized (lock)
        {
            return size() == 0;
        }
    }

    @Override
    public boolean isFull ()
    {
        synchronized (lock)
        {
            return size() == capacity();
        }
    }

    @Override
    public InternalInput<E> forEach (final Consumer<E> functor)
    {
        synchronized (lock)
        {
            if (queue != null)
            {
                Preconditions.checkNotNull(functor, "functor");
                queue.forEach(functor);
            }
            return this;
        }
    }

    @Override
    public int remainingCapacity ()
    {
        synchronized (lock)
        {
            final int cap = capacity() - size();
            Verify.verify(cap >= 0);
            return cap;
        }
    }

    @Override
    public E peekOrNull ()
    {
        return peekOrDefault(null);
    }

    @Override
    public Optional<E> peek ()
    {
        return Optional.ofNullable(queue.peek());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Input<E> named (final String name)
    {
        synchronized (lock)
        {
            this.name = Objects.requireNonNull(name, "name");
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Input<E> verify (final Predicate<E> condition)
    {
        synchronized (lock)
        {
            final UnaryOperator<E> checker = x ->
            {
                final boolean test = condition.test(x);
                Verify.verify(test); // TODO: Correct exception type?
                return x;
            };

            final UnaryOperator<E> op = verifications;
            verifications = x -> checker.apply(op.apply(x));
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
    public Reactor reactor ()
    {
        return reactor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Input<E> connect (final Output<E> output)
    {
        Preconditions.checkNotNull(output, "output");

        synchronized (lock)
        {
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
    public Input<E> disconnect ()
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
    public Input<E> send (final E value)
    {
        if (value == null)
        {
            throw new NullPointerException("Refusing to send() null!");
        }

        final E transformed = verifications.apply(value);
        final boolean inserted = transformed != null && offer(transformed);

        if (inserted)
        {
            reactor.ping();
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
