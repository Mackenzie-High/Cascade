package com.mackenziehigh.cascade;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Set;
import java.util.SortedMap;

/**
 *
 */
public final class CascadeDispatcher
{

    /**
     * Sole constructor.
     */
    private CascadeDispatcher ()
    {
        // Pass
    }

    /**
     * Create a new dispatcher with default configuration settings.
     *
     * @return the new dispatcher.
     */
    public static CascadeDispatcher newDispatcher ()
    {
        return null;
    }

    /**
     * This method causes the given actor to begin receiving messages from the given event.
     *
     * @param event identifies the event to listen for.
     * @param actor will receive the corresponding event-messages.
     * @return this.
     */
    public CascadeDispatcher subscribe (final CascadeToken event,
                                        final CascadeActor actor)
    {
        return this;
    }

    /**
     * This method causes given the actor to stop receiving messages from the given event.
     *
     * <p>
     * If the actor is not currently subscribed to the given event,
     * then this method is simply a no-op.
     * </p>
     *
     * @param event identifies the event to no longer listen for.
     * @param actor will no longer receive the corresponding event-messages.
     * @return this.
     */
    public CascadeDispatcher unsubscribe (final CascadeToken event,
                                          final CascadeActor actor)
    {
        return this;
    }

    public Set<CascadeActor> subscribers ()
    {
        return ImmutableSet.of();
    }

    public Set<CascadeChannel> subscriptionsOf (CascadeActor actor)
    {
        return ImmutableSet.of();
    }

    /**
     * This method retrieves the event-channel identified by the given token.
     *
     * @param event identifies the event-channel to find.
     * @return the channel.
     */
    public CascadeChannel lookup (CascadeToken event)
    {
        return null;
    }

    /**
     * Getter.
     *
     * @return an immutable map containing all the event-channels herein.
     */
    public SortedMap<CascadeToken, CascadeChannel> channels ()
    {
        return ImmutableSortedMap.of();
    }
}
