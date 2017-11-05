package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadePipeline;
import com.mackenziehigh.cascade.CascadePowerplant;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Concrete Implementation Of: Cascade.
 */
public final class ConcreteCascade
        implements Cascade
{
    private static final AtomicLong counter = new AtomicLong();

    private final ImmutableSortedMap<String, CascadeAllocator> allocators;

    private final ImmutableSortedMap<String, CascadePowerplant> powerplants;

    private final ImmutableSortedMap<String, CascadeActor> actors;

    private final ImmutableMap<CascadeActor, String> actorsToNames;

    private final ImmutableMap<CascadeActor, CascadePowerplant> actorsToPowerplants;

    private final ImmutableMap<CascadeActor, ImmutableSet<CascadePipeline>> actorsToInputs;

    private final ImmutableMap<CascadeActor, ImmutableSet<CascadePipeline>> actorsToOutputs;

    private final ImmutableMap<CascadeActor, CascadeLogger> actorsToLoggers;

    private final ImmutableSet<CascadePipeline> pipelines;

    private final AtomicReference<ExecutionPhase> phase = new AtomicReference<>(ExecutionPhase.INITIAL);

    private final Thread thread;

    public ConcreteCascade (final Map<String, CascadeAllocator> allocators,
                            final Map<String, CascadePowerplant> powerplants,
                            final Map<String, CascadeActor> actors,
                            final Set<CascadePipeline> pipelines)
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
    public CascadeLogger loggerOf (final CascadeActor actor)
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
    public SortedMap<String, CascadePowerplant> powerplants ()
    {
        return powerplants;
    }

    @Override
    public SortedMap<String, CascadeActor> actors ()
    {
        return actors;
    }

    @Override
    public Set<CascadePipeline> pipelines ()
    {
        return pipelines;
    }

    @Override
    public CascadePowerplant powerplantOf (final CascadeActor actor)
    {
        Preconditions.checkNotNull(actor, "actor");
        Preconditions.checkArgument(actorsToPowerplants.containsKey(actor), "Wrong Cascade Object");
        return actorsToPowerplants.get(actor);
    }

    @Override
    public String nameOf (final CascadeActor actor)
    {
        Preconditions.checkNotNull(actor, "actor");
        Preconditions.checkArgument(actorsToNames.containsKey(actor), "Wrong Cascade Object");
        return actorsToNames.get(actor);
    }

    @Override
    public Set<CascadePipeline> inputsOf (final CascadeActor actor)
    {
        Preconditions.checkNotNull(actor, "actor");
        Preconditions.checkArgument(actorsToInputs.containsKey(actor), "Wrong Cascade Object");
        return actorsToInputs.get(actor);
    }

    @Override
    public Set<CascadePipeline> outputsOf (final CascadeActor actor)
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
                    ((CascadeActor) element).onSetup(null);
                }
                else if (phase.get() == ExecutionPhase.START)
                {
                    ((CascadeActor) element).onStart(null);
                }
                else if (phase.get() == ExecutionPhase.STOP)
                {
                    ((CascadeActor) element).onStop(null);
                }
                else if (phase.get() == ExecutionPhase.DESTROY)
                {
                    ((CascadeActor) element).onDestroy(null);
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
