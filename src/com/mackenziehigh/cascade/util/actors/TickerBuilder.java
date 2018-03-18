package com.mackenziehigh.cascade.util.actors;

import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeScript;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeStage;
import com.mackenziehigh.cascade.CascadeToken;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public final class TickerBuilder
        implements CascadeActor.Builder
{
    private final CascadeStage stage;

    private volatile CascadeToken output;

    private volatile long period = 0;

    public TickerBuilder (final CascadeStage stage)
    {
        this.stage = Objects.requireNonNull(stage, "stage");
    }

    public TickerBuilder setOutput (final CascadeToken event)
    {
        output = event;
        return this;
    }

    public TickerBuilder setOutput (final String event)
    {
        return setOutput(CascadeToken.token(event));
    }

    public TickerBuilder setDelay (final long period)
    {
        return this;
    }

    public TickerBuilder setDelay (final Duration period)
    {
        return setDelay(period.toMillis());
    }

    public TickerBuilder setPeriod (final long period)
    {
        this.period = period;
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
        final CascadeScript script = new CascadeScript()
        {
            // Pass
        };

        final ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);
        timer.scheduleAtFixedRate(() -> send(), 0, period, TimeUnit.MILLISECONDS);

        final CascadeActor actor = stage().newActor(script);
        return actor;
    }

    private void send ()
    {
        stage.cascade().send(output, CascadeStack.newStack().pushLong(System.currentTimeMillis()));
    }
}
