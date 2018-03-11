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
public final class BoundedInflowQueue
        implements InflowQueue
{
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
    public boolean push (final CascadeToken event,
                         final CascadeStack stack)
    {
        Preconditions.checkNotNull(event, "event");
        Preconditions.checkNotNull(stack, "stack");

        final boolean overflow = size() == capacity();

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

        tokenSink.set(null);
        operandSink.set(null);

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
    public void apply (final BiConsumer<CascadeToken, CascadeStack> functor)
    {
        delegate.apply(functor);
    }
}
