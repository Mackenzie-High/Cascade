package com.mackenziehigh.cascade;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mackenziehigh.cascade.CascadeAllocator.AllocationPool;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.CascadeNode.Context;
import com.mackenziehigh.cascade.CascadeNode.Core;
import com.mackenziehigh.cascade.CascadeNode.CoreBuilder;
import com.mackenziehigh.cascade.internal.ConcreteEdge;
import com.mackenziehigh.cascade.internal.ConcreteNode;
import com.mackenziehigh.cascade.internal.ConcretePump;
import com.mackenziehigh.cascade.internal.Controller;
import com.mackenziehigh.cascade.internal.DefaultMessageConsumer;
import com.mackenziehigh.cascade.internal.SharedState;
import com.mackenziehigh.cascade.internal.messages.ConcreteAllocator;
import com.mackenziehigh.cascade.internal.pumps.ConnectionSchema;
import com.mackenziehigh.cascade.internal.pumps.DedicatedEngine;
import com.mackenziehigh.cascade.internal.pumps.Engine.MessageConsumer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

/**
 * Use an instance of this class to create a Cascade object.
 */
public final class CascadeSchema
{
    private final CascadeSchema SELF = this;

    private final Map<String, DynamicPoolSchema> dynamicPools = Maps.newHashMap();

    private final Map<String, FixedPoolSchema> fixedPools = Maps.newHashMap();

    private final Map<String, CompositePoolSchema> compositePools = Maps.newHashMap();

    private final Map<String, DirectPumpSchema> directPumps = Maps.newHashMap();

    private final Map<String, DedicatedPumpSchema> dedicatedPumps = Maps.newHashMap();

    private final Map<String, PooledPumpSchema> pooledPumps = Maps.newHashMap();

    private final Map<String, SpawningPumpSchema> spawningPumps = Maps.newHashMap();

    private final Map<String, NodeSchema> nodes = Maps.newHashMap();

    private final MutableNetwork<String, EdgeSchema> network = NetworkBuilder.directed().allowsParallelEdges(false).allowsSelfLoops(true).build();

    private final Stack<String> namespaces = new Stack<>();

    private Stack<CascadeLogger> implicitLogger = new Stack<>();

    private Stack<String> implicitPool = new Stack<>();

    private Stack<String> implicitPump = new Stack<>();

    private final ConcreteAllocator allocator = new ConcreteAllocator();

    private final SharedState sharedState = new SharedState();

    /**
     * Builder.
     */
    public abstract class Schema
    {
        public CascadeSchema end ()
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

        private OptionalInt minAllocationSize = OptionalInt.empty();

        private OptionalInt maxAllocationSize = OptionalInt.empty();

        private PoolSchema (final String name)
        {
            Preconditions.checkNotNull(name, "name");
            this.name = name;
        }

        public String getName ()
        {
            return name;
        }

        public OptionalInt getMinAllocationSize ()
        {
            return minAllocationSize;
        }

        public PoolSchema<T> setMinAllocationSize (final int value)
        {
            Preconditions.checkArgument(value >= 0, "value < 0");
            this.minAllocationSize = OptionalInt.of(value);
            return this;
        }

        public OptionalInt getMaxAllocationSize ()
        {
            return maxAllocationSize;
        }

        public PoolSchema<T> setMaxAllocationSize (final int value)
        {
            Preconditions.checkArgument(value >= 0, "value < 0");
            this.maxAllocationSize = OptionalInt.of(value);
            return this;
        }

    }

