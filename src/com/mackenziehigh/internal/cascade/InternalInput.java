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
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Array-based <code>Input</code>.
 */
final class InternalInput<E>
        implements Input<E>
{
    private final Reactor reactor;

    private final UUID uuid = UUID.randomUUID();

    private volatile String name = uuid.toString();

    private final Class<E> type;

    private volatile InflowDeque<E> inflowQueue;

    private volatile OverflowHandler<E> overflowHandler;

    private volatile UnaryOperator<E> verifications = UnaryOperator.identity();

    private volatile Optional<Output<E>> connection = Optional.empty();

    public InternalInput (final Reactor reactor,
                          final Class<E> type)
    {
        this.reactor = Objects.requireNonNull(reactor, "reactor");
        this.type = Objects.requireNonNull(type, "type");
        this.inflowQueue = new LinkedInflowDeque<>(Integer.MAX_VALUE, OverflowPolicy.DROP_INCOMING);
        this.overflowHandler = new OverflowHandler<>(inflowQueue, inflowQueue.capacity(), inflowQueue.overflowPolicy());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Input<E> useInflowDeque (final InflowDeque<E> queue)
    {
        Objects.requireNonNull(queue, "queue");

        /**
         * The new queue should contain the old messages, if possible.
         */
        final OverflowHandler<E> handler = new OverflowHandler<>(queue, queue.capacity(), queue.overflowPolicy());
        queue.forEach(x -> handler.offer(x));

        /**
         * The new queue is now officially the new queue.
         */
        inflowQueue = queue;
        overflowHandler = handler;

        /**
         * Notify the interested reactors that the queue changed.
         */
        signalInput();
        reactor.signal();

        return this;
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
    public synchronized OverflowPolicy overflowPolicy ()
    {
        return overflowHandler.policy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized InternalInput<E> clear ()
    {
        inflowQueue.clear();
        signalInput();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized E peekOrDefault (final E defaultValue)
    {
        final E head = inflowQueue.peek();
        return head == null ? defaultValue : head;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized E pollOrDefault (final E defaultValue)
    {
        final E head = inflowQueue.poll();
        signalInput();
        return head == null ? defaultValue : head;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized int capacity ()
    {
        return overflowHandler.capacity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized int size ()
    {
        final int size = inflowQueue.size();
        return size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean isFull ()
    {
        return size() >= capacity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized InternalInput<E> forEach (final Consumer<E> functor)
    {
        Objects.requireNonNull(functor, "functor");
        inflowQueue.forEach(functor);
        return this;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized int remainingCapacity ()
    {
        final int cap = capacity() - size();
        Utils.verify(cap >= 0);
        return cap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Input<E> named (final String name)
    {
        this.name = Objects.requireNonNull(name, "name");
        reactor.signal();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Input<E> verify (final Predicate<E> condition)
    {
        final UnaryOperator<E> checker = x ->
        {
            final boolean test = condition.test(x);
            if (test == false)
            {
                throw new IllegalArgumentException();
            }
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
    public synchronized Input<E> connect (final Output<E> output)
    {
        Objects.requireNonNull(output, "output");

        if (connection.map(x -> x.equals(output)).orElse(false))
        {
            return this;
        }
        else if (connection.isPresent())
        {
            throw new IllegalStateException("Already Connected!");
        }
        else
        {
            connection = Optional.of(output);
            output.connect(this);
            signalInput();
            reactor.signal();
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Input<E> disconnect ()
    {
        final Output<E> output = connection.orElse(null);
        connection = Optional.empty();
        if (output != null)
        {
            output.disconnect();
            signalInput();
            reactor.signal();
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Optional<Output<E>> connection ()
    {
        return connection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Input<E> send (final E value)
    {
        if (value == null)
        {
            throw new NullPointerException("Refusing to send() null!");
        }

        final E transformed = verifications.apply(value);
        final boolean inserted = transformed != null && offer(transformed);

        if (inserted)
        {
            reactor.signal();
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized String toString ()
    {
        return name;
    }

    private void signalInput ()
    {
        if (connection.isPresent())
        {
            connection.get().reactor().signal();
        }
    }

    private boolean offer (final E value)
    {
        return overflowHandler.offer(value);
    }

    public InflowDeque<?> inflowQueue ()
    {
        return inflowQueue;
    }
}
