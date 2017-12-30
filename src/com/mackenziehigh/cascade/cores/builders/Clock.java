package com.mackenziehigh.cascade.cores.builders;

import com.google.common.base.Verify;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.CascadeReactor;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.commons.CascadeProperty;
import com.mackenziehigh.cascade.internal.Utils;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

/**
 * A clock that ticks by sending event-messages.
 *
 * <p>
 * This type of clock uses broadcast(*) to send event-messages,
 * because a single underlying timer thread is shared by all
 * of the clock instances. If a send method was used that could
 * block due to a subscriber being back-logged, then all of
 * the clocks would risk be adversely affected,
 * which is unacceptable.
 * </p>
 */
public final class Clock
        implements CascadeReactor.CoreBuilder
{

    /**
     * (Required) This is the name of the event-channel to send the ticks to.
     */
    public final CascadeProperty<CascadeToken> event = CascadeProperty.<CascadeToken>newBuilder("event")
            .makeFinal()
            .build();

    /**
     * (Optional) If this flag is true, then the clock will require that (at least)
     * the period number of nanoseconds has elapsed before firing another tick.
     * Otherwise, ticks may be closer together than the specified period
     * occasionally due to the time needed to transmit messages.
     */
    public final CascadeProperty<Boolean> spaced = CascadeProperty.<Boolean>newBuilder("spaced")
            .makeFinal()
            .build();

    /**
     * (Optional) This is the number of nanoseconds to wait before the first tick.
     */
    public final CascadeProperty<Long> delayNanos = CascadeProperty.<Long>newBuilder("delayNanos")
            .makeFinal()
            .build();

    /**
     * (Required) This is the number of nanoseconds to wait between ticks.
     */
    public final CascadeProperty<Long> periodNanos = CascadeProperty.<Long>newBuilder("periodNanos")
            .makeFinal()
            .build();

    /**
     * (Optional) This is the maximum number of ticks to send throughout the lifetime of the clock.
     */
    public final CascadeProperty<Long> limit = CascadeProperty.<Long>newBuilder("tickLimit")
            .makeFinal()
            .build();

    /**
     * (Optional) Each tick will be an (8-bit) (Java primitive-byte compatible) counter.
     */
    public final CascadeProperty<Boolean> formatAsByteCounter = CascadeProperty.<Boolean>newBuilder("formatAsByteCounter")
            .makeFinal()
            .build();

    /**
     * (Optional) Each tick will be an (16-bit, Big Endian) (Java primitive-short compatible) counter.
     */
    public final CascadeProperty<Boolean> formatAsShortCounter = CascadeProperty.<Boolean>newBuilder("formatAsShortCounter")
            .makeFinal()
            .build();

    /**
     * (Optional) Each tick will be an (32-bit, Big Endian) (Java primitive-int compatible) counter.
     */
    public final CascadeProperty<Boolean> formatAsIntCounter = CascadeProperty.<Boolean>newBuilder("formatAsIntCounter")
            .makeFinal()
            .build();

    /**
     * (Optional) Each tick will be an (64-bit, Big Endian) (Java primitive-long compatible) counter.
     */
    public final CascadeProperty<Boolean> formatAsLongCounter = CascadeProperty.<Boolean>newBuilder("formatAsLongCounter")
            .makeFinal()
            .build();

    /**
     * (Optional) Each tick will be an (64-bit, Big Endian) (Java primitive-long compatible)
     * representation of the number of milliseconds since the epoch.
     *
     * <p>
     * Equivalent To: <code>System.currentTimeMillis()</code>
     * </p>
     */
    public final CascadeProperty<Boolean> formatAsEpochMillis = CascadeProperty.<Boolean>newBuilder("formatAsEpochMillis")
            .makeFinal()
            .build();

    /**
     * (Optional) Each tick will be an (64-bit, Big Endian) (Java primitive-long compatible)
     * representation of the number of nanoseconds since some arbitrary point in time.
     *
     * <p>
     * Equivalent To: <code>System.nanoTime()</code>
     * </p>
     */
    public final CascadeProperty<Boolean> formatAsMonotonicNanos = CascadeProperty.<Boolean>newBuilder("formatAsMonotonicNanos")
            .makeFinal()
            .build();

    /**
     * (Optional) Each tick will be an (64-bit, Big Endian) (Java primitive-long compatible)
     * representation of the number of nanoseconds since the first tick of this clock.
     *
     * <p>
     * See Also: <code>System.nanoTime()</code>
     * </p>
     */
    public final CascadeProperty<Boolean> formatAsMonotonicElapsedNanos = CascadeProperty.<Boolean>newBuilder("formatAsMonotonicElapsedNanos")
            .makeFinal()
            .build();

    /**
     * (Optional) Each tick will be a textual representation of the current time formatted using this formatter.
     */
    public final CascadeProperty<DateTimeFormatter> formatAsDateTime = CascadeProperty.<DateTimeFormatter>newBuilder("formatAsDateTime")
            .makeFinal()
            .build();

    /**
     * (Optional) If this is true, then each tick will produce an ASCII encoded string.
     */
    public final CascadeProperty<Boolean> textual = CascadeProperty.<Boolean>newBuilder("textual")
            .makeFinal()
            .build();

    /**
     * (Optional) If this is true, then each tick will produce a binary integer.
     */
    public final CascadeProperty<Boolean> binary = CascadeProperty.<Boolean>newBuilder("binary")
            .makeFinal()
            .build();

    private static volatile ScheduledExecutorService timer;

    private final Set<ScheduledFuture> futures = Sets.newConcurrentHashSet();

    private final Set<OperandStack> cleanup = Sets.newConcurrentHashSet();

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeReactor.Core build ()
    {
        return new CascadeReactor.Core()
        {
            @Override
            public void onSetup (final CascadeReactor.Context context)
                    throws Throwable
            {
                submit(context.reactor());
            }

            @Override
            public void onDestroy (final CascadeReactor.Context context)
                    throws Throwable
            {
                cancelAll();
            }
        };
    }

    private void submit (final CascadeReactor reactor)
    {
        Runnable txtTask = null;
        Runnable binTask = null;

        final AtomicLong tickCounter = new AtomicLong();
        final AtomicLong firstTick = new AtomicLong();
        final OperandStack stack = reactor.allocator().newOperandStack();
        final CascadeToken eventId = this.event.get();

        final boolean useText = this.textual.getOrDefault(false) || !binary.getOrDefault(false);

        if (formatAsByteCounter.getOrDefault(false))
        {
            binTask = () -> reactor.broadcast(eventId, stack.clear().push((byte) tickCounter.get()));
            txtTask = () -> reactor.broadcast(eventId, stack.clear().push(Byte.toString((byte) tickCounter.get())));
        }
        else if (formatAsShortCounter.getOrDefault(false))
        {
            binTask = () -> reactor.broadcast(eventId, stack.clear().push((short) tickCounter.get()));
            txtTask = () -> reactor.broadcast(eventId, stack.clear().push(Short.toString((short) tickCounter.get())));
        }
        else if (formatAsIntCounter.getOrDefault(false))
        {
            binTask = () -> reactor.broadcast(eventId, stack.clear().push((int) tickCounter.get()));
            txtTask = () -> reactor.broadcast(eventId, stack.clear().push(Integer.toString((int) tickCounter.get())));
        }
        else if (formatAsLongCounter.getOrDefault(false))
        {
            binTask = () -> reactor.broadcast(eventId, stack.clear().push((long) tickCounter.get()));
            txtTask = () -> reactor.broadcast(eventId, stack.clear().push(Long.toString((long) tickCounter.get())));
        }
        else if (formatAsEpochMillis.getOrDefault(false))
        {
            binTask = () -> reactor.broadcast(eventId, stack.clear().push(System.currentTimeMillis()));
            txtTask = () -> reactor.broadcast(eventId, stack.clear().push(Long.toString(System.currentTimeMillis())));
        }
        else if (formatAsMonotonicNanos.getOrDefault(false))
        {
            binTask = () -> reactor.broadcast(eventId, stack.clear().push(System.nanoTime()));
            txtTask = () -> reactor.broadcast(eventId, stack.clear().push(Long.toString(System.nanoTime())));
        }
        else if (formatAsMonotonicElapsedNanos.getOrDefault(false))
        {
            final LongSupplier fun = () -> tickCounter.get() == 0 ? 0 : System.nanoTime() - firstTick.get();
            binTask = () -> reactor.broadcast(eventId, stack.clear().push(fun.getAsLong()));
            txtTask = () -> reactor.broadcast(eventId, stack.clear().push(Long.toString(fun.getAsLong())));
        }
        else if (formatAsDateTime.isSet() && binary.getOrDefault(false))
        {
            // Error
        }
        else if (formatAsDateTime.isSet())
        {
            txtTask = () -> reactor.broadcast(eventId, stack.clear().push(formatAsDateTime.get().format(Instant.now())));
            binTask = null;
        }
        else
        {
            // Error
        }

        /**
         * Select the actual task.
         */
        final Runnable task = useText ? txtTask : binTask;
        Verify.verifyNotNull(task);

        /**
         * Make the clock permanently stoppable after a user-specified number of ticks.
         * This also causes the ticks to be counted, which is used, if the format is a counter.
         * Likewise, this also reports the time of the first tick, which is used,
         * if the format is elapsed time.
         */
        final AtomicReference<ScheduledFuture> refFuture = new AtomicReference<>();
        final long maxTicks = limit.getOrDefault(Long.MAX_VALUE);
        final Runnable limitedTask = () ->
        {
            if (tickCounter.get() == 0)
            {
                firstTick.set(System.nanoTime());
            }
            task.run();
            if (tickCounter.incrementAndGet() >= maxTicks)
            {
                refFuture.get().cancel(false);
            }
        };

        /**
         * Ensure that exceptions never cause problems.
         */
        final Runnable safeTask = () ->
        {
            try
            {
                limitedTask.run();
            }
            catch (Throwable ex)
            {
                Utils.safeWarn(ex, reactor.logger());
            }
        };

        final long delay = delayNanos.getOrDefault(0L);
        final long period = periodNanos.getOrDefault(TimeUnit.SECONDS.toNanos(1));
        final ScheduledFuture future;
        if (spaced.getOrDefault(false))
        {
            future = timer().scheduleWithFixedDelay(safeTask, delay, period, TimeUnit.NANOSECONDS);
        }
        else
        {
            future = timer().scheduleAtFixedRate(safeTask, delay, period, TimeUnit.NANOSECONDS);
        }
        refFuture.set(future);
        futures.add(future);
    }

    private void cancelAll ()
    {
        futures.forEach(x -> x.cancel(false));
        cleanup.forEach(x -> x.close());
    }

    private static synchronized ScheduledExecutorService timer ()
    {
        final ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
        builder.setDaemon(true);
        builder.setNameFormat("Clock Thread");

        if (timer == null)
        {
            timer = Executors.newScheduledThreadPool(1, builder.build());
        }

        return timer;
    }
}
