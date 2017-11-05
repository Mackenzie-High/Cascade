package com.mackenziehigh.cascade.actors;

import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeActor.Context;
import com.mackenziehigh.cascade.CascadePtr;
import java.util.function.Function;

/**
 *
 */
public class TransformActor
        implements CascadeActor
{
    private Function<CascadePtr, CascadePtr> transform;

    public Function<CascadePtr, CascadePtr> getTransform ()
    {
        return transform;
    }

    public void setTransform (final Function<CascadePtr, CascadePtr> transform)
    {
        this.transform = transform;
    }

    @Override
    public void onMessage (Context ctx)
            throws Throwable
    {
        ctx.send(transform.apply(ctx.message()));
    }

}
