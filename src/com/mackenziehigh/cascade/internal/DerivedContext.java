package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadeReactor.Context;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import com.mackenziehigh.cascade.CascadePump;
import com.mackenziehigh.cascade.CascadeReactor;
import com.mackenziehigh.cascade.CascadeProcessor;

/**
 *
 */
public final class DerivedContext
        implements Context
{
    private final Context context;

    public final AtomicReference<CascadeToken> event = new AtomicReference<>();

    public final AtomicReference<OperandStack> message = new AtomicReference<>();

    public final AtomicReference<Throwable> exception = new AtomicReference<>();

    public DerivedContext (final Context context)
    {
        this.context = Objects.requireNonNull(context, "context");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cascade cascade ()
    {
        return context.cascade();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeLogger logger ()
    {
        return context.logger();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeAllocator allocator ()
    {
        return context.allocator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeAllocator.AllocationPool pool ()
    {
        return context.pool();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadePump engine ()
    {
        return context.engine();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeReactor node ()
    {
        return context.node();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeToken name ()
    {
        return context.name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeProcessor input ()
    {
        return context.input();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeToken event ()
    {
        return event.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OperandStack message ()
    {
        return message.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Throwable exception ()
    {
        return exception.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean async (final CascadeToken event,
                          final OperandStack message)
    {
        return context.async(event, message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean sync (final CascadeToken event,
                         final OperandStack message,
                         final long timeout,
                         final TimeUnit timeoutUnits)
    {
        return context.sync(event, message, timeout, timeoutUnits);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send (final CascadeToken event,
                      final OperandStack message)
            throws CascadeReactor.SendFailureException
    {
        context.send(event, message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int broadcast (final CascadeToken event,
                          final OperandStack message)
    {
        return context.broadcast(event, message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<CascadeToken> subscriptions ()
    {
        return context.subscriptions();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context subscribe (final CascadeToken event)
    {
        return context.subscribe(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context unsubscribe (final CascadeToken event)
    {
        return context.unsubscribe(event);
    }

}
