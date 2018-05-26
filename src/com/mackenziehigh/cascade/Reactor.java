package com.mackenziehigh.cascade;

import com.mackenziehigh.cascade.Reaction;
import com.mackenziehigh.cascade.Powerplant;
import com.mackenziehigh.cascade.Output;
import com.mackenziehigh.cascade.Input;
import java.util.SortedMap;
import java.util.UUID;

/**
 *
 */
public interface Reactor
{
    public UUID uuid ();

    public String name ();

    public SortedMap<String, Input<?>> inputs ();

    public SortedMap<String, Output<?>> outputs ();

    public SortedMap<String, Reaction> reactions ();

    public Reactor start ();

    public Reactor stop ();

    public boolean isUnstarted ();

    public boolean isStarting ();

    public boolean isStarted ();

    public boolean isStopping ();

    public boolean isStopped ();

    public boolean isAlive ();

    public boolean isReacting ();

    public Powerplant executor ();

    public Reactor ping ();

    public boolean crank ();

}
