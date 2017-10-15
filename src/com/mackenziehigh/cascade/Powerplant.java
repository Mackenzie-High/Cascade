package com.mackenziehigh.cascade;

import com.mackenziehigh.cascade.Pipeline;
import com.mackenziehigh.cascade.MessageStack;
import com.mackenziehigh.cascade.Actor;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public interface Powerplant
{
    public String name ();

    public int minimumThreads ();

    public int maximumThreads ();

    public Set<Thread> threads ();

    public Set<Actor> actors ();

    public Map<Actor, Set<Pipeline>> inflow ();

    /**
     *
     *
     * @param actor
     * @param message
     * @return true, iff the message was successfully sent
     * to *all* of the relevant actors.
     */
    public boolean send (Actor actor,
                         MessageStack message);
}
