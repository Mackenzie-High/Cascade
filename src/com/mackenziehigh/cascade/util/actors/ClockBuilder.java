package com.mackenziehigh.cascade.util.actors;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeStage;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.scripts.LambdaScript;
import com.mackenziehigh.cascade.scripts.LambdaScript.MessageFunction;
import com.mackenziehigh.cascade.util.Final;
import com.mackenziehigh.cascade.util.actors.faces.Resettable;
import com.mackenziehigh.cascade.util.actors.faces.Source;
import com.mackenziehigh.cascade.util.actors.faces.Togglable;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Builds an actor that sends clock-pulses periodically.
 *
 * <p>
 * Every time that a clock-pulse occurs an event-message will be sent,
 * unless this clock is disabled at that time.
 * </p>
 *
 * <p>
 * Each clock-pulse event-message will be one of the following:
 * <ul>
 * <li>Sequence Number - A monotonically-increasing zero-based primitive-long sequence-number. Resets reset it to zero.</li>
 * <li>Instant - The current time as an Instant object. </li>
 * <li>Elapsed Duration - A Duration object indicating the time since the start or the last reset.</li>
 * <li>Elapsed Numeric Duration - A monotonically-increasing primitive-long indicating the time since the start or the last reset. </li>
 * <li>Epoch Duration - A Duration object indicating the time since the epoch. </li>
 * <li>Custom Duration - A Duration object indicating the time since a user-specified time.</li>
 * </ul>
 * </p>
 *
 * <p>
 * The clock can be toggled on and off via event-messages sent to the Toggle Input.
 * If an event-message is received with primitive-boolean True on the top of the stack, the clock will be turned on.
 * If an event-message is received with primitive-boolean False on the top of the stack, the clock will be turned off.
 * Toggling simply blocks or unblocks sending of clock-pulse event-messages.
 * Internal state, such is used to compute elapsed time, will be unaffected.
 * </p>
 *
 * <p>
 * The clock can be rest via event-messages sent to the Reset Input.
 * Any event-message sent thereto will cause the clock to be reset, regardless of message content.
 * </p>
 *
 * <p>
 * The clock can use either fixed-rate (default) or fixed-delay periodicity.
 * These options control how the clock will respond when the underlying timing thread is busy.
 * If the underlying timing thread gets slowed down by long-running tasks,
 * then the clock will make an effort to send clock-pulses every (period) amount of time apart,
 * even if this means that two or more clock-pulses would occur less than (period) apart.
 * In short, fixed-rate periodicity can sometimes result in clock-pulses being sent back-to-back.
 * If fixed-delay periodicity is used, then clock-pulses will always be at least (period) amount
 * of time apart, even if this means that clock-pulses are sent at less than (period) rate.
 * For a further discussion of this topic, see class <code>ScheduledExecutorService</code>.
 * </p>
 */
public final class ClockBuilder
        implements CascadeActor.Builder,
                   Resettable<ClockBuilder>,
                   Togglable<ClockBuilder>,
                   Source<ClockBuilder>

