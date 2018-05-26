package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.RateLimiter;
import com.mackenziehigh.cascade.Input;
import com.mackenziehigh.cascade.MutableInput;
import com.mackenziehigh.cascade.Output;
import com.mackenziehigh.cascade.Reactor;
import com.mackenziehigh.cascade.builder.InputBuilder;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 *
 * @param <E>
 */
public abstract class AbstractInput<E, T extends AbstractInput<E, T>>
        implements InputBuilder<E>,
                   MutableInput<E>
{
    protected abstract T self ();

    protected abstract boolean offer (E value);

    protected final Object lock = new Object();

    protected final MockableReactor reactor;

    protected final AtomicBoolean built = new AtomicBoolean();

    private final UUID uuid = UUID.randomUUID();

    private volatile String name = uuid.toString();

    private volatile UnaryOperator<E> operator = UnaryOperator.identity();

    private final Set<Output<E>> connections = new CopyOnWriteArraySet<>();

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
        for (Output<?> conn : connections)
        {
            conn.reactor().ifPresent(x -> x.ping());
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
    public T filter (final Predicate<E> filter)
    {
        synchronized (lock)
        {
            requireEgg();
            operator = x -> filter.test(x) ? x : null;
            return self();
        }
    }

    @Override
    public T transform (final UnaryOperator<E> transform)
    {
        Preconditions.checkNotNull(transform, "transform");

        final UnaryOperator<E> wrapper = x ->
        {
            /**
             * The send() method ensures that null was not the input.
             * Thus, a null here must be the output of a filter.
             * Propagate the null.
             */
            if (x == null)
            {
                return null;
            }

            /**
             * Apply the transform, but do not allow it to return null.
             */
            final E result = transform.apply(x);
            if (result == null)
            {
                final String msg = "A transform returned null in input: " + name();
                throw new NullPointerException(msg);
            }
            return result;
        };

        synchronized (lock)
        {
            requireEgg();
            operator = x -> wrapper.apply(operator.apply(x));
            return self();
        }
    }

    @Override
    public T verify (Predicate<E> condition)
    {
        final Predicate<E> checker = x ->
        {
            final boolean test = condition.test(x);
            Verify.verify(test);
            return true;
        };

        return filter(checker);
    }

    @Override
    public T rateLimit (final int permits,
                        final ChronoUnit unit)
    {
        // TODO

        final Object limiterLock = new Object();

        final RateLimiter limiter = RateLimiter.create(permits);

        final Predicate<E> filter = x ->
        {
            synchronized (limiterLock)
            {
                return limiter.tryAcquire();
            }
        };

        return filter(filter);
    }

    @Override
    public T limit (final long count)
    {
        Preconditions.checkArgument(count >= 0, "limit < 0");

        final AtomicLong counter = new AtomicLong(1);

        final Predicate<E> condition = x -> counter.incrementAndGet() <= count;

        filter(condition);
        return self();
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
    public Input<E> connect (Output<E> output)
    {
        synchronized (lock)
        {
            Preconditions.checkNotNull(output, "output");
            if (connections.contains(output) == false)
            {
                connections.add(output);
                output.connect(this);
            }
            return this;
        }
    }

    @Override
    public Input<E> disconnect (Output<E> output)
    {
        synchronized (lock)
        {
            Preconditions.checkNotNull(output, "output");
            if (connections.contains(output))
            {
                connections.remove(output);
                output.disconnect(this);
            }
            return this;
        }
    }

    @Override
    public Set<Output<E>> connections ()
    {
        return ImmutableSet.copyOf(connections);
    }

    @Override
    public Input<E> send (final E value)
    {
        if (value == null)
        {
            throw new NullPointerException("Refusing to send() null!");
        }

        if (built.get())
        {
            final E transformed = operator.apply(value);
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
