package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.CascadeNode.Context;
import com.mackenziehigh.cascade.CascadeNode.Core;
import com.mackenziehigh.cascade.internal.pumps3.Engine;

/**
 *
 */
public final class DefaultMessageConsumer
        implements Engine.MessageConsumer
{
    private final Core kernel;

    private final OperandStack stack;

    private final DerivedContext context;

    public DefaultMessageConsumer (final Context protoContext,
                                   final Core kernel,
                                   final OperandStack stack)
    {
        this.kernel = kernel;
        this.stack = stack;
        this.context = new DerivedContext(protoContext);
    }

    @Override
    public void accept (final OperandStack message)
            throws Throwable
    {
        context.message.set(stack);
        context.exception.set(null);

        try
        {
            kernel.onMessage(context);
        }
        catch (Throwable ex)
        {
            context.exception.set(ex);
            throw ex;
        }
    }

    @Override
    public void handle (final Throwable exception)
    {
        // TODO
    }

    @Override
    public int concurrentLimit ()
    {
        return kernel.concurrentCapacity();
    }

}
