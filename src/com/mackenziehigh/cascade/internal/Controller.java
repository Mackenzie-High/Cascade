package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator.AllocationPool;
import com.mackenziehigh.cascade.CascadeEdge;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadeNode;
import com.mackenziehigh.cascade.CascadePump;
import java.util.Set;
import java.util.SortedMap;

/**
 *
 * @author mackenzie
 */
public final class Controller
        implements Cascade
{

    @Override
    public CascadeLogger defaultLogger ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SortedMap<String, AllocationPool> pools ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SortedMap<String, CascadePump> pumps ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SortedMap<String, CascadeNode> nodes ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<CascadeEdge> edges ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ExecutionPhase phase ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Cascade start ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Cascade stop ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
