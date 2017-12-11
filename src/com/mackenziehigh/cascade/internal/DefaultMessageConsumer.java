package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.CascadeNode.Context;
import com.mackenziehigh.cascade.internal.pumps.Engine;
import com.mackenziehigh.cascade.CascadeNode.Core;

/**
 *
 */
public final class DefaultMessageConsumer
        implements Engine.MessageConsumer
{
    private final Core kernel;

    private final DerivedContext context;

    public DefaultMessageConsumer (final Context protoContext,
                                   final Core kernel)
    {
        this.kernel = kernel;
        this.context = new DerivedContext(protoContext);
    }

    @Override
    public void accept (final OperandStack message)
            throws Throwable
    {
        context.message.set(message);
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
