package com.mackenziehigh.cascade;

import com.mackenziehigh.cascade.Allocator;
import com.mackenziehigh.cascade.Actor;
import java.util.SortedMap;

/**
 *
 */
public interface Cascade
{
    public static enum RunState
    {
        UNSTARTED,
        STARTING,
        RUNNING,
        STOPPING,
        STOPPED
    }

    public SortedMap<String, Allocator> allocators ();

    public SortedMap<String, Powerplant> powerplants ();

    public SortedMap<String, Actor> actors ();

    public RunState getRunState ();

    public Cascade start ();

    public Cascade stop ();
}
