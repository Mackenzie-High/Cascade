package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator.AllocationPool;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadeReactor.CoreBuilder;
import com.mackenziehigh.cascade.internal.Controller;
import com.mackenziehigh.cascade.internal.SharedState;
import com.mackenziehigh.cascade.internal.StandardLogger;
import com.mackenziehigh.cascade.internal.Utils;
import com.mackenziehigh.cascade.internal.messages.ConcreteAllocator;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import com.mackenziehigh.cascade.CascadeReactor;

/**
 * Use an instance of this class to create a Cascade object.
 */
public final class ConcreteSchema implements CascadeSchema1
{
    private final ConcreteSchema SELF = this;

    private final Map<String, PoolSchema> pools = Maps.newHashMap();

    private final Map<String, DynamicPoolSchema> dynamicPools = Maps.newHashMap();

    private final Map<String, FixedPoolSchema> fixedPools = Maps.newHashMap();

    private final Map<String, CompositePoolSchema> compositePools = Maps.newHashMap();

    private final Map<String, PumpSchema> pumps = Maps.newHashMap();

    private final Map<String, DirectPumpSchema> directPumps = Maps.newHashMap();

    private final Map<String, DedicatedPumpSchema> dedicatedPumps = Maps.newHashMap();

    private final Map<String, PooledPumpSchema> pooledPumps = Maps.newHashMap();

    private final Map<String, SpawningPumpSchema> spawningPumps = Maps.newHashMap();

    private final Map<String, NodeSchema> nodes = Maps.newHashMap();

    private final MutableNetwork<String, EdgeSchema> network = NetworkBuilder.directed().allowsParallelEdges(false).allowsSelfLoops(true).build();

    private final Stack<String> namespaces = new Stack<>();

    private final Stack<CascadeLogger> implicitLogger = new Stack<>();

    private final Stack<String> implicitPool = new Stack<>();

    private final Stack<String> implicitPump = new Stack<>();

    private final ConcreteAllocator allocator = new ConcreteAllocator();

    private final SharedState sharedState = new SharedState();

    private CascadeLogger defaultLogger = new StandardLogger("default");

    /**
     * Builder.
     */
    public abstract class Schema
    {
        public ConcreteSchema end ()
        {
            return SELF;
        }
    }

    /**
     * Builder.
     *
     * @param <T>
     */
    public abstract class PoolSchema<T extends PoolSchema<T>>
            extends Schema
    {
        private final String name;

        private final String simpleName;

        private OptionalInt minAllocationSize = OptionalInt.empty();

        private OptionalInt maxAllocationSize = OptionalInt.empty();

        private PoolSchema (final String name)
        {
            Preconditions.checkState(getNamespace().isEmpty() == false, "No Namespace");
            Utils.checkSimpleName(name);
            final String key = getNamespace() + '.' + name;
            this.name = key;
            this.simpleName = name;

            Preconditions.checkState(pools.containsKey(key) == false, "Duplicate Pool: " + key);
            pools.put(key, this);
        }

        /**
         * Getter.
         *
         * @return the full-name of this allocation-pool.
         */
        public String getName ()
        {
            return name;
        }

        /**
         * Getter.
         *
         * @return the simple-name of this allocation-pool.
         */
        public String getSimpleName ()
        {
            return simpleName;
        }

        /**
         * Getter.
         *
         * @return the minimum size of each operand allocated in the pool,
         * if a minimum has been specified.
         */
        public OptionalInt getMinAllocationSize ()
        {
            return minAllocationSize;
        }

        /**
         * Setter.
         *
         * @param value will be the minimum size of each operand allocated in the pool.
         * @return this.
         */
        @SuppressWarnings ("unchecked")
        public T setMinAllocationSize (final int value)
        {
            Preconditions.checkArgument(value >= 0, "value < 0");
            this.minAllocationSize = OptionalInt.of(value);
            return (T) this;
        }

        /**
         * Getter.
         *
         * @return the maximum size of each operand allocated in the pool,
         * if a maximum has been specified.
         */
        public OptionalInt getMaxAllocationSize ()
        {
            return maxAllocationSize;
        }

        /**
         * Setter.
         *
         * @param value will be the maximum size of each operand allocated in the pool.
         * @return this.
         */
        @SuppressWarnings ("unchecked")
        public T setMaxAllocationSize (final int value)
        {
            Preconditions.checkArgument(value >= 0, "value < 0");
            this.maxAllocationSize = OptionalInt.of(value);
            return (T) this;
        }

    }

