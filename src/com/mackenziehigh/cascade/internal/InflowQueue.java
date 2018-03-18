package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 * An inflow-queue contains a series of event-messages
 * that are flowing into an actor.
 */
public interface InflowQueue
{

    /**
     * Use this method to add an event-message to the queue.
     *
     * @param event identifies the event.
     * @param stack is the content of the message.
     * @return true, iff the message was inserted without being dropped.
     */
    public boolean offer (CascadeToken event,
                          CascadeStack stack);

    /**
     * Use this method to retrieve and remove an element from this queue.
     *
     * @param eventOut will receive the identity of the event-message.
     * @param stackOut will receive the content of the event-message.
     * @return true, iff the an event-message was available and removed.
     */
    public boolean removeOldest (AtomicReference<CascadeToken> eventOut,
                                 AtomicReference<CascadeStack> stackOut);

    /**
     * Use this method to retrieve and remove an element from this queue.
     *
     * @param eventOut will receive the identity of the event-message.
     * @param stackOut will receive the content of the event-message.
     * @return true, iff the an event-message was available and removed.
     */
    public boolean removeNewest (AtomicReference<CascadeToken> eventOut,
                                 AtomicReference<CascadeStack> stackOut);

    /**
     * Remove all of the elements that are in this queue.
     */
    public void clear ();

    /**
     * Getter.
     *
     * @return the total number of elements currently in the queue.
     */
    public int size ();

    /**
     * Getter.
     *
     * @return true, if this queue is empty.
     */
    public default boolean isEmpty ()
    {
        return size() == 0;
    }

    /**
     * Getter.
     *
     * @return the maximum number of elements that can be stored in the queue.
     */
    public int capacity ();

    /**
     * Apply the given functor to each message in this queue.
     *
     * @param functor will act on each message.
     */
    public void forEach (BiConsumer<CascadeToken, CascadeStack> functor);

}
