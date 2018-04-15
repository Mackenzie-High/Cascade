package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.CascadeExecutor;
import com.mackenziehigh.cascade.CascadeStage;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A CascadeExecutor implemented using a fixed-size thread-pool.
 */
public final class PooledExecutor
        implements CascadeExecutor
{
    /**
     * Permits will be added to this semaphore whenever an actor
     * signals this executor that the actor needs toe be executed.
     * The threads in this pool will keep trying to obtain a permit.
     * When one becomes available, the thread will power the stage.
     */
    private final Semaphore permits = new Semaphore(0);

    /**
     * These are all the threads that are in this thread-pool.
     * Each of these threads executes the run() method herein.
     */
    private final Set<Thread> threads = Sets.newConcurrentHashSet();

    /**
     * This is the stage that this executor is providing power to.
     */
    private final AtomicReference<CascadeStage> stage = new AtomicReference<>();

    private PooledExecutor (final ThreadFactory factory,
                            final int count)
    {
        Preconditions.checkNotNull(factory, "factory");
        Preconditions.checkArgument(count > 0, "count <= 0");

        for (int i = 0; i < count; i++)
        {
            final Runnable task = () -> run();
            final Thread thread = factory.newThread(task);
            threads.add(thread);
        }
    }

    private void run ()
    {
        while (true)
        {
            try
            {
                while (permits.tryAcquire(1, TimeUnit.SECONDS))
                {
                    if (stage.get() != null)
                    {
//                        stage.get().crank();
                    }
                }

                /**
                 * If the stage is dead, then this executor must die too.
                 */
                if (stage.get() != null && stage.get().isClosed())
                {
                    return;
                }
            }
            catch (InterruptedException ex)
            {
                Thread.currentThread().interrupt();
            }
            catch (Throwable ex)
            {
                // Pass
            }
        }
    }

    /**
     * Factory method.
     *
     * @param factory will be used to create the threads in this thread-pool.
     * @param count will be the number of threads in the new thread-pool.
     * @return the new thread-pool based executor.
     */
    public static PooledExecutor create (final ThreadFactory factory,
                                         final int count)
    {
        final PooledExecutor executor = new PooledExecutor(factory, count);

        for (Thread thread : executor.threads)
        {
            thread.start();
        }

        return executor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStageOpened (final CascadeStage stage)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStageClosed (final CascadeStage stage)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTask (final CascadeStage value)
    {
        Preconditions.checkArgument(stage.get() == null || stage.get().equals(value),
                                    "An executor can only be used to power one stage at a time.");
        stage.compareAndSet(null, value);
        permits.release();
    }
}
