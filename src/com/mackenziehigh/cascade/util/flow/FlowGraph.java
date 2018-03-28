package com.mackenziehigh.cascade.util.flow;

import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.function.Function;

/**
 *
 * @author mackenzie
 */
public class FlowGraph
{
    public Straight streamIn (final CascadeToken event)
    {
        return null;
    }

    public Straight streamOut (final CascadeToken event)
    {
        return null;
    }

    public Straight newStraight ()
    {
        return null;
    }

    public Fanout newFanout ()
    {
        return null;
    }

    public Splitter newLoadBalancer ()
    {
        return null;
    }

    public Splitter newSorter (final Function<CascadeStack, Integer> router)
    {
        return null;
    }
}
