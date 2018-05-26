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

    private volatile Deque<E> queue;

    private volatile OverflowHandler<E> overflowHandler;

    private volatile OverflowPolicy policy = OverflowPolicy.UNSPECIFIED;

    private volatile int capacity;

    public ArrayInput (final InternalReactor reactor,
                       final Class<E> type)
    {
        super(reactor);
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
            queue.clear();
            return this;
        }
    }

    @Override
    protected void offer (final E value)
    {
        synchronized (lock)
        {
            overflowHandler.offer(value);
        }
    }

    @Override
    public E peekOrDefault (final E defaultValue)
    {
        synchronized (lock)
        {
            final E head = queue.peek();
            return head == null ? defaultValue : head;
        }
    }

    @Override
    public E pollOrDefault (final E defaultValue)
    {
        synchronized (lock)
        {
            final E head = queue.poll();
            pingInputs();
            return head == null ? defaultValue : head;
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
            return queue.size();
        }
    }

    @Override
    public boolean isEmpty ()
    {
        synchronized (lock)
        {
            return queue.isEmpty();
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
            queue.forEach(functor);
            return this;
        }
    }

}