    /**
     * Builder.
     */
    public final class DynamicPoolSchema
            extends PoolSchema<DynamicPoolSchema>
    {
        private DynamicPoolSchema (final String name)
        {
            super(name);
            setMinAllocationSize(0);
            setMaxAllocationSize(Integer.MAX_VALUE);
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
        }

        public OptionalInt getBufferCount ()
        {
            return bufferCount;
        }

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

        private CompositePoolSchema (final String name)
        {
            super(name);
        }

        public CompositePoolSchema addMember (final String name)
        {
            members.add(name);
            return this;
        }

        public CompositePoolSchema addMember (final PoolSchema member)
        {
            members.add(member.getName());
            return this;
        }

        public Set<String> getMembers ()
        {
            return Collections.unmodifiableSet(members);
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

        private ThreadFactory threadFactory = new ThreadFactoryBuilder().setDaemon(false).build();

        private OptionalInt backlogCapacity;

        private PumpSchema (final String name)
        {
            Preconditions.checkNotNull(name);
            this.name = name;
        }

        public String getName ()
        {
            return name;
        }

        public ThreadFactory getThreadFactory ()
        {
            return threadFactory;
        }

        public PumpSchema<T> setThreadFactory (final ThreadFactory threadFactory)
        {
            Preconditions.checkNotNull(threadFactory, "threadFactory");
            this.threadFactory = threadFactory;
            return this;
        }

        public PumpSchema<T> setBacklogCapacity (final int value)
        {
            this.backlogCapacity = OptionalInt.of(value);
            return this;
        }

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
        }

    }

