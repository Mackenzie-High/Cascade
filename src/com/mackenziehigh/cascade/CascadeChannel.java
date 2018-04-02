package com.mackenziehigh.cascade;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Event-Channel.
 */
public interface CascadeChannel
{
    /**
     * Retrieve the name of events that are sent via this channel.
     *
     * @return identifies this event-channel.
     */
    public CascadeToken event ();

    /**
     * Getter.
     *
     * @return the number of subscribers hereto.
     */
    public int subscriberCount ();

    /**
     * Retrieve the actors that have subscribed to events sent via this channel.
     *
     * @return the subscribers hereto, as an immutable Set.
     */
    public Set<CascadeActor> subscribers ();

    /**
     * This method will pass each subscriber to the given consumer.
     *
     * <p>
     * This method is more efficient than the subscribers() method,
     * as it does not require the allocation of a new Set object.
     * </p>
     *
     * @param functor will receive the subscribers.
     */
    public void forEachSubscriber (Consumer<CascadeActor> functor);

    /**
     * This method broadcasts an event-message to all interested actors.
     *
     * <p>
     * This method is a no-op, if no actors are subscribed to the given event.
     * </p>
     *
     * @param event identifies the event being produced.
     * @param stack contains the content of the message.
     * @return this.
     */
    public CascadeChannel send (CascadeToken event,
                                CascadeStack stack);

    /**
     * Two channels are equal, if they have equal event() objects.
     *
     * @param other may equal this object.
     * @return true, if this object equals other.
     */
    @Override
    public boolean equals (Object other);

    /**
     * Compute a hash-code based on the checksum of the event().
     *
     * @return the hash-code.
     */
    @Override
    public int hashCode ();
}
