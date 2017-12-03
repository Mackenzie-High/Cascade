package com.mackenziehigh.cascade.internal;

import com.google.common.collect.ImmutableSet;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeNode;
import com.mackenziehigh.cascade.CascadePump;
import com.mackenziehigh.cascade.internal.pumps3.Engine;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 *
 */
public final class ConcretePump
        implements CascadePump
{
    private final Kernel kernel;

    private final String name;

    private final Engine engine;

    private final int minimumThreads;

    private final int maximumThreads;

    private final LazyRef<ImmutableSet<CascadeNode>> nodes;

    public ConcretePump (final String name,
                         final Kernel kernel,
                         final Engine engine,
                         final int minThreads,
                         final int maxThreads)
    {
        this.name = Objects.requireNonNull(name);
        this.kernel = Objects.requireNonNull(kernel);
        this.engine = Objects.requireNonNull(engine);
        this.minimumThreads = minThreads;
        this.maximumThreads = maxThreads;
        this.nodes = LazyRef.create(() -> ImmutableSet.copyOf(kernel.pumpsToNodes.get(this)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cascade cascade ()
    {
        return Objects.requireNonNull(kernel.cascade);
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
        return Collections.unmodifiableSet(engine.threads());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<CascadeNode> nodes ()
    {
        return nodes.get();
    }
}
