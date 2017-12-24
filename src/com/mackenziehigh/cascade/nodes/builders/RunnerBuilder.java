package com.mackenziehigh.cascade.nodes.builders;

import com.mackenziehigh.cascade.nodes.NodeBuilder;
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
