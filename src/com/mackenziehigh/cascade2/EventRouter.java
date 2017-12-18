package com.mackenziehigh.cascade2;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;

/**
 * This class provides the logic needed to route
 * events from publishers to subscribers.
 *
 * <p>
 * Conceptually, the publishers and subscribers are nodes in a graph.
 * If a node publishes event(s) (X) and another node subscribes to (X),
 * then there exists a (single) edge between the two nodes.
 * </p>
 *
 * @param <E> is the type of the edge-related meta-data.
 */
public final class EventRouter<E>
{

    private final Map<Token, Node> publishersToNodes = Maps.newConcurrentMap();

    private final Map<Token, Node> subscribersToNodes = Maps.newConcurrentMap();

    private final SetMultimap<Token, Node> eventsToPublisherNodes = Multimaps.newSetMultimap(new ConcurrentHashMap(), () -> Sets.newConcurrentHashSet());

    private final SetMultimap<Token, Node> eventsToSubscriberNodes = Multimaps.newSetMultimap(new ConcurrentHashMap(), () -> Sets.newConcurrentHashSet());

    private final MutableNetwork<Node, Edge> network = NetworkBuilder
            .directed()
            .allowsSelfLoops(false)
            .allowsParallelEdges(false).build();

    /**
     * (producer) -> ((event) -> [ (edge) + ])
     */
    private final Map<Token, Map<Token, List<Edge>>> routes = new ConcurrentHashMap<>();

    private final BiFunction<Token, Token, E> edgeSupplier;

    private final Lock lock = new ReentrantLock(false);

    /**
     * Sole Constructor.
     *
     * @param edgeSupplier
     */
    public EventRouter (final BiFunction<Token, Token, E> edgeSupplier)
    {
        this.edgeSupplier = edgeSupplier;
    }

    public void addPublisher (final Token publisherId,
                              final Token eventId)
    {
        lock.lock();

        try
        {
            final Node node;

            if (publishersToNodes.containsKey(publisherId))
            {
                node = publishersToNodes.get(publisherId);
            }
            else
            {
                node = new Node(publisherId, true);
                publishersToNodes.put(publisherId, node);
                network.addNode(node);
            }

            node.publishedEvents.add(eventId);
            eventsToPublisherNodes.put(eventId, node);
            updateConnections(eventId);
        }
        finally
        {
            lock.unlock();
        }
    }

    public void addSubscriber (final Token subscriberId,
                               final Token eventId)
    {
        lock.lock();

        try
        {
            final Node node;

            if (subscribersToNodes.containsKey(subscriberId))
            {
                node = subscribersToNodes.get(subscriberId);
            }
            else
            {
                node = new Node(subscriberId, false);
                subscribersToNodes.put(subscriberId, node);
                network.addNode(node);
            }

            node.subscribedEvents.add(eventId);
            eventsToSubscriberNodes.put(eventId, node);
            updateConnections(eventId);
        }
        finally
        {
            lock.unlock();
        }
    }

    public void removePublisher (final Token publisherId,
                                 final Token eventId)
    {
        lock.lock();

        try
        {

        }
        finally
        {
            lock.unlock();
        }
    }

    public void removeSubscriber (final Token subscriberId,
                                  final Token eventId)
    {
        lock.lock();

        try
        {

        }
        finally
        {
            lock.unlock();
        }
    }

    public List<Edge> resolve (final Token producerId,
                               final Token eventId)
    {
        return routes.getOrDefault(producerId, Collections.emptyMap()).getOrDefault(eventId, Collections.emptyList());
    }

    private void updateConnections (final Token eventId)
    {
        /**
         * TODO: Is this too inefficient?
         */
        for (Node pub : eventsToPublisherNodes.get(eventId))
        {
            for (Node sub : eventsToSubscriberNodes.get(eventId))
            {
                updateConnection(pub.name, sub.name);
            }
        }
    }

