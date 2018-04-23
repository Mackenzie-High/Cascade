package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
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
         * Drop everything already in the queue, but accept the incoming message.
         */
        DROP_PENDING,

        /**
         * Drop everything already in the queue and the incoming message.
         */
        DROP_ALL
    }

    private final OverflowPolicy policy;

    private final InflowQueue delegate;

    private final AtomicReference<CascadeToken> tokenSink = new AtomicReference<>();

    private final AtomicReference<CascadeStack> operandSink = new AtomicReference<>();

    private final AtomicLong offeredCount = new AtomicLong();

    private final AtomicLong overflowCount = new AtomicLong();

    private final AtomicLong acceptedCount = new AtomicLong();

    private final AtomicLong droppedCount = new AtomicLong();

    private final AtomicLong removedCount = new AtomicLong();

    private final AtomicBoolean disabled = new AtomicBoolean();

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
     * (Thread Safe) Causes this queue to stop accepting new messages.
     */
    public void disable ()
    {
        disabled.set(true);
    }

    /**
     * Getter (Thread Safe).
     *
     * @return the current overflow-policy.
     */
    public OverflowPolicy policy ()
    {
        return policy;
    }

    /**
     * Getter (Thread Safe).
     *
     * @return the number of times someone offered a message.
     */
    public long offered ()
    {
        return offeredCount.get();
    }

    /**
     * Getter (Thread Safe).
     *
     * @return the number of times someone offered a message,
     * which was not immediately dropped.
     */
    public long accepted ()
    {
        return acceptedCount.get();
    }

    /**
     * Getter (Thread Safe).
     *
     * @return the number of messages that were dropped immediately upon insertion.
     */
    public long dropped ()
    {
        return droppedCount.get();
    }

    /**
     * Getter (Thread-Safe).
     *
     * @return the number of messages that have been inserted and then removed (not dropped).
     */
    public long removed ()
    {
        return removedCount.get();
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

        offeredCount.incrementAndGet();

        if (disabled.get())
        {
            droppedCount.incrementAndGet();
            return false;
        }

        tokenSink.set(null);
        operandSink.set(null);

        final boolean overflow = size() >= capacity();

        if (overflow)
        {
            overflowCount.incrementAndGet();
        }

        if (overflow && policy == OverflowPolicy.DROP_OLDEST)
        {
            droppedCount.incrementAndGet();
            delegate.removeOldest(tokenSink, operandSink);
        }
        else if (overflow && policy == OverflowPolicy.DROP_NEWEST)
        {
            droppedCount.incrementAndGet();
            delegate.removeNewest(tokenSink, operandSink);
        }
        else if (overflow && policy == OverflowPolicy.DROP_INCOMING)
        {
            droppedCount.incrementAndGet();
            return false; // TODO: Should this really be returned here???
        }
        else if (overflow && policy == OverflowPolicy.DROP_PENDING)
        {
            droppedCount.addAndGet(delegate.size());
            delegate.clear();
        }
        else if (overflow && policy == OverflowPolicy.DROP_ALL)
        {
            droppedCount.addAndGet(delegate.size() + 1);
            delegate.clear();
            return false;
        }

        final boolean delivered = delegate.offer(event, stack);

        if (delivered)
        {
            acceptedCount.incrementAndGet();
        }
        else
        {
            droppedCount.incrementAndGet();
        }

        // TODO
//        final long size = (acceptedCount.get() - droppedCount.get()) - removedCount.get();
//        Verify.verify(size() == size);
        return delivered;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeOldest (final AtomicReference<CascadeToken> eventOut,
                                 final AtomicReference<CascadeStack> stackOut)
    {
        if (delegate.removeOldest(eventOut, stackOut))
        {
            removedCount.incrementAndGet();
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeNewest (final AtomicReference<CascadeToken> eventOut,
                                 final AtomicReference<CascadeStack> stackOut)
    {
        if (delegate.removeNewest(eventOut, stackOut))
        {
            removedCount.incrementAndGet();
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear ()
    {
        removedCount.addAndGet(delegate.size());
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
