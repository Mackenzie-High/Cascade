package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.CascadeContext;
import com.mackenziehigh.cascade.CascadeScript;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This is the actual implementation of the CascadeScript interface.
 */
public final class InternalScript
        implements CascadeScript
{
    public final CascadeScript delegate;

    public final AtomicLong unhandledExceptionCount = new AtomicLong();

    public final AtomicLong consumedMessageCount = new AtomicLong();

    public InternalScript (final CascadeScript inner)
    {
        this.delegate = Objects.requireNonNull(inner);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void onSetup (final CascadeContext ctx)
            throws Throwable
    {
        try
        {
            delegate.onSetup(ctx);
        }
        catch (Throwable ex)
        {
            onUnhandledException(ctx, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void onMessage (final CascadeContext ctx,
                                        final CascadeToken event,
                                        final CascadeStack stack)
            throws Throwable
    {
        try
        {
            consumedMessageCount.incrementAndGet();
            delegate.onMessage(ctx, event, stack);
        }
        catch (Throwable ex)
        {
            onUnhandledException(ctx, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void onUndeliveredMessage (final CascadeContext ctx,
                                                   final CascadeToken event,
                                                   final CascadeStack stack)
            throws Throwable
    {
        try
        {
            delegate.onUndeliveredMessage(ctx, event, stack);
        }
        catch (Throwable ex)
        {
            onUnhandledException(ctx, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void onUnhandledException (final CascadeContext ctx,
                                                   final Throwable cause)
            throws Throwable
    {
        try
        {
            unhandledExceptionCount.incrementAndGet();
            delegate.onUnhandledException(ctx, cause);
        }
        catch (Throwable ex)
        {
            unhandledExceptionCount.incrementAndGet();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void onClose (final CascadeContext ctx)
            throws Throwable
    {
        try
        {
            delegate.onClose(ctx);
        }
        catch (Throwable ex)
        {
            onUnhandledException(ctx, ex);
        }
    }
}