    private void updateConnection (final Token publisherId,
                                   final Token subscriberId)
    {
        final Node pub = publishersToNodes.get(publisherId);
        final Node sub = subscribersToNodes.get(subscriberId);

        /**
         * If only zero or one of the two required nodes are present,
         * then there cannot possibly be a connection between them.
         */
        if (pub == null || sub == null)
        {
            return;
        }

        /**
         * These are the events that the publisher is nominally producing
         * and that the subscriber is nominally interested in receiving.
         */
        final Set<Token> activeEvents = ImmutableSet.copyOf(Sets.intersection(pub.publishedEvents, sub.subscribedEvents));

        /**
         * If the publisher is producing events that the subscriber is interested in,
         * then there must exist an edge between the two nodes.
         */
        if (activeEvents.isEmpty() == false && network.edgesConnecting(pub, sub).isEmpty())
        {
            final E edgeData = edgeSupplier.apply(publisherId, subscriberId);
            network.addEdge(pub, sub, new Edge(pub, sub, edgeData));
        }

        /**
         * If the publisher is *not* producing anything of interest to the subscriber,
         * and the two nodes are already disconnected, then there is nothing more
         * that we need to do at this time.
         */
        if (activeEvents.isEmpty() && network.edgesConnecting(pub, sub).isEmpty())
        {
            return; // Nothing Necessary
        }

        /**
         * By case analysis, there is an edge already connecting the two nodes.
         */
        final Edge edge = (Edge) network.edgesConnecting(pub, sub).toArray()[0];

        /**
         * Remove stale connections.
         */
        final Set<Token> removed = Sets.newHashSet();
        for (Token eventId : edge.events)
        {
            if (activeEvents.contains(eventId) == false)
            {
                // TODO: Deregister Event
                removed.add(eventId);
            }
        }
        edge.events.removeAll(removed);

        /**
         * Add active connections.
         */
        for (Token eventId : activeEvents)
        {
            if (edge.events.contains(eventId) == false)
            {
                register(eventId, edge);
            }
        }
        edge.events.addAll(activeEvents);

        /**
         * If the subscriber is no longer interested in any events from the publisher,
         * then go ahead and disconnect the two nodes.
         */
        if (activeEvents.isEmpty())
        {
            network.removeEdge(edge);
        }
    }

    /**
     * This method adds an entry to the 'routes' map.
     *
     * @param eventId is the event being updated.
     * @param edge will be added to the map.
     */
    private void register (final Token eventId,
                           final Edge edge)
    {
        System.out.println("Register2: " + eventId);

        if (routes.containsKey(edge.publisher.name) == false)
        {
            routes.put(edge.publisher.name, Maps.newConcurrentMap());
        }

        final Map<Token, List<Edge>> eventMap = routes.get(edge.publisher.name);

        if (eventMap.containsKey(eventId) == false)
        {
            eventMap.put(eventId, new CopyOnWriteArrayList<>());
        }

        final List<Edge> edges = eventMap.get(eventId);

        if (edges.stream().anyMatch(x -> x.equals(edge)))
        {
            return; // Already Registered
        }
        else
        {
            edges.add(edge);
        }
    }

    public final class Node
    {
        public final Token name;

        private final Set<Token> publishedEvents;

        private final Set<Token> subscribedEvents;

        private Node (final Token name,
                      final boolean publisher)
        {
            this.name = name;
            this.publishedEvents = publisher ? Sets.newConcurrentHashSet() : ImmutableSet.of();
            this.subscribedEvents = publisher ? ImmutableSet.of() : Sets.newConcurrentHashSet();
        }

    }

    public final class Edge
    {
        private final Set<Token> events = Sets.newConcurrentHashSet();

        public final Node publisher;

        public final Node subscriber;

        public final E data;

        private Edge (final Node pub,
                      final Node sub,
                      final E data)
        {
            this.publisher = pub;
            this.subscriber = sub;
            this.data = data;
        }
    }

    public static void main (String[] args)
    {
        final EventRouter<String> dispatcher = new EventRouter((x, y) -> x.toString() + " -> " + y.toString());

        dispatcher.addPublisher(Token.create("P1"), Token.create("E1"));
        dispatcher.addPublisher(Token.create("P1"), Token.create("E2"));
        dispatcher.addPublisher(Token.create("P1"), Token.create("E3"));

        dispatcher.addSubscriber(Token.create("P2"), Token.create("E2"));

        dispatcher.resolve(Token.create("P1"), Token.create("E2")).forEach(x -> System.out.println(x.data));
    }
}
