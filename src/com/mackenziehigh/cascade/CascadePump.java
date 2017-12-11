package com.mackenziehigh.cascade;

import java.util.Set;

/**
 * A pump powers the execution of zero-or-more nodes.
 */
public interface CascadePump
{
    /**
     * Getter.
     *
     * @return the system that this pump is part of.
     */
    public Cascade cascade ();

    /**
     * Getter.
     *
     * @return the user-defined full-name of this pump.
     */
    public String name ();

    /**
     * Getter.
     *
     * @return the user-defined simple-name of this pump.
     */
    public String simpleName ();

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
     * @return the nodes that this pump powers.
     */
    public Set<CascadeNode> nodes ();
}
