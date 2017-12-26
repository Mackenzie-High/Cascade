package com.mackenziehigh.cascade.internal.schema;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadePump;
import com.mackenziehigh.cascade.CascadeReactor;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Collections;
import java.util.SortedMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class ConcreteCascade
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
            performStart();
        }

        return this;
    }

    @Override
    public Cascade stop ()
    {
        if (stopWasCalled.compareAndSet(false, true))
        {
            performStop();
        }

        return this;
    }

    @Override
    public String toString ()
    {
        return name.name();
    }

    private void performStart ()
    {
        final OperandStack stack = allocator.newOperandStack(); // TODO: Free

        for (ConcreteReactor reactor : reactors.values())
        {
            final ConcreteContext ctx = new ConcreteContext(reactor);
            ctx.set(null, stack, null);

            try
            {
                reactor.core().onSetup(ctx);
            }
            catch (Throwable ex)
            {
                ex.printStackTrace(System.err);
            }
        }

        pumps.values().forEach(x -> x.start());
    }

    private void performStop ()
    {

    }

}
