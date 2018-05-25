package com.mackenziehigh.cascade.reactor;

import com.mackenziehigh.cascade.reactor.builder.ReactorBuilder;
import com.mackenziehigh.cascade.reactor.internal.InternalReactor;

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
