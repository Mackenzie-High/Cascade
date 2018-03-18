package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 * An inflow-queue that synchronizes all operations performed thereon.
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
     * Perform the given task synchronously with respect
     * to other operations performed on this queue.
     *
     * @param task will be performed.
     */
    public synchronized void sync (final Runnable task)
    {
        Preconditions.checkNotNull(task, "task");
        task.run();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean offer (final CascadeToken event,
                                       final CascadeStack stack)
    {
        Preconditions.checkNotNull(event, "event");
        Preconditions.checkNotNull(stack, "stack");
        return delegate.offer(event, stack);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean removeOldest (final AtomicReference<CascadeToken> eventOut,
                                              final AtomicReference<CascadeStack> stackOut)
    {
        return delegate.removeOldest(eventOut, stackOut);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean removeNewest (final AtomicReference<CascadeToken> eventOut,
                                              final AtomicReference<CascadeStack> stackOut)
    {
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
    public synchronized void forEach (final BiConsumer<CascadeToken, CascadeStack> functor)
    {
        delegate.forEach(functor);
    }
}
