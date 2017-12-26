package com.mackenziehigh.cascade.cores;

import com.mackenziehigh.cascade.CascadeReactor;

/**
 *
 * @param <T>
 */
public interface NodeBuilder<T extends NodeBuilder<T>>
{
    public CascadeReactor.Core build ();
}
