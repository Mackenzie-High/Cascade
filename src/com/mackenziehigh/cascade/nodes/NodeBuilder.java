package com.mackenziehigh.cascade.nodes;

import com.mackenziehigh.cascade.CascadeReactor;

/**
 *
 * @param <T>
 */
public interface NodeBuilder<T extends NodeBuilder<T>>
{
    public CascadeReactor.Core build ();
}
