package com.mackenziehigh.cascade.reactor.builder;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.RateLimiter;
import com.mackenziehigh.cascade.reactor.MutableInput;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 *
 */
public interface InputBuilder<T>
{
    public InputBuilder<T> named (String name);

    public InputBuilder<T> verify (Predicate<T> condition);

    public InputBuilder<T> filter (Predicate<T> filter);

    public InputBuilder<T> transform (UnaryOperator<T> transform);

    public default InputBuilder<T> rateLimit (final int permits,
                                              final ChronoUnit unit)
    {
        // TODO

        final Object lock = new Object();

        final RateLimiter limiter = RateLimiter.create(permits);

        final Predicate<T> filter = x ->
        {
            synchronized (lock)
            {
                return limiter.tryAcquire();
            }
        };

        filter(filter);

        return this;
    }

    public default InputBuilder<T> limit (final long count)
    {
        Preconditions.checkArgument(count >= 0, "limit < 0");

        final AtomicLong counter = new AtomicLong(1);

        final Predicate<T> condition = x -> counter.incrementAndGet() <= count;

        return filter(condition);
    }

    public InputBuilder<T> withOverflowPolicyNever ();

    public InputBuilder<T> withOverflowPolicyDropOldest ();

    public InputBuilder<T> withOverflowPolicyDropNewest ();

    public InputBuilder<T> withOverflowPolicyDropPending ();

    public InputBuilder<T> withOverflowPolicyDropIncoming ();

    public InputBuilder<T> withOverflowPolicyAll ();

    public InputBuilder<T> withArrayQueue (int capacity);

    public InputBuilder<T> withLinkedQueue ();

    public InputBuilder<T> withLinkedQueue (int capacity);

    public InputBuilder<T> withConcurrentQueue ();

    public MutableInput<T> build ();
}
