package com.mackenziehigh.cascade;

import com.google.common.base.Preconditions;
import com.mackenziehigh.cascade.allocators.CascadeAllocator;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * A stage contains actor(s) that are powered by a shared pool of threads.
 */
public interface CascadeStage
{
    /**
     * Getter.
     *
     * @return a universally-unique-identifier of this object.
     */
    public UUID uuid ();

    /**
     * Getter.
     *
     * @return the cascade that contains this stage.
     */
    public Cascade cascade ();

    /**
     * Getter.
     *
     * @return the threads that are currently powering this stage.
     */
    public Set<Thread> threads ();

    /**
     * Setter.
     *
     * <p>
     * In effect, this method overrides the default loggerFactory() that was
     * specified by the enclosing cascade() when this stage was created.
     * </p>
     *
     * @param logger will be used to create default loggers,
     * for any actors created after this method returns.
     * @return this.
     */
    public CascadeStage useLoggerFactory (final CascadeLogger.Factory logger);

    /**
     * Getter.
     *
     * @return the logger-factory that is currently in use herein.
     */
    public CascadeLogger.Factory loggerFactory ();

    /**
     * Setter.
     *
     * <p>
     * In effect, this method overrides the default allocator() that was
     * specified by the enclosing cascade() when this stage was created.
     * </p>
     *
     * @param allocator will be the default allocator,
     * for any actors created after this method returns.
     * @return this.
     */
    public CascadeStage useAllocator (final CascadeAllocator allocator);

    /**
     * Getter.
     *
     * @return the current default allocator for newly created actors.
     */
    public CascadeAllocator allocator ();

    /**
     * Getter.
     *
     * @return the actors that are currently alive on this stage.
     */
    public Set<CascadeActor> actors ();

    /**
     * This method creates a new actor that will execute the given
     * script and adds the actor to this stage.
     *
     * @param script describes what the actor will do.
     * @return the new actor.
     */
    public CascadeActor newActor (CascadeScript script);

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
    public default CascadeActor newActor (CascadeActor.Builder builder)
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
    public default <T extends CascadeActor.Builder> T newActor (CascadeActor.Builder.Factory<T> factory)
    {
        return factory.newBuilder(this);
    }

    /**
     * Getter.
     *
     * @return true, if and only if, this stage has not yet closed.
     */
    public boolean isActive ();

    /**
     * Getter.
     *
     * @return true, if and only if, this stage is closing.
     */
    public boolean isClosing ();

    /**
     * This method kills all the actors on the stage and removes
     * this stage from the enclosing cascade().
     */
    public void close ();

    /**
     * This method blocks, until this stage closes.
     *
     * @param timeout is the maximum amount of time to wait.
     * @param timeoutUnit describes the timeout.
     * @throws java.lang.InterruptedException
     */
    public void awaitClose (final long timeout,
                            final TimeUnit timeoutUnit)
            throws InterruptedException;

    /**
     * Getter.
     *
     * @return the time that this stage was created.
     */
    public Instant creationTime ();

    /**
     * Getter.
     *
     * @return how long this stage has been active.
     */
    public default Duration age ()
    {
        return Duration.between(creationTime(), Instant.now());
    }
}
