package com.mackenziehigh.cascade.internal;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator.AllocationPool;
import com.mackenziehigh.cascade.CascadeEdge;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadeNode;
import com.mackenziehigh.cascade.CascadePump;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
public final class Controller
        implements Cascade
{

    private final SharedState sharedState;

    private final AtomicReference<ExecutionPhase> phase = new AtomicReference<>(ExecutionPhase.INITIAL);

    private final LazyRef<CascadeLogger> defaultLogger;

    private final LazyRef<ImmutableSortedMap<String, AllocationPool>> pools;

    private final LazyRef<ImmutableSortedMap<String, CascadePump>> pumps;

    private final LazyRef<ImmutableSortedMap<String, CascadeNode>> nodes;

    private final LazyRef<ImmutableSet<CascadeEdge>> edges;

    public Controller (final SharedState sharedState)
    {
        this.sharedState = Objects.requireNonNull(sharedState);
        this.defaultLogger = LazyRef.create(() -> sharedState.defaultLogger);
        this.pools = LazyRef.create(() -> ImmutableSortedMap.copyOf(sharedState.allocator.pools())); // TODO: Remove? Only allocator instead???
        this.pumps = LazyRef.create(() -> ImmutableSortedMap.copyOf(resolvePumps()));
        this.nodes = LazyRef.create(() -> ImmutableSortedMap.copyOf(resolveNodes()));
        this.edges = LazyRef.create(() -> ImmutableSet.copyOf(resolveEdges()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeLogger defaultLogger ()
    {
        return defaultLogger.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedMap<String, AllocationPool> pools ()
    {
        return pools.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedMap<String, CascadePump> pumps ()
    {
        return pumps.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedMap<String, CascadeNode> nodes ()
    {
        return nodes.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<CascadeEdge> edges ()
    {
        return edges.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutionPhase phase ()
    {
        return phase.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cascade start ()
    {
        sharedState.engines.values().forEach(x -> x.start());
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cascade stop ()
    {
        sharedState.stop.set(true);
        return this;
    }

    private SortedMap<String, CascadePump> resolvePumps ()
    {
        final SortedMap<String, CascadePump> map = Maps.newTreeMap(String.CASE_INSENSITIVE_ORDER);

        for (String name : sharedState.nodesToPumps.values())
        {
            final CascadePump pump = sharedState.namesToPumps.get(name);
            map.put(name, pump);
        }

        return ImmutableSortedMap.copyOf(map);
    }

    private SortedMap<String, CascadeNode> resolveNodes ()
    {
        final SortedMap<String, CascadeNode> map = Maps.newTreeMap(String.CASE_INSENSITIVE_ORDER);

        for (CascadeNode x : sharedState.namesToNodes.values())
        {
            map.put(x.name(), x);
        }

        return ImmutableSortedMap.copyOf(map);
    }

    private Set<CascadeEdge> resolveEdges ()
    {
        final Set<CascadeEdge> set = Sets.newHashSet();

        for (CascadeEdge x : sharedState.nodesToInputs.values())
        {
            set.add(x);
        }

        for (CascadeEdge x : sharedState.nodesToOutputs.values())
        {
            set.add(x);
        }

        return ImmutableSet.copyOf(set);
    }

}
