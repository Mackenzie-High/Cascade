package com.mackenziehigh.cascade.util.actors;

import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeStage;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.scripts.AnnotatedScript;
import com.mackenziehigh.cascade.scripts.OnMessage;
import java.time.Duration;
import java.util.Objects;

/**
 *
 */
public final class TickerBuilder
        implements CascadeActor.Builder
{
    private final CascadeStage stage;

    public TickerBuilder (final CascadeStage stage)
    {
        this.stage = Objects.requireNonNull(stage, "stage");
    }

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
        return stage;
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
