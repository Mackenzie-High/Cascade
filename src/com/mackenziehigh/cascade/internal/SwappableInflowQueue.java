package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 * An inflow-queue that forwards all method-calls to an
 * underlying delegate queue and also facilitates the
 * replacement of that underlying queue on-demand.
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
        delegate = Objects.requireNonNull(initial, "initial");
    }

    public InflowQueue getDelegate ()
    {
        return delegate;
    }

    /**
     * Replace the current delegate queue with a different queue.
     *
     * <p>
     * The new delegate will first be cleared.
     * Then, the contents of the current delegate will be
     * transferred into the new delegate, which will cause
     * the current delegate to be cleared in the process.
     * </p>
     *
     * @param queue will be the new delegate.
     */
    public void replaceDelegate (final InflowQueue queue)
    {
        Preconditions.checkNotNull(queue, "queue");

        final AtomicReference<CascadeToken> tokenOut = new AtomicReference<>();
        final AtomicReference<CascadeStack> stackOut = new AtomicReference<>();

        queue.clear();

        while (delegate.isEmpty() == false)
        {
            Verify.verify(removeOldest(tokenOut, stackOut));
            queue.offer(tokenOut.get(), stackOut.get());
        }

        delegate = queue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean offer (final CascadeToken event,
                          final CascadeStack stack)
    {
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
}
