package com.mackenziehigh.cascade.nodes.builders;

import com.mackenziehigh.cascade.CascadeNode;
import com.mackenziehigh.cascade.nodes.NodeBuilder;

/**
 * Node Builder for Forwarder nodes.
 */
public final class ForwarderBuilder
        implements NodeBuilder<ForwarderBuilder>
{

    @Override
    public CascadeNode.Core build ()
    {
        return new CascadeNode.Core()
        {
            @Override
            public void onMessage (final CascadeNode.Context context)
                    throws Throwable
            {
                context.send(context.message());
            }
        };
    }

}