    /**
     * Builder.
     */
    public final class DynamicPoolSchema
            extends PoolSchema
    {
        private DynamicPoolSchema (final String name)
        {
            super(name);
            setMinAllocationSize(0);
            setMaxAllocationSize(Integer.MAX_VALUE);
            dynamicPools.put(getName(), this);
        }
    }

    /**
     * Builder.
     */
    public final class FixedPoolSchema
            extends PoolSchema<FixedPoolSchema>
    {
        private OptionalInt bufferCount = OptionalInt.empty();

        private FixedPoolSchema (final String name)
        {
            super(name);
            setMinAllocationSize(0);
            fixedPools.put(getName(), this);
        }

        /**
         * Getter.
         *
         * @return the number of preallocated buffers in the pool,
         * if the value has been specified already.
         */
        public OptionalInt getBufferCount ()
        {
            return bufferCount;
        }

        /**
         * Setter.
         *
         * @param value will be the number of preallocated buffers in the pool.
         * @return this.
         */
        public FixedPoolSchema setBufferCount (final int value)
        {
            Preconditions.checkArgument(value >= 0, "value < 0");
            this.bufferCount = OptionalInt.of(value);
            return this;
        }
    }

    /**
     * Builder.
     */
    public final class CompositePoolSchema
            extends PoolSchema<CompositePoolSchema>
    {
        private final Set<String> members = new TreeSet<>();

        private Optional<String> fallback = Optional.empty();

        private CompositePoolSchema (final String name)
        {
            super(name);
            compositePools.put(getName(), this);
        }

        /**
         * Use this method to add a pool to this conglomeration of pools.
         *
         * @param name is either the simple-name or full-name of a pool.
         * @return this.
         */
        public CompositePoolSchema addMemberPool (final String name)
        {
            Preconditions.checkNotNull(name);
            members.add(name);
            return this;
        }

        /**
         * Getter.
         *
         * <p>
         * This excludes the name of the fallback pool, if any.
         * </p>
         *
         * @return the names of the members herein.
         */
        public Set<String> getMemberPools ()
        {
            return ImmutableSet.copyOf(members);
        }

        /**
         * Use this method to specify the pool to use,
         * if will not fit into another pool,
         * or the relevant pool is already full.
         *
         * @param name is either the simple-name or full-name of a pool.
         * @return this.
         */
        public CompositePoolSchema setFallbackPool (final String name)
        {
            fallback = Optional.of(name);
            return this;
        }

        /**
         * Getter.
         *
         * @return the name of the fallback pool, if any.
         */
        public Optional<String> getFallbackPool ()
        {
            return fallback;
        }
    }

    /**
     * Builder.
     *
     * @param <T>
     */
    public abstract class PumpSchema<T extends PumpSchema<T>>
            extends Schema
    {
        private final String name;

        private final String simpleName;

        private ThreadFactory threadFactory;

        private OptionalInt backlogCapacity;

        private PumpSchema (final String name)
        {
            Preconditions.checkState(getNamespace().isEmpty() == false, "No Namespace");
            Utils.checkSimpleName(name);
            final String key = getNamespace() + '.' + name;
            this.name = key;
            this.simpleName = name;

            this.threadFactory = new ThreadFactoryBuilder()
                    .setDaemon(false)
                    .setNameFormat(this.name + "[%d]-" + UUID.randomUUID().toString())
                    .build();

            Preconditions.checkState(pumps.containsKey(key) == false, "Duplicate Pump: " + key);
            pumps.put(key, this);
        }

        /**
         * Getter.
         *
         * @return the full-name of the pump.
         */
        public String getName ()
        {
            return name;
        }

        /**
         * Getter.
         *
         * @return the simple-name of the pump.
         */
        public String getSimpleName ()
        {
            return simpleName;
        }

        /**
         * Getter.
         *
         * @return the thread-factory used to create threads, if any.
         */
        public ThreadFactory getThreadFactory ()
        {
            return threadFactory;
        }

        /**
         * Setter.
         *
         * @param value will be the thread-factory used to create threads, if any.
         * @return this.
         */
        @SuppressWarnings ("unchecked")
        public T setThreadFactory (final ThreadFactory value)
        {
            Preconditions.checkNotNull(threadFactory, "value");
            this.threadFactory = value;
            return (T) this;
        }

        /**
         * Getter.
         *
         * @param value will be the maximum number of messages that can be, enqueued
         * in edges incident into the nodes managed by the pump, awaiting processing.
         * @return this.
         */
        @SuppressWarnings ("unchecked")
        public T setBacklogCapacity (final int value)
        {
            Preconditions.checkArgument(value >= 0, "value < 0");
            this.backlogCapacity = OptionalInt.of(value);
            return (T) this;
        }

        /**
         * Getter.
         *
         * @return the maximum number of messages that can be,
         * enqueued in edges incident into the nodes managed by the pump,
         * awaiting processing, if the capacity has been specified.
         */
        public OptionalInt getBacklogCapacity ()
        {
            return backlogCapacity;
        }
    }

