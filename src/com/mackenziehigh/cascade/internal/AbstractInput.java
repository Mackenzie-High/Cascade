package com.mackenziehigh.cascade.internal;

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
 *
 * @param <E>
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
    public PrivateInput<E> connect (final Output<E> output)
    {
        synchronized (lock)
        {
            Preconditions.checkNotNull(output, "output");
            if (connection.isPresent())
            {
                throw new IllegalStateException("Alreayd Connected!");
            }
            else
            {
                connection = Optional.of(output);
                output.connect(this);
            }
            return this;
        }
    }

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
            }
            return this;
        }
    }

    @Override
    public Optional<Output<E>> connection ()
    {
        return connection;
    }

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

    @Override
    public String toString ()
    {
        return name;
    }
}
