package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeContext;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Objects;

/**
 * This is the actual implementation of the CascadeContext interface.
 */
public final class InternalContext
        implements CascadeContext
{
    private final InternalActor actor;

    public InternalContext (final InternalActor actor)
    {
        this.actor = Objects.requireNonNull(actor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeActor actor ()
    {
        return actor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeContext send (final CascadeToken event,
                                final CascadeStack stack)
    {
        actor.send(event, stack);
        return this;
    }
}