    /**
     * Builder.
     */
    public final class DirectPumpSchema
            extends PumpSchema<DirectPumpSchema>
    {
        private DirectPumpSchema (final String name)
        {
            super(name);
            directPumps.put(getName(), this);
        }
    }

    /**
     * Builder.
     *
     * @param <T>
     */
    public abstract class IndirectPumpSchema<T extends IndirectPumpSchema<T>>
            extends PumpSchema<T>
    {
        private boolean sharedQueues = false;

        private IndirectPumpSchema (final String name)
        {
            super(name);
        }

        /**
         * Use this method to cause the pump to use a single
         * underlying fixed-size data-structure to provide
         * storage for messages enqueued in the edges.
         *
         * <p>
         * This option has the advantage of allowing you
         * to preallocate less memory for use by edge queues.
         * More specifically, the amount of preallocated memory
         * will be directly proportional to the backlog-capacity
         * of the pump. In cases were there are a significant
         * number of edges incident to the nodes managed by this pump,
         * this option can potentially offer considerable memory savings,
         * while providing ample backlog-capacity, by allowing edges
         * to share capacity that would be allocated but usually unused.
         * </p>
         *
         * <p>
         * This option has the disadvantage of introducing an
         * increased risk of queue exhaustion in the edges.
         * If the total number of messages enqueued in a subset
         * of the edges reaches the capacity of the shared
         * underlying data-structure, then the rest of the edges
         * will be unable to enqueue any messages, because
         * the underlying data-structure is full.
         * </p>
         *
         * <p>
         * This option has the disadvantage of introducing
         * additional opportunity for lock-contention.
         * Under this option, whenever a message is enqueued
         * in an edge, the shared underlying data-structure
         * must be synchronized. Since many threads may
         * be enqueuing messages at the same time into
         * different edges that share the same underlying
         * data-structure, there is potential for contention.
         * </p>
         *
         * @return this.
         */
        @SuppressWarnings ("unchecked")
        public T useSharedQueues ()
        {
            sharedQueues = true;
            return (T) this;
        }

        /**
         * Getter.
         *
         * @return true, if the edges will use a shared underlying data-structure.
         */
        public boolean isUsingSharedQueues ()
        {
            Verify.verify(isUsingIndependentQueues() != isUsingSharedQueues());
            return sharedQueues;
        }

        /**
         * Use this method to cause the pump to use a independent
         * underlying fixed-size data-structures to provide
         * storage for messages enqueued in the edges.
         *
         * <p>
         * This option has the advantage of ensuring that
         * the edge queues are completely independent
         * of one another. Thus, filling up one queue
         * does not risk exhausting the capacity of
         * all of the queues due to shared resources.
         * Likewise, each queue is independent; therefore,
         * less synchronization is needed in order to
         * enqueue messages.
         * </p>
         *
         * <p>
         * This option has the disadvantage of usually
         * requiring more preallocated memory,
         * which may usually be non-utilized.
         * For example, if message processing is usually fast,
         * but can occasionally hiccup, then you will need
         * to ensure that the queue-capacity of each edge
         * is large enough to absorb the hiccups.
         * In that case, the queues will usually be near empty,
         * whenever processing is actually moving quickly.
         * Thus, you will have memory sitting around doing
         * nothing most of the time.
         * </p>
         *
         * @return this.
         */
        @SuppressWarnings ("unchecked")
        public T useIndependentQueues ()
        {
            sharedQueues = false;
            return (T) this;
        }

        /**
         * Getter.
         *
         * @return true, if the edges will independent of one another.
         */
        public boolean isUsingIndependentQueues ()
        {
            Verify.verify(isUsingIndependentQueues() != isUsingSharedQueues());
            return !sharedQueues;
        }
    }

