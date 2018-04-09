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
 *
 * TODO: Much work needs done!
 */
public final class InternalChannel
        implements CascadeChannel
{
    private final SimpleDispatcher dispatcher;

    private final CascadeToken event;

    public InternalChannel (final SimpleDispatcher dispatcher,
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
    public CascadeChannel send (final CascadeStack stack)
    {
        dispatcher.send(event, stack);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode ()
    {
        return 97 * event.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals (final Object other)
    {
        return other instanceof CascadeChannel && ((CascadeChannel) other).event().equals(event);
    }

}
