package com.mackenziehigh.cascade.nodes;

import com.mackenziehigh.cascade.CascadeNode;

/**
 *
 * @param <T>
 */
public interface NodeBuilder<T extends NodeBuilder<T>>
{
    public CascadeNode.Core build ();
}
