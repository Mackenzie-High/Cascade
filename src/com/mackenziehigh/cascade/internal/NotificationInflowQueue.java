package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * An inflow-queue that will invoke a given function-object
 * whenever an event-message is added to the queue.
 */
public final class NotificationInflowQueue
        implements InflowQueue
{
    private final InflowQueue delegate;

    private final Consumer<InflowQueue> listener;

    /**
     * Sole Constructor.
     *
     * @param delegate will be the delegate queue.
     * @param listener will be invoked after any addition of an element to this queue.
     */
    public NotificationInflowQueue (final InflowQueue delegate,
                                    final Consumer<InflowQueue> listener)
    {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.listener = Objects.requireNonNull(listener, "listener");
    }

    private static final AtomicInteger offers = new AtomicInteger();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean offer (final CascadeToken event,
                          final CascadeStack stack)
    {
        final boolean added = delegate.offer(event, stack);

        if (added)
        {
            listener.accept(this);
        }

        return added;
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
