package com.mackenziehigh.cascade;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;

/**
 * An instance of this interface is a concurrent set of stages,
 * which each contain zero-or-more actors, which send and receive
 * event-messages for processing according to their scripts.
 */
public interface Cascade
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
     * @return the current logger.
     */
    public CascadeLogger logger ();

    /**
     * Creates a new stage using a non-daemon thread.
     *
     * <p>
     * Initially, the stage will not have any threads.
     * </p>
     *
     * @return the new stage.
     */
    public CascadeStage newStage ();

    /**
     * Creates a new stage that will use the given thread-factory,
     * whenever the stage needs to create a thread.
     *
     * <p>
     * Initially, the stage will not have any threads.
     * </p>
     *
     * @param factory will provide the threads for the new stage.
     * @return the new stage.
     */
    public CascadeStage newStage (ThreadFactory factory);

    /**
     * Adds a new custom stage.
     *
     * @param stage will now be part of this cascade.
     * @return the given stage.
     */
    public CascadeStage newStage (CascadeStage stage);

    /**
     * Getter.
     *
     * @return all of the stages currently controlled by this cascade.
     */
    public Set<CascadeStage> stages ();

    /**
     * Getter.
     *
     * @return true, if and only if, this cascade is not closed or closing.
     */
    public boolean isActive ();

    /**
     * Getter.
     *
     * @return true, if and only if, this cascade is closing.
     */
    public boolean isClosing ();

    /**
     * Getter.
     *
     * @return true, if and only, this cascade is now closed.
     */
    public boolean isClosed ();

    /**
     * This method retrieves the event-channel identified by the given token.
     *
     * @param event identifies the event-channel to find.
     * @return the channel.
     */
    public CascadeChannel lookup (CascadeToken event);

    /**
     * This method closes all of the stages and permanently terminates this cascade.
     *
     * <p>
     * This method returns immediately; however, the cascade will
     * not be closed until all stages therein close.
     * </p>
     */
    public void close ();

    /**
     * This method blocks, until this cascade closes.
     *
     * <p>
     * This method does not cause the cascade to close.
     * Rather, this method merely causes the calling thread
     * to wait for the cascade to close.
     * </p>
     *
     * @param timeout is the maximum amount of time to wait.
     * @throws java.lang.InterruptedException
     */
    public void awaitClose (Duration timeout)
            throws InterruptedException;
}
