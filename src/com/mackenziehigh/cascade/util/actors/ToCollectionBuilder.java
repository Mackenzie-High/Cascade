package com.mackenziehigh.cascade.util.actors;

import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeStage;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.util.actors.faces.Resettable;
import com.mackenziehigh.cascade.util.actors.faces.Sink;
import com.mackenziehigh.cascade.util.actors.faces.Togglable;
import java.util.Optional;

/**
 * Append/prepend incoming messages onto user-provided collections.
 */
public final class ToCollectionBuilder
        implements CascadeActor.Builder,
                   Resettable<ToCollectionBuilder>,
                   Togglable<ToCollectionBuilder>,
                   Sink<ToCollectionBuilder>
{
    /**
     * {@inheritDoc}
     */
    @Override
    public ToCollectionBuilder setResetInput (CascadeToken input)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CascadeToken> getResetInput ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ToCollectionBuilder defaultToggleOn ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ToCollectionBuilder defaultToggleOff ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ToCollectionBuilder setToggleInput (CascadeToken input)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CascadeToken> getToggleInput ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ToCollectionBuilder setDataInput (CascadeToken input)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CascadeToken> getDataInput ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeStage stage ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeActor build ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
