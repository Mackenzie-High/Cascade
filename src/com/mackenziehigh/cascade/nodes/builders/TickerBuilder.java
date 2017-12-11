package com.mackenziehigh.cascade.nodes.builders;

import com.mackenziehigh.cascade.CascadeNode;
import com.mackenziehigh.cascade.nodes.NodeBuilder;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public final class TickerBuilder
        implements NodeBuilder<TickerBuilder>
{
    private static ScheduledExecutorService timer;

    private volatile long delay;

    private volatile long period;

    private volatile ScheduledFuture future;

    public TickerBuilder delay (final long value,
                                final TimeUnit unit)
    {
        delay = unit.toNanos(value);
        return this;
    }

    public TickerBuilder period (final long value,
                                 final TimeUnit unit)
    {
        period = unit.toNanos(value);
        return this;
    }

    public TickerBuilder formatByteCounter ()
    {
        return this;
    }

    public TickerBuilder formatShortCounter ()
    {
        return this;
    }

    public TickerBuilder formatIntCounter ()
    {
        return this;
    }

    public TickerBuilder formatLongCounter ()
    {
        return this;
    }

    public TickerBuilder formatEpochMillis ()
    {
        return this;
    }

    public TickerBuilder formatMonotonicNanos ()
    {
        return this;
    }

    public TickerBuilder format (final DateTimeFormatter format)
    {
        return this;
    }

    @Override
    public CascadeNode.Core build ()
    {
        return new CascadeNode.Core()
        {
            @Override
            public void onSetup (final CascadeNode.Context context)
                    throws Throwable
            {
                final Runnable task = () -> run();
                future = timer().scheduleAtFixedRate(task, delay, period, TimeUnit.NANOSECONDS);
            }

            @Override
            public void onDestroy (final CascadeNode.Context context)
                    throws Throwable
            {
                final boolean interrupt = false;
                future.cancel(interrupt);
            }
        };
    }

    private void run ()
    {
        // TODO: ASYNC!
    }

    private static synchronized ScheduledExecutorService timer ()
    {
        if (timer == null)
        {
            timer = Executors.newScheduledThreadPool(1);
        }

        return timer;
    }
}
