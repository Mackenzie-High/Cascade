package com.mackenziehigh.cascade.nodes.builders;

import com.mackenziehigh.cascade.CascadeNode;
import com.mackenziehigh.cascade.nodes.NodeBuilder;

/**
 *
 */
public final class RunnerBuilder
        implements NodeBuilder<RunnerBuilder>
{

    @Override
    public CascadeNode.Core build ()
    {
        return new CascadeNode.Core()
        {
        };
    }

}
