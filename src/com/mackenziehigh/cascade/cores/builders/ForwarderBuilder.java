package com.mackenziehigh.cascade.cores.builders;

import com.mackenziehigh.cascade.CascadeReactor;
import com.mackenziehigh.cascade.CascadeReactor.Context;

/**
 * Node Builder for Forwarder nodes.
 */
public final class ForwarderBuilder
        implements CascadeReactor.CoreBuilder
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
