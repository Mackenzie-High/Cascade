package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.CascadeNode;
import java.util.Objects;

/**
 *
 */
public final class ConcreteNode
        implements CascadeNode
{
    private final ProtoContext context;

    private final Core core;

    public ConcreteNode (final String name,
                         final SharedState sharedState,
                         final Core core)
    {
        this.context = new ProtoContext(name, sharedState);
        this.core = Objects.requireNonNull(core, "core");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context protoContext ()
    {
        return context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Core core ()
    {
        return core;
    }

}
