package com.mackenziehigh.cascade.cores.builders;

import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.CascadeReactor;
import com.mackenziehigh.cascade.CascadeToken;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 *
 */
public final class TickerBuilder
        implements CascadeReactor.CoreBuilder
{
    private static ScheduledExecutorService timer;

    private volatile long delay;

    private volatile long period;

    private volatile ScheduledFuture future;

    private final Set<CascadeToken> outputs = Sets.newConcurrentHashSet();

    private volatile Supplier<byte[]> formatter;

    public TickerBuilder sendTo (final String eventId)
    {
        outputs.add(CascadeToken.create(eventId));
        return this;
    }

    public TickerBuilder withDelay (final long value,
                                    final TimeUnit unit)
    {
        delay = unit.toNanos(value);
        return this;
    }

    public TickerBuilder withPeriod (final long value,
                                     final TimeUnit unit)
    {
        period = unit.toNanos(value);
        return this;
    }

    public TickerBuilder withFormatByteCounter ()
    {
        return this;
    }

    public TickerBuilder formatShortCounter ()
    {
        return this;
    }

    public TickerBuilder withFormatIntCounter ()
    {
        return this;
    }

    public TickerBuilder withFormatLongCounter ()
    {
        return this;
    }

    public TickerBuilder withFormatEpochMillis ()
    {
        return this;
    }

    public TickerBuilder withFormatMonotonicNanos ()
    {
        formatter = () -> String.valueOf(System.nanoTime()).getBytes();
        return this;
    }

    public TickerBuilder withFormatMonotonicElapsedNanos ()
    {
        return this;
    }

    public TickerBuilder withFormat (final DateTimeFormatter format)
    {
        return this;
    }

    @Override
    public CascadeReactor.Core build ()
    {
        return new CascadeReactor.Core()
        {
            private OperandStack stack;

            @Override
            public void onSetup (final CascadeReactor.Context context)
                    throws Throwable
            {
                stack = context.allocator().newOperandStack();
                final Runnable task = () ->
                {
                    try
                    {
                        context.message().push(formatter.get());
                        outputs.forEach(x -> context.broadcast(x, stack));
                    }
                    catch (Throwable ex)
                    {
                        ex.printStackTrace(System.err);
                    }
                };
                future = timer().scheduleAtFixedRate(task, delay, period, TimeUnit.NANOSECONDS);
            }

            @Override
            public void onDestroy (final CascadeReactor.Context context)
                    throws Throwable
            {
                final boolean interrupt = false;
                future.cancel(interrupt);
                stack.close();
            }
        };
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
