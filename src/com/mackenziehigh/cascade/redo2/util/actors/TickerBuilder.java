package com.mackenziehigh.cascade.redo2.util.actors;

import com.mackenziehigh.cascade.redo2.CascadeActor;
import com.mackenziehigh.cascade.redo2.CascadeStage;
import com.mackenziehigh.cascade.redo2.CascadeToken;
import com.mackenziehigh.cascade.redo2.scripts.AnnotatedScript;
import com.mackenziehigh.cascade.redo2.scripts.OnMessage;
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
        final AnnotatedScript script = new AnnotatedScript()
        {
            @OnMessage
            public void onTick ()
            {

            }
        };

        script.subscribe("OnTick", "output");

        final CascadeActor actor = stage().newActor(script);
        return actor;
    }

}
