package com.mackenziehigh.cascade.redo2.util.actors;

import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.redo2.CascadeActor;
import com.mackenziehigh.cascade.redo2.CascadeContext;
import com.mackenziehigh.cascade.redo2.CascadeOperand;
import com.mackenziehigh.cascade.redo2.CascadeScript;
import com.mackenziehigh.cascade.redo2.CascadeStage;
import java.time.Duration;

/**
 *
 */
public final class TickerBuilder
        implements CascadeActor.Builder
{
    public TickerBuilder setOutput (final CascadeToken event)
    {
        return this;
    }

    public TickerBuilder setOutput (final String event)
    {
        return this;
    }

    public TickerBuilder setDelay (final long period)
    {
        return this;
    }

    public TickerBuilder setDelay (final Duration period)
    {
        return setPeriod(period.toMillis());
    }

    public TickerBuilder setPeriod (final long period)
    {
        return this;
    }

    public TickerBuilder setPeriod (final Duration period)
    {
        return setPeriod(period.toMillis());
    }

    public Duration getDelay ()
    {
        return null;
    }

    public Duration getPeriod ()
    {
        return null;
    }

    public TickerBuilder useFixedRate ()
    {
        return this;
    }

    public TickerBuilder useFixedDelay ()
    {
        return this;
    }

    @Override
    public CascadeStage stage ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CascadeActor build ()
    {
        final CascadeScript script = new CascadeScript()
        {
            @Override
            public void onMessage (CascadeContext ctx,
                                   CascadeToken event,
                                   CascadeOperand stack)
                    throws Throwable
            {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        final CascadeActor actor = stage().newActor(script);
        actor.subscribe("output");
        return actor;
    }

}
