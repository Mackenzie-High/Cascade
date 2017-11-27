package com.mackenziehigh.cascade.internal.pumps;

import com.mackenziehigh.cascade.CascadeEdge;
import com.mackenziehigh.cascade.CascadeNode;
import com.mackenziehigh.cascade.CascadePump;

/**
 *
 */
public interface BasePowerplant
        extends CascadePump
{
    public CascadeNode declareActor (CascadeNode actor);

    public CascadeEdge connect (CascadeNode src,
                                    CascadeNode dest);

    public void start ();

    public void stop ();
}
