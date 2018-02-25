package com.mackenziehigh.cascade.internal;

import com.google.common.base.Verify;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.mackenziehigh.cascade.redo2.CascadeToken;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 */
public final class EventDispatcher
{
    /**
     * Empty List.
     */
    private final ImmutableList<InflowQueue> EMPTY_LIST = ImmutableList.of();

    /**
     * (subscriber) -> (queue).
     *
     * <p>
     * This map maps the name of a subscriber to the inflow-queue that
     * stores the pending event-messages flowing into that subscriber.
     * </p>
     */
    private final ImmutableSortedMap<CascadeToken, InflowQueue> subscribersToInflowQueues;

    /**
     * (event) -> [ (queue) ].
     *
     * <p>
     * This map maps the name of an event to the inflow-queues that feed
     * event-messages to the subscribers interested in that event.
     * </p>
     */
    private final Map<CascadeToken, ImmutableList<InflowQueue>> eventsToSubscriberQueues = Maps.newConcurrentMap();

    /**
     * (publisher) -> (sender).
     *
     * <p>
     * This method maps the name of a publisher to an object that can
     * be used to send event-messages in an orderly and atomic manner.
     * Whenever an event-message is sent using the sender object,
     * the sender object will resolve the list of inflow-queues
     * pertaining to the subscribers that are interested in
     * the message being sent, and then, the sender object
     * will enqueue the even-message in those inflow-queues,
     * if possible.
     * </p>
     */
    private final Map<CascadeToken, ConcurrentEventSender> senders = Maps.newConcurrentMap();

    /**
     * (event) -> [ (subscriber) ].
     *
     * <p>
     * This method maps the name of an event to the names of the subscribers
     * that are currently interested in receiving that type of event.
     * </p>
     */
    private final SetMultimap<CascadeToken, CascadeToken> eventsToSubscribers = Multimaps.synchronizedSetMultimap(HashMultimap.create());

    /**
     * (subscriber) -> [ (event) ].
     *
     * <p>
     * This method maps the name of a subscriber to the names of the events
     * that the subscriber is currently interested in receiving.
     * </p>
     */
    private final SetMultimap<CascadeToken, CascadeToken> subscribersToEvents = Multimaps.synchronizedSetMultimap(HashMultimap.create());

    /**
     * This lock is used to ensure that registrations, de-registrations, and look-ups are synchronous.
     */
    private final Lock lock = new ReentrantLock();

    /**
     * Sole constructor.
     *
     * @param queues maps subscribers to their input sources.
     */
    public EventDispatcher (final Map<CascadeToken, InflowQueue> queues)
    {
        this.subscribersToInflowQueues = ImmutableSortedMap.copyOf(queues);
    }

    /**
     * Use this method to find the names of any subscribers that
     * are currently registered to receive the given event.
     *
     * @param eventId identifies the event-channel.
     * @return the subscribes to the given event-channel.
     */
    public Set<CascadeToken> subscribersOf (final CascadeToken eventId)
    {
        return ImmutableSet.copyOf(eventsToSubscribers.get(eventId));
    }

    /**
     * Use this method to find the names of any events that
     * a subscriber with a given name is registered to receive.
     *
     * @param subscriberId identifies the subscriber.
     * @return the names of the events, if any.
     */
    public Set<CascadeToken> subscriptionsOf (final CascadeToken subscriberId)
    {
        return ImmutableSet.copyOf(subscribersToEvents.get(subscriberId));
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
        final InflowQueue handler = subscribersToInflowQueues.get(subscriberId);

        if (handler != null)
        {
            /**
             * Subscribe the subscriber to the event-channel.
             * This is slow, O(N) copy each time, but we need an immutable list during sends.
             * Given that the number of subscribers is expected to be a small number (usually);
             * therefore, the trade-off is acceptable here at this time.
             * In theory, we could use a truly functional data-structure.
             * Unfortunately, we do not have such a data-structure conveniently
             * available and adding a dependency just for this is not justifiable now.
             */
            final ImmutableList<InflowQueue> original = eventsToSubscriberQueues.getOrDefault(eventId, ImmutableList.of());
            final ImmutableList<InflowQueue> modified = ImmutableList.<InflowQueue>builder().addAll(original).add(handler).build();
            eventsToSubscriberQueues.put(eventId, modified);
            eventsToSubscribers.put(eventId, subscriberId);
            subscribersToEvents.put(subscriberId, eventId);
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
        if (eventsToSubscriberQueues.containsKey(eventId) == false)
        {
            return;
        }

        /**
         * Unsubscribe the subscriber from the event-channel.
         * This is slow, O(N) copy each time.
         * See the discussion in the registration method.
         */
        final InflowQueue removeThis = subscribersToInflowQueues.get(subscriberId);
        final List<InflowQueue> original = Lists.newArrayList(eventsToSubscriberQueues.getOrDefault(eventId, ImmutableList.of()));
        final int originalSize = original.size();
        original.removeIf(x -> x.equals(removeThis));
        final ImmutableList<InflowQueue> modified = ImmutableList.copyOf(original);
        final int modifiedSize = modified.size();
        Verify.verify(modifiedSize == (originalSize - 1));
        eventsToSubscriberQueues.put(eventId, modified);
        eventsToSubscribers.remove(eventId, subscriberId);
        subscribersToEvents.remove(subscriberId, eventId);
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
                                        final ArrayList<InflowQueue> out)
        {
            /**
             * The list is expected to be reused.
             * Make sure the list is actually clear.
             */
            out.clear();

            /**
             * Resolve the interested subscribers and add them to the output list.
             * I am deliberately using a int-based for loop here,
             * rather than a for-each loop or an addAll() method,
             * because I do not want to cause an Iterator object to be allocated.
             * Due to the frequency with which this method will be invoked,
             * that would be a lot of unnecessary garbage.
             */
            final ImmutableList<InflowQueue> connections = eventsToSubscriberQueues.getOrDefault(eventId, EMPTY_LIST);
            for (int i = 0; i < connections.size(); i++)
            {
                final InflowQueue connection = connections.get(i);
                Verify.verifyNotNull(connection);
                out.add(connection);
            }
        }
    }
}
