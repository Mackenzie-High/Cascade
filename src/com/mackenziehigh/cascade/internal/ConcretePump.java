package com.mackenziehigh.cascade.internal;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeNode;
import com.mackenziehigh.cascade.CascadePump;
import com.mackenziehigh.cascade.internal.pumps.Engine;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 *
 */
public final class ConcretePump
        implements CascadePump
{
    private final SharedState sharedState;

    private final String name;

    private final int minimumThreads;

    private final int maximumThreads;

    private final LazyRef<ImmutableSet<CascadeNode>> nodes;

    private final LazyRef<Engine> engine;

    public ConcretePump (final String name,
                         final SharedState sharedState,
                         final int minThreads,
                         final int maxThreads)
    {
        this.name = Objects.requireNonNull(name);
        this.sharedState = Objects.requireNonNull(sharedState);
        this.minimumThreads = minThreads;
        this.maximumThreads = maxThreads;
        this.nodes = LazyRef.create(() -> resolveNodes());
        this.engine = LazyRef.create(() -> sharedState.engines.get(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cascade cascade ()
    {
        return Objects.requireNonNull(sharedState.cascade);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name ()
    {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int minimumThreads ()
    {
        return minimumThreads;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int maximumThreads ()
    {
        return maximumThreads;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Thread> threads ()
    {
        return Collections.unmodifiableSet(engine.get().threads());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<CascadeNode> nodes ()
    {
        return nodes.get();
    }

    private ImmutableSet<CascadeNode> resolveNodes ()
    {
        final Set<CascadeNode> set = Sets.newHashSet();

        for (String nodeName : sharedState.pumpsToNodes.get(name))
        {
            final CascadeNode node = sharedState.namesToNodes.get(nodeName);
            set.add(node);
        }

        return ImmutableSet.copyOf(set);
    }
}
