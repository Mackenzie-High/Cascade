package com.mackenziehigh.cascade.internal;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeChannel;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadeStage;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.loggers.StandardIoLogger;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An instance of this class is a concurrent set of stages,
 * when each contain zero or more actors, where the actors send
 * and receive event-messages for processing using "scripts".
 */
public final class InternalCascade
        implements Cascade
{
    private final UUID uuid = UUID.randomUUID();

    private final Dispatcher dispatcher = new Dispatcher();

    private final Set<InternalStage> stages = Sets.newConcurrentHashSet();

    private volatile CascadeLogger cascadeLogger = new StandardIoLogger(this);

    private final AtomicBoolean cascadeActive = new AtomicBoolean(true);

    private final AtomicBoolean cascadeClosing = new AtomicBoolean(false);

    private final CountDownLatch cascadeAwaitCloseLatch = new CountDownLatch(1);

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID uuid ()
    {
        return uuid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeLogger logger ()
    {
        return cascadeLogger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeStage newStage ()
    {
        final ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
        builder.setDaemon(false);
        return newStage(builder.build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeStage newStage (final ThreadFactory factory)
    {
        synchronized (this)
        {
            if (isActive() == false)
            {
                throw new IllegalStateException("Cascade is already closed.");
            }
            else if (cascadeClosing.get())
            {
                throw new IllegalStateException("Cascade is already closing.");
            }
            else
            {
                final InternalStage stage = new InternalStage(this, dispatcher, factory);
                stages.add(stage);
                return stage;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive ()
    {
        return cascadeActive.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClosing ()
    {
        return cascadeClosing.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClosed ()
    {
        return !isActive() && !isClosing();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<CascadeStage> stages ()
    {
        return ImmutableSet.copyOf(stages);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close ()
    {
        /**
         * Prevent new stages from being created as we close them.
         * Close the existing stages.
         */
        cascadeClosing.set(true);
        synchronized (this)
        {
            for (InternalStage stage : stages)
            {
                stage.close();
            }
        }

        /**
         * We are now closed.
         */
        cascadeActive.set(false);
        cascadeClosing.set(false); // Closed != Closing
        cascadeAwaitCloseLatch.countDown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void awaitClose (final Duration timeout)
            throws InterruptedException
    {
        cascadeAwaitCloseLatch.await(timeout.toNanos(), TimeUnit.NANOSECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeStage newStage (CascadeStage stage)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeChannel lookup (CascadeToken event)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
