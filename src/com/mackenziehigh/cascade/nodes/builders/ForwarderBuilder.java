package com.mackenziehigh.cascade.nodes.builders;

import com.mackenziehigh.cascade.CascadeReactor.Context;
import com.mackenziehigh.cascade.nodes.NodeBuilder;
import com.mackenziehigh.cascade.CascadeReactor;

/**
 * Node Builder for Forwarder nodes.
 */
public final class ForwarderBuilder
        implements NodeBuilder<ForwarderBuilder>
{

    @Override
    public CascadeReactor.Core build ()
    {
        return new CascadeReactor.Core()
        {
            @Override
            public void onMessage (final Context context)
                    throws Throwable
            {
                context.send(context.event(), context.message());
            }
        };
    }

}
