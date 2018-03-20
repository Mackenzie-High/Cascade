package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 * An inflow-queue that will automatically remove elements
 * from therein in order to obey its capacity restrictions.
 */
public final class BoundedInflowQueue
        implements InflowQueue
{
    /**
     * What to do when overflow occurs.
     */
    public enum OverflowPolicy
    {
        /**
         * Drop the oldest message that is already in the queue.
         */
        DROP_OLDEST,

        /**
         * Drop the newest message that is already in the queue.
         */
        DROP_NEWEST,

        /**
         * Drop the message that is being inserted into the queue,
         * rather than removing an element that is already in the queue.
         */
        DROP_INCOMING,

        /**
         * Drop everything already in the queue and the incoming message.
         */
        DROP_ALL
    }

    private final OverflowPolicy policy;

    private final InflowQueue delegate;

    private final AtomicReference<CascadeToken> tokenSink = new AtomicReference<>();

    private final AtomicReference<CascadeStack> operandSink = new AtomicReference<>();

    private final AtomicLong overflowCount = new AtomicLong();

    /**
     * Sole Constructor.
     *
     * @param policy will dictate how overflow will be handled.
     * @param delegate will provide the actual queue data-structure.
     */
    public BoundedInflowQueue (final OverflowPolicy policy,
                               final InflowQueue delegate)
    {
        this.policy = Objects.requireNonNull(policy, "policy");
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean offer (final CascadeToken event,
                          final CascadeStack stack)
    {
        Preconditions.checkNotNull(event, "event");
        Preconditions.checkNotNull(stack, "stack");

        tokenSink.set(null);
        operandSink.set(null);

        final boolean overflow = size() >= capacity();

        if (overflow)
        {
            overflowCount.incrementAndGet();
        }

        if (overflow && policy == OverflowPolicy.DROP_OLDEST)
        {
            delegate.removeOldest(tokenSink, operandSink);
        }
        else if (overflow && policy == OverflowPolicy.DROP_NEWEST)
        {
            delegate.removeNewest(tokenSink, operandSink);
        }
        else if (overflow && policy == OverflowPolicy.DROP_INCOMING)
        {
            return false;
        }
        else if (overflow && policy == OverflowPolicy.DROP_ALL)
        {
            delegate.clear();
        }

        return delegate.offer(event, stack);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeOldest (final AtomicReference<CascadeToken> eventOut,
                                 final AtomicReference<CascadeStack> stackOut)
    {
        return delegate.removeOldest(eventOut, stackOut);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeNewest (final AtomicReference<CascadeToken> eventOut,
                                 final AtomicReference<CascadeStack> stackOut)
    {
        return delegate.removeNewest(eventOut, stackOut);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear ()
    {
        delegate.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size ()
    {
        return delegate.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int capacity ()
    {
        return delegate.capacity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void forEach (final BiConsumer<CascadeToken, CascadeStack> functor)
    {
        delegate.forEach(functor);
    }

    /**
     * Getter.
     *
     * @return the number of overflows that have occurred.
     */
    public long getOverflowCount ()
    {
        return overflowCount.get();
    }
}
