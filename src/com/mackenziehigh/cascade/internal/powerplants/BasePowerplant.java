package com.mackenziehigh.cascade.internal.powerplants;

import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadePipeline;
import com.mackenziehigh.cascade.CascadePowerplant;

/**
 *
 */
public interface BasePowerplant
        extends CascadePowerplant
{
    public CascadeActor declareActor (CascadeActor actor);

    public CascadePipeline connect (CascadeActor src,
                                    CascadeActor dest);

    public void start ();

    public void stop ();
}
