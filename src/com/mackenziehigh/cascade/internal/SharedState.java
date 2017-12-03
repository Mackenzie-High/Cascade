package com.mackenziehigh.cascade.internal;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeEdge;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadeNode;
import com.mackenziehigh.cascade.CascadePump;
import com.mackenziehigh.cascade.internal.pumps.Connector.Connection;
import com.mackenziehigh.cascade.internal.pumps.Engine;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public final class SharedState
{
    public final AtomicBoolean stop = new AtomicBoolean();

    public volatile Cascade cascade;

    public volatile CascadeLogger defaultLogger;

    public volatile CascadeAllocator allocator;

    public final MutableNetwork<CascadeNode, CascadeEdge> network = NetworkBuilder.directed().allowsParallelEdges(false).allowsSelfLoops(true).build();

    public final Map<String, CascadePump> namesToPumps = Maps.newConcurrentMap();

    public final Map<String, CascadeNode> namesToNodes = Maps.newConcurrentMap();

    public final Map<String, String> nodesToPools = Maps.newConcurrentMap();

    public final Map<String, String> nodesToPumps = Maps.newConcurrentMap();

    public final Multimap<String, CascadeEdge> nodesToInputs = Multimaps.synchronizedSetMultimap(HashMultimap.create());

    public final Multimap<String, CascadeEdge> nodesToOutputs = Multimaps.synchronizedSetMultimap(HashMultimap.create());

    public final Map<String, CascadeLogger> nodesToLoggers = Maps.newConcurrentMap();

    public final Map<String, Engine> engines = Maps.newConcurrentMap();

    public final Map<CascadeEdge, Connection> connections = Maps.newConcurrentMap();

    public final Multimap<String, String> pumpsToNodes = Multimaps.synchronizedSetMultimap(HashMultimap.create());
}
