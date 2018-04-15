package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.CascadeExecutor;
import com.mackenziehigh.cascade.CascadeStage;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public final class ServiceExecutor
        implements CascadeExecutor
{
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
    public void onStageOpened (final CascadeStage stage)
    {
        Preconditions.checkNotNull(stage, "stage");
        stages.add(stage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStageClosed (final CascadeStage stage)
    {
        Preconditions.checkNotNull(stage, "stage");
        stages.remove(stage);

        if (stages.isEmpty())
        {
            service.shutdown();
        }
    }

    public final AtomicInteger runs = new AtomicInteger();

    public final AtomicInteger tasks = new AtomicInteger();

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTask (final CascadeStage stage)
    {
        final Runnable task = () ->
        {
            runs.incrementAndGet();
            stage.crank(Duration.ofHours(1));
        };
        tasks.incrementAndGet();
        service.submit(task);
    }

}
