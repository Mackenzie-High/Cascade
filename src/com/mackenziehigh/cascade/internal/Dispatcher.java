package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Routes event-messages to interested inflow-queues.
 */
public final class Dispatcher
{
    /**
     * This map maps the name of an event-stream to the inflow-queues
     * that are interested in that particular event-stream.
     *
     * <p>
     * This map is optimized for performance.
     * The ConcurrentHashMap uses stripped locking, so that insertions/removals
     * will have less impact on lookups that are occurring concurrently.
     * The AtomicReference enables us to atomically swap-in a replacement
     * list whenever an insertion or removal is performed, which ensures
     * that no blocking of senders must be performed to add additional
     * subscriptions to a given event-stream. Otherwise, unrelated listeners
     * that are interested in the same event-stream could cause random
     * performance problems for each other by momentarily blocking lookups
     * due to subscriptions and/or unsubscriptions being synchronized.
     * Notice that the List is never modified while in the map.
     * When the List needs modified, a copy is created, modified,
     * and then swapped-in as the replacement for the existing list.
     * </p>
     */
    private final Map<CascadeToken, AtomicReference<List<InflowQueue>>> subscriptions = new ConcurrentHashMap<>();

    /**
     * After this method returns, the given inflow-queue will
     * receive any event-messages from the given event-stream.
     *
     * <p>
     * This method is synchronized in order to prevent concurrent
     * registrations or deregistrations for the same event
     * </p>
     *
     * @param event identifies the event-stream to subscribe to.
     * @param subscriber will be subscribed to the event-stream.
     */
    public synchronized void register (final CascadeToken event,
                                       final InflowQueue subscriber)
    {
        Preconditions.checkNotNull(event, "event");
        Preconditions.checkNotNull(subscriber, "subscriber");

        if (subscriptions.containsKey(event))
        {
            final AtomicReference<List<InflowQueue>> ref = subscriptions.get(this);
            final CopyOnWriteArrayList modified = new CopyOnWriteArrayList(ref.get());
            modified.add(subscriber);
            ref.set(modified);
        }
        else
        {
            final List<InflowQueue> list = new CopyOnWriteArrayList<>();
            list.add(subscriber);
            subscriptions.put(event, new AtomicReference<>(list));
        }
    }

    /**
     * After this method returns, the given inflow-queue will no longer
     * receive any event-messages from the given event-stream.
     *
     * <p>
     * This method is a no-op, if the subscriber is not
     * actually subscribed to the given event-stream.
     * </p>
     *
     * <p>
     * This method is synchronized in order to prevent concurrent
     * registrations or deregistrations for the same event
     * </p>
     *
     * @param event identifies the event-stream to unsubscribe from.
     * @param subscriber will be unsubscribed to the event-stream.
     */
    public synchronized void deregister (final CascadeToken event,
                                         final InflowQueue subscriber)
    {
        Preconditions.checkNotNull(event, "event");
        Preconditions.checkNotNull(subscriber, "subscriber");

        if (subscriptions.containsKey(event) == false)
        {
            return;
        }

        final AtomicReference<List<InflowQueue>> ref = subscriptions.get(this);

        if (ref.get().size() == 1)
        {
            subscriptions.remove(event);
        }
        else
        {
            final CopyOnWriteArrayList modified = new CopyOnWriteArrayList(ref.get());
            modified.remove(subscriber);
            ref.set(modified);
        }
    }

    /**
     * Use this method to enqueue an event-message in all interested inflow-queues.
     *
     * <p>
     * This method is deliberately *not* synchronized.
     * The manipulated internal data-structures are themselves thread-safe.
     * </p>
     *
     * @param event identifies the event-message.
     * @param stack is the content of the event-message.
     * @return true, if any recipients are interested in the message,
     * even if the recipients drop the message upon receiving it.
     */
    public boolean send (final CascadeToken event,
                         final CascadeStack stack)
    {
        Preconditions.checkNotNull(event, "event");
        Preconditions.checkNotNull(stack, "stack");

        /**
         * These are all of the inflow-queues that are interested
         * in receiving messages from the given event, if any.
         */
        final AtomicReference<List<InflowQueue>> ref = subscriptions.get(event);

        if (ref == null)
        {
            return false;
        }

        final List<InflowQueue> list = ref.get();

        if (list == null)
        {
            return false;
        }

        /**
         * Enqueue the event-message in each of the inflow-queues.
         * Iterate using a for-loop, rather than a foreach-loop,
         * because a foreach-loop would cause an Iterator to
         * be allocated, which is an unnecessary waste here
         * given that this method requires extremely high performance.
         */
        final int length = list.size();
        for (int i = 0; i < length; i++)
        {
            final InflowQueue queue = list.get(i);
            queue.offer(event, stack);
        }

        return length > 0;
    }

}
