package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeChannel;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * This is the actual implementation of the CascadeChannel interface.
 */
public final class InternalChannel
        implements CascadeChannel
{
    private final Dispatcher dispatcher;

    private final CascadeToken event;

    public InternalChannel (final Dispatcher dispatcher,
                            final CascadeToken event)
    {
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
        this.event = Objects.requireNonNull(event, "event");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeToken event ()
    {
        return event;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int subscriberCount ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<CascadeActor> subscribers ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void forEachSubscriber (final Consumer<CascadeActor> functor)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeChannel send (final CascadeToken event,
                                final CascadeStack stack)
    {
        dispatcher.send(event, stack);
        return this;
    }

}
