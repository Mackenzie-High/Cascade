package com.mackenziehigh.cascade.redo2;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mackenziehigh.cascade.internal.StandardLogger;
import com.mackenziehigh.cascade.redo2.internal.Dispatcher;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An instance of this class is a concurrent set of stages,
 * when each contain zero or more actors, where the actors send
 * and receive event-messages for processing using "scripts".
 */
public final class Cascade
{
    private final Cascade cascade = this;

    private final UUID cascadeUUID = UUID.randomUUID();

    private final Dispatcher dispatcher = new Dispatcher();

    private final Set<Stage> stages = Sets.newConcurrentHashSet();

    private volatile CascadeLogger.Factory cascadeLoggerFactory = site -> new StandardLogger(site);

    private volatile CascadeAllocator cascadeAllocator = new CascadeAllocator()
    {
        // Temp
    };

    private final AtomicBoolean cascadeActive = new AtomicBoolean(true);

    private final AtomicBoolean cascadeClosing = new AtomicBoolean(false);

    private final CountDownLatch cascadeAwaitCloseLatch = new CountDownLatch(1);

    private Cascade ()
    {
        // Pass.
    }

    /**
     * Getter.
     *
     * @return a new instance.
     */
    public static Cascade create ()
    {
        return new Cascade();
    }

    /**
     * Getter.
     *
     * @return a universally-unique-identifier of this object.
     */
    public UUID uuid ()
    {
        return cascadeUUID;
    }

    /**
     * Setter.
     *
     * @param logger will be used to create default loggers,
     * for any stages created after this method returns.
     * @return this.
     */
    public Cascade useLoggerFactory (final CascadeLogger.Factory logger)
    {
        this.cascadeLoggerFactory = Objects.requireNonNull(logger, "logger");
        return this;
    }

    /**
     * Getter.
     *
     * @return the logger-factory that is currently in use herein.
     */
    public CascadeLogger.Factory loggerFactory ()
    {
        return cascadeLoggerFactory;
    }

    /**
     * Setter.
     *
     * @param allocator will be the default allocator,
     * for any stages created after this method returns.
     * @return this.
     */
    public Cascade useAllocator (final CascadeAllocator allocator)
    {
        this.cascadeAllocator = Objects.requireNonNull(allocator, "allocator");
        return this;
    }

    /**
     * Getter.
     *
     * @return the current default allocator for newly created actors.
     */
    public CascadeAllocator allocator ()
    {
        return cascadeAllocator;
    }

    /**
     * Creates a new single-threaded stage using a non-daemon thread.
     *
     * @return the new stage.
     */
    public CascadeStage newStage ()
    {
        return newPooledStage(1);
    }

    /**
     * Creates a new single-threaded stage using the given factory.
     *
     * @param factory will provide the thread for the new stage.
     * @return the new stage.
     */
    public CascadeStage newStage (final ThreadFactory factory)
    {
        return newPooledStage(factory, 1);
    }

    /**
     * Creates a new multi-threaded stage using non-daemon thread(s).
     *
     * @param threadPoolSize is the number of threads to create.
     * @return the new stage.
     */
    public CascadeStage newPooledStage (final int threadPoolSize)
    {
        final ThreadFactoryBuilder factory = new ThreadFactoryBuilder();
        factory.setDaemon(false);
        return newPooledStage(factory.build(), threadPoolSize);
    }

    /**
     * Creates a new multi-threaded stage using non-daemon thread(s).
     *
     * @param factory will provide the thread(s) for the new stage.
     * @param threadPoolSize is the number of threads to create.
     * @return the new stage.
     */
    public CascadeStage newPooledStage (final ThreadFactory factory,
                                        final int threadPoolSize)
    {
        synchronized (cascade)
        {
            if (cascade.isActive() == false)
            {
                throw new IllegalStateException("Cascade is already closed.");
            }
            else if (cascadeClosing.get())
            {
                throw new IllegalStateException("Cascade is already closing.");
            }
            else
            {
                final Stage stage = new Stage(factory, threadPoolSize);
                stages.add(stage);
                return stage;
            }
        }
    }

    /**
     * Getter.
     *
     * @return true, if and only if, this cascade has not yet closed.
     */
    public boolean isActive ()
    {
        return cascadeActive.get();
    }

    /**
     * Getter.
     *
     * @return true, if and only if, this cascade is closing.
     */
    public boolean isClosing ()
    {
        return cascadeClosing.get();
    }