{
    /**
     * This is the stage that will contain the actor.
     */
    private final CascadeStage stage;

    /**
     * This clock will be used to get the current time whenever needed.
     */
    private final Final<Clock> clock = Final.empty();

    /**
     * This Executor Service will provide the timer thread(s),
     * which execute periodically causing the clock-pulses to be sent.
     */
    private final Final<ScheduledExecutorService> timer = Final.empty();

    /**
     * This object will produce the values to send during clock-pulses.
     * The possible output-types are defined as nested classes below.
     */
    private final Final<OutputType> outputType = Final.empty();

    /**
     * This is the event-stream that will signal this clock to reset, if present.
     */
    private final Final<CascadeToken> reset = Final.empty();

    /**
     * This is the event-stream that will toggle this clock on/off, if present.
     */
    private final Final<CascadeToken> toggle = Final.empty();

    /**
     * This is the event-stream that the clock-pulse event-messages will be sent to.
     */
    private final Final<CascadeToken> output = Final.empty();

    /**
     * This is the amount of time to wait, initially or after rest,
     * before sending clock-pulse event-messages to the output.
     */
    private final Final<Duration> delay = Final.empty();

    /**
     * This is periodicity of the clock-pulses.
     */
    private final Final<Duration> period = Final.empty();

    /**
     * If this field is set, then the clock-pulses
     * will always be at least (period) apart.
     */
    private final Final<Boolean> fixedDelay = Final.empty();

    /**
     * IF this field is set, then the clock-pulses will
     * be sent at (period) rate when possible, even if
     * this means that two clock-pulses are less than
     * (period) apart due to the timer thread being busy.
     */
    private final Final<Boolean> fixedRate = Final.empty();

    /**
     * True, if this clock should be enabled by default.
     * If the user does not set this, it will default to true.
     */
    private final Final<Boolean> defaultToggle = Final.empty();

    /**
     * True, if build() was already invoked once.
     */
    private final AtomicBoolean built = new AtomicBoolean();

    /**
     * (Actor State) True, if this clock is currently enabled.
     */
    private final AtomicBoolean enabled = new AtomicBoolean(true);

    /**
     * (Actor State) The task that the Executor Service is periodically executing.
     */
    private volatile ScheduledFuture future = null;

    /**
     * This is the actor that was created by this builder.
     */
    private volatile CascadeActor actor;

    /**
     * Sole Constructor.
     *
     * @param stage will contain the new actor.
     */
    public ClockBuilder (final CascadeStage stage)
    {
        this.stage = Objects.requireNonNull(stage, "stage");
    }

    /**
     * The clock-pulses will send monotonically-increasing primitive-long zero-based sequence-numbers.
     *
     * @return this.
     */
    public ClockBuilder sendSequenceNumber ()
    {
        outputType.set(new SequenceNumberType());
        return this;
    }

    /**
     * The clock-pulses will send Instant objects indicating the time the pulse was sent.
     *
     * @return this.
     */
    public ClockBuilder sendInstant ()
    {
        outputType.set(new InstantType());
        return this;
    }

    /**
     * The clock-pulses will send Duration objects indicating the elapsed-time since the epoch.
     *
     * @return this.
     */
    public ClockBuilder sendEpochDuration ()
    {
        final FixedDurationType out = new FixedDurationType();
        out.base.set(Instant.ofEpochMilli(0));
        outputType.set(out);
        return this;
    }

    /**
     * The clock-pulses will send Duration objects indicating the elapsed-time
     * since either the actor was created of the actor was last reset.
     *
     * @return this.
     */
    public ClockBuilder sendElapsedDuration ()
    {
        outputType.set(new ElapsedDurationType());
        return this;
    }

    /**
     * The clock-pulses will send monotonically-increasing primitive-longs indicating
     * the elapsed-time since either the actor was created of the actor was last reset.
     *
     * @param unit dictates whether the primitive-longs represent seconds, days, etc.
     * @return this.
     */
    public ClockBuilder sendElapsed (final TimeUnit unit)
    {
        final ElapsedTimeUnitType out = new ElapsedTimeUnitType();
        out.unit.set(unit);
        outputType.set(out);
        return this;
    }

    /**
     * The clock-pulses will send Duration objects indicating the elapsed-time since the given time.
     *
     * @param base is the given time to base the duration off of
     * @return this.
     */
    public ClockBuilder sendDuration (final Instant base)
    {
        final FixedDurationType out = new FixedDurationType();
        out.base.set(base);
        outputType.set(out);
        return this;
    }

    /**
     * Specifies the clock to use to determine the current time.
     *
     * @param clock will be used to obtain the current-time whenever necessary.
     * @return this.
     */
    public ClockBuilder setClock (final Clock clock)
    {
        this.clock.set(clock);
        return this;
    }

    /**
     * Getter.
     *
     * @return the clock that is used to get the current-time.
     */
    public Optional<Clock> getClock ()
    {
        return clock.get();
    }

    /**
     * Specify the amount of time that the clock will
     * wait before sending the first clock-pulse.
     *
     * @param delay the amount of time to wait.
     * @return this.
     */
    public ClockBuilder setDelay (final Duration delay)
    {
        Preconditions.checkNotNull(delay, "delay");
        this.delay.set(delay);
        return this;
    }

    /**
     * Specify how often clock-pulses will be sent.
     *
     * @param period is how often the clock shall tick.
     * @return this.
     */
    public ClockBuilder setPeriod (final Duration period)
    {
        this.period.set(period);
        return this;
    }

    /**
     * Getter.
     *
     * @return the delay setting, if specified.
     */
    public Optional<Duration> getDelay ()
    {
        return delay.get();
    }

    /**
     * Getter.
     *
     * @return the period setting, if specified.
     */
    public Optional<Duration> getPeriod ()
    {
        return period.get();
    }

    /**
     * Specify that the clock-pulses will be sent at a fixed-rate.
     *
     * @return this.
     */
    public ClockBuilder useFixedRate ()
    {
        fixedRate.set(true);
        return this;
    }

    /**
     * Specify that the clock-pulses will be sent at a fixed-delay.
     *
     * @return this.
     */
    public ClockBuilder useFixedDelay ()
    {
        fixedDelay.set(true);
        return this;
    }

    /**
     * Getter.
     *
     * @return true, if clock-pulses will be sent at a fixed-rate.
     */
    public boolean usesFixedRate ()
    {
        return fixedRate.get().orElse(false);
    }

    /**
     * Getter.
     *
     * @return true, if clock-pulses will be sent at a fixed-delay.
     */
    public boolean usesFixedDelay ()
    {
        return fixedRate.get().orElse(false);
    }

    /**
     * Specifies the underlying Executor Service that provides the timer thread.
     *
     * @param clock will provide the timer thread.
     * @return this.
     */
    public ClockBuilder poweredBy (final ScheduledExecutorService clock)
    {
        this.timer.set(clock);
        return this;
    }

    /**
     * Getter.
     *
     * @return the Executor Service that provides the timer thread.
     */
    public Optional<ScheduledExecutorService> getScheduledExecutorService ()
    {
        return timer.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeStage stage ()
    {
        return stage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClockBuilder setResetInput (final CascadeToken input)
    {
        reset.set(input);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CascadeToken> getResetInput ()
    {
        return reset.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClockBuilder defaultToggleOn ()
    {
        defaultToggle.set(true);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClockBuilder defaultToggleOff ()
    {
        defaultToggle.set(false);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClockBuilder setToggleInput (final CascadeToken input)
    {
        toggle.set(input);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CascadeToken> getToggleInput ()
    {
        return toggle.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClockBuilder setDataOutput (final CascadeToken event)
    {
        output.set(event);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CascadeToken> getDataOutput ()
    {
        return output.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeActor build ()
    {
        Preconditions.checkState(built.compareAndSet(false, true), "Already Built!");

        /**
         * If the user did not specify a Clock, then use the default.
         */
        if (clock.isNotSet())
        {
            clock.set(Clock.systemUTC());
        }

        /**
         * If the user did not specify the output type,
         * then use the default, which is the current-time.
         */
        if (outputType.isNotSet())
        {
            outputType.set(new InstantType());
        }

        /**
         * This will define how the actor behaves in-response to Toggle Inputs, Reset Inputs, or actor death.
         * The clock-pulses themselves are send via the Executor Service asynchronously.
         * The clock-pulses only appear to come from the actor itself.
         */
        final LambdaScript script = new LambdaScript();

        /**
         * Ensure the output producer is configured correctly for the first pulse.
         */
        reset();

        /**
         * If the user specified that the actor can be reset via event-messages,
         * then the actor must be told how to handle those.
         */
        if (reset.isSet())
        {
            final CascadeToken event = toggle.get().get();
            final MessageFunction handler = (ctx, evt, stack) -> reset();
            script.subscribe(event, handler);
        }

        /**
         * If the user specified that the actor can be toggled on/off via event-messages,
         * then the actor must be told how to handle those.
         */
        if (toggle.isSet())
        {
            final CascadeToken event = toggle.get().get();
            final MessageFunction handler = (ctx, evt, stack) -> toggle(stack);
            script.subscribe(event, handler);
        }

        /**
         * Create the actor itself.
         */
        actor = stage().newActor(script);

        /**
         * Obtain the executor that actually fires the clock-pulses.
         */
        final ScheduledExecutorService ticker = timer.isSet()
                ? timer.get().get()
                : Executors.newSingleThreadScheduledExecutor(); // TODO How shutdown?

        /**
         * This task will be invoked by the executor periodically,
         * which in-turn will cause the clock-pulse event-messages to be sent.
         */
        final Runnable task = () -> send();

        /**
         * Schedule the task for periodic-execution.
         */
        final long delayNanos = delay.get().get().toNanos();
        final long periodNanos = period.get().get().toNanos();

        if (fixedDelay.isSet())
        {
            ticker.scheduleWithFixedDelay(task, delayNanos, periodNanos, TimeUnit.NANOSECONDS);
        }
        else
        {
            ticker.scheduleAtFixedRate(task, delayNanos, periodNanos, TimeUnit.NANOSECONDS);
        }

        return actor;
    }

    private synchronized void reset ()
    {
        // TODO: Reschedule the task. Or change the documentation.
        outputType.get().get().reset();
    }

    private synchronized void toggle (final CascadeStack stack)
    {
        final boolean condition = stack.peekAsBoolean();
        enabled.set(condition);
    }

    private synchronized void send ()
    {
        if (enabled.get())
        {
            final CascadeToken event = output.get().get();
            final CascadeStack stack = outputType.get().get().next();
            actor.context().send(event, stack);
        }
    }

    /**
     * Creates, but does not send, the
     */
    private interface OutputType
    {
        public void reset ();

        public CascadeStack next ();
    }

    /**
     * Creates monotonically-increasing sequence-number event-messages.
     */
    private final class SequenceNumberType
            implements OutputType
    {
        private final AtomicLong value = new AtomicLong();

        @Override
        public void reset ()
        {
            value.set(0);
        }

        @Override
        public CascadeStack next ()
        {
            final long result = value.getAndIncrement();
            return CascadeStack.newStack().pushLong(result);
        }
    }

    /**
     * Creates current-time event-messages.
     */
    private final class InstantType
            implements OutputType
    {
        @Override
        public void reset ()
        {
            // Pass
        }

        @Override
        public CascadeStack next ()
        {
            final Instant now = clock.get().get().instant();
            return CascadeStack.newStack().pushObject(now);
        }
    }

    /**
     * Creates event-messages indicating Durations from a fixed point in time.
     */
    private final class FixedDurationType
            implements OutputType
    {
        private final Final<Instant> base = Final.empty();

        @Override
        public void reset ()
        {
            // Pass
        }

        @Override
        public CascadeStack next ()
        {
            final Instant now = clock.get().get().instant();
            final Duration result = Duration.between(base.get().get(), now);
            return CascadeStack.newStack().pushObject(result);
        }
    }

    /**
     * Creates event-messages indicating elapsed Durations.
     */
    private final class ElapsedDurationType
            implements OutputType
    {
        private volatile Instant start;

        @Override
        public void reset ()
        {
            start = clock.get().get().instant();
        }

        @Override
        public CascadeStack next ()
        {
            final Instant now = clock.get().get().instant();
            final Duration result = Duration.between(start, now);
            return CascadeStack.newStack().pushObject(result);
        }
    }

    /**
     * Creates event-messages indicating elapsed durations in numeric form.
     */
    private final class ElapsedTimeUnitType
            implements OutputType
    {
        public final Final<TimeUnit> unit = Final.empty();

        private final Stopwatch stopwatch = Stopwatch.createUnstarted();

        @Override
        public void reset ()
        {
            stopwatch.reset().start();
        }

        @Override
        public CascadeStack next ()
        {
            final long elapsed = stopwatch.elapsed(unit.get().get());
            return CascadeStack.newStack().pushLong(elapsed);
        }
    }
}
