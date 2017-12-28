package com.mackenziehigh.cascade.internal;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableSet;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadePump;
import com.mackenziehigh.cascade.CascadeReactor;
import com.mackenziehigh.cascade.CascadeReactor.Context;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Collections;
import java.util.SortedMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 *
 */
public final class ConcreteCascade
        implements Cascade
{
    private volatile CascadeToken name;

    private final UUID uuid = UUID.randomUUID();

    private volatile CascadeLogger defaultLogger;

    private volatile CascadeAllocator allocator;

    private final SortedMap<CascadeToken, ConcretePump> pumps = new ConcurrentSkipListMap<>();

    private final SortedMap<CascadeToken, CascadePump> unmodPumps = Collections.unmodifiableSortedMap(pumps);

    private final SortedMap<CascadeToken, ConcreteReactor> reactors = new ConcurrentSkipListMap<>();

    private final SortedMap<CascadeToken, CascadeReactor> unmodReactors = Collections.unmodifiableSortedMap(reactors);

    private final AtomicInteger phaseIdx = new AtomicInteger();

    private final AtomicBoolean startWasCalled = new AtomicBoolean();

    private final AtomicBoolean stopWasCalled = new AtomicBoolean();

    /**
     * Invariant Checking.
     */
    public void selfTest ()
    {
        Verify.verifyNotNull(name());
        Verify.verifyNotNull(uuid());
        Verify.verifyNotNull(phase());
        Verify.verifyNotNull(defaultLogger());
        Verify.verifyNotNull(pumps());
        Verify.verifyNotNull(reactors());

        Verify.verify(name().toString().equals(toString()));
        Verify.verify(phase().equals(ExecutionPhase.INITIAL));
        Verify.verify(allocator().cascade().equals(this));

        Verify.verify(phase().equals(phase()));
        Verify.verify(uuid().equals(uuid()));

        /**
         * pumps() must map the name of a pump to that pump.
         * Each pump must have this object as its cascade().
         */
        Verify.verify(pumps().values().stream().allMatch(x -> x.cascade().equals(this) && pumps().get(x.name()).equals(x)));

        /**
         * reactors() must map the name of a reactor to that reactor.
         * Each reactor must have this object as its cascade().
         */
        Verify.verify(reactors().values().stream().allMatch(x -> x.cascade().equals(this) && reactors().get(x.name()).equals(x)));

        /**
         * Verify that all of the pumps known-by the reactors
         * are also directly known by this object as well.
         */
        if (reactors.size() > 0)
        {
            Verify.verify(reactors().values().stream().map(x -> x.pump()).collect(Collectors.toSet()).equals(ImmutableSet.copyOf(pumps().values())));
        }

        /**
         * Verify that all of the reactors known-by the pumps
         * are also directly known by this object as well.
         */
        Verify.verify(pumps().values().stream().map(x -> x.reactors()).flatMap(x -> x.stream()).collect(Collectors.toSet()).equals(ImmutableSet.copyOf(reactors().values())));
    }

    public void setName (final CascadeToken value)
    {
        name = value;
    }

    public void setDefaultLogger (final CascadeLogger value)
    {
        defaultLogger = value;
    }

    public void setAllocator (final CascadeAllocator value)
    {
        allocator = value;
    }

    public void addPump (final ConcretePump pump)
    {
        pumps.put(pump.name(), pump);
    }

    public void addReactor (final ConcreteReactor reactor)
    {
        reactors.put(reactor.name(), reactor);
    }

    @Override
    public CascadeToken name ()
    {
        return name;
    }

    @Override
    public UUID uuid ()
    {
        return uuid;
    }

    @Override
    public CascadeLogger defaultLogger ()
    {
        return defaultLogger;
    }

    @Override
    public CascadeAllocator allocator ()
    {
        return allocator;
    }

    @Override
    public SortedMap<CascadeToken, CascadePump> pumps ()
    {
        return unmodPumps;
    }

    @Override
    public SortedMap<CascadeToken, CascadeReactor> reactors ()
    {
        return unmodReactors;
    }

    @Override
    public ExecutionPhase phase ()
    {
        return ExecutionPhase.values()[phaseIdx.get()];
    }

    @Override
    public Cascade start ()
    {
        if (startWasCalled.compareAndSet(false, true))
        {
            Verify.verify(phase().equals(ExecutionPhase.INITIAL));
            performStartOnNewThread();
        }

        return this;
    }

    @Override
    public Cascade stop ()
    {
        // TODO: This causes problems, if stop is called during startup. Need to postpone.

        if (stopWasCalled.compareAndSet(false, true))
        {
            Verify.verify(phase().equals(ExecutionPhase.RUN));
            performStopOnNewThread();
        }

        return this;
    }

    @Override
    public String toString ()
    {
        return name.name();
    }

    private void performStartOnNewThread ()
    {
        final String threadName = "Cascade Startup Thread (" + uuid() + ")";
        final Thread thread = new Thread(() -> performStart(), threadName);
        thread.setDaemon(false);
        thread.start();
    }

    private void performStart ()
    {
        /**
         * We need to start the pumps first, so that any messages sent from within
         * the onSetup(*) and/or onStartup() event-handlers can at least be enqueued
         * for processing, even if the recipient reactors are not fully online yet.
         * Of course, this introduces the possibility that one of those event-handlers
         * will never return due to using a blocking send to a recipient that does
         * not have enough queue space available to store the incoming messages
         * pending startup; however, that would be a logic bug in the user program,
         * not something that we can reasonably deal with here.
         */
        startThePumps();

        /**
         * Per the contract, bring each reactor online.
         */
        phaseIdx.incrementAndGet();
        invokeSetupOnEachReactor();

        /**
         * Per the contract, notify each reactor that the pumps have started
         * and all of the reactors have been brought online.
         */
        phaseIdx.incrementAndGet();
        invokeStartOnEachReactor();

        /**
         * The system is now running.
         */
        phaseIdx.incrementAndGet();
        Verify.verify(phase().equals(ExecutionPhase.RUN));
    }

    private void startThePumps ()
    {
        Verify.verify(phase().equals(ExecutionPhase.INITIAL));

        for (ConcretePump pump : pumps.values())
        {
            try
            {
                pump.start();
            }
            catch (Throwable ex)
            {
                safelyLog(ex);
            }
        }
    }

    private void invokeSetupOnEachReactor ()
    {
        Verify.verify(phase().equals(ExecutionPhase.SETUP));

        for (CascadeReactor reactor : reactors.values())
        {
            invokeEventHandler(reactor, ctx -> reactor.core().onSetup(ctx));
        }
    }

    private void invokeStartOnEachReactor ()
    {
        Verify.verify(phase().equals(ExecutionPhase.START));

        for (CascadeReactor reactor : reactors.values())
        {
            invokeEventHandler(reactor, ctx -> reactor.core().onStart(ctx));
        }
    }

    private void performStopOnNewThread ()
    {
        final String threadName = "Cascade Shutdown Thread (" + uuid() + ")";
        final Thread thread = new Thread(() -> performStop(), threadName);
        thread.setDaemon(false);
        thread.start();
    }

    private void performStop ()
    {
        /**
         * Per the contract, inform each reactor that shutdown has begun.
         */
        phaseIdx.incrementAndGet();
        invokeStopOnEachReactor();

        /**
         * Per the contract, wait, indefinitely, for the reactors to shutdown.
         * If any of the reactors are buggy and fail to shutdown, then this call
         * will never return, which is out of our reasonable control.
         */
        waitForEachReactorToStop();

        /**
         * Since the reactors are now shutdown, reactors (should) not be sending or
         * receiving anymore event-messages; therefore, we can now shutdown the pumps.
         */
        stopThePumps();

        /**
         * Per the contract, notify each of the reactors that shutdown has occurred,
         * so that they can close/release any leak-able resources.
         */
        phaseIdx.incrementAndGet();
        invokeDestroyOnEachReactor();

        /**
         * The system is now shutdown.
         */
        phaseIdx.incrementAndGet();
        Verify.verify(phase().equals(ExecutionPhase.TERMINATED));
    }

    private void invokeStopOnEachReactor ()
    {
        Verify.verify(phase().equals(ExecutionPhase.STOP));

        for (CascadeReactor reactor : reactors.values())
        {
            invokeEventHandler(reactor, ctx -> reactor.core().onStop(ctx));
        }
    }

    private void waitForEachReactorToStop ()
    {
        Verify.verify(phase().equals(ExecutionPhase.STOP));

        for (CascadeReactor reactor : reactors.values())
        {
            final AtomicBoolean flag = new AtomicBoolean();

            while (flag.get() == false)
            {
                invokeEventHandler(reactor, ctx -> flag.set(reactor.core().isDestroyable()));

                try
                {
                    Thread.sleep(250);
                }
                catch (InterruptedException ex)
                {
                    safelyLog(ex);
                }
            }
        }
    }

    private void stopThePumps ()
    {
        Verify.verify(phase().equals(ExecutionPhase.STOP));

        for (ConcretePump pump : pumps.values())
        {
            try
            {
                pump.stop();
            }
            catch (Throwable ex)
            {
                safelyLog(ex);
            }
        }
    }

    private void invokeDestroyOnEachReactor ()
    {
        Verify.verify(phase().equals(ExecutionPhase.DESTROY));

        for (CascadeReactor reactor : reactors.values())
        {
            invokeEventHandler(reactor, ctx -> reactor.core().onDestroy(ctx));
        }
    }

    private void invokeEventHandler (final CascadeReactor reactor,
                                     final UnsafeConsumer<Context> action)
    {
        final Thread currentThread = Thread.currentThread();
        final ConcreteContext context = new ConcreteContext(reactor);

        try (OperandStack stack = new CheckedOperandStack(currentThread, allocator.newOperandStack()))
        {
            try

            {
                context.set(currentThread, null, stack, null);
                action.accept(context);
            }
            catch (Throwable ex1)
            {
                try
                {
                    context.set(currentThread, null, stack, ex1);
                    reactor.core().onException(context);
                }
                catch (Throwable ex2)
                {
                    safelyLog(ex2);
                }
            }
        }
    }

    private void safelyLog (final Throwable ex1)
    {
        try
        {
            defaultLogger().warn(ex1);
        }
        catch (Throwable ex2)
        {
            ex1.printStackTrace(System.err);
            ex2.printStackTrace(System.err);
        }
    }
}
