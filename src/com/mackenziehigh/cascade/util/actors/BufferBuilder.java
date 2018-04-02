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
 * Temporarily queue messages, send on clock/trigger pulse.
 *
 * Based on FIFO or LIFO queuing.
 */
public final class BufferBuilder
        implements CascadeActor.Builder,
                   Clocked<BufferBuilder>,
                   Resettable<BufferBuilder>,
                   Togglable<BufferBuilder>,
                   OneToOne<BufferBuilder>
{
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

    /**
     * {@inheritDoc}
     */
    @Override
    public BufferBuilder setClockInput (CascadeToken input)
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
    public BufferBuilder setResetInput (CascadeToken input)
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
    public BufferBuilder defaultToggleOn ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BufferBuilder defaultToggleOff ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BufferBuilder setToggleInput (CascadeToken input)
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
    public BufferBuilder setDataInput (CascadeToken input)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BufferBuilder setDataOutput (CascadeToken output)
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

}
