package com.mackenziehigh.cascade.internal.powerplants;

import com.mackenziehigh.cascade.CascadePlant;
import com.mackenziehigh.cascade.CascadePipe;
import com.mackenziehigh.cascade.CascadePump;

/**
 *
 */
public interface BasePowerplant
        extends CascadePump
{
    public CascadePlant declareActor (CascadePlant actor);

    public CascadePipe connect (CascadePlant src,
                                    CascadePlant dest);

    public void start ();

    public void stop ();
}
