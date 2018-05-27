package com.mackenziehigh.cascade.internal;

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
 * TODO: Already built; locks.
 *
 * @author mackenzie
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
        this.reactor = Objects.requireNonNull(reactor);
        this.type = Objects.requireNonNull(type);
    }

    @Override
    public Class<T> type ()
    {
        return type;
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
    public InternalOutput<T> build ()
    {
        Preconditions.checkState(!built, "Already Built!");
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
    public PrivateOutput<T> connect (final Input<T> input)
    {
        Preconditions.checkNotNull(input, "input");
        if (connection.isPresent())
        {
            throw new IllegalStateException("Already Connected!");
        }
        else
        {
            connection = Optional.of(input);
        }
        return this;
    }

    @Override
    public PrivateOutput<T> disconnect ()
    {
        if (connection.isPresent())
        {
            final Input<T> input = connection.get();
            connection = Optional.empty();
            input.disconnect();
        }
        return this;
    }

    @Override
    public Optional<Input<T>> connection ()
    {
        return connection;
    }

    @Override
    public boolean isFull ()
    {
        if (built)
        {
            final boolean answer = connection.map(x -> x.isFull()).orElse(false);
            return answer;
        }
        else
        {
            return false;
        }
    }

    @Override
    public PrivateOutput<T> send (final T value)
    {
        if (connection.isPresent())
        {
            connection.get().send(value);
        }
        return this;
    }

}
