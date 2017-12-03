package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.CascadeEdge;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadeNode;
import com.mackenziehigh.cascade.CascadeNode.Context;
import com.mackenziehigh.cascade.CascadePump;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
public final class DerivedContext
        implements Context
{
    private final Context context;

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
    public CascadePump pump ()
    {
        return context.pump();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeNode node ()
    {
        return context.node();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name ()
    {
        return context.name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CascadeEdge> inputs ()
    {
        return context.inputs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CascadeEdge> outputs ()
    {
        return context.outputs();
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
    public boolean async (final OperandStack message)
    {
        return context.async(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean sync (final OperandStack message,
                         final long timeout,
                         final TimeUnit timeoutUnits)
    {
        return context.sync(message, timeout, timeoutUnits);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send (final OperandStack message)
            throws CascadeNode.SendFailureException
    {
        context.send(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int broadcast (final OperandStack message)
    {
        return context.broadcast(message);
    }

}
