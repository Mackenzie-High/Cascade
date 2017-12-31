package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator.AllocationPool;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadeReactor;
import com.mackenziehigh.cascade.CascadeReactor.Core;
import com.mackenziehigh.cascade.CascadeSchema;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.internal.EventDispatcher.ConcurrentEventSender;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

/**
 * TODO: The user must always specify a default pool.
 */
public final class ConcreteSchema
        implements CascadeSchema
{
    // TODO: Set uncaught exception handler
    private final ThreadFactory defaultThreadFactory = new ThreadFactoryBuilder().setDaemon(false).build();

    /**
     * This is the name() of the Cascade object being built.
     */
    private final CascadeToken name;

    /**
     * This contains the "using" settings for the current namespace.
     */
    private final Scope scope = new Scope();

    /**
     * There are the names of all of the pools that have been added thus far.
     */
    private final Set<CascadeToken> declaredPools = Sets.newHashSet();

    /**
     * There are the names of all of the pumps that have been added thus far.
     */
    private final Set<CascadeToken> declaredPumps = Sets.newHashSet();

    /**
     * There are the names of all of the reactors that have been added thus far.
     */
    private final Set<CascadeToken> declaredReactors = Sets.newHashSet();

    /**
     * This map maps the name of an allocation-pool to the corresponding configuration.
     */
    private final Map<CascadeToken, DynamicPoolSchemaImp> dynamicPools = Maps.newHashMap();

    /**
     * This map maps the name of an allocation-pool to the corresponding configuration.
     */
    private final Map<CascadeToken, FixedPoolSchemaImp> fixedPools = Maps.newHashMap();

    /**
     * This map maps the name of an allocation-pool to the corresponding configuration.
     */
    private final Map<CascadeToken, CompositePoolSchemaImp> compositePools = Maps.newHashMap();

    /**
     * This map maps the name of a pump to the corresponding configuration.
     */
    private final Map<CascadeToken, PumpSchemaImp> pumps = Maps.newHashMap();

    /**
     * This map maps the name of a reactor to the corresponding configuration.
     */
    private final Map<CascadeToken, ReactorSchemaImp> reactors = Maps.newHashMap();

    /**
     * Eventually, this map will map the name of a pool to the pool itself.
     */
    private final Map<CascadeToken, AllocationPool> namesToPools = Maps.newHashMap();

    /**
     * Eventually, this map will map the name of a pump to the pump itself.
     */
    private final Map<CascadeToken, ConcretePump> namesToPumps = Maps.newHashMap();

    /**
     * Eventually, this map will map the name of a reactor to the reactor itself.
     */
    private final Multimap<CascadeToken, ConcreteReactor> pumpsToReactors = LinkedListMultimap.create();

    /**
     * This map maps the name of a reactor to the corresponding queue
     * that will be used to store pending event-messages that are
     * flowing into that particular reactor.
     */
    private final Map<CascadeToken, InflowQueue> reactorsToQueues = Maps.newConcurrentMap();

    /**
     * At runtime, this object will route event-messages to
     * the reactors that subscribed to those event-messages.
     */
    private EventDispatcher dispatcher;

    /**
     * This is the schema that describes the global default allocation-pool,
     * if the user explicitly specified such a pool.
     */
    private CascadeToken defaultPool;

    /**
     * This is the object that is being built/configured by this schema.
     */
    private final ConcreteCascade cascade = new ConcreteCascade();

    /**
     * This is the allocator() of the Cascade object.
     */
    private final ConcreteAllocator allocator = new ConcreteAllocator(cascade);

    /**
     * This is used to prevent build() from being called again.
     */
    private boolean built;

    /**
     * Sole constructor.
     *
     * @param name will be the name of the cascade object.
     */
    public ConcreteSchema (final String name)
    {
        this.name = CascadeToken.create(name);
        resetScope();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeSchema enter (final String name)
    {
        Preconditions.checkNotNull(name, "name");
        final CascadeToken token = CascadeToken.create(name);
        resetScope();
        scope.namespace = token;
        return this;
    }

    private void resetScope ()
    {
        scope.logger = site -> new StandardLogger(site);
        scope.pool = null;
        scope.pump = null;
        scope.queueType = QueueType.LINKED;
        scope.queueCapacity = Integer.MAX_VALUE;
    }

    private void requireNamespace ()
    {
        if (scope.namespace == null)
        {
            throw new IllegalStateException("No namespace was specified!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeSchema usingLogger (final CascadeLogger.Factory factory)
    {
        Preconditions.checkNotNull(factory, "factory");
        requireNamespace();
        scope.logger = factory;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeSchema usingPool (final String name)
    {
        requireNamespace();
        scope.pool = convertName(name);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeSchema usingPump (final String name)
    {
        requireNamespace();
        scope.pump = convertName(name);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeSchema usingLinkedQueues (final int capacity)
    {
        Preconditions.checkArgument(capacity >= 0, "capacity < 0");
        requireNamespace();
        scope.queueType = QueueType.LINKED;
        scope.queueCapacity = capacity;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeSchema usingArrayQueues (final int capacity)
    {
        Preconditions.checkArgument(capacity >= 0, "capacity < 0");
        requireNamespace();
        scope.queueType = QueueType.ARRAY;
        scope.queueCapacity = capacity;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DynamicPoolSchema addDynamicPool (final String name)
    {
        requireNamespace();
        final DynamicPoolSchemaImp result = new DynamicPoolSchemaImp();
        result.name = convertName(name);
        dynamicPools.put(result.name, result);
        require(name, "Duplicate Dynamic Pool", !declaredPools.contains(result.name));
        declaredPools.add(result.name);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FixedPoolSchema addFixedPool (final String name)
    {
        requireNamespace();
        final FixedPoolSchemaImp result = new FixedPoolSchemaImp();
        result.name = convertName(name);
        fixedPools.put(result.name, result);
        require(name, "Duplicate Fixed Pool", !declaredPools.contains(result.name));
        declaredPools.add(result.name);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompositePoolSchema addCompositePool (final String name)
    {
        requireNamespace();
        final CompositePoolSchemaImp result = new CompositePoolSchemaImp();
        result.name = convertName(name);
        compositePools.put(result.name, result);
        require(name, "Duplicate Composite Pool", !declaredPools.contains(result.name));
        declaredPools.add(result.name);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PumpSchema addPump (final String name)
    {
        requireNamespace();
        final PumpSchemaImp result = new PumpSchemaImp();
        result.name = convertName(name);
        pumps.put(result.name, result);
        require(name, "Duplicate Pump", !declaredPumps.contains(result.name));
        declaredPumps.add(result.name);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReactorSchema addReactor (final String name)
    {
        requireNamespace();
        final ReactorSchemaImp result = new ReactorSchemaImp();
        result.name = convertName(name);
        result.logger = scope.logger.create(result.name);
        result.pool = scope.pool;
        result.pump = scope.pump;
        result.queueType = scope.queueType;
        result.queueCapacity = scope.queueCapacity;
        reactors.put(result.name, result);
        require(name, "Duplicate Reactor", !declaredReactors.contains(result.name));
        declaredPools.add(result.name);

        /**
         * Create a default queue, which may get replaced.
         */
        result.queue = scope.queueType == QueueType.ARRAY
                ? new ArrayInflowQueue(allocator, scope.queueCapacity)
                : new LinkedInflowQueue(allocator, scope.queueCapacity);

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cascade build ()
    {
        if (built)
        {
            throw new IllegalStateException("build() was already called once!");
        }
        else
        {
            built = true;
        }

        /**
         * The user must specify a default allocation-pool.
         */
        if (defaultPool == null)
        {
            throw new IllegalStateException("No default allocation-pool was specified!");
        }

        /**
         * The user must specify at least one pump.
         */
        if (declaredPumps.isEmpty())
        {
            throw new IllegalStateException("No pumps were specified!");
        }

        /**
         * Global.
         */
        cascade.setName(name);
        cascade.setAllocator(allocator);

        /**
         * Validate.
         */
        dynamicPools.values().forEach(x -> validate(x));
        fixedPools.values().forEach(x -> validate(x));
        compositePools.values().forEach(x -> validate(x));
        pumps.values().forEach(x -> validate(x));
        reactors.values().forEach(x -> validate(x));

        /**
         * Create the routing-table.
         */
        reactors.values().forEach(x -> reactorsToQueues.put(x.name, x.queue));
        dispatcher = new EventDispatcher(reactorsToQueues);
        reactors.values().forEach(s -> s.subscriptions.forEach(e -> dispatcher.register(s.name, e)));

        /**
         * Compile - Pass #1.
         */
        dynamicPools.values().forEach(x -> compile1(x));
        fixedPools.values().forEach(x -> compile1(x));
        compositePools.values().forEach(x -> compile1(x));

        /**
         * Compile - Pass #2.
         */
        reactors.values().forEach(x -> compile2(x));

        /**
         * Compile - Pass #3.
         */
        pumps.values().forEach(x -> compile3(x));

        /**
         * Global.
         */
        pumps.values().forEach(x -> cascade.addPump(x.pump));
        reactors.values().forEach(x -> cascade.addReactor(x.reactor));
        allocator.setDefaultPool(defaultPool);

        /**
         * Verify.
         */
        verifyCascade();
        dynamicPools.values().forEach(x -> verify(x));
        fixedPools.values().forEach(x -> verify(x));
        compositePools.values().forEach(x -> verify(x));
        pumps.values().forEach(x -> verify(x));
        reactors.values().forEach(x -> verify(x));

        /**
         * Verify - Self Tests.
         */
        cascade.selfTest();
        pumps.values().forEach(x -> x.pump.selfTest());
        reactors.values().forEach(x -> x.reactor.selfTest());

        return cascade;
    }

    private void validate (final DynamicPoolSchemaImp object)
    {
        require(object.name.name(), "Dynamic Pool: Unspecified Minimum Size", object.minimumSize != null);
        require(object.name.name(), "Dynamic Pool: Unspecified Maximum Size", object.maximumSize != null);
        require(object.name.name(), "Dynamic Pool: Minimum Size < 0", object.minimumSize >= 0);
        require(object.name.name(), "Dynamic Pool: Maximum Size < 0", object.maximumSize >= 0);
        require(object.name.name(), "Dynamic Pool: Minimum Size > Maximum Size", object.minimumSize <= object.maximumSize);
    }

    private void validate (final FixedPoolSchemaImp object)
    {
        require(object.name.name(), "Fixed Pool: Unspecified Minimum Size", object.minimumSize != null);
        require(object.name.name(), "Fixed Pool: Unspecified Maximum Size", object.maximumSize != null);
        require(object.name.name(), "Fixed Pool: Unspecified Buffer Count", object.bufferCount != null);
        require(object.name.name(), "Fixed Pool: Minimum Size < 0", object.minimumSize >= 0);
        require(object.name.name(), "Fixed Pool: Maximum Size < 0", object.maximumSize >= 0);
        require(object.name.name(), "Fixed Pool: Buffer Count < 0", object.bufferCount >= 0);
        require(object.name.name(), "Fixed Pool: Minimum Size > Maximum Size", object.minimumSize <= object.maximumSize);
    }

    private void validate (final CompositePoolSchemaImp object)
    {
        /**
         * The fallback pool must exist.
         */
        if (object.fallback != null)
        {
            require(object.name.name(), "Composite Pool: No Such Fallback Pool (" + object.fallback + ")", declaredPools.contains(object.fallback));
        }

        /**
         * All of the member pools must exist.
         */
        object.members.forEach(x -> require(object.name.name(), "Composite Pool: No Such Member Pool (" + x + ")", declaredPools.contains(x)));

        /**
         * The fallback-pool cannot be a composite-pool.
         */
        if (object.fallback != null)
        {
            require(object.name.name(), "Composite Pool: Composite Fallback Pool (" + object.fallback + ")", !compositePools.containsKey(object.fallback));
        }

        /**
         * The member-pools cannot be composite-pools.
         */
        object.members.forEach(x -> require(object.name.name(), "Composite Pool: Composite Member Pool (" + x + ")", !compositePools.containsKey(x)));
    }

    private void validate (final PumpSchemaImp object)
    {
        require(object.name.name(), "Pump: Unspecified Name", object.name != null);
        require(object.name.name(), "Pump: Unspecified Thread Count", object.threadCount != null);
        require(object.name.name(), "Pump: Thread Count < 0", object.threadCount >= 0);
    }

    private void validate (final ReactorSchemaImp object)
    {
        require(object.name.name(), "Reactor: Unspecified Name", object.name != null);
        require(object.name.name(), "Reactor: Unspecified Logger", object.logger != null);
        require(object.name.name(), "Reactor: Unspecified Allocation Pool", object.pool != null);
        require(object.name.name(), "Reactor: Unspecified Pump", object.pump != null);
        require(object.name.name(), "Reactor: Unspecified Core", object.core != null);
        require(object.name.name(), "Reactor: Unspecified Queue Type", object.queueType != null);
    }

    private void compile1 (final DynamicPoolSchemaImp object)
    {
        object.pool = allocator.addDynamicPool(object.name,
                                               object.minimumSize,
                                               object.maximumSize);

        namesToPools.put(object.name, object.pool);
    }

    private void compile1 (final FixedPoolSchemaImp object)
    {
        object.pool = allocator.addFixedPool(object.name,
                                             object.minimumSize,
                                             object.maximumSize,
                                             object.bufferCount);

        namesToPools.put(object.name, object.pool);
    }

    private void compile1 (final CompositePoolSchemaImp object)
    {
        final AllocationPool fallback = object.fallback == null ? null : allocator.pools().get(object.fallback);
        final List<AllocationPool> delegates = object.members.stream().map(x -> allocator.pools().get(x)).collect(Collectors.toList());
        object.pool = allocator.addCompositePool(object.name, fallback, delegates);
        namesToPools.put(object.name, object.pool);
    }

    private void compile2 (final ReactorSchemaImp object)
    {
        Verify.verify(object.name != null);
        Verify.verify(object.core != null);
        Verify.verify(object.pool != null);
        Verify.verify(object.logger != null);
        Verify.verify(object.queue != null);

        final ConcurrentEventSender sender = dispatcher.lookup(object.name);

        final AllocationPool pool = allocator.pools().get(object.pool);

        object.reactor = new ConcreteReactor(cascade,
                                             object.name,
                                             object.core,
                                             pool,
                                             object.pump,
                                             object.logger,
                                             object.queue,
                                             sender);

        pumpsToReactors.put(object.pump, object.reactor);
    }

    private void compile3 (final PumpSchemaImp object)
    {
        final ThreadFactory factory = object.threadFactory != null ? object.threadFactory : defaultThreadFactory;

        object.pump = new ConcretePump(cascade,
                                       object.name,
                                       pumpsToReactors.get(object.name),
                                       factory,
                                       object.threadCount);

        namesToPumps.put(object.pump.name(), object.pump);
    }

    private void verifyCascade ()
    {
        Verify.verifyNotNull(cascade.name());
        Verify.verifyNotNull(cascade.uuid());
        Verify.verifyNotNull(cascade.allocator());
        Verify.verifyNotNull(cascade.phase());
        Verify.verifyNotNull(cascade.pumps());
        Verify.verifyNotNull(cascade.reactors());
        Verify.verify(cascade.name().equals(name));
        Verify.verify(cascade.name().toString().equals(cascade.toString()));
        Verify.verify(cascade.name().name().equals(cascade.toString()));
        Verify.verify(cascade.phase().equals(Cascade.ExecutionPhase.INITIAL));
    }

    private void verify (final DynamicPoolSchemaImp object)
    {
        Verify.verifyNotNull(object.pool);
        Verify.verifyNotNull(object.name);
        Verify.verifyNotNull(object.minimumSize);
        Verify.verifyNotNull(object.maximumSize);
        Verify.verify(object.minimumSize >= 0);
        Verify.verify(object.maximumSize >= 0);
        Verify.verify(object.minimumSize <= object.maximumSize);
        Verify.verify(object.pool.name().equals(object.name));
        Verify.verify(object.minimumSize.equals(object.pool.minimumAllocationSize()));
        Verify.verify(object.maximumSize.equals(object.pool.maximumAllocationSize()));
        Verify.verify(object.pool.isFixed() == false);
        Verify.verify(object.pool.size().isPresent() == false);
        Verify.verify(object.pool.capacity().isPresent() == false);
        Verify.verify(object.pool.allocator().equals(cascade.allocator()));
        Verify.verify(cascade.allocator().pools().get(object.name).equals(object.pool));
        Verify.verify(cascade.equals(object.pool.allocator().cascade()));
    }

    private void verify (final FixedPoolSchemaImp object)
    {
        Verify.verifyNotNull(object.pool);
        Verify.verifyNotNull(object.name);
        Verify.verifyNotNull(object.minimumSize);
        Verify.verifyNotNull(object.maximumSize);
        Verify.verifyNotNull(object.bufferCount);
        Verify.verify(object.minimumSize >= 0);
        Verify.verify(object.maximumSize >= 0);
        Verify.verify(object.minimumSize <= object.maximumSize);
        Verify.verify(object.bufferCount >= 0);
        Verify.verify(object.pool.name().equals(object.name));
        Verify.verify(object.minimumSize.equals(object.pool.minimumAllocationSize()));
        Verify.verify(object.maximumSize.equals(object.pool.maximumAllocationSize()));
        Verify.verify(object.pool.isFixed());
        Verify.verify(object.pool.size().isPresent());
        Verify.verify(object.pool.capacity().isPresent());
        Verify.verify(object.pool.size().getAsLong() == 0);
        Verify.verify(object.pool.capacity().getAsLong() == object.bufferCount);
        Verify.verify(object.pool.allocator().equals(cascade.allocator()));
        Verify.verify(cascade.allocator().pools().get(object.name).equals(object.pool));
        Verify.verify(cascade.equals(object.pool.allocator().cascade()));
    }

    private void verify (final CompositePoolSchemaImp object)
    {
        Verify.verifyNotNull(object.pool);
        Verify.verifyNotNull(object.name);
        Verify.verify(object.pool.name().equals(object.name));
        Verify.verify(object.pool.size().isPresent() == false);
        Verify.verify(object.pool.capacity().isPresent() == false);
        Verify.verify(object.pool.allocator().equals(cascade.allocator()));
        Verify.verify(cascade.allocator().pools().get(object.name).equals(object.pool));
        Verify.verify(cascade.equals(object.pool.allocator().cascade()));
    }

    private void verify (final PumpSchemaImp object)
    {
        Verify.verifyNotNull(object.pump);
        Verify.verifyNotNull(object.name);
        Verify.verifyNotNull(object.threadCount);
        Verify.verify(cascade.equals(object.pump.cascade()));
        Verify.verify(object.pump.name().equals(object.name));
        Verify.verify(object.pump.threads().size() == object.threadCount);
        Verify.verify(cascade.pumps().get(object.name).equals(object.pump));
    }

    private void verify (final ReactorSchemaImp object)
    {
        Verify.verifyNotNull(object.reactor);
        Verify.verifyNotNull(object.core);
        Verify.verifyNotNull(object.name);
        Verify.verifyNotNull(object.queueType);
        Verify.verifyNotNull(object.queueCapacity);
        Verify.verifyNotNull(object.queue);
        Verify.verifyNotNull(object.logger);
        Verify.verifyNotNull(object.pool);
        Verify.verifyNotNull(object.pump);
        Verify.verify(cascade.equals(object.reactor.cascade()));
        Verify.verify(object.reactor.name().equals(object.name));
        Verify.verify(object.reactor.allocator().equals(cascade.allocator()));
        Verify.verify(object.reactor.pool().name().equals(object.pool));
        Verify.verify(object.reactor.core().equals(object.core));
        Verify.verify(object.reactor.input().equals(object.queue));
        Verify.verify(object.reactor.queueCapacity() == object.queueCapacity);
        Verify.verifyNotNull(object.reactor.logger());
        Verify.verify(object.reactor.pump().name().equals(object.pump));
        Verify.verify(cascade.pumps().get(object.reactor.pump().name()).equals(object.reactor.pump()));
        Verify.verify(cascade.reactors().get(object.reactor.name()).equals(object.reactor));
        Verify.verify(object.reactor.pump().reactors().contains(object.reactor));
    }

    private void require (final String site,
                          final String message,
                          final boolean condition)
    {
        if (condition == false)
        {
            throw new RuntimeException("(" + site + ") " + message);
        }
    }

    private CascadeToken convertName (final String name)
    {
        Preconditions.checkNotNull(name, "name");

        final CascadeToken token = CascadeToken.create(name);

        if (token.isSimpleName() && scope.namespace != null)
        {
            return scope.namespace.append(name);
        }
        else
        {
            return token;
        }
    }

    private final class DynamicPoolSchemaImp
            implements DynamicPoolSchema
    {
        public CascadeToken name;

        public Integer minimumSize = 0;

        public Integer maximumSize = Integer.MAX_VALUE;

        public AllocationPool pool;

        @Override
        public DynamicPoolSchema withMinimumSize (final int bound)
        {
            this.minimumSize = bound;
            return this;
        }

        @Override
        public DynamicPoolSchema withMaximumSize (final int bound)
        {
            this.maximumSize = bound;
            return this;
        }

        @Override
        public DynamicPoolSchema makeGlobalDefault ()
        {
            defaultPool = name;
            return this;
        }
    };

    private final class FixedPoolSchemaImp
            implements FixedPoolSchema
    {
        public CascadeToken name;

        public Integer minimumSize;

        public Integer maximumSize;

        public Integer bufferCount;

        public AllocationPool pool;

        @Override
        public FixedPoolSchema withMinimumSize (final int bound)
        {
            this.minimumSize = bound;
            return this;
        }

        @Override
        public FixedPoolSchema withMaximumSize (final int bound)
        {
            this.maximumSize = bound;
            return this;
        }

        @Override
        public FixedPoolSchema withBufferCount (final int count)
        {
            this.bufferCount = count;
            return this;
        }

        @Override
        public FixedPoolSchema makeGlobalDefault ()
        {
            defaultPool = name;
            return this;
        }

    };

    private final class CompositePoolSchemaImp
            implements CompositePoolSchema
    {
        public CascadeToken name;

        public CascadeToken fallback;

        public final Set<CascadeToken> members = Sets.newHashSet();

        public AllocationPool pool;

        @Override
        public CompositePoolSchema withFallbackPool (final String name)
        {
            final CascadeToken token = convertName(name);
            this.fallback = token;
            return this;
        }

        @Override
        public CompositePoolSchema withMemberPool (final String name)
        {
            final CascadeToken member = convertName(name);
            members.add(member);
            return this;
        }

        @Override
        public CompositePoolSchema makeGlobalDefault ()
        {
            defaultPool = name;
            return this;
        }

    };

    private final class PumpSchemaImp
            implements PumpSchema
    {

        public CascadeToken name;

        public ThreadFactory threadFactory = defaultThreadFactory;

        public Integer threadCount = 1;

        public ConcretePump pump;

        @Override
        public PumpSchema usingThreadFactory (final ThreadFactory factory)
        {
            this.threadFactory = factory;
            return this;
        }

        @Override
        public PumpSchema withThreadCount (final int count)
        {
            this.threadCount = count;
            return this;
        }

    };

    private enum QueueType
    {
        ARRAY,
        LINKED,
    }

    private final class ReactorSchemaImp
            implements ReactorSchema
    {

        public CascadeToken name;

        public Core core;

        public CascadeLogger logger;

        public CascadeToken pool;

        public CascadeToken pump;

        public QueueType queueType;

        public Integer queueCapacity;

        public InflowQueue queue;

        public final SortedSet<CascadeToken> subscriptions = Sets.newTreeSet();

        public ConcreteReactor reactor;

        @Override
        public ReactorSchema withCore (final CascadeReactor.Core core)
        {
            // Prevent Nulls
            final Set<CascadeToken> initial = ImmutableSet.copyOf(core.initialSubscriptions());
            subscriptions.addAll(initial);
            this.core = core;
            return this;
        }

        @Override
        public ReactorSchema withCore (final CascadeReactor.CoreBuilder core)
        {
            return withCore(core.build());
        }

        @Override
        public ReactorSchema usingLogger (final CascadeLogger.Factory factory)
        {
            Verify.verifyNotNull(name);
            this.logger = factory.create(name);
            return this;
        }

        @Override
        public ReactorSchema usingPool (final String name)
        {
            this.pool = convertName(name);
            return this;
        }

        @Override
        public ReactorSchema usingPump (final String name)
        {
            this.pump = convertName(name);
            return this;
        }

        @Override
        public ReactorSchema withArrayQueue (final int capacity)
        {
            Preconditions.checkArgument(capacity >= 0, "capacity < 0");
            final QueueType type = QueueType.ARRAY;
            this.queueType = type;
            this.queueCapacity = capacity;
            this.queue = new ArrayInflowQueue(allocator, capacity);
            return this;
        }

        @Override
        public ReactorSchema withLinkedQueue (final int capacity)
        {
            Preconditions.checkArgument(capacity >= 0, "capacity < 0");
            final QueueType type = QueueType.LINKED;
            this.queueType = type;
            this.queueCapacity = capacity;
            this.queue = new LinkedInflowQueue(allocator, capacity);
            return this;
        }

        @Override
        public ReactorSchema subscribeTo (final String event)
        {
            final CascadeToken token = CascadeToken.create(event);
            subscriptions.add(token);
            return this;
        }
    };

    private final class Scope
    {
        public CascadeToken namespace;

        public CascadeLogger.Factory logger;

        public CascadeToken pool;

        public CascadeToken pump;

        public QueueType queueType;

        public Integer queueCapacity;
    }

}
