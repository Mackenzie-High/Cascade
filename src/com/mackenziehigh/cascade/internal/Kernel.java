package com.mackenziehigh.cascade.internal;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeAllocator.AllocationPool;
import com.mackenziehigh.cascade.CascadeEdge;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadeNode;
import com.mackenziehigh.cascade.CascadePump;
import com.mackenziehigh.cascade.internal.pumps3.Connector.Connection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public final class Kernel
{
    public final AtomicBoolean stop = new AtomicBoolean();

    public volatile Cascade cascade;

    public volatile CascadeLogger defaultLogger;

    public volatile CascadeAllocator allocator;

    public final Map<CascadeNode, AllocationPool> actorsToPools = Maps.newConcurrentMap();

    public final Map<CascadeNode, CascadePump> actorsToPumps = Maps.newConcurrentMap();

    public final Multimap<CascadeNode, CascadeEdge> actorsToInputs = Multimaps.synchronizedSetMultimap(HashMultimap.create());

    public final Multimap<CascadeNode, CascadeEdge> actorsToOutputs = Multimaps.synchronizedSetMultimap(HashMultimap.create());

    public final Multimap<CascadeNode, Connection> actorsToOutputConnections = Multimaps.synchronizedSetMultimap(HashMultimap.create());

    public final Map<CascadeNode, CascadeLogger> actorsToLoggers = Maps.newConcurrentMap();

    public final Map<String, CascadeNode> namesToNodes = Maps.newConcurrentMap();

    public final Map<CascadeNode, Connection> nodesToConnections = Maps.newConcurrentMap();

    public final Multimap<CascadePump, CascadeNode> pumpsToNodes = Multimaps.synchronizedSetMultimap(HashMultimap.create());
}
