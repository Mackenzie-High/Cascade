package com.mackenziehigh.cascade.internal;

import com.google.common.collect.Maps;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.internal.engines.Connection;
import com.mackenziehigh.cascade.internal.routing.EventDispatcher;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import com.mackenziehigh.cascade.CascadePump;

/**
 *
 */
public final class SharedState
{
    public final AtomicBoolean stop = new AtomicBoolean();

    public volatile Cascade cascade;

    public volatile CascadeLogger defaultLogger;

    public volatile CascadeAllocator allocator;

    public volatile EventDispatcher dispatcher;

    public final Map<CascadeToken, CascadePump> nodesToEngines = Maps.newConcurrentMap();

    public final Map<CascadeToken, Connection> nodesToInputs = Maps.newConcurrentMap();

    public final Map<CascadeToken, CascadeLogger> nodesToLoggers = Maps.newConcurrentMap();

    public final Map<CascadeToken, CascadeAllocator.AllocationPool> nodesToPools = Maps.newConcurrentMap();

}
