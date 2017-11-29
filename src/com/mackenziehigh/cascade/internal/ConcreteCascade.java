package com.mackenziehigh.cascade.internal;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeAllocator.AllocationPool;
import com.mackenziehigh.cascade.CascadeEdge;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadeNode;
import com.mackenziehigh.cascade.CascadePump;
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

    private final ImmutableSortedMap<String, CascadePump> powerplants;

    private final ImmutableSortedMap<String, CascadeNode> actors;

    private final ImmutableMap<CascadeNode, String> actorsToNames;

    private final ImmutableMap<CascadeNode, CascadePump> actorsToPowerplants;

    private final ImmutableMap<CascadeNode, ImmutableSet<CascadeEdge>> actorsToInputs;

    private final ImmutableMap<CascadeNode, ImmutableSet<CascadeEdge>> actorsToOutputs;

    private final ImmutableMap<CascadeNode, CascadeLogger> actorsToLoggers;

    private final ImmutableSet<CascadeEdge> pipelines;

    private final AtomicReference<ExecutionPhase> phase = new AtomicReference<>(ExecutionPhase.INITIAL);

    private final Thread thread;

    public ConcreteCascade (final Map<String, CascadeAllocator> allocators,
                            final Map<String, CascadePump> powerplants,
                            final Map<String, CascadeNode> actors,
                            final Set<CascadeEdge> pipelines)
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
    public SortedMap<String, AllocationPool> pools ()
    {
//        return allocators;
        return null;
    }

    @Override
    public SortedMap<String, CascadePump> pumps ()
    {
        return powerplants;
    }

    @Override
    public SortedMap<String, CascadeNode> nodes ()
    {
        return actors;
    }

    @Override
    public Set<CascadeEdge> edges ()
    {
        return pipelines;
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
                    ((CascadeNode) element).core().onSetup(null);
                }
                else if (phase.get() == ExecutionPhase.START)
                {
                    ((CascadeNode) element).core().onStart(null);
                }
                else if (phase.get() == ExecutionPhase.STOP)
                {
                    ((CascadeNode) element).core().onStop(null);
                }
                else if (phase.get() == ExecutionPhase.DESTROY)
                {
                    ((CascadeNode) element).core().onDestroy(null);
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
