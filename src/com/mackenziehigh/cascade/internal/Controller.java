package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.atomic.AtomicReference;
import com.mackenziehigh.cascade.CascadePump;
import com.mackenziehigh.cascade.CascadeReactor;
import com.mackenziehigh.cascade.CascadeProcessor;

/**
 *
 */
public final class Controller
        implements Cascade
{

    private final SharedState sharedState;

    private final AtomicReference<ExecutionPhase> phase = new AtomicReference<>(ExecutionPhase.INITIAL);

    private final LazyRef<CascadeLogger> defaultLogger;

    private final LazyRef<ImmutableSortedMap<String, CascadePump>> pumps;

    private final LazyRef<ImmutableSortedMap<String, CascadeReactor>> nodes;

    private final LazyRef<ImmutableSet<CascadeProcessor>> edges;

    public Controller (final SharedState sharedState)
    {
        this.sharedState = Objects.requireNonNull(sharedState);
        this.defaultLogger = LazyRef.create(() -> sharedState.defaultLogger);
        this.pumps = LazyRef.create(() -> ImmutableSortedMap.copyOf(resolvePumps()));
        this.nodes = LazyRef.create(() -> ImmutableSortedMap.copyOf(resolveNodes()));
        this.edges = LazyRef.create(() -> ImmutableSet.copyOf(resolveEdges()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeLogger defaultLogger ()
    {
        return defaultLogger.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeAllocator allocator ()
    {
        return sharedState.allocator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedMap<CascadeToken, CascadePump> engines ()
    {
        return pumps.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedMap<CascadeToken, CascadeReactor> nodes ()
    {
        return nodes.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<CascadeProcessor> edges ()
    {
        return edges.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutionPhase phase ()
    {
        return phase.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Cascade start ()
    {
        Preconditions.checkState(phase.get().equals(ExecutionPhase.INITIAL), "Already Started!");

        /**
         * Since this method is going to return immediately,
         * we must change the phase here, rather than in the
         * startup task in order to prevent a race-condition.
         * If this method gets called again, then we want
         * the already-started exception to be thrown!
         */
        phase.set(ExecutionPhase.SETUP);

        /**
         * Use a non-daemon thread to startup; otherwise, the program may close too soon.
         */
        final Thread thread = new Thread(() -> startupTask(), "Cascade Startup Task");
        thread.setDaemon(false);
        thread.start();

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Cascade stop ()
    {
        sharedState.stop.set(true);
        return this;
    }

    private SortedMap<String, CascadePump> resolvePumps ()
    {
        final SortedMap<String, CascadePump> map = Maps.newTreeMap(String.CASE_INSENSITIVE_ORDER);

        for (String name : sharedState.nodesToPumps.values())
        {
            final CascadePump pump = sharedState.namesToPumps.get(name);
            map.put(name, pump);
        }

        return ImmutableSortedMap.copyOf(map);
    }

    private SortedMap<String, CascadeReactor> resolveNodes ()
    {
        final SortedMap<String, CascadeReactor> map = Maps.newTreeMap(String.CASE_INSENSITIVE_ORDER);

        for (CascadeReactor x : sharedState.namesToNodes.values())
        {
            map.put(x.name(), x);
        }

        return ImmutableSortedMap.copyOf(map);
    }

    private Set<CascadeProcessor> resolveEdges ()
    {
        final Set<CascadeProcessor> set = Sets.newHashSet();

        for (CascadeProcessor x : sharedState.nodesToInputs.values())
        {
            set.add(x);
        }

        for (CascadeProcessor x : sharedState.nodesToOutputs.values())
        {
            set.add(x);
        }

        return ImmutableSet.copyOf(set);
    }

    private void startupTask ()
    {
        /**
         * Setup each node.
         */
        for (CascadeReactor node : sharedState.namesToNodes.values())
        {
            final DerivedContext context = new DerivedContext(node.protoContext());

            try
            {
                context.message.set(null);
                context.exception.set(null);
                node.core().onSetup(context);
            }
            catch (Throwable ex1)
            {
                context.message.set(null);
                context.exception.set(ex1);

                try
                {
                    node.core().onException(context);
                }
                catch (Throwable ex2)
                {
                    defaultLogger().error(ex2);
                }
            }
        }

        /**
         * Transition to the next execution-phase.
         * We must set this before starting the pumps;
         * otherwise, a race-condition could occur,
         * such that a node starts fast and then
         * processes a message while the phase is
         * still officially SETUP.
         */
        phase.set(ExecutionPhase.START);

        /**
         * Start the pumps, which will in-turn start the asynchronous message processing.
         */
        sharedState.engines.values().forEach(x -> x.start());

        /**
         * Notify each node of startup.
         */
        for (CascadeReactor node : sharedState.namesToNodes.values())
        {
            final DerivedContext context = new DerivedContext(node.protoContext());

            try
            {
                context.message.set(null);
                context.exception.set(null);
                node.core().onStart(context);
            }
            catch (Throwable ex1)
            {
                context.message.set(null);
                context.exception.set(ex1);

                try
                {
                    node.core().onException(context);
                }
                catch (Throwable ex2)
                {
                    defaultLogger().error(ex2);
                }
            }
        }

        /**
         * The system is now running, so the startup thread will exit.
         * The threads in the pumps will continue, unless they are daemons.
         */
        phase.set(ExecutionPhase.RUN);
    }
}