    /**
     * Builder.
     */
    public final class DedicatedPumpSchema
            extends IndirectPumpSchema<DedicatedPumpSchema>
    {
        private boolean fifo;

        private DedicatedPumpSchema (final String name)
        {
            super(name);
            dedicatedPumps.put(getName(), this);
        }

        /**
         * Use this method to cause the pump to use a FIFO
         * scheduling strategy for handling incoming messages.
         *
         * <p>
         * Under this strategy, the pump will dequeue the
         * least-recently-received message from the highest-priority
         * non-empty edge and then execute the onMessage(*) event-handler
         * in the node incident to that edge.
         * </p>
         *
         * @return this.
         */
        public DedicatedPumpSchema useFirstComeFirstServe ()
        {
            fifo = true;
            return this;
        }

        /**
         * Getter.
         *
         * @return true, if the pump will use FIFO scheduling.
         */
        public boolean isFirstComeFirstServe ()
        {
            Verify.verify(isFirstComeFirstServe() != isRoundRobin());
            return fifo;
        }

        /**
         * Use this method to cause the pump to use a round-robin
         * scheduling strategy for handling incoming messages.
         *
         * <p>
         * Under this strategy, the pump will iterate across
         * the highest-priority non-empty edges.
         * At each non-empty edge, the pump will dequeue exactly
         * one message, execute the onMessage(*) event-handler in
         * the node that is incident to the edge, and then the pump
         * will move onto the next non-empty edge.
         * </p>
         *
         * @return this.
         */
        public DedicatedPumpSchema useRoundRobin ()
        {
            fifo = false;
            return this;
        }

        /**
         * Getter.
         *
         * @return true, if the pump will use round-robin scheduling.
         */
        public boolean isRoundRobin ()
        {
            Verify.verify(isFirstComeFirstServe() != isRoundRobin());
            return !fifo;
        }
    }

    /**
     * Builder.
     */
    public final class PooledPumpSchema
            extends PumpSchema
    {
        private OptionalInt threadCount = OptionalInt.empty();

        private PooledPumpSchema (final String name)
        {
            super(name);
            pooledPumps.put(getName(), this);
        }

        /**
         * Setter.
         *
         * @param count will be the number of threads in the thread-pool.
         * @return this.
         */
        public PooledPumpSchema setThreadCount (final int count)
        {
            Preconditions.checkArgument(count >= 1, "count < 1");
            threadCount = OptionalInt.of(count);
            return this;
        }

        /**
         * Getter.
         *
         * @return the number of threads in the thread-pool,
         * if the the number has been specified already.
         */
        public OptionalInt getThreadCount ()
        {
            return threadCount;
        }
    }

