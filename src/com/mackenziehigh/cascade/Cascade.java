package com.mackenziehigh.cascade;

import com.mackenziehigh.cascade.builder.ReactorBuilder;
import com.mackenziehigh.cascade.internal.InternalReactor;

/**
 *
 */
public final class Cascade
{
    /**
     * Factory Method.
     *
     * @return a builder that can create a new <code>Reactor</code>.
     */
    public static ReactorBuilder newReactor ()
    {
        return new InternalReactor();
    }
}
