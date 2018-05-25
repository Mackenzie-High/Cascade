package com.mackenziehigh.cascade.reactor.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.reactor.Input;
import com.mackenziehigh.cascade.reactor.MutableInput;
import com.mackenziehigh.cascade.reactor.Output;
import com.mackenziehigh.cascade.reactor.Reactor;
import com.mackenziehigh.cascade.reactor.builder.InputBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 *
 */
public final class InternalInput<T>
        implements InputBuilder<T>,
                   MutableInput<T>
{

    private final InternalReactor reactor;

    private final UUID uuid = UUID.randomUUID();

    private final Object lock = new Object();

    private volatile boolean built = false;

    private volatile String name = uuid.toString();

    private volatile Queue<T> queue;

    private volatile boolean concurrent = false;

    private volatile int capacity;

    private final Set<Output<T>> connections = Sets.newCopyOnWriteArraySet();

    public InternalInput (final InternalReactor reactor,
                          final Class<T> type)
    {
        this.reactor = Objects.requireNonNull(reactor);
    }

    @Override
    public InputBuilder<T> named (final String name)
    {
        this.name = Objects.requireNonNull(name, "name");
        return this;
    }

    @Override
    public InputBuilder<T> verify (final Predicate<T> condition)
    {
        return this;
    }

    @Override
    public InputBuilder<T> filter (final Predicate<T> filter)
    {
        return this;
    }

    @Override
    public InputBuilder<T> transform (final UnaryOperator<T> transform)
    {
        return this;
    }

    @Override
    public InputBuilder<T> withOverflowPolicyNever ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InputBuilder<T> withOverflowPolicyDropOldest ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InputBuilder<T> withOverflowPolicyDropNewest ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InputBuilder<T> withOverflowPolicyDropPending ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InputBuilder<T> withOverflowPolicyDropIncoming ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InputBuilder<T> withOverflowPolicyAll ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InputBuilder<T> withArrayQueue (int capacity)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InputBuilder<T> withLinkedQueue ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InputBuilder<T> withLinkedQueue (int capacity)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InputBuilder<T> withConcurrentQueue ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MutableInput<T> build ()
    {
        queue = new LinkedBlockingQueue<>();
        built = true;
        return this;
    }

    @Override
    public MutableInput<T> clear ()
    {
        queue.clear();
        return this;
    }

    @Override
    public T pollOrDefault (final T defaultValue)
    {
        final T head = queue.poll();
        pingInputs();
        return head == null ? defaultValue : head;
    }

    @Override
    public Optional<T> poll ()
    {
        return Optional.ofNullable(pollOrDefault(null));
    }

    @Override
    public UUID uuid ()
    {
        return uuid;
    }

    @Override
    public String name ()
    {
        return name;
    }

    @Override
    public Optional<Reactor> reactor ()
    {
        return reactor.reactor();
    }

    @Override
    public Input<T> connect (final Output<T> output)
    {
        Preconditions.checkNotNull(output, "output");
        if (connections.contains(output) == false)
        {
            connections.add(output);
            output.connect(this);
        }
        return this;
    }

    @Override
    public Input<T> disconnect (final Output<T> output)
    {
        Preconditions.checkNotNull(output, "output");
        if (connections.contains(output))
        {
            connections.remove(output);
            output.disconnect(this);
        }
        return this;
    }

    @Override
    public Set<Output<T>> connections ()
    {
        return ImmutableSet.copyOf(connections);
    }

    @Override
    public int capacity ()
    {
        return capacity;
    }

    @Override
    public int size ()
    {
        return queue.size();
    }

    @Override
    public boolean isEmpty ()
    {
        return queue.isEmpty();
    }

    @Override
    public boolean isFull ()
    {
        return size() == capacity();
    }

    @Override
    public T peekOrDefault (final T defaultValue)
    {
        final T head = queue.peek();
        return head == null ? defaultValue : head;
    }

    @Override
    public Optional<T> peek ()
    {
        return Optional.ofNullable(peekOrDefault(null));
    }

    @Override
    public Input<T> forEach (Consumer<T> functor)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Input<T> send (final T value)
    {
        if (concurrent)
        {
            queue.offer(value);
            reactor.ping();
        }
        else
        {
            syncSend(value);
        }

        return this;
    }

    private void syncSend (final T value)
    {
        synchronized (lock)
        {
            queue.offer(value);
            reactor.ping();
            // TODO Overflow Policy
        }
    }

    private void pingInputs ()
    {
        connections.forEach(x -> x.reactor().ifPresent(y -> y.ping()));
    }
}
