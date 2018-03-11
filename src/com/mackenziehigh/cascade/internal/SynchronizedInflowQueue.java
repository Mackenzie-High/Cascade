package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 *
 * @author mackenzie
 */
public final class SynchronizedInflowQueue
        implements InflowQueue
{
    private final InflowQueue delegate;

    /**
     * Sole Constructor.
     *
     * @param delegate will be the delegate queue.
     */
    public SynchronizedInflowQueue (final InflowQueue delegate)
    {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean push (final CascadeToken event,
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
    public synchronized boolean removeOldest (final AtomicReference<CascadeToken> eventOut,
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
    public synchronized boolean removeNewest (final AtomicReference<CascadeToken> eventOut,
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
    public synchronized void clear ()
    {
        delegate.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized int size ()
    {
        return delegate.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized int capacity ()
    {
        return delegate.capacity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void apply (final BiConsumer<CascadeToken, CascadeStack> functor)
    {
        Preconditions.checkNotNull(functor, "functor");
        delegate.apply(functor);
    }
}
