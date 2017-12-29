package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableMap;
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
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ThreadFactory;

/**
 * TODO: The user must always specify a default pool.
 */
public final class ConcreteSchema
        implements CascadeSchema
{
    // TODO: Set uncaught exception handler
    private final ThreadFactory defaultThreadFactory = new ThreadFactoryBuilder().setDaemon(false).build();

    private CascadeToken name;

    private Scope scope = new Scope();

    private final Set<DynamicPoolSchemaImp> dynamicPools = Sets.newHashSet();

    private final Set<FixedPoolSchemaImp> fixedPools = Sets.newHashSet();

    private final Set<CompositePoolSchemaImp> compositePools = Sets.newHashSet();

    private final Set<PumpSchemaImp> pumps = Sets.newHashSet();

    private final Set<ReactorSchemaImp> reactors = Sets.newHashSet();

    private final Map<CascadeToken, AllocationPool> namesToPools = Maps.newHashMap();

    private final Map<CascadeToken, ConcretePump> namesToPumps = Maps.newHashMap();

    private final Multimap<CascadeToken, ConcreteReactor> pumpsToReactors = LinkedListMultimap.create();

    /**
     * Maps the name of a reactor to the corresponding queue.
     */
    private final Map<CascadeToken, InflowQueue> reactorsToQueues = Maps.newConcurrentMap();

    private EventDispatcher dispatcher;

    private final ConcreteCascade cascade = new ConcreteCascade();

    private final ConcreteAllocator allocator = new ConcreteAllocator(cascade);

    private CascadeLogger globalDefaultLogger = new StandardLogger(CascadeToken.create("default"));

    public ConcreteSchema ()
    {
        scope.logger = globalDefaultLogger;
    }

    @Override
    public CascadeSchema named (final String name)
    {
        preventChange("Name", this.name, name);
        this.name = convertName(name);
        return this;
    }

    @Override
    public CascadeSchema begin (final String name)
    {
        Preconditions.checkNotNull(name, "name");
        final CascadeToken token = CascadeToken.create(name);
        final Scope newScope = new Scope();
        newScope.below = scope;
        newScope.namespace = token.isSimpleName() && scope.namespace != null ? scope.namespace.append(name) : token;
        newScope.logger = scope.logger;
        newScope.loggerFactory = scope.loggerFactory;
        newScope.pool = scope.pool;
        newScope.pump = scope.pump;
        scope = newScope;
        return this;
    }

    @Override
    public CascadeSchema end ()
    {
        Preconditions.checkState(scope.below != null, "At Bottom Scope");
        scope = scope.below;
        return this;

    }

    @Override
    public CascadeSchema usingLogger (final CascadeLogger.Factory factory)
    {
        scope.loggerFactory = factory;
        scope.logger = null;
        return this;
    }

    @Override
    public CascadeSchema usingLogger (final CascadeLogger logger)
    {
        scope.loggerFactory = null;
        scope.logger = logger;
        return this;
    }

    @Override
    public CascadeSchema usingPool (final String name)
    {
        scope.pool = convertName(name);
        return this;
    }

    @Override
    public CascadeSchema usingPump (final String name)
    {
        scope.pump = convertName(name);
        return this;
    }

    @Override
    public DynamicPoolSchema addDynamicPool ()
    {
        final DynamicPoolSchemaImp result = new DynamicPoolSchemaImp();
        dynamicPools.add(result);
        return result;
    }

    @Override
    public FixedPoolSchema addFixedPool ()
    {
        final FixedPoolSchemaImp result = new FixedPoolSchemaImp();
        fixedPools.add(result);
        return result;
    }

    @Override
    public CompositePoolSchema addCompositePool ()
    {
        final CompositePoolSchemaImp result = new CompositePoolSchemaImp();
        compositePools.add(result);
        return result;
    }

    @Override
    public PumpSchema addPump ()
    {
        final PumpSchemaImp result = new PumpSchemaImp();
        pumps.add(result);
        return result;
    }

    @Override
    public ReactorSchema addReactor ()
    {
        final ReactorSchemaImp result = new ReactorSchemaImp();
        result.defaultLogger = scope.logger;
        result.defaultLoggerFactory = scope.loggerFactory;
        result.defaultPool = scope.pool;
        result.defaultPump = scope.pump;
        reactors.add(result);
        return result;
    }

    @Override
    public Cascade build ()
    {
        /**
         * Global.
         */
        cascade.setName(name);
        cascade.setAllocator(allocator);
        cascade.setDefaultLogger(globalDefaultLogger);

        /**
         * Declare.
         */
        dynamicPools.forEach(x -> declare(x));
        fixedPools.forEach(x -> declare(x));
        compositePools.forEach(x -> declare(x));
        pumps.forEach(x -> declare(x));
        reactors.forEach(x -> declare(x));

        /**
         * Validate.
         */
        dynamicPools.forEach(x -> validate(x));
        fixedPools.forEach(x -> validate(x));
        compositePools.forEach(x -> validate(x));
        pumps.forEach(x -> validate(x));
        reactors.forEach(x -> validate(x));

        /**
         * Create the routing-table.
         */
        reactors.forEach(x -> reactorsToQueues.put(x.name, x.queue));
        dispatcher = new EventDispatcher(reactorsToQueues);
        reactors.forEach(s -> s.subscriptions.forEach(e -> dispatcher.register(s.name, e)));

        /**
         * Compile - Pass #1.
         */
        dynamicPools.forEach(x -> compile1(x));
        fixedPools.forEach(x -> compile1(x));
        compositePools.forEach(x -> compile1(x));

        /**
         * Compile - Pass #2.
         */
        reactors.forEach(x -> compile2(x));

        /**
         * Compile - Pass #3.
         */
        pumps.forEach(x -> compile3(x));

        /**
         * Global.
         */
        pumps.forEach(x -> cascade.addPump(x.pump));
        reactors.forEach(x -> cascade.addReactor(x.reactor));

        /**
         * Verify.
         */
        verifyCascade();
        dynamicPools.forEach(x -> verify(x));
        fixedPools.forEach(x -> verify(x));
        compositePools.forEach(x -> verify(x));
        pumps.forEach(x -> verify(x));
        reactors.forEach(x -> verify(x));

        /**
         * Verify - Self Tests.
         */
        cascade.selfTest();
        pumps.forEach(x -> x.pump.selfTest());
        reactors.forEach(x -> x.reactor.selfTest());

        return cascade;
    }

    private void declare (final DynamicPoolSchemaImp object)
    {

    }

    private void declare (final FixedPoolSchemaImp object)
    {

    }

    private void declare (final CompositePoolSchemaImp object)
    {

    }

    private void declare (final PumpSchemaImp object)
    {

    }

    private void declare (final ReactorSchemaImp object)
    {

    }

    private void validate (final DynamicPoolSchemaImp object)
    {
        require("Dynamic Pool: Unspecified Name", object.name != null);
        require("Dynamic Pool: Unspecified Minimum Size", object.minimumSize != null);
        require("Dynamic Pool: Unspecified Maximum Size", object.maximumSize != null);
        require("Dynamic Pool: Minimum Size > Maximum Size", object.minimumSize <= object.maximumSize);
    }

    private void validate (final FixedPoolSchemaImp object)
    {
        require("Fixed Pool: Unspecified Name", object.name != null);
        require("Fixed Pool: Unspecified Minimum Size", object.minimumSize != null);
        require("Fixed Pool: Unspecified Maximum Size", object.maximumSize != null);
        require("Fixed Pool: Unspecified Buffer Count", object.bufferCount != null);
        require("Fixed Pool: Minimum Size > Maximum Size", object.minimumSize <= object.maximumSize);
    }

    private void validate (final CompositePoolSchemaImp object)
    {

    }

    private void validate (final PumpSchemaImp object)
    {
        require("Pump: Unspecified Name", object.name != null);
        require("Pump: Unspecified Thread Count", object.threadCount != null);
        // TODO: Thread Factory
    }

    private void validate (final ReactorSchemaImp object)
    {
        require("Reactor: Unspecified Name", object.name != null);
        require("Reactor: Unspecified Logger", object.logger != null || object.defaultLogger != null || object.loggerFactory != null || object.defaultLoggerFactory != null);
        require("Reactor: Unspecified Allocation Pool", object.pool != null || object.defaultPool != null);
        require("Reactor: Unspecified Pump", object.pump != null || object.defaultPump != null);
        require("Reactor: Unspecified Core", object.core != null);
        require("Reactor: Unspecified Queue Type", object.queueType != null);
    }

    private void compile1 (final DynamicPoolSchemaImp object)
    {
        object.pool = allocator.addDynamicPool(object.name.name(),
                                               object.minimumSize,
                                               object.maximumSize);

        namesToPools.put(object.name, object.pool);
    }

    private void compile1 (final FixedPoolSchemaImp object)
    {
        object.pool = allocator.addFixedPool(object.name.name(),
                                             object.minimumSize,
                                             object.maximumSize,
                                             object.bufferCount);

        namesToPools.put(object.name, object.pool);
    }

    private void compile1 (final CompositePoolSchemaImp object)
    {
        namesToPools.put(object.name, object.pool);
    }

    private void compile2 (final ReactorSchemaImp object)
    {
        Verify.verify(object.name != null);
        Verify.verify(object.core != null);
        Verify.verify(object.getPool() != null);
        Verify.verify(object.getLogger() != null);
        Verify.verify(object.queue != null);

        final ConcurrentEventSender sender = dispatcher.lookup(object.name);

        object.pump = object.pump == null ? object.defaultPump : object.pump;

        object.reactor = new ConcreteReactor(cascade,
                                             object.name,
                                             object.core,
                                             object.getPool(),
                                             object.pump,
                                             object.getLogger(),
                                             ImmutableMap.of(), // TODO
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
        Verify.verifyNotNull(cascade.defaultLogger());
        Verify.verifyNotNull(cascade.pumps());
        Verify.verifyNotNull(cascade.reactors());
        Verify.verify(cascade.name().equals(name));
        Verify.verify(cascade.name().toString().equals(cascade.toString()));
        Verify.verify(cascade.name().name().equals(cascade.toString()));
        Verify.verify(cascade.defaultLogger().equals(globalDefaultLogger));     // TODO: Make sure this is always true!
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
        Verify.verify(object.pool.name().equals(object.name.name()));
        Verify.verify(object.minimumSize.equals(object.pool.minimumAllocationSize()));
        Verify.verify(object.maximumSize.equals(object.pool.maximumAllocationSize()));
        Verify.verify(object.pool.isFixed() == false);
        Verify.verify(object.pool.size().isPresent() == false);
        Verify.verify(object.pool.capacity().isPresent() == false);
        Verify.verify(object.pool.allocator().equals(cascade.allocator()));
        Verify.verify(cascade.allocator().pools().get(object.name.name()).equals(object.pool));
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
        Verify.verify(object.pool.name().equals(object.name.name()));
        Verify.verify(object.minimumSize.equals(object.pool.minimumAllocationSize()));
        Verify.verify(object.maximumSize.equals(object.pool.maximumAllocationSize()));
        Verify.verify(object.pool.isFixed());
        Verify.verify(object.pool.size().isPresent());
        Verify.verify(object.pool.capacity().isPresent());
        Verify.verify(object.pool.size().getAsLong() == 0);
        Verify.verify(object.pool.capacity().getAsLong() == object.bufferCount);
        Verify.verify(object.pool.allocator().equals(cascade.allocator()));
        Verify.verify(cascade.allocator().pools().get(object.name.name()).equals(object.pool));
        Verify.verify(cascade.equals(object.pool.allocator().cascade()));
    }

    private void verify (final CompositePoolSchemaImp object)
    {

    }

    private void verify (final PumpSchemaImp object)
    {
        Verify.verifyNotNull(object.pump);
        Verify.verifyNotNull(object.name);
        Verify.verifyNotNull(object.threadCount);
//        Verify.verifyNotNull(object.threadFactory); TODO: ??????????????????????????
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
        Verify.verifyNotNull(object.getLogger());
        Verify.verifyNotNull(object.getPool());
        Verify.verifyNotNull(object.getPump());
        Verify.verify(cascade.equals(object.reactor.cascade()));
        Verify.verify(object.reactor.name().equals(object.name));
        Verify.verify(object.reactor.allocator().equals(cascade.allocator()));
        Verify.verify(object.reactor.pool().equals(object.getPool()));
        Verify.verify(object.reactor.core().equals(object.core));
        Verify.verify(object.reactor.input().equals(object.queue));
        Verify.verify(object.reactor.queueCapacity() == object.queueCapacity);
        // Verify.verify(object.reactor.logger().equals(object.getLogger())); TODO: currently may differ becasue getLogger does not cache!!!
        Verify.verify(object.reactor.pump().name().equals(object.pump));
        Verify.verify(cascade.pumps().get(object.reactor.pump().name()).equals(object.reactor.pump()));
        Verify.verify(cascade.reactors().get(object.reactor.name()).equals(object.reactor));
        Verify.verify(object.reactor.pump().reactors().contains(object.reactor));
    }

    private void require (final String message,
                          final boolean condition)
    {
        if (condition == false)
        {
            throw new IllegalStateException(message);
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

    private void preventChange (final String entity,
                                final Object original,
                                final Object value)
    {
        if (original != null)
        {
            throw new IllegalStateException(String.format("Redefinition of %s (%s, %s)",
                                                          entity,
                                                          String.valueOf(original),
                                                          String.valueOf(value)));
        }
    }

    private final class DynamicPoolSchemaImp
            implements DynamicPoolSchema
    {
        public CascadeToken name;

        public Integer minimumSize;

        public Integer maximumSize;

        public AllocationPool pool;

        @Override
        public DynamicPoolSchema named (final String name)
        {
            preventChange("Pool Name", this.name, name);
            this.name = convertName(name);
            return this;
        }

        @Override
        public DynamicPoolSchema withMinimumSize (final int bound)
        {
            preventChange("Minimum Size", this.minimumSize, bound);
            this.minimumSize = bound;
            return this;
        }

        @Override
        public DynamicPoolSchema withMaximumSize (int bound)
        {
            preventChange("Maximum Size", this.maximumSize, bound);
            this.maximumSize = bound;
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
        public FixedPoolSchema named (final String name)
        {
            preventChange("Pool Name", this.name, name);
            this.name = convertName(name);
            return this;
        }

        @Override
        public FixedPoolSchema withMinimumSize (final int bound)
        {
            preventChange("Minimum Size", this.minimumSize, bound);
            this.minimumSize = bound;
            return this;
        }

        @Override
        public FixedPoolSchema withMaximumSize (final int bound)
        {
            preventChange("Maximum Size", this.maximumSize, bound);
            this.maximumSize = bound;
            return this;
        }

        @Override
        public FixedPoolSchema withBufferCount (final int count)
        {
            preventChange("Buffer Count", this.bufferCount, count);
            this.bufferCount = count;
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
        public CompositePoolSchema named (final String name)
        {
            preventChange("Pool Name", this.name, name);
            this.name = convertName(name);
            return this;
        }

        @Override
        public CompositePoolSchema withFallbackPool (final String name)
        {
            final CascadeToken token = convertName(name);
            preventChange("Fallback Pool", this.fallback, token);
            return this;
        }

        @Override
        public CompositePoolSchema withMemberPool (final String name)
        {
            final CascadeToken member = convertName(name);
            members.add(member);
            return this;
        }

    };

    private final class PumpSchemaImp
            implements PumpSchema
    {

        public CascadeToken name;

        public ThreadFactory threadFactory;

        public Integer threadCount;

        public ConcretePump pump;

        @Override
        public PumpSchema named (final String name)
        {
            preventChange("Pump Name", this.name, name);
            this.name = convertName(name);
            return this;
        }

        @Override
        public PumpSchema usingThreadFactory (final ThreadFactory factory)
        {
            preventChange("Thread Factory", this.threadFactory, threadFactory);
            this.threadFactory = factory;
            return this;
        }

        @Override
        public PumpSchema withThreadCount (final int count)
        {
            preventChange("Buffer Count", this.threadCount, count);
            this.threadCount = count;
            return this;
        }

    };

    private enum QueueType
    {
        LINEAR_LINKED,
        LINEAR_ARRAY,
        LINEAR_SHARED,
        CIRCULAR_LINKED,
        CIRCULAR_ARRAY,
    }

    private final class ReactorSchemaImp
            implements ReactorSchema
    {

        public CascadeToken name;

        public Core core;

        public CascadeLogger logger;

        public CascadeLogger.Factory loggerFactory;

        public CascadeLogger defaultLogger = new StandardLogger(CascadeToken.create("TODO"));

        public CascadeLogger.Factory defaultLoggerFactory;

        public CascadeToken pool;

        public CascadeToken defaultPool;

        public CascadeToken pump;

        public CascadeToken defaultPump;

        public QueueType queueType;

        public InflowQueue queue;

        public Integer queueCapacity;

        public Integer backlogCapacity;

        public final SortedSet<CascadeToken> subscriptions = Sets.newTreeSet();

        public ConcreteReactor reactor;

        public CascadeLogger getLogger ()
        {
            if (logger != null)
            {
                return logger;
            }
            else if (loggerFactory != null)
            {
                return loggerFactory.create(name); // TODO: Should this be cached? Currently called multiple times potentially.
            }
            else if (defaultLogger != null)
            {
                return defaultLogger;
            }
            else if (defaultLoggerFactory != null)
            {
                return defaultLoggerFactory.create(name);
            }
            else
            {
                return new StandardLogger(name); // TODO: Dev Null Logger Instead
            }
        }

        public AllocationPool getPool ()
        {
            if (pool != null)
            {
                return namesToPools.get(pool);
            }
            else if (defaultPool != null)
            {
                return namesToPools.get(defaultPool);
            }
            else
            {
                return allocator.defaultPool();
            }
        }

        public ConcretePump getPump ()
        {
            if (pump != null)
            {
                return namesToPumps.get(pump);
            }
            else
            {
                Verify.verify(defaultPump != null);
                return namesToPumps.get(defaultPump);
            }
        }

        @Override
        public ReactorSchema named (final String name)
        {
            preventChange("Reactor Name", this.name, name);
            this.name = convertName(name);
            return this;
        }

        @Override
        public ReactorSchema withCore (final CascadeReactor.Core core)
        {
            preventChange("Core", this.core, core);
            subscriptions.addAll(core.initialSubscriptions());
            this.core = core;
            return this;
        }

        @Override
        public ReactorSchema usingLogger (final CascadeLogger.Factory factory)
        {
            this.loggerFactory = factory;
            return this;
        }

        @Override
        public ReactorSchema usingLogger (final CascadeLogger logger)
        {
            this.logger = logger;
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
        public ReactorSchema withArrayQueue (final int queueCapacity)
        {
            final QueueType type = QueueType.LINEAR_ARRAY;
            preventChange("Queue Type", this.queueType, type);
            this.queueType = type;
            this.queueCapacity = queueCapacity;
            this.queue = new ArrayInflowQueue(allocator, queueCapacity);
            return this;
        }

        @Override
        public ReactorSchema withLinkedQueue (final int queueCapacity)
        {
            final QueueType type = QueueType.LINEAR_LINKED;
            preventChange("Queue Type", this.queueType, type);
            this.queueType = type;
            this.queueCapacity = queueCapacity;
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

        public CascadeLogger.Factory loggerFactory;

        public CascadeLogger logger;

        public CascadeToken pool;

        public CascadeToken pump;

        public Scope below;
    }

}
