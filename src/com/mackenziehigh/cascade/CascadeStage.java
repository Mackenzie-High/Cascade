package com.mackenziehigh.cascade;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.internal.Scheduler;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A stage contains actor(s) that are powered by a shared pool of threads.
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

    private final Instant creationTime = Instant.now();

    private final AtomicInteger state = new AtomicInteger();

    private final CountDownLatch stageAwaitCloseLatch = new CountDownLatch(1);

    CascadeStage (final Cascade cascade)
    {
        this.cascade = Objects.requireNonNull(cascade, "cascade");
    }

    public CascadeStage incrementThreadCount ()
    {
        return this;
    }

    public CascadeStage decrementThreadCount ()
    {
        return this;
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
     * @return the time that this stage was created.
     */
    public Instant creationTime ()
    {
        return creationTime;
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
    public CascadeActor newActor ()
    {
        final CascadeActor actor = new CascadeActor(this);
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
     * Removes a closed actor from this cascade.
     *
     * <p>
     * Do not invoke this method directly.
     * Instead, close the actor and it will remove itself.
     * </p>
     *
     * @param actor will be removed from this stage.
     * @return this.
     * @throws IllegalArgumentException if the actor is not yet fully closed.
     * @throws IllegalArgumentException if the actor is not part of this stage.
     */
    public CascadeStage removeActor (final CascadeActor actor)
    {
        Preconditions.checkNotNull(actor, "actor");
        Preconditions.checkArgument(actor.isClosed(), "The actor is not closed yet!");
        Preconditions.checkArgument(this.equals(actor.stage()), "The actor is on a different stage!");
        actors.remove(actor);
        return this;
    }

    /**
     * Schedule the given actor for execution.
     *
     * <p>
     * Do not invoke this method directly.
     * Instead, send a message to the actor and the actor will schedule itself.
     * </p>
     *
     * @param actor will perform on this stage at the next available opportunity.
     * @return this.
     */
    public CascadeStage schedule (final CascadeActor actor)
    {
        return this;
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
        stageAwaitCloseLatch.countDown();
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
        stageAwaitCloseLatch.await(timeout.getNano(), TimeUnit.NANOSECONDS);
        return this;
    }

    private void run ()
    {
        while (isClosed() == false)
        {
            try
            {
                unsafeRun();
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

    private void unsafeRun ()
            throws InterruptedException
    {
        final Scheduler.Process<CascadeActor> process = scheduler.poll(1000);

        if (process == null)
        {
            return;
        }

        try (Scheduler.Process<CascadeActor> task = process)
        {
            task.getUserObject().perform();
        }
    }
}
