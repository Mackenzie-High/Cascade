package com.mackenziehigh.cascade.internal;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadePowerSource;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
public final class NopExecutor
        implements CascadePowerSource
{
    private final Set<CascadeActor> actors = Sets.newConcurrentHashSet();

    private final AtomicLong cranks = new AtomicLong();

    public Set<CascadeActor> getActors ()
    {
        return ImmutableSet.copyOf(actors);
    }

    public long getCrankCount ()
    {
        return cranks.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addActor (final CascadeActor actor,
                          final AtomicReference<?> pocket)
    {
        actors.add(actor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeActor (final CascadeActor actor,
                             final AtomicReference<?> pocket)
    {
        actors.remove(actor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void submit (final CascadeActor actor,
                        final AtomicReference<?> pocket)
    {
        cranks.incrementAndGet();
    }

}
