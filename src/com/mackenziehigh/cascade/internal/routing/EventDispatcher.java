package com.mackenziehigh.cascade.internal.routing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.internal.engines.Connection;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 */
public final class EventDispatcher
{
    /**
     * (subscriber) -> (connection)
     */
    private final ImmutableSortedMap<CascadeToken, Connection> queues;

    /**
     * (event) -> [ (connection) ]
     */
    private final Map<CascadeToken, ImmutableList<Connection>> subscriptions = Maps.newConcurrentMap();

    /**
     * (publisher) -> (sender)
     */
    private final Map<CascadeToken, ConcurrentEventSender> senders = Maps.newConcurrentMap();

    /**
     * This lock is used to ensure that registrations, de-registrations, and look-ups are synchronous.
     */
    private final Lock lock = new ReentrantLock();

    /**
     * Sole constructor.
     *
     * @param queues
     */
    public EventDispatcher (final Map<CascadeToken, Connection> queues)
    {
        this.queues = ImmutableSortedMap.copyOf(queues);
    }

    /**
     * Register a message-queue to receive messages for a given event.
     *
     * @param subscriberId will now receive messages for (eventId).
     * @param eventId identifies the event to listen for.
     */
    public void register (final CascadeToken subscriberId,
                          final CascadeToken eventId)
    {
        lock.lock();

        try
        {
            performRegister(subscriberId, eventId);
        }
        finally
        {
            lock.unlock();
        }
    }

    private void performRegister (final CascadeToken subscriberId,
                                  final CascadeToken eventId)
    {
        final Connection handler = queues.get(subscriberId);

        if (handler != null)
        {
            /**
             * Subscribe the subscriber to the event channel.
             * This is slow, but we need an immutable list during sends.
             */
            final ImmutableList<Connection> original = subscriptions.getOrDefault(eventId, ImmutableList.of());
            final ImmutableList<Connection> modified = ImmutableList.<Connection>builder().addAll(original).add(handler).build();
            subscriptions.put(eventId, modified);
        }
        else
        {
            throw new IllegalArgumentException("No Such Subscriber: " + subscriberId);
        }
    }

    /**
     * Deregister a message-queue from receiving message for a given event.
     *
     * @param subscriberId will no longer receive messages for (eventId).
     * @param eventId identifies the event to no longer listen for.
     */
    public void deregister (final CascadeToken subscriberId,
                            final CascadeToken eventId)
    {
        lock.lock();

        try
        {
            performDeregister(subscriberId, eventId);
        }
        finally
        {
            lock.unlock();
        }
    }

    private void performDeregister (final CascadeToken subscriberId,
                                    final CascadeToken eventId)
    {
        /**
         * If no one was subscribed to the event,
         * then an this should simply be a no-op.
         */
        if (subscriptions.containsKey(eventId) == false)
        {
            return;
        }

        /**
         * Unsubscribe the subscriber to the event channel.
         */
        subscriptions.remove(eventId, subscriberId);
    }

    /**
     * Use this method to lookup the special object that event-producers shall use to dispatch events.
     *
     * <p>
     * Per use-site, this method should be called once and then the result cached.
     * </p>
     *
     * @param publisherId identifies the logical entity that will dispatch-events.
     * @return the API for sending events.
     */
    public ConcurrentEventSender lookup (final CascadeToken publisherId)
    {
        lock.lock();

        try
        {
            return performLookup(publisherId);
        }
        finally
        {
            lock.unlock();
        }
    }

    private ConcurrentEventSender performLookup (final CascadeToken publisherId)
    {
        if (senders.containsKey(publisherId) == false)
        {
            senders.put(publisherId, new ConcurrentEventSender());
        }

        final ConcurrentEventSender result = senders.get(publisherId);

        return result;
    }

    /**
     * Use this API to send events.
     */
    public final class ConcurrentEventSender
            extends OrderlyAtomicSender
    {

        /**
         * Private.
         *
         * @param eventId identifies the event being sent.
         * @param out will receive the connections to the subscribers that are interested in the event.
         */
        @Override
        public void resolveConnections (final CascadeToken eventId,
                                        final ArrayList<Connection> out)
        {
            out.clear();

            final ImmutableList<Connection> connections = subscriptions.getOrDefault(eventId, ImmutableList.of());

            for (int i = 0; i < connections.size(); i++)
            {
                final Connection connection = connections.get(i);
                out.add(connection);
            }
        }
    }
}