    /**
     * This method closes all of the stages and permanently terminates this cascade.
     */
    public void close ()
    {
        /**
         * Prevent new stages from being created as we close them.
         * Close the existing stages.
         */
        cascadeClosing.set(true);
        synchronized (cascade)
        {
            for (Stage stage : stages)
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
     * This method blocks, until this cascade closes.
     *
     * @param timeout is the maximum amount of time to wait.
     * @param timeoutUnit describes the timeout.
     *
     * @throws java.lang.InterruptedException
     */
    public void awaitClose (final long timeout,
                            final TimeUnit timeoutUnit)
            throws InterruptedException
    {
        cascadeAwaitCloseLatch.await(timeout, timeoutUnit);
    }

    /**
     * Getter.
     *
     * @return all of the stages currently controlled by this cascade.
     */
    public Set<CascadeStage> stages ()
    {
        return ImmutableSet.copyOf(stages);
    }

    /**
     * This method broadcasts an event-message to all interested actors.
     *
     * @param event identifies the event being produced.
     * @param stack contains the content of the message.
     * @return this.
     */
    public Cascade send (final String event,
                         final CascadeOperand stack)
    {
        return send(CascadeToken.create(event), stack);
    }

    /**
     * This method broadcasts an event-message to all interested actors.
     *
     * @param event identifies the event being produced.
     * @param stack contains the content of the message.
     * @return this.
     */
    public Cascade send (final CascadeToken event,
                         final CascadeOperand stack)
    {
        // TODO
        return this;
    }

    /**
     * This is the actual implementation of the CascadeStage interface.
     */
    private final class Stage
            implements CascadeStage
    {
        private final Stage stage = this;

        private final UUID stageUUID = UUID.randomUUID();

        private volatile CascadeLogger.Factory loggerFactory;

        private volatile CascadeAllocator allocator;

        private final Set<Actor> actors = Sets.newConcurrentHashSet();

        private final AtomicBoolean stageActive = new AtomicBoolean(true);

        private final AtomicBoolean stageClosing = new AtomicBoolean(false);

        private final CountDownLatch stageAwaitCloseLatch = new CountDownLatch(1);

        private final Instant creationTime = Instant.now();

        private final Set<Thread> threads;

        public Stage (final ThreadFactory factory,
                      final int poolSize)
        {
            this.allocator = cascade.allocator();
            this.loggerFactory = cascade.loggerFactory();

            final Set<Thread> threadsSet = new HashSet<>();

            for (int i = 0; i < poolSize; i++)
            {
                threadsSet.add(new Thread());
            }

            this.threads = ImmutableSet.copyOf(threadsSet);
        }

        @Override
        public UUID uuid ()
        {
            return stageUUID;
        }

        @Override
        public Cascade cascade ()
        {
            return cascade;
        }

        @Override
        public Set<Thread> threads ()
        {
            return isActive() ? threads : ImmutableSet.of();
        }

        @Override
        public CascadeStage useLoggerFactory (final CascadeLogger.Factory logger)
        {
            this.loggerFactory = Objects.requireNonNull(logger, "logger");
            return this;
        }

        @Override
        public CascadeLogger.Factory loggerFactory ()
        {
            return loggerFactory;
        }

        @Override
        public CascadeStage useAllocator (final CascadeAllocator allocator)
        {
            this.allocator = Objects.requireNonNull(allocator, "allocator");
            return this;
        }

        @Override
        public CascadeAllocator allocator ()
        {
            return allocator;
        }

        @Override
        public Set<CascadeActor> actors ()
        {
            return ImmutableSet.copyOf(actors);
        }

        @Override
        public CascadeActor newActor (final CascadeScript script)
        {
            synchronized (stage)
            {
                if (stage.isActive() == false)
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
                    final Actor actor = new Actor(this, script);
                    actors.add(actor);
                    return actor;
                }
            }
        }

        @Override
        public boolean isActive ()
        {
            return stageActive.get();
        }

        @Override
        public boolean isClosing ()
        {
            return stageClosing.get();
        }

        @Override
        public void close ()
        {
            /**
             * Prevent new actors from being created as we close them.
             * Close the existing actors.
             */
            stageClosing.set(true);
            synchronized (stage)
            {
                for (Actor actor : actors)
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

        @Override
        public void awaitClose (final long timeout,
                                final TimeUnit timeoutUnit)
                throws InterruptedException
        {
            stageAwaitCloseLatch.await(timeout, timeoutUnit);
        }

        @Override
        public Instant creationTime ()
        {
            return creationTime;
        }

    }

    /**
     * This is the actual implementation of the CascadeActor interface.
     */
    private final class Actor
            implements CascadeActor
    {
        private final UUID actorUUID = UUID.randomUUID();

        private final Stage stage;

        private volatile CascadeLogger logger;

        private volatile CascadeAllocator allocator;

        private final AtomicBoolean acting = new AtomicBoolean(false);

        private final AtomicBoolean alive = new AtomicBoolean(true);

        private final AtomicBoolean closing = new AtomicBoolean(false);

        private final CountDownLatch actorAwaitCloseLatch = new CountDownLatch(1);

        private final Instant creationTime = Instant.now();

        private final Context context = new Context(this);

        private final Script script;

        public Actor (final Stage stage,
                      final CascadeScript script)
        {
            this.stage = stage;
            this.logger = stage.loggerFactory().create(CascadeToken.create(actorUUID.toString()));
            this.allocator = stage.allocator();
            this.script = new Script(script);
        }

        @Override
        public UUID uuid ()
        {
            return actorUUID;
        }

        @Override
        public Cascade cascade ()
        {
            return cascade;
        }

        @Override
        public CascadeStage stage ()
        {
            return stage;
        }

        @Override
        public CascadeActor useLogger (final CascadeLogger logger)
        {
            this.logger = Objects.requireNonNull(logger, "logger");
            return this;
        }

        @Override
        public CascadeLogger logger ()
        {
            return logger;
        }

        @Override
        public CascadeActor useAllocator (final CascadeAllocator allocator)
        {
            this.allocator = Objects.requireNonNull(allocator, "allocator");
            return this;
        }

        @Override
        public CascadeAllocator allocator ()
        {
            return allocator;
        }

        @Override
        public CascadeScript script ()
        {
            return script;
        }

        @Override
        public CascadeActor useArrayInflowQueue (int capacity)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CascadeActor useGrowableArrayInflowQueue (int size,
                                                         int capacity,
                                                         int delta)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CascadeActor useLinkedInflowQueue (int capacity)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CascadeActor useOverflowPolicyDropAll ()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CascadeActor useOverflowPolicyDropOldest ()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CascadeActor useOverflowPolicyDropNewest ()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CascadeActor useOverflowPolicyDropIncoming ()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CascadeActor subscribe (CascadeToken eventId)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CascadeActor unsubscribe (CascadeToken eventId)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Set<CascadeToken> subscriptions ()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isActing ()
        {
            return acting.get();
        }

        @Override
        public boolean isAlive ()
        {
            return alive.get();
        }

        @Override
        public boolean isClosing ()
        {
            return closing.get();
        }

        @Override
        public void close ()
        {
            // TODO
            closing.set(true);
            alive.set(false);
            closing.set(false);
            acting.set(false);
        }

        @Override
        public void awaitClose (final long timeout,
                                final TimeUnit timeoutUnit)
                throws InterruptedException
        {
            actorAwaitCloseLatch.await(timeout, timeoutUnit);
        }

        @Override
        public Instant creationTime ()
        {
            return creationTime;
        }

        @Override
        public int backlogSize ()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int backlogCapacity ()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long receivedMessageCount ()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long droppedMessageCount ()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long consumedMessageCount ()
        {
            return script.consumedMessageCount.get();
        }

        @Override
        public long producedMessageCount ()
        {
            return context.producedMessageCount.get();
        }

        @Override
        public long undeliveredMessageCount ()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long unhandledExceptionCount ()
        {
            return script.unhandledExceptionCount.get();
        }

        @Override
        public CascadeActor toggleStopwatch (boolean state)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long resetStopwatch ()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Optional<Duration> elapsedTime ()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public OptionalLong timedMessageCount ()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Optional<Duration> maximumTime ()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Optional<Duration> minimumTime ()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Optional<Duration> totalTime ()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Optional<Duration> averageTime ()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CascadeActor monitorInput (CascadeToken dest)
        {
            // How do we stop monitoring????
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CascadeActor monitorOutput (CascadeToken dest)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    /**
     * This is the actual implementation of the CascadeScript interface.
     */
    private final class Script
            implements CascadeScript
    {
        public final CascadeScript delegate;

        public final AtomicLong unhandledExceptionCount = new AtomicLong();

        public final AtomicLong consumedMessageCount = new AtomicLong();

        public Script (final CascadeScript inner)
        {
            this.delegate = Objects.requireNonNull(inner);
        }

        @Override
        public void onClose (final CascadeContext ctx)
                throws Throwable
        {
            try
            {
                delegate.onClose(ctx);
            }
            catch (Throwable ex)
            {
                unhandledExceptionCount.incrementAndGet();
                throw ex;
            }
        }

        @Override
        public void onMessage (final CascadeContext ctx,
                               final CascadeToken event,
                               final CascadeOperand stack)
                throws Throwable
        {
            try
            {
                consumedMessageCount.incrementAndGet();
                delegate.onMessage(ctx, event, stack);
            }
            catch (Throwable ex)
            {
                unhandledExceptionCount.incrementAndGet();
                throw ex;
            }
        }

        @Override
        public void onSetup (final CascadeContext ctx)
                throws Throwable
        {
            try
            {
                delegate.onSetup(ctx);
            }
            catch (Throwable ex)
            {
                unhandledExceptionCount.incrementAndGet();
                throw ex;
            }
        }
    }

    /**
     * This is the actual implementation of the CascadeContext interface.
     */
    private final class Context
            implements CascadeContext
    {
        private final Actor actor;

        public final AtomicLong producedMessageCount = new AtomicLong();

        public Context (final Actor actor)
        {
            this.actor = Objects.requireNonNull(actor);
        }

        @Override
        public CascadeActor actor ()
        {
            return actor;
        }

        @Override
        public CascadeContext send (final CascadeToken event,
                                    final CascadeOperand stack)
        {
            producedMessageCount.incrementAndGet();
            cascade.send(event, stack);
            return this;
        }
    }
}
