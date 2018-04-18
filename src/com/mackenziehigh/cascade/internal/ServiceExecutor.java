package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.CascadeExecutor;
import com.mackenziehigh.cascade.CascadeStage;
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

    private final Set<CascadeStage> stages = Sets.newCopyOnWriteArraySet();

    public ServiceExecutor (final ExecutorService service)
    {
        this.service = Objects.requireNonNull(service, "service");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void onStageOpened (final CascadeStage stage)
    {
        Preconditions.checkNotNull(stage, "stage");
        if (closed.get() == false)
        {
            stages.add(stage);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void onStageClosed (final CascadeStage stage)
    {
        Preconditions.checkNotNull(stage, "stage");
        if (closed.get() == false)
        {
            Verify.verify(stage.actors().isEmpty());

            stages.remove(stage);

            if (stages.isEmpty())
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
    public synchronized void onTask (final CascadeStage stage)
    {
        if (closed.get() == false)
        {
            final Runnable task = () ->
            {
                stage.crank();
            };
            service.submit(task);
        }
    }

}
