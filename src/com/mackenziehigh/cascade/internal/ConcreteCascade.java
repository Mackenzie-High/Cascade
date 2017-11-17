package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeLogger;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import com.mackenziehigh.cascade.CascadePlant;
import com.mackenziehigh.cascade.CascadePipe;
import com.mackenziehigh.cascade.CascadePump;

/**
 * Concrete Implementation Of: Cascade.
 */
public final class ConcreteCascade
        implements Cascade
{
    private static final AtomicLong counter = new AtomicLong();

    private final ImmutableSortedMap<String, CascadeAllocator> allocators;

    private final ImmutableSortedMap<String, CascadePump> powerplants;

    private final ImmutableSortedMap<String, CascadePlant> actors;

    private final ImmutableMap<CascadePlant, String> actorsToNames;

    private final ImmutableMap<CascadePlant, CascadePump> actorsToPowerplants;

    private final ImmutableMap<CascadePlant, ImmutableSet<CascadePipe>> actorsToInputs;

    private final ImmutableMap<CascadePlant, ImmutableSet<CascadePipe>> actorsToOutputs;

    private final ImmutableMap<CascadePlant, CascadeLogger> actorsToLoggers;

    private final ImmutableSet<CascadePipe> pipelines;

    private final AtomicReference<ExecutionPhase> phase = new AtomicReference<>(ExecutionPhase.INITIAL);

    private final Thread thread;

    public ConcreteCascade (final Map<String, CascadeAllocator> allocators,
                            final Map<String, CascadePump> powerplants,
                            final Map<String, CascadePlant> actors,
                            final Set<CascadePipe> pipelines)
    {
        this.allocators = ImmutableSortedMap.copyOf(allocators);
        this.powerplants = ImmutableSortedMap.copyOf(powerplants);
        this.actors = ImmutableSortedMap.copyOf(actors);
        this.pipelines = ImmutableSet.copyOf(pipelines);
        this.actorsToNames = ImmutableMap.copyOf(HashBiMap.create(actors).inverse());
        this.actorsToPowerplants = null;
        this.actorsToInputs = null;
        this.actorsToOutputs = null;
        this.actorsToLoggers = null;

        this.thread = new Thread(() -> run());
        this.thread.setName("CascadeThread_" + counter.incrementAndGet());
        this.thread.setDaemon(true);
    }

    @Override
    public CascadeLogger defaultLogger ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CascadeLogger loggerOf (final CascadePlant actor)
    {
        Preconditions.checkNotNull(actor, "actor");
        Preconditions.checkArgument(actorsToPowerplants.containsKey(actor), "Wrong Cascade Object");
        return actorsToLoggers.get(actor);
    }

    @Override
    public SortedMap<String, CascadeAllocator> allocators ()
    {
        return allocators;
    }

    @Override
    public SortedMap<String, CascadePump> powerplants ()
    {
        return powerplants;
    }

    @Override
    public SortedMap<String, CascadePlant> actors ()
    {
        return actors;
    }

    @Override
    public Set<CascadePipe> pipelines ()
    {
        return pipelines;
    }

    @Override
    public CascadePump powerplantOf (final CascadePlant actor)
    {
        Preconditions.checkNotNull(actor, "actor");
        Preconditions.checkArgument(actorsToPowerplants.containsKey(actor), "Wrong Cascade Object");
        return actorsToPowerplants.get(actor);
    }

    @Override
    public String nameOf (final CascadePlant actor)
    {
        Preconditions.checkNotNull(actor, "actor");
        Preconditions.checkArgument(actorsToNames.containsKey(actor), "Wrong Cascade Object");
        return actorsToNames.get(actor);
    }

    @Override
    public Set<CascadePipe> inputsOf (final CascadePlant actor)
    {
        Preconditions.checkNotNull(actor, "actor");
        Preconditions.checkArgument(actorsToInputs.containsKey(actor), "Wrong Cascade Object");
        return actorsToInputs.get(actor);
    }

    @Override
    public Set<CascadePipe> outputsOf (final CascadePlant actor)
    {
        Preconditions.checkNotNull(actor, "actor");
        Preconditions.checkArgument(actorsToOutputs.containsKey(actor), "Wrong Cascade Object");
        return actorsToOutputs.get(actor);
    }

    @Override
    public ExecutionPhase phase ()
    {
        return phase.get();
    }

    @Override
    public synchronized Cascade start ()
    {
        if (phase.get() == ExecutionPhase.INITIAL)
        {
            phase.set(ExecutionPhase.SETUP);
            thread.start();
        }

        return this;
    }

    @Override
    public synchronized Cascade stop ()
    {
        if (phase.get() == ExecutionPhase.RUN)
        {
            phase.set(ExecutionPhase.STOP);
            thread.start();
        }

        return this;
    }

    private void run ()
    {
        final BlockingQueue<Object> queue = new LinkedBlockingQueue<>();

        while (phase.get() == ExecutionPhase.TERMINATED == false)
        {
            try
            {
                final Object element = queue.poll(1, TimeUnit.SECONDS);

                if (element == null)
                {
                    // Pass
                }
                else if (phase.get() == ExecutionPhase.SETUP)
                {
                    ((CascadePlant) element).onSetup(null);
                }
                else if (phase.get() == ExecutionPhase.START)
                {
                    ((CascadePlant) element).onStart(null);
                }
                else if (phase.get() == ExecutionPhase.STOP)
                {
                    ((CascadePlant) element).onStop(null);
                }
                else if (phase.get() == ExecutionPhase.DESTROY)
                {
                    ((CascadePlant) element).onDestroy(null);
                }
                else if (phase.get() == ExecutionPhase.CLOSE)
                {

                }
            }
            catch (Throwable ex1)
            {
                try
                {
                    defaultLogger().error(ex1);
                }
                catch (Throwable ex2)
                {
                    // Pass
                }
            }
        }
    }
}
