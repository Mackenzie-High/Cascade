package com.mackenziehigh.cascade;

import com.mackenziehigh.cascade.CascadeAllocator.AllocationPool;
import java.util.Set;
import java.util.SortedMap;

/**
 * An instance of this class is a concurrent network of nodes that pass
 * stacks of messages to one another for processing via edges (queues).
 * Each node is powered by a pump, which may be shared amongst multiple
 * nodes or dedicated to a single node.
 */
public interface Cascade
{
    /**
     * This is the current phase of execution.
     * Execution starts in the INITIAL phase.
     * Execution will progress to the TERMINATED phase.
     * Once the TERMINATED phase is reached,
     * no further phase transitions will occur.
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
     * @return an immutable map that maps the names of allocation-pools
     * to the allocation-pools themselves.
     */
    public SortedMap<String, AllocationPool> pools ();

    /**
     * Getter.
     *
     * @return an immutable map that maps the names of power-plants
     * to the power-plants themselves.
     */
    public SortedMap<String, CascadePump> pumps ();

    /**
     * Getter.
     *
     * @return an immutable map that maps the names of nodes
     * to the nodes themselves.
     */
    public SortedMap<String, CascadeNode> nodes ();

    /**
     * Getter.
     *
     * @return all of the edges between all of the nodes.
     */
    public Set<CascadeEdge> edges ();

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
     * If all nodes() have well-behaved implementations,
     * then this method will return quickly, but the stopping
     * of all threads may still be in-progress.
     * </p>
     *
     * <p>
     * If any node has a non well-behaved implementation,
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
