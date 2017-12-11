package com.mackenziehigh.cascade.nodes.builders;

import com.mackenziehigh.cascade.CascadeNode;
import com.mackenziehigh.cascade.nodes.NodeBuilder;
import java.util.Arrays;

/**
 *
 */
public final class ConstantBuilder
        implements NodeBuilder<ConstantBuilder>
{
    private volatile boolean clearStack = false;

    private byte[] constant = new byte[0];

    public ConstantBuilder clearStack ()
    {
        clearStack = false;
        return this;
    }

    public ConstantBuilder pushOnly ()
    {
        clearStack = false;
        return this;
    }

    public ConstantBuilder set (final byte[] value)
    {
        this.constant = Arrays.copyOf(value, 0);
        return this;
    }

    @Override
    public CascadeNode.Core build ()
    {
        return new CascadeNode.Core()
        {
            @Override
            public void onMessage (final CascadeNode.Context context)
                    throws Throwable
            {
                if (clearStack)
                {
                    context.message().clear();
                }

                context.message().push(constant);

                context.send(context.message());
            }
        };
    }

}
