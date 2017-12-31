package com.mackenziehigh.cascade;

import java.util.SortedMap;
import java.util.UUID;

/**
 * An instance of this class is a concurrent network of reactors
 * that pass stacks of event-messages to one another for processing.
 * Each reactor is powered by a pump, which may be shared amongst
 * multiple reactors or dedicated to a single reactor.
 * Each reactor contains a queue of pending event-messages
 * that are awaiting processing by the reactor.
 */
public interface Cascade
{
    /**
     * This is the current phase of execution.
     * Execution starts in the INITIAL phase.
     * Execution will progress to the TERMINATED phase.
     * Once the TERMINATED phase is reached,
     * no further phase transitions will occur.
     *
     * <p>
     * More phases may be added in the future.
     * </p>
     */
    public static enum ExecutionPhase
    {
        INITIAL,
        SETUP,
        START,
        RUN,
        STOP,
        DESTROY,
        TERMINATED,
    }

    /**
     * Getter.
     *
     * @return the name of this object.
     */
    public CascadeToken name ();

    /**
     * Getter.
     *
     * @return a UUID that uniquely identifies this object.
     */
    public UUID uuid ();

    /**
     * Getter.
     *
     * @return the allocator used by this system.
     */
    public CascadeAllocator allocator ();

    /**
     * Getter.
     *
     * @return an immutable map that maps the full-names of pumps to the pumps themselves.
     */
    public SortedMap<CascadeToken, CascadePump> pumps ();

    /**
     * Getter.
     *
     * @return an immutable map that maps the full-names of reactors to the reactors themselves.
     */
    public SortedMap<CascadeToken, CascadeReactor> reactors ();

    /**
     * Getter.
     *
     * @return the current phase of execution.
     */
    public ExecutionPhase phase ();

    /**
     * Use this method to start the execution of the system.
     *
     * <p>
     * This method does *not* block (returns immediately).
     * </p>
     *
     * <p>
     * Subsequent invocations of this method are no-ops.
     * </p>
     *
     * @return this.
     */
    public Cascade start ();

    /**
     * Use this method to stop the execution of the system.
     *
     * <p>
     * This method does *not* block (returns immediately).
     * </p>
     *
     * <p>
     * Subsequent invocations of this method are no-ops.
     * </p>
     *
     * <p>
     * An arbitrary amount of time may be needed in order to stop.
     * </p>
     *
     * <p>
     * If any reactor has a non well-behaved implementation,
     * such as creating threads that do not get notified
     * of stop requests, then it may not be possible to
     * entirely clean up the system. Thus, this method
     * should generally not be considered a *guaranteed*
     * way to stop the system.
     * </p>
     *
     * @return this.
     */
    public Cascade stop ();
}
