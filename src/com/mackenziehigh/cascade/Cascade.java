package com.mackenziehigh.cascade;

import java.util.Set;
import java.util.SortedMap;

/**
 * An instance of this class is a concurrent system of actors
 * that pass stacks of messages to one another for processing.
 * Each actor is powered by a powerplant, which may be shared
 * amongst multiple actors or dedicated to a single actor.
 */
public interface Cascade
{
    /**
     * This is the current phase of execution.
     * Execution starts in the INITIAL phase.
     * Execution will progress to the TERMINATED phase.
     */
    public static enum ExecutionPhase
    {
        INITIAL,
        SETUP,
        START,
        RUN,
        STOP,
        DESTROY,
        CLOSE,
        TERMINATED,
    }

    /**
     * Getter.
     *
     * @return the logger for use, if no other logger is more appropriate.
     */
    public CascadeLogger defaultLogger ();

    /**
     * Getter.
     *
     * @param actor owns the logger.
     * @return the logger intended for-use by the given actor.
     */
    public CascadeLogger loggerOf (CascadePlant actor);

    /**
     * Getter.
     *
     * @return an immutable map that maps the names of allocators
     * to the allocators themselves.
     */
    public SortedMap<String, CascadeAllocator> allocators ();

    /**
     * Getter.
     *
     * @return an immutable map that maps the names of power-plants
     * to the power-plants themselves.
     */
    public SortedMap<String, CascadePump> powerplants ();

    /**
     * Getter.
     *
     * @return an immutable map that maps the names of actors
     * to the actors themselves.
     */
    public SortedMap<String, CascadePlant> actors ();

    /**
     * Getter.
     *
     * @return all of the pipelines between the actors.
     */
    public Set<CascadePipe> pipelines ();

    /**
     * Getter.
     *
     * @param actor is powered by the requested power-plant.
     * @return the power-planet that powers the given actor.
     */
    public CascadePump powerplantOf (CascadePlant actor);

    /**
     * Getter.
     *
     * @param actor is the actor whose name is sought.
     * @return the name of the given actor.
     */
    public String nameOf (CascadePlant actor);

    /**
     * Getter.
     *
     * @param actor the the consumer-side actor.
     * @return the supply-side pipelines.
     */
    public Set<CascadePipe> inputsOf (CascadePlant actor);

    /**
     * Getter.
     *
     * @param actor the the supply-side actor.
     * @return the consumer-side pipelines.
     */
    public Set<CascadePipe> outputsOf (CascadePlant actor);

    /**
     * Getter.
     *
     * @return the current phase of execution.
     */
    public ExecutionPhase phase ();

    /**
     * Use this method to start the execution of the system.
     *
     * @return this.
     */
    public Cascade start ();

    /**
     * Use this method to stop the execution of the system.
     *
     * <p>
     * An arbitrary amount of time may be needed in order to stop.
     * If all actors() have well-behaved implementations,
     * then this method will return quickly,
     * but the stopping of all threads may still be in-progress.
     * </p>
     *
     * @return this.
     */
    public Cascade stop ();
}
