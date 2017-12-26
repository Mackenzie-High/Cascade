package com.mackenziehigh.cascade.cores.builders;

import com.mackenziehigh.cascade.cores.NodeBuilder;
import com.mackenziehigh.cascade.CascadeReactor;

/**
 *
 */
public final class RunnerBuilder
        implements NodeBuilder<RunnerBuilder>
{

    @Override
    public CascadeReactor.Core build ()
    {
        return new CascadeReactor.Core()
        {
        };
    }

}
