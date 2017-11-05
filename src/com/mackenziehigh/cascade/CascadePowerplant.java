package com.mackenziehigh.cascade;

import java.util.Set;

/**
 * A power-plant powers the execution of an actor.
 */
public interface CascadePowerplant
{
    /**
     * Getter.
     *
     * @return the system that this powerplant is part of.
     */
    public Cascade cascade ();

    /**
     * Getter.
     *
     * @return the user-defined name of this power-plant.
     */
    public String name ();

    /**
     * Getter.
     *
     * @return the minimum number of threads alive simultaneously herein.
     */
    public int minimumThreads ();

    /**
     * Getter.
     *
     * @return the maximum number of threads alive simultaneously herein.
     */
    public int maximumThreads ();

    /**
     * Getter.
     *
     * @return the threads that are currently alive herein.
     */
    public Set<Thread> threads ();

    /**
     * Getter.
     *
     * @return the actors that this power-plant powers.
     */
    public Set<CascadeActor> actors ();
}
