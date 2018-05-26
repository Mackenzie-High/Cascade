package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.mackenziehigh.cascade.MutableInput;
import com.mackenziehigh.cascade.builder.ArrayInputBuilder;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.Consumer;

/**
 *
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
    public MutableInput<E> build ()
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
    public MutableInput<E> clear ()
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
            return size() == capacity();
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

}
