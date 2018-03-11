package com.mackenziehigh.cascade.old;

import com.mackenziehigh.cascade.old.Cascade;
import com.mackenziehigh.cascade.CascadeToken;
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
    public CascadeToken name ();

    /**
     * Getter.
     *
     * @return the threads that are currently alive herein.
     */
    public Set<Thread> threads ();

    /**
     * Getter.
     *
     * @return the reactors that this pump powers.
     */
    public Set<CascadeReactor> reactors ();
}
