package com.mackenziehigh.cascade.actors;

import com.mackenziehigh.cascade.CascadePlant.Context;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import java.util.function.Function;
import com.mackenziehigh.cascade.CascadePlant;

/**
 *
 */
public class TransformActor
        implements CascadePlant
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
