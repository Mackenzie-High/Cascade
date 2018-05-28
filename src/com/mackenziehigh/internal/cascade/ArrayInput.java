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
import com.mackenziehigh.cascade.builder.ArrayInputBuilder;
import com.mackenziehigh.cascade.builder.OverflowPolicy;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * TODO: Default capacity should not be zero!!!
 */
public final class ArrayInput<E>
        extends AbstractInput<E, ArrayInput<E>>
        implements ArrayInputBuilder<E>
{

    private final Class<E> type;

    private volatile Deque<E> queue;

    private volatile OverflowHandler<E> overflowHandler;

    private volatile OverflowPolicy policy = OverflowPolicy.UNSPECIFIED;

    private volatile int capacity = 0;

    public ArrayInput (final MockableReactor reactor,
                       final Class<E> type)
    {
        super(reactor);
        this.type = Objects.requireNonNull(type, "type");
    }

    @Override
    public Class<E> type ()
    {
        return type;
    }

    @Override
    protected ArrayInput<E> self ()
    {
        return this;
    }

    @Override
    public ArrayInput<E> withCapacity (final int capacity)
    {
        synchronized (lock)
        {
            requireEgg();
            Preconditions.checkArgument(capacity >= 0, "capacity < 0");
            this.capacity = capacity;
            return this;
        }
    }

    @Override
    public ArrayInput<E> withOverflowPolicy (final OverflowPolicy policy)
    {
        synchronized (lock)
        {
            requireEgg();
            this.policy = Objects.requireNonNull(policy, "policy");
            return this;
        }
    }

    @Override
    public ArrayInput<E> build ()
    {
        synchronized (lock)
        {
            requireEgg();
            queue = new ArrayDeque<>(capacity);
            overflowHandler = new OverflowHandler<>(queue, capacity, policy);
            built.set(true);
            return this;
        }
    }

    @Override
    public ArrayInput<E> clear ()
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
        return capacity;
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
            return built.get() ? size() == capacity() : false;
        }
    }

    @Override
    public ArrayInput<E> forEach (final Consumer<E> functor)
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

}
