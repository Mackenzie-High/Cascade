package com.mackenziehigh.cascade;

import java.util.Set;

/**
 *
 */
public interface EventChannel
{
    public CascadeToken name ();

    public Set<CascadeActor> subscribers ();
}
