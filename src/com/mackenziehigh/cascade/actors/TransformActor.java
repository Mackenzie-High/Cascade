package com.mackenziehigh.cascade.actors;

import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeActor.Context;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import java.util.function.Function;

/**
 *
 */
public class TransformActor
        implements CascadeActor
{
    private Function<OperandStack, OperandStack> transform;

    public Function<OperandStack, OperandStack> getTransform ()
    {
        return transform;
    }

    public void setTransform (final Function<OperandStack, OperandStack> transform)
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