    /**
     * Builder.
     */
    public final class SpawningPumpSchema
            extends PumpSchema
    {
        private OptionalInt minimumThreadCount = OptionalInt.empty();

        private OptionalInt maximumThreadCount = OptionalInt.empty();

        private OptionalLong spawnDelayNanos = OptionalLong.empty();

        private OptionalLong keepAliveNanos = OptionalLong.empty();

        private SpawningPumpSchema (final String name)
        {
            super(name);
            spawningPumps.put(getName(), this);
        }

        /**
         * Setter.
         *
         * @param count will be the minimum number of threads simultaneously in the thread-pool.
         * @return this.
         */
        public SpawningPumpSchema setMinThreadCount (final int count)
        {
            Preconditions.checkArgument(count >= 1, "count < 1");
            minimumThreadCount = OptionalInt.of(count);
            return this;
        }

        /**
         * Getter.
         *
         * @return the minimum number of threads simultaneously in the thread-pool.
         */
        public OptionalInt getMinThreadCount ()
        {
            return minimumThreadCount;
        }

        /**
         * Setter.
         *
         * @param count will be the maximum number of threads simultaneously in the thread-pool.
         * @return this.
         */
        public SpawningPumpSchema setMaxThreadCount (final int count)
        {
            Preconditions.checkArgument(count >= 1, "count < 1");
            maximumThreadCount = OptionalInt.of(count);
            return this;
        }

        /**
         * Getter.
         *
         * @return the maximum number of threads simultaneously in the thread-pool.
         */
        public OptionalInt getMaxThreadCount ()
        {
            return maximumThreadCount;
        }

        /**
         * Setter.
         *
         * @param value is the length of time that must pass between spawning threads.
         * @param unit describes the value.
         * @return this.
         */
        public SpawningPumpSchema setSpawnDelay (final long value,
                                                 final TimeUnit unit)
        {
            Preconditions.checkNotNull(unit, "unit");
            spawnDelayNanos = OptionalLong.of(unit.toNanos(value));
            return this;
        }

        /**
         * Getter.
         *
         * @return the length of time that must pass between spawning threads.
         */
        public Optional<Duration> getSpawnDelay ()
        {
            return null; // TODO
        }

        /**
         * Setter.
         *
         * @param value is the length of time that must pass before
         * a thread becomes eligible for reclamation.
         * @param unit describes the value.
         * @return this.
         */
        public SpawningPumpSchema setKeepAliveTime (final long value,
                                                    final TimeUnit unit)
        {
            Preconditions.checkNotNull(unit, "unit");
            keepAliveNanos = OptionalLong.of(unit.toNanos(value));
            return this;
        }

        /**
         * Getter.
         *
         * @return the length of time that must pass before
         * a thread becomes eligible for reclamation.
         */
        public Optional<Duration> getKeepAliveTime ()
        {
            return null; // TODO
        }
    }

    /**
     * Builder.
     *
     * @param <T> the the type of the actor being built.
     */
    public final class NodeSchema<T extends NodeSchema>
            extends Schema
    {
        private final String name;

        private final CoreBuilder builder;

        private CascadeLogger explicitLogger;

        private CascadeLogger implicitLogger;

        private String explicitPool;

        private String implicitPool;

        private String explicitPump;

        private String implicitPump;

        private NodeSchema (final String name,
                            final CoreBuilder builder)
        {
            Preconditions.checkNotNull(name, "name");
            Preconditions.checkNotNull(builder, "builder");
            this.name = name;
            this.builder = builder;
        }

        public String getName ()
        {
            return name;
        }

        public CoreBuilder getBuilder ()
        {
            return builder;
        }

        public NodeSchema<T> setPump (final String name)
        {
            explicitPump = name;
            return this;
        }

        public NodeSchema<T> setPump (final PumpSchema value)
        {
            explicitPump = value.getName();
            return this;
        }

        public String getPump ()
        {
            return explicitPump;
        }

        public CascadeLogger getLogger ()
        {
            return explicitLogger;
        }

        public NodeSchema<T> setLogger (final CascadeLogger value)
        {
            this.explicitLogger = value;
            return this;
        }

        public String getPool ()
        {
            return explicitPool;
        }

        public NodeSchema<T> setPool (final AllocationPool value)
        {
            this.explicitPool = value.name();
            return this;
        }

        public NodeSchema<T> setPool (final String name)
        {
            this.explicitPool = name;
            return this;
        }

    }

    /**
     * Builder.
     */
    public final class EdgeSchema
            extends Schema
    {
        private final String supplier;

        private final String consumer;

        private OptionalInt queueCapacity = OptionalInt.empty();

        private OptionalInt priority = OptionalInt.empty();

        private EdgeSchema (final String supplier,
                            final String consumer)
        {
            Preconditions.checkNotNull(supplier, "supplier");
            Preconditions.checkNotNull(consumer, "consumer");
            this.supplier = supplier;
            this.consumer = consumer;
        }

        public String getSupplier ()
        {
            return supplier;
        }

        public String getConsumer ()
        {
            return consumer;
        }

        public OptionalInt getQueueCapacity ()
        {
            return queueCapacity;
        }

        public EdgeSchema setQueueCapacity (final int value)
        {
            this.queueCapacity = OptionalInt.of(value);
            return this;
        }
    }

