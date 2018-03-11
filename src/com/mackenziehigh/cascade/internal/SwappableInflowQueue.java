package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 * Facade
 */
public final class SwappableInflowQueue
        implements InflowQueue
{
    private volatile InflowQueue delegate;

    /**
     * Sole Constructor.
     *
     * @param initial will be the delegate queue initially.
     */
    public SwappableInflowQueue (final InflowQueue initial)
    {
        this.delegate = Objects.requireNonNull(initial, "initial");
    }

    public void setQueue (final InflowQueue queue)
    {
        delegate = Objects.requireNonNull(queue, "queue");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean push (final CascadeToken event,
                         final CascadeStack stack)
    {
        Preconditions.checkNotNull(event, "event");
        Preconditions.checkNotNull(stack, "stack");
        return delegate.push(event, stack);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeOldest (final AtomicReference<CascadeToken> eventOut,
                                 final AtomicReference<CascadeStack> stackOut)
    {
        Preconditions.checkNotNull(eventOut, "eventOut");
        Preconditions.checkNotNull(stackOut, "stackOut");
        return delegate.removeOldest(eventOut, stackOut);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeNewest (final AtomicReference<CascadeToken> eventOut,
                                 final AtomicReference<CascadeStack> stackOut)
    {
        Preconditions.checkNotNull(eventOut, "eventOut");
        Preconditions.checkNotNull(stackOut, "stackOut");
        return delegate.removeNewest(eventOut, stackOut);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
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
    public void apply (final BiConsumer<CascadeToken, CascadeStack> functor)
    {
        Preconditions.checkNotNull(functor, "functor");
        delegate.apply(functor);
    }
}
