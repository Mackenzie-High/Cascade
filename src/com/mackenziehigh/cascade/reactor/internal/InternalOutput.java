package com.mackenziehigh.cascade.reactor.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.mackenziehigh.cascade.reactor.Input;
import com.mackenziehigh.cascade.reactor.Output;
import com.mackenziehigh.cascade.reactor.Reactor;
import com.mackenziehigh.cascade.reactor.builder.OutputBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;

/**
 *
 * @author mackenzie
 */
public final class InternalOutput<T>
        implements OutputBuilder<T>,
                   Output<T>
{
    private final InternalReactor reactor;

    private final UUID uuid = UUID.randomUUID();

    private final Object lock = new Object();

    private volatile boolean built = false;

    private volatile String name = uuid.toString();

    private final Set<Input<T>> connections = new CopyOnWriteArraySet<>();

    public InternalOutput (final InternalReactor reactor,
                           final Class<T> type)
    {
        this.reactor = Objects.requireNonNull(reactor);
    }

    @Override
    public OutputBuilder<T> named (final String name)
    {
        this.name = Objects.requireNonNull(name, "name");
        return this;
    }

    @Override
    public OutputBuilder<T> verify (Predicate<T> condition)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Output<T> build ()
    {
        built = true;
        return this;
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
    public Output<T> connect (final Input<T> input)
    {
        Preconditions.checkNotNull(input, "input");
        if (connections.contains(input) == false)
        {
            connections.add(input);
            input.connect(this);
        }
        return this;
    }

    @Override
    public Output<T> disconnect (final Input<T> input)
    {
        Preconditions.checkNotNull(input, "input");
        if (connections.contains(input))
        {
            connections.remove(input);
            input.disconnect(this);
        }
        return this;
    }

    @Override
    public Set<Input<T>> connections ()
    {
        return ImmutableSet.copyOf(connections);
    }

    @Override
    public boolean isFull ()
    {
        final boolean answer = connections.stream().anyMatch(x -> x.isFull());
        return answer;
    }

    @Override
    public Output<T> send (final T value)
    {
        connections.stream().forEach(x -> x.send(value));
        return this;
    }

}
