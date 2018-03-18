package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 * An inflow-queue that is based on an array data-structure.
 */
public final class ArrayInflowQueue
        implements InflowQueue
{
    private final Deque<CascadeToken> tokens;

    private final Deque<CascadeStack> stacks;

    private final int capacity;

    /**
     * Sole Constructor.
     *
     * @param capacity will be the maximum capacity of the new queue.
     */
    public ArrayInflowQueue (final int capacity)
    {
        Preconditions.checkArgument(capacity >= 1, "capacity < 0");
        this.capacity = capacity;
        this.tokens = new ArrayDeque<>(capacity);
        this.stacks = new ArrayDeque<>(capacity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean offer (final CascadeToken event,
                          final CascadeStack stack)
    {
        checkState();
        Preconditions.checkState(size() < capacity, "size >= capacity");
        Preconditions.checkNotNull(event, "event");
        Preconditions.checkNotNull(stack, "stack");
        tokens.addLast(event);
        stacks.addLast(stack);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeOldest (final AtomicReference<CascadeToken> eventOut,
                                 final AtomicReference<CascadeStack> stackOut)
    {
        checkState();
        Preconditions.checkNotNull(eventOut, "eventOut");
        Preconditions.checkNotNull(stackOut, "stackOut");
        if (size() == 0)
        {
            eventOut.set(null);
            stackOut.set(null);
            return false;
        }
        else
        {
            eventOut.set(tokens.removeFirst());
            stackOut.set(stacks.removeFirst());
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeNewest (final AtomicReference<CascadeToken> eventOut,
                                 final AtomicReference<CascadeStack> stackOut)
    {
        checkState();
        Preconditions.checkNotNull(eventOut, "eventOut");
        Preconditions.checkNotNull(stackOut, "stackOut");
        if (size() == 0)
        {
            eventOut.set(null);
            stackOut.set(null);
            return false;
        }
        else
        {
            eventOut.set(tokens.removeLast());
            stackOut.set(stacks.removeLast());
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear ()
    {
        checkState();
        tokens.clear();
        stacks.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size ()
    {
        checkState();
        return tokens.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int capacity ()
    {
        checkState();
        return capacity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void forEach (final BiConsumer<CascadeToken, CascadeStack> functor)
    {
        checkState();
        Preconditions.checkNotNull(functor, "functor");

        final Iterator<CascadeToken> tokenIter = tokens.iterator();
        final Iterator<CascadeStack> stackIter = stacks.iterator();

        while (tokenIter.hasNext() && stackIter.hasNext())
        {
            functor.accept(tokenIter.next(), stackIter.next());
        }
    }

    private void checkState ()
    {
        Verify.verify(tokens.size() < capacity);
        Verify.verify(tokens.size() == stacks.size());
    }
}
