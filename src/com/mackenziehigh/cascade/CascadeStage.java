package com.mackenziehigh.cascade;

import com.google.common.base.Preconditions;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * A stage contains actor(s) that are powered by a shared pool of threads.
 */
public interface CascadeStage
{
    /**
     * Setter.
     *
     * @param name will henceforth be the name of this stage.
     * @return this.
     */
    public CascadeStage named (String name);

    /**
     * Getter.
     *
     * <p>
     * By default, the name of this stage is the string representation of the stage's UUID.
     * </p>
     *
     * @return the current name of this stage.
     */
    public String name ();

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
     * Setter.
     *
     * <p>
     * In effect, this method overrides the default logger provided by
     * the default logger() that was specified by the enclosing cascade()
     * when this actor was created.
     * </p>
     *
     * @param logger will be used as the logger() henceforth.
     * @return this.
     */
    public CascadeStage useLogger (CascadeLogger logger);

    /**
     * Getter.
     *
     * @return the current logger.
     */
    public CascadeLogger logger ();

    /**
     * Getter.
     *
     * @return the time that this stage was created.
     */
    public Instant creationTime ();

    /**
     * Getter.
     *
     * @return the threads that are currently powering this stage.
     */
    public Set<Thread> threads ();

    /**
     * Causes a new thread to be added to the thread-pool.
     *
     * @return this.
     */
    public CascadeStage incrementThreadCount ();

    /**
     * Causes an existing thread to be removed from the thread-pool.
     *
     * <p>
     * This method will return immediately.
     * This method merely informs a running thread that it needs to stop.
     * The thread itself will not stop, until it is done performing
     * any work that it is currently engaged in.
     * </p>
     *
     * @return this.
     * @throws IllegalStateException if threads() is already empty.
     */
    public CascadeStage decrementThreadCount ();

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
     * Getter.
     *
     * @return true, if and only if, this stage is closed.
     */
    public boolean isClosed ();

    /**
     * This method kills all the actors on the stage and removes
     * this stage from the enclosing cascade().
     */
    public void close ();

    /**
     * This method blocks, until this stage closes.
     *
     * @param timeout is the maximum amount of time to wait.
     * @throws java.lang.InterruptedException
     */
    public void awaitClose (final Duration timeout)
            throws InterruptedException;
}
