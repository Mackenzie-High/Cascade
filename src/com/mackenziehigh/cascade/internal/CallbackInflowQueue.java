package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 *
 */
public final class CallbackInflowQueue
        implements InflowQueue
{
    public enum ChangeType
    {

    }

    private final InflowQueue delegate;

    private final BiConsumer<InflowQueue, ChangeType> beforeChange;

    private final BiConsumer<InflowQueue, ChangeType> afterChange;

    /**
     * Sole Constructor.
     *
     * @param delegate will be the delegate queue.
     * @param beforeChange will be invoked before any structural change.
     * @param afterChange will be invoked after any structural change.
     */
    public CallbackInflowQueue (final InflowQueue delegate,
                                final BiConsumer<InflowQueue, ChangeType> beforeChange,
                                final BiConsumer<InflowQueue, ChangeType> afterChange)
    {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.beforeChange = Objects.requireNonNull(beforeChange, "beforeChange");
        this.afterChange = Objects.requireNonNull(afterChange, "afterChange");
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
