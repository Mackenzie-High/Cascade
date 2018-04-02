package com.mackenziehigh.cascade.internal;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadeScript;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeStage;
import com.mackenziehigh.cascade.CascadeToken;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This is the actual implementation of the CascadeStage interface.
 */
public final class InternalStage
        implements CascadeStage
{
    private final Cascade cascade;

    private final Dispatcher dispatcher;

    private final UUID uuid = UUID.randomUUID();

    private volatile CascadeLogger logger;

    private final Set<InternalActor> actors = Sets.newConcurrentHashSet();

    private final AtomicBoolean stageAlive = new AtomicBoolean(true);

    private final AtomicBoolean stageActive = new AtomicBoolean(true);

    private final AtomicBoolean stageClosing = new AtomicBoolean(false);

    private final CountDownLatch stageAwaitCloseLatch = new CountDownLatch(1);

    private final Instant creationTime = Instant.now();

    private final ThreadFactory threadFactory;

    private final Set<Thread> threads = Sets.newConcurrentHashSet();

    private final Scheduler<InternalActor> scheduler = new Scheduler<>();

    public InternalStage (final Cascade cascade,
                          final Dispatcher dispatcher,
                          final ThreadFactory factory)
    {
        this.cascade = Objects.requireNonNull(cascade, "cascade");
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
        this.logger = cascade.logger();
        this.threadFactory = Objects.requireNonNull(factory, "factory");
    }

    private void run ()
    {
        final AtomicReference<CascadeToken> event = new AtomicReference<>();
        final AtomicReference<CascadeStack> stack = new AtomicReference<>();

        while (stageAlive.get())
        {
            try
            {
                event.set(null);
                stack.set(null);
                unsafeRun(event, stack);
            }
            catch (InterruptedException ex1)
            {
                Thread.currentThread().interrupt();
            }
            catch (Throwable ex2)
            {
                // Pass
            }
        }
    }

    private void unsafeRun (final AtomicReference<CascadeToken> event,
                            final AtomicReference<CascadeStack> stack)
            throws InterruptedException
    {
        final Scheduler.Process<InternalActor> process = scheduler.poll(1000);

        if (process == null)
        {
            return;
        }

        try (Scheduler.Process<InternalActor> task = process)
        {

            final InternalActor actor = task.getUserObject();

            actor.setupIfNeeded(); // TODO: Rework, this is probably broken somehow.

            final InflowQueue queue = actor.inflowQueue();
            queue.removeOldest(event, stack);
            final boolean delivered = event.get() != null; // Not Always True (Overflow Effects)
            if (delivered)
            {
                actor.script().onMessage(actor.context(), event.get(), stack.get());
            }
        }
        catch (Throwable ex)
        {
            // TODO
            ex.printStackTrace();
        }
    }

    public Scheduler<InternalActor> scheduler ()
    {
        return scheduler;
    }

    public Dispatcher dispatcher ()
    {
        return dispatcher;
    }

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
    public Cascade cascade ()
    {
        return cascade;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Thread> threads ()
    {
        return ImmutableSet.copyOf(threads);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<CascadeActor> actors ()
    {
        return ImmutableSet.copyOf(actors);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeActor newActor (final CascadeScript script)
    {
        synchronized (this)
        {
            if (isClosed())
            {
                throw new IllegalStateException("Stage is already closed.");
            }
            else if (stageClosing.get())
            {
                throw new IllegalStateException("Stage is already closing.");
            }
            else
            {
                Objects.requireNonNull(script, "script");
                final InternalActor actor = new InternalActor(this, script);
                actors.add(actor);
                return actor;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive ()
    {
        return stageActive.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClosing ()
    {
        return stageClosing.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClosed ()
    {
        return !isActive();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Instant creationTime ()
    {
        return creationTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeStage incrementThreadCount ()
    {
        final Thread thread = threadFactory.newThread(() -> run());
        thread.start();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeStage decrementThreadCount ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeStage useLogger (final CascadeLogger logger)
    {
        this.logger = Objects.requireNonNull(logger, "logger");
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeLogger logger ()
    {
        return logger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close ()
    {
        /**
         * Prevent new actors from being created as we close them.
         * Close the existing actors.
         */
        stageClosing.set(true);
        synchronized (this)
        {
            for (InternalActor actor : actors)
            {
                actor.close();
            }
        }

        /**
         * We are now closed.
         */
        stageActive.set(false);
        stageClosing.set(false); // Closed != Closing
        stageAwaitCloseLatch.countDown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void awaitClose (final Duration timeout)
            throws InterruptedException
    {
        stageAwaitCloseLatch.await(timeout.getNano(), TimeUnit.NANOSECONDS);
    }
}
