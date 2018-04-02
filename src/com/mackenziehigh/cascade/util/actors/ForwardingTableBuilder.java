package com.mackenziehigh.cascade.util.actors;

import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeStage;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.util.actors.faces.Togglable;
import java.util.Optional;
import java.util.SortedSet;

/**
 * Contains entries (source, dest1, ... , destN). Forwards source to the destinations. May have many sources/entries.
 *
 * Effectively renames event-streams.
 */
public final class ForwardingTableBuilder
        implements CascadeActor.Builder,
                   Togglable<ForwardingTableBuilder>
{

    /**
     * {@inheritDoc}
     */
    @Override
    public ForwardingTableBuilder defaultToggleOn ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ForwardingTableBuilder defaultToggleOff ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ForwardingTableBuilder setToggleInput (CascadeToken input)
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
    public ForwardingTableBuilder addDataInput (CascadeToken input)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    public ForwardingTableBuilder addDataOutput (CascadeToken input)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    public SortedSet<CascadeToken> getDataInputs ()
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
