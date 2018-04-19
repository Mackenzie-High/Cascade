package com.mackenziehigh.cascade;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.internal.Dispatcher;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * A stage contains actor(s) that are powered by an underlying executor.
 */
public final class CascadeStage
{
    private static final int ACTIVE = 0;

    private static final int CLOSING = 1;

    private static final int CLOSED = 2;

    private final Cascade cascade;

    private final UUID uuid = UUID.randomUUID();

    private volatile String name = uuid.toString();

    private final Set<CascadeActor> actors = Sets.newConcurrentHashSet();

    private final CascadeExecutor executor;

    private final AtomicInteger state = new AtomicInteger();

    private final CountDownLatch stageAwaitCloseLatch = new CountDownLatch(1);

    private final Dispatcher dispatcher;

    private final Consumer<CascadeStage> undertaker;

    CascadeStage (final Cascade cascade,
                  final Dispatcher dispatcher,
                  final CascadeExecutor executor,
                  final Consumer<CascadeStage> undertaker)
    {
        this.cascade = Objects.requireNonNull(cascade, "cascade");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
        this.undertaker = Objects.requireNonNull(undertaker, "undertaker");

    }

    public final AtomicInteger cranks = new AtomicInteger();

    /**
     * Getter.
     *
     * @return the executor that powers this stage.
     */
    public CascadeExecutor executor ()
    {
        return executor;
    }

    /**
     * Setter.
     *
     * @param name will henceforth be the name of this stage.
     * @return this.
     */
    public CascadeStage named (final String name)
    {
        this.name = Objects.requireNonNull(name, "name");
        return this;
    }

    /**
     * Getter.
     *
     * <p>
     * By default, the name of this stage is the string representation of the stage's UUID.
     * </p>
     *
     * @return the current name of this stage.
     */
    public String name ()
    {
        return name;
    }

    /**
     * Getter.
     *
     * @return a universally-unique-identifier of this object.
     */
    public UUID uuid ()
    {
        return uuid;
    }

    /**
     * Getter.
     *
     * @return the cascade that contains this stage.
     */
    public Cascade cascade ()
    {
        return cascade;
    }

    /**
     * Getter.
     *
     * @return the actors that are currently alive on this stage.
     */
    public Set<CascadeActor> actors ()
    {
        return ImmutableSet.copyOf(actors);
    }

    /**
     * This method creates a new actor with no behavior defined.
     *
     * @return the new actor.
     */
    public synchronized CascadeActor newActor ()
    {
        if (isClosing())
        {
            throw new IllegalStateException("Already Closing!");
        }

        /**
         * This is the new actor itself, which will invoke the callback.
         */
        final CascadeActor actor = new CascadeActor(this,
                                                    dispatcher,
                                                    executor,
                                                    x -> removeActor(x));
        actors.add(actor);
        return actor;
    }

    /**
     * This method creates a new actor using the given builder.
     *
     * <p>
     * The builder must create an actor that is on this stage.
     * Thus, directly or indirectly, the builder actually calls
     * the <code>newActor(CascadeScript)</code> overload
     * of this method and returns the resultant actor.
     * In short, the builder just provides a nice way
     * of being able to configure the new actor,
     * usually via the use of a fluent API.
     * </p>
     *
     * @param builder will create the new actor.
     * @return the new actor.
     */
    public CascadeActor newActor (CascadeActor.Builder builder)
    {
        final CascadeActor actor = builder.build();
        Preconditions.checkState(this.equals(actor.stage()), "Wrong Stage");
        return actor;
    }

    /**
     * This method creates a new builder that will later
     * create a new actor on this stage.
     *
     * <p>
     * This method is just syntactic sugar to aid fluent APIs.
     * </p>
     *
     * @param <T> is the type of builder that will be returned.
     * @param factory will produce the new builder.
     * @return the new builder.
     */
    public <T extends CascadeActor.Builder> T newActor (CascadeActor.Builder.Factory<T> factory)
    {
        return factory.newBuilder(this);
    }

    /**
     * Getter.
     *
     * @return true, if and only if, this stage is not closing or closed.
     */
    public boolean isActive ()
    {
        return state.get() == ACTIVE;
    }

    /**
     * Getter.
     *
     * @return true, if and only if, this stage is closing.
     */
    public boolean isClosing ()
    {
        return state.get() == CLOSING;
    }

    /**
     * Getter.
     *
     * @return true, if and only if, this stage is closed.
     */
    public boolean isClosed ()
    {
        return state.get() == CLOSED;
    }

    /**
     * This method kills all the actors on the stage and removes
     * this stage from the enclosing cascade().
     *
     * @return this.
     */
    public CascadeStage close ()
    {
        state.set(CLOSING);
        for (CascadeActor actor : actors)
        {
            actor.close();
        }
        return this;
    }

    /**
     * This method blocks, until this stage closes.
     *
     * @param timeout is the maximum amount of time to wait.
     * @return this.
     * @throws java.lang.InterruptedException
     */
    public CascadeStage awaitClose (final Duration timeout)
            throws InterruptedException
    {
        stageAwaitCloseLatch.await(timeout.toNanos(), TimeUnit.NANOSECONDS);
        return this;
    }

    private synchronized void removeActor (final CascadeActor actor)
    {
        actors.remove(actor);

        if (actors.isEmpty() && isClosing())
        {
            state.set(CLOSED);
            undertaker.accept(this);
            stageAwaitCloseLatch.countDown();
        }
    }
}
