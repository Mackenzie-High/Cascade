package com.mackenziehigh.cascade;

import com.google.common.base.Preconditions;
import java.io.Closeable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Must have a nullary constructor.
 */
public abstract class AbstractActor
        implements Actor
{
    private final AtomicBoolean bindingComplete = new AtomicBoolean();

    private final Set<Pipeline> inputs = new CopyOnWriteArraySet<>();

    private final Set<Pipeline> outputs = new CopyOnWriteArraySet<>();

    private final Set<Closeable> closeables = Collections.synchronizedSet(new HashSet<>());

    private final Set<Pipeline> unmodInputs = Collections.unmodifiableSet(inputs);

    private final Set<Pipeline> unmodOutputs = Collections.unmodifiableSet(outputs);

    private final Set<Closeable> unmodCloseables = Collections.unmodifiableSet(closeables);

    protected final boolean send (final MessageStack message)
    {
        Preconditions.checkState(bindingComplete.get());
        return false;
    }

    protected Closeable autoclose (final Closeable value)
    {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void bindCascade (final Cascade value)
    {
        Preconditions.checkState(bindingComplete.get() == false);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void bindLogger (final CascadeLogger value)
    {
        Preconditions.checkState(bindingComplete.get() == false);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void bindPowerplant (final Powerplant value)
    {
        Preconditions.checkState(bindingComplete.get() == false);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void bindInputs (final Set<Pipeline> value)
    {
        Preconditions.checkState(bindingComplete.get() == false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void bindOutputs (final Set<Pipeline> value)
    {
        Preconditions.checkState(bindingComplete.get() == false);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void bindingComplete ()
    {
        bindingComplete.set(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Cascade cascade ()
    {
        Preconditions.checkState(bindingComplete.get());
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final CascadeLogger logger ()
    {
        Preconditions.checkState(bindingComplete.get());
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Powerplant powerplant ()
    {
        Preconditions.checkState(bindingComplete.get());
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Set<Pipeline> inputs ()
    {
        Preconditions.checkState(bindingComplete.get());
        return unmodInputs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Set<Pipeline> outputs ()
    {
        Preconditions.checkState(bindingComplete.get());
        return unmodOutputs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Set<Closeable> closeables ()
    {
        return unmodCloseables;
    }

}
