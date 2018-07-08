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
import com.mackenziehigh.cascade.OverflowPolicy;
import com.mackenziehigh.cascade.Reactor;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Array-based <code>Input</code>.
 *
 * TODO: Default capacity should not be zero!!!
 */
final class InternalInput<E>
        extends AbstractInput<E, InternalInput<E>>
{

    private final Class<E> type;

    private volatile Deque<E> queue;

    private volatile OverflowHandler<E> overflowHandler;

    private InternalInput (final Reactor reactor,
                           final Class<E> type,
                           final Deque<E> queue,
                           final OverflowHandler<E> handler)
    {
        super(reactor);
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
    protected InternalInput<E> self ()
    {
        return this;
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
    protected boolean offer (final E value)
    {
        synchronized (lock)
        {
            return overflowHandler.offer(value);
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

}
