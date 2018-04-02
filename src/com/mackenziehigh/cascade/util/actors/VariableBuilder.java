package com.mackenziehigh.cascade.util.actors;

import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeStage;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.util.actors.faces.Clocked;
import com.mackenziehigh.cascade.util.actors.faces.OneToOne;
import com.mackenziehigh.cascade.util.actors.faces.Resettable;
import com.mackenziehigh.cascade.util.actors.faces.Togglable;
import java.util.Optional;

/**
 * may be set on each input, max input, min input, first input, change (filters duplicates), etc. Sends value on every clock pulse.
 *
 * Variable also functions as a valve, if the clock-input is disconnected.
 */
public class VariableBuilder
        implements CascadeActor.Builder,
                   Resettable<VariableBuilder>,
                   Togglable<VariableBuilder>,
                   Clocked<VariableBuilder>,
                   OneToOne<VariableBuilder>
{

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableBuilder setResetInput (CascadeToken input)
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
    public VariableBuilder defaultToggleOn ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableBuilder defaultToggleOff ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableBuilder setToggleInput (CascadeToken input)
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
    public VariableBuilder setClockInput (CascadeToken input)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CascadeToken> getClockInput ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableBuilder setDataInput (CascadeToken input)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableBuilder setDataOutput (CascadeToken output)
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
    public Optional<CascadeToken> getDataOutput ()
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