    /**
     * Builder.
     */
    public final class DedicatedPumpSchema
            extends PumpSchema<DedicatedPumpSchema>
    {
        private DedicatedPumpSchema (final String name)
        {
            super(name);
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
        }

        public PooledPumpSchema setThreadCount (final int count)
        {
            Preconditions.checkArgument(count >= 1, "count < 1");
            threadCount = OptionalInt.of(count);
            return this;
        }

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

        private SpawningPumpSchema (final String name)
        {
            super(name);
        }

        public SpawningPumpSchema setMinThreadCount (final int count)
        {
            Preconditions.checkArgument(count >= 1, "count < 1");
            minimumThreadCount = OptionalInt.of(count);
            return this;
        }

        public OptionalInt getMinThreadCount ()
        {
            return minimumThreadCount;
        }

        public SpawningPumpSchema setMaxThreadCount (final int count)
        {
            Preconditions.checkArgument(count >= 1, "count < 1");
            maximumThreadCount = OptionalInt.of(count);
            return this;
        }

        public OptionalInt getMaxThreadCount ()
        {
            return maximumThreadCount;
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

        private CascadeLogger logger;

        private String pool;

        private String pump;

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
            pump = name;
            return this;
        }

        public NodeSchema<T> setPump (final PumpSchema value)
        {
            pump = value.getName();
            return this;
        }

        public String getPump ()
        {
            return pump;
        }

        public CascadeLogger getLogger ()
        {
            return logger;
        }

        public NodeSchema<T> setLogger (final CascadeLogger value)
        {
            this.logger = value;
            return this;
        }

        public String getPool ()
        {
            return pool;
        }

        public NodeSchema<T> setPool (final AllocationPool value)
        {
            this.pool = value.name();
            return this;
        }

        public NodeSchema<T> setPool (final String name)
        {
            this.pool = name;
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
    public DirectPumpSchema addDirectPump (final String name)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(directPumps.containsKey(name) == false, "Duplicate Powerplant: " + name);
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
    public DedicatedPumpSchema addDedicatedPump (final String name)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(dedicatedPumps.containsKey(name) == false, "Duplicate Powerplant: " + name);
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
    public PooledPumpSchema addPooledPump (final String name)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(pooledPumps.containsKey(name) == false, "Duplicate Powerplant: " + name);
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
    public SpawningPumpSchema addSpawningPump (final String name)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(spawningPumps.containsKey(name) == false, "Duplicate Powerplant: " + name);
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
    public <T extends NodeSchema> NodeSchema<T> addNode (final String name,
                                                         final CascadeNode.CoreBuilder builder)
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
    public CascadeSchema enter (final String name)
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
    public CascadeSchema exit ()
    {
//        namespaces.pop();
//        implicitLogger.pop();
//        implicitPool.pop();
//        implicitPump.pop();
        return this;
    }

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
    public CascadeSchema setDefaultLogger (final CascadeLogger logger)
    {
        return this;
    }

    /**
     * Getter.
     *
     * @return the default logger.
     */
    public CascadeLogger getDefaultLogger ()
    {
        return null;
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
    public CascadeSchema usingLogger (final CascadeLogger logger)
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
    public CascadeSchema usingPool (final String name)
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
    public CascadeSchema usingPool (final AllocationPool pool)
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
    public CascadeSchema usingPump (final String name)
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
    public CascadeSchema usingPump (final PumpSchema pump)
    {
        implicitPump.push(pump.getName());
        return this;
    }

    /**
     * Use this method to construct the new system.
     *
     * @return the new system.
     */
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
        final ConcreteNode node = new ConcreteNode(schema.getName(), sharedState, schema.getBuilder().build()); // TODO: Should build have already been called???
        sharedState.namesToNodes.put(schema.getName(), node);
//        sharedState.nodesToLoggers.put(schema.getName(), schema.getLogger());
        sharedState.nodesToPools.put(schema.getName(), schema.getPool());
        sharedState.nodesToPumps.put(schema.getName(), schema.getPump());
        sharedState.pumpsToNodes.put(schema.getPump(), schema.getName());
        sharedState.network.addNode(node);
    }

    private void declare (final EdgeSchema schema)
    {
        final CascadeNode supplier = sharedState.namesToNodes.get(schema.getSupplier());
        final CascadeNode consumer = sharedState.namesToNodes.get(schema.getConsumer());

        final ConcreteEdge edge = new ConcreteEdge(sharedState, schema.getSupplier(), schema.getConsumer());
        sharedState.nodesToInputs.put(schema.getConsumer(), edge);
        sharedState.nodesToOutputs.put(schema.getSupplier(), edge);

        sharedState.network.addEdge(supplier, consumer, edge);
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
        final int minThreads = 1;
        final int maxThreads = 1;

        final CascadePump pump = new ConcretePump(schema.getName(), sharedState, minThreads, maxThreads);

        final List<ConnectionSchema> connections = Lists.newArrayList();

        // TODO: Speed up via a map!
        for (NodeSchema node : nodes.values().stream().filter(x -> x.getPump().equals(schema.getName())).collect(Collectors.toSet()))
        {
            for (EdgeSchema input : network.inEdges(node.getName()))
            {
                final CascadeNode supplier = sharedState.namesToNodes.get(input.getSupplier());
                final CascadeNode consumer = sharedState.namesToNodes.get(input.getConsumer());
                final CascadeEdge edge = ImmutableList.copyOf(sharedState.network.edgesConnecting(supplier, consumer)).get(0);

                final Context context = sharedState.namesToNodes.get(node.getName()).protoContext();
                final Core kernel = node.getBuilder().build(); // Should this have already been called????

                final int capacity = input.getQueueCapacity().orElse(16); // TODO: What should the default be????
                final MessageConsumer action = new DefaultMessageConsumer(context, kernel);

                final ConnectionSchema connection = new ConnectionSchema(edge, capacity, action);
                connections.add(connection);
            }
        }

        final DedicatedEngine engine = new DedicatedEngine(schema.getThreadFactory(), allocator, connections);

        sharedState.engines.put(schema.getName(), engine);
        sharedState.namesToPumps.put(schema.getName(), pump);

        /**
         * Map the edges to the connections.
         */
        engine.connections().entrySet().forEach(entry -> sharedState.connections.put((CascadeEdge) entry.getKey().correlationId, entry.getValue()));
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
        for (String name : nodes.keySet())
        {
            Verify.verify(sharedState.namesToNodes.containsKey(name));
            final CascadeNode node = sharedState.namesToNodes.get(name);
            final NodeSchema schema = nodes.get(name);
            Verify.verify(node.name().equals(name));
            Verify.verify(node.name().equals(schema.getName()));
            Verify.verify(node.protoContext().name().equals(name));
            Verify.verify(node.protoContext().allocator().equals(allocator));
            Verify.verify(node.protoContext().message() == null);
            Verify.verify(node.protoContext().exception() == null);
//            Verify.verify(node.protoContext().logger().equals(schema.getLogger()));
//            Objects.requireNonNull(schema.getLogger());
            Verify.verify(node.protoContext().pool().name().equals(schema.getPool()));
            Verify.verify(node.protoContext().pump().name().equals(schema.getPump()));
            Verify.verify(node.protoContext().pump().nodes().contains(node));
            Verify.verify(node.protoContext().inputs().size() == network.inDegree(name));
            Verify.verify(node.protoContext().outputs().size() == network.outDegree(name));
            Verify.verify(node.protoContext().node() == (Object) node); // Identity Equals
            node.protoContext().inputs().forEach(edge -> Verify.verify(node.equals(edge.consumer())));
            node.protoContext().outputs().forEach(edge -> Verify.verify(node.equals(edge.supplier())));
        }
    }

    public static void main (String[] args)
    {
        final CoreBuilder a = () -> new CascadeNode.Core()
        {
            @Override
            public void onSetup (Context context)
                    throws Throwable
            {
                System.out.println("A = SETUP");
            }

            @Override
            public void onStart (Context context)
                    throws Throwable
            {
                System.out.println("A = START");
            }

            @Override
            public void onMessage (final Context context)
                    throws Throwable
            {
                final int value = context.message().asInt();
                context.message().pop().push(value + 1);
//                System.out.println("A = " + value);
                context.async(context.message());
//                Thread.sleep(1000);
            }
        };

        final CoreBuilder b = () -> new CascadeNode.Core()
        {
            @Override
            public void onMessage (final Context context)
                    throws Throwable
            {
                final int value = context.message().asInt();
                context.message().pop().push(value + 1);
                if (value % 1_000_000 == 0)
                {
                    System.out.println("B = " + value);
                }
                context.async(context.message());
            }
        };

        final CoreBuilder c = () -> new CascadeNode.Core()
        {
            @Override
            public void onMessage (final Context context)
                    throws Throwable
            {
                final int value = context.message().asInt();
                context.message().pop().push(value + 1);
//                System.out.println("C = " + value);
                context.async(context.message());
            }
        };

        final CoreBuilder d = () -> new CascadeNode.Core()
        {
            @Override
            public void onMessage (final Context context)
                    throws Throwable
            {
                final int value = context.message().asInt();
                context.message().pop().push(value + 1);
                if (value % 1_000_000 == 0)
                {
                    System.out.println("D = " + value);
                }
                context.async(context.message());
            }
        };

        final CascadeSchema cs = new CascadeSchema();
        cs.enter("mackenziehigh");
        cs.usingPump("P1");
        cs.usingPool("default");
        cs.addDedicatedPump("P1");
        cs.addDedicatedPump("P2");
        cs.addDedicatedPump("P3");
        cs.addDedicatedPump("P4");
        cs.addFixedPool("default").setBufferCount(100).setMaxAllocationSize(128);
//        cs.addDynamicPool("default").setMaxAllocationSize(128);
        cs.addNode("a", a).setPump("P1").end();
        cs.addNode("b", b).setPump("P2").end();
        cs.addNode("c", c).setPump("P3").end();
        cs.addNode("d", d).setPump("P4").end();
        cs.exit();

        cs.connect("a", "a");
        cs.connect("b", "b");
        cs.connect("c", "c");
        cs.connect("d", "d");

        final Cascade cas = cs.build();
        cas.start();

        final OperandStack stack = cas.pools().get("default").allocator().newOperandStack();
        stack.push(100);

        cas.nodes().get("a").protoContext().outputs().get(0).async(stack);
        cas.nodes().get("b").protoContext().outputs().get(0).async(stack);
        cas.nodes().get("c").protoContext().outputs().get(0).async(stack);
        cas.nodes().get("d").protoContext().outputs().get(0).async(stack);
    }
}