    /**
     * Use this method to add an allocator that allocates operands
     * on-demand and supports automated garbage-collection.
     *
     * @param name is the name of the new allocator.
     * @return the schema of the new allocator.
     */
    @Override
    public DynamicPoolSchema addDynamicPool (final String name)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(dynamicPools.containsKey(name) == false, "Duplicate Allocator: " + name);
        final DynamicPoolSchema result = new DynamicPoolSchema(name);
        dynamicPools.put(name, result);
        return result;
    }

    /**
     * Use this method to add an allocator that allocates operands
     * using pre-allocated buffers that do <b>not</b> support
     * automatic garbage-collection.
     *
     * @param name is the name of the new allocator.
     * @return the schema of the new allocator.
     */
    @Override
    public FixedPoolSchema addFixedPool (final String name)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(fixedPools.containsKey(name) == false, "Duplicate Allocator: " + name);
        final FixedPoolSchema result = new FixedPoolSchema(name);
        fixedPools.put(name, result);
        return result;
    }

    /**
     * Use this method to add a powerplant that powers (consumer) actor(s)
     * using the thread(s) that are powering the (supplier) actor(s).
     *
     * @param name is the name of the new powerplant.
     * @return the schema of the new powerplant.
     */
    @Override
    public DirectPumpSchema addDirectPump (final String name)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(directPumps.containsKey(name) == false, "Duplicate Pump: " + name);
        final DirectPumpSchema result = new DirectPumpSchema(name);
        directPumps.put(name, result);
        return result;
    }

    /**
     * Use this method to add a powerplant that powers (consumer) actor(s)
     * using dedicated threads per actor.
     *
     * @param name is the name of the new powerplant.
     * @return the schema of the new powerplant.
     */
    @Override
    public DedicatedPumpSchema addDedicatedPump (final String name)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(dedicatedPumps.containsKey(name) == false, "Duplicate Pump: " + name);
        final DedicatedPumpSchema result = new DedicatedPumpSchema(name);
        dedicatedPumps.put(name, result);
        return result;
    }

    /**
     * Use this method to add a powerplant that powers (consumer) actor(s)
     * using a fixed-size pool of threads.
     *
     * @param name is the name of the new powerplant.
     * @return the schema of the new powerplant.
     */
    @Override
    public PooledPumpSchema addPooledPump (final String name)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(pooledPumps.containsKey(name) == false, "Duplicate Pump: " + name);
        final PooledPumpSchema result = new PooledPumpSchema(name);
        pooledPumps.put(name, result);
        return result;
    }

    /**
     * Use this method to add a powerplant that powers (consumer) actor(s)
     * using a pool of threads that resizes on-demand as needed.
     *
     * @param name is the name of the new powerplant.
     * @return the schema of the new powerplant.
     */
    @Override
    public SpawningPumpSchema addSpawningPump (final String name)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(spawningPumps.containsKey(name) == false, "Duplicate Pump: " + name);
        final SpawningPumpSchema result = new SpawningPumpSchema(name);
        spawningPumps.put(name, result);
        return result;
    }

    /**
     * Use this method to add an actor to the system.
     *
     * @param <T> is the type of the actor class.
     * @param name is the name of the new actor object.
     * @param builder will create the underlying implementation of the node.
     * @return the schema of the new actor.
     */
    @Override
    public <T extends NodeSchema> NodeSchema<T> addNode (final String name,
                                                         final CascadeReactor.CoreBuilder builder)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(nodes.containsKey(name) == false, "Duplicate Node: " + name);
        final NodeSchema<T> node = new NodeSchema<>(name, builder);
        nodes.put(name, node);
        network.addNode(name);

        if (implicitLogger.isEmpty() == false)
        {
            node.setLogger(implicitLogger.peek());
        }

        if (implicitPool.isEmpty() == false)
        {
            node.setPool(implicitPool.peek());
        }

        if (implicitPump.isEmpty() == false)
        {
            node.setPump(implicitPump.peek());
        }

        return node;
    }

    /**
     * Use this method to connect two actors using a directed pipeline.
     *
     * @param supplier is the supplying actor.
     * @param consumer is the consuming actor.
     * @return the schema of the new edge.
     */
    @Override
    public EdgeSchema connect (final String supplier,
                               final String consumer)
    {
        final EdgeSchema edge = new EdgeSchema(supplier, consumer);
        network.addEdge(supplier, consumer, edge);
        return edge;
    }

    /**
     * Use this method to connect two actors using a directed pipeline.
     *
     * @param supplier is the supplying actor.
     * @param consumer is the consuming actor.
     * @return the schema of the new edge.
     */
    @Override
    public EdgeSchema connect (final NodeSchema supplier,
                               final NodeSchema consumer)
    {
        return connect(supplier.getName(), consumer.getName());
    }

    /**
     * Use this method to enter a name-space.
     *
     * @param name will become part of the name-space path.
     * @return this.
     */
    @Override
    public ConcreteSchema enter (final String name)
    {
        Preconditions.checkArgument(name.matches("[A-Za-z0-9_$]+"));
        namespaces.push(name);

        if (implicitLogger.isEmpty() == false)
        {
            implicitLogger.push(implicitLogger.peek());
        }

        if (implicitPool.isEmpty() == false)
        {
            implicitPool.push(implicitPool.peek());
        }

        if (implicitPump.isEmpty() == false)
        {
            implicitPump.push(implicitPump.peek());
        }

        return this;
    }

    /**
     * Use this method to exit a name-space.
     *
     * @return this.
     */
    @Override
    public ConcreteSchema exit ()
    {
//        namespaces.pop();
//        implicitLogger.pop();
//        implicitPool.pop();
//        implicitPump.pop();
        return this;
    }

    @Override
    public String getNamespace ()
    {
        return null;
    }

    /**
     * Setter.
     *
     * @param logger will be provided as the default logger.
     * @return this.
     */
    @Override
    public ConcreteSchema setDefaultLogger (final CascadeLogger logger)
    {
        Preconditions.checkNotNull(logger);
        this.defaultLogger = logger;
        return this;
    }

    /**
     * Getter.
     *
     * @return the default logger.
     */
    @Override
    public CascadeLogger getDefaultLogger ()
    {
        return defaultLogger;
    }

    /**
     * Use this method to specify the implicit logger going forward.
     *
     * <p>
     * This method may be called multiple times during schema declaration.
     * </p>
     *
     * @param logger is the logger to use, unless one is explicitly specified.
     * @return this.
     */
    @Override
    public ConcreteSchema usingLogger (final CascadeLogger logger)
    {
        if (implicitLogger.isEmpty() == false)
        {
            implicitLogger.pop();
        }

        implicitLogger.push(logger);

        return this;
    }

    /**
     * Use this method to specify the implicit allocation-pool going forward.
     *
     * <p>
     * This method may be called multiple times during schema declaration.
     * </p>
     *
     * @param name identifies the pool to use, unless one is explicitly specified.
     * @return this.
     */
    @Override
    public ConcreteSchema usingPool (final String name)
    {
        if (implicitPool.isEmpty() == false)
        {
            implicitPool.pop();
        }

        implicitPool.push(name);

        return this;
    }

    /**
     * Use this method to specify the implicit allocation-pool going forward.
     *
     * <p>
     * This method may be called multiple times during schema declaration.
     * </p>
     *
     * @param pool is the pool to use, unless one is explicitly specified.
     * @return this.
     */
    @Override
    public ConcreteSchema usingPool (final AllocationPool pool)
    {
        usingPool(pool.name());
        return this;
    }

    /**
     * Use this method to specify the implicit pump going forward.
     *
     * <p>
     * This method may be called multiple times during schema declaration.
     * </p>
     *
     * @param name identifies the pump to use, unless one is explicitly specified.
     * @return this.
     */
    @Override
    public ConcreteSchema usingPump (final String name)
    {
        if (implicitPump.isEmpty() == false)
        {
            implicitPump.pop();
        }

        implicitPump.push(name);

        return this;
    }

    /**
     * Use this method to specify the implicit pump going forward.
     *
     * <p>
     * This method may be called multiple times during schema declaration.
     * </p>
     *
     * @param pump is the pump to use, unless one is explicitly specified.
     * @return this.
     */
    @Override
    public ConcreteSchema usingPump (final PumpSchema pump)
    {
        implicitPump.push(pump.getName());
        return this;
    }

    /**
     * Use this method to construct the new system.
     *
     * @return the new system.
     */
    @Override
    public Cascade build ()
    {
        /**
         * Initialize.
         */
        sharedState.allocator = allocator;

        /**
         * Pass #1.
         */
        dynamicPools.values().forEach(x -> validate(x));
        fixedPools.values().forEach(x -> validate(x));
        compositePools.values().forEach(x -> validate(x));
        dedicatedPumps.values().forEach(x -> validate(x));
        directPumps.values().forEach(x -> validate(x));
        pooledPumps.values().forEach(x -> validate(x));
        spawningPumps.values().forEach(x -> validate(x));
        nodes.values().forEach(x -> validate(x));
        network.edges().forEach(x -> validate(x));

        /**
         * Pass #2.
         */
        nodes.values().forEach(x -> declare(x));
        network.edges().forEach(x -> declare(x));

        /**
         * Pass #3.
         */
        dynamicPools.values().forEach(x -> compile(x));
        fixedPools.values().forEach(x -> compile(x));
        compositePools.values().forEach(x -> compile(x));
        dedicatedPumps.values().forEach(x -> compile(x));
        directPumps.values().forEach(x -> compile(x));
        pooledPumps.values().forEach(x -> compile(x));
        spawningPumps.values().forEach(x -> compile(x));
        nodes.values().forEach(x -> compile(x));
        network.edges().forEach(x -> compile(x));

        verifySharedState();

        final Cascade result = new Controller(sharedState);

        return result;
    }

    private void report (final String message,
                         final Object... args)
    {
        throw new RuntimeException(String.format(message, args));
    }

    private void validate (final DynamicPoolSchema schema)
    {

    }

    private void validate (final FixedPoolSchema schema)
    {

    }

    private void validate (final CompositePoolSchema schema)
    {
        // TODO: Topological Dependency Checking
    }

    private void validate (final DedicatedPumpSchema schema)
    {

    }

    private void validate (final DirectPumpSchema schema)
    {

    }

    private void validate (final PooledPumpSchema schema)
    {

    }

    private void validate (final SpawningPumpSchema schema)
    {

    }

    private void validate (final NodeSchema schema)
    {

    }

    private void validate (final EdgeSchema schema)
    {

    }

    private void declare (final NodeSchema schema)
    {

    }

    private void declare (final EdgeSchema schema)
    {

    }

    private void compile (final DynamicPoolSchema schema)
    {
        final int min = schema.getMinAllocationSize().getAsInt();
        final int max = schema.getMaxAllocationSize().getAsInt();
        final String name = schema.getName();
        allocator.addDynamicPool(name, min, max);
    }

    private void compile (final FixedPoolSchema schema)
    {
        final int min = schema.getMinAllocationSize().getAsInt();
        final int max = schema.getMaxAllocationSize().getAsInt();
        final int cap = schema.getBufferCount().getAsInt();
        final String name = schema.getName();
        allocator.addFixedPool(name, min, max, cap);
    }

    private void compile (final CompositePoolSchema schema)
    {
        // Dependencies must be linearized first!
    }

    private void compile (final DedicatedPumpSchema schema)
    {

    }

    private void compile (final DirectPumpSchema schema)
    {

    }

    private void compile (final PooledPumpSchema schema)
    {

    }

    private void compile (final SpawningPumpSchema schema)
    {

    }

    private void compile (final NodeSchema schema)
    {

    }

    private void compile (final EdgeSchema schema)
    {

    }

    private void verifySharedState ()
    {
        /**
         * Verify the nodes.
         */
//        for (String name : nodes.keySet())
//        {
//            Verify.verify(sharedState.namesToNodes.containsKey(name));
//            final CascadeNode node = sharedState.namesToNodes.get(name);
//            final NodeSchema schema = nodes.get(name);
//            Verify.verify(node.name().equals(name));
//            Verify.verify(node.name().equals(schema.getName()));
//            Verify.verify(node.protoContext().name().equals(name));
//            Verify.verify(node.protoContext().allocator().equals(allocator));
//            Verify.verify(node.protoContext().message() == null);
//            Verify.verify(node.protoContext().exception() == null);
////            Verify.verify(node.protoContext().logger().equals(schema.getLogger()));
////            Objects.requireNonNull(schema.getLogger());
//            Verify.verify(node.protoContext().pool().name().equals(schema.getPool()));
//            Verify.verify(node.protoContext().pump().name().equals(schema.getPump()));
//            Verify.verify(node.protoContext().pump().nodes().contains(node));
//            Verify.verify(node.protoContext().inputs().size() == network.inDegree(name));
//            Verify.verify(node.protoContext().outputs().size() == network.outDegree(name));
//            Verify.verify(node.protoContext().node() == (Object) node); // Identity Equals
//            node.protoContext().inputs().forEach(edge -> Verify.verify(node.equals(edge.consumer())));
//            node.protoContext().outputs().forEach(edge -> Verify.verify(node.equals(edge.supplier())));
//        }
    }

}
