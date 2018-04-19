package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeExecutor;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public final class ServiceExecutor
        implements CascadeExecutor
{
    private final AtomicBoolean closed = new AtomicBoolean();

    private final ExecutorService service;

    private final Set<CascadeActor> actors = Sets.newCopyOnWriteArraySet();

    public ServiceExecutor (final ExecutorService service)
    {
        this.service = Objects.requireNonNull(service, "service");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void onActorOpened (final CascadeActor actor)
    {
        Preconditions.checkNotNull(actor, "actor");
        if (closed.get() == false)
        {
            actors.add(actor);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void onActorClosed (final CascadeActor actor)
    {
        Preconditions.checkNotNull(actor, "actor");
        if (closed.get() == false)
        {
            actors.remove(actor);

            if (actors.isEmpty())
            {
                closed.set(true);
                service.shutdown();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void onTask (final CascadeActor actor)
    {
        if (closed.get() == false)
        {
            final Runnable task = () ->
            {
                actor.crank();
            };
            service.submit(task);
        }
    }

}
