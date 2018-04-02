package com.mackenziehigh.cascade.util.actors;

import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeStage;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.util.actors.faces.Togglable;
import java.util.Optional;
import java.util.SortedSet;

/**
 * Maps single input to multiple outputs.
 */
public final class DemultiplexerBuilder
        implements CascadeActor.Builder,
                   Togglable<DemultiplexerBuilder>
{
    public DemultiplexerBuilder alias (final CascadeToken source,
                                       final CascadeToken destination)
    {
        return this;
    }

    /**
     * Route, even if no alias is known.
     *
     * @return
     */
    public DemultiplexerBuilder promiscuous ()
    {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemultiplexerBuilder defaultToggleOn ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemultiplexerBuilder defaultToggleOff ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemultiplexerBuilder setToggleInput (CascadeToken input)
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
    public DemultiplexerBuilder setDataInput (CascadeToken input)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    public DemultiplexerBuilder addDataOutput (CascadeToken input)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    public Optional<CascadeToken> getDataInput ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    public SortedSet<CascadeToken> getDataOutputs ()
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
