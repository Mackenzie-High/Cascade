package com.mackenziehigh.cascade.internal.schema;

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
import com.mackenziehigh.cascade.cores.Cores;
import com.mackenziehigh.cascade.internal.StandardLogger;
import com.mackenziehigh.cascade.internal.engines.Connection;
import com.mackenziehigh.cascade.internal.engines.LinearArrayQueue;
import com.mackenziehigh.cascade.internal.messages.ConcreteAllocator;
import com.mackenziehigh.cascade.internal.routing.EventDispatcher;
import com.mackenziehigh.cascade.internal.routing.EventDispatcher.ConcurrentEventSender;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

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

    private final ConcreteAllocator allocator = new ConcreteAllocator();

    private final Map<CascadeToken, AllocationPool> namesToPools = Maps.newHashMap();

    private final Map<CascadeToken, ConcretePump> namesToPumps = Maps.newHashMap();

    private final Multimap<CascadeToken, ConcreteReactor> pumpsToReactors = LinkedListMultimap.create();

    /**
     * Maps the name of a reactor to the corresponding connection.
     */
    private final Map<CascadeToken, Connection> reactorConnections = Maps.newConcurrentMap();

    private EventDispatcher dispatcher;

    private final ConcreteCascade cascade = new ConcreteCascade();

    public ConcreteSchema ()
    {
        scope.logger = new StandardLogger(CascadeToken.create("default"));
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
        reactors.forEach(x -> reactorConnections.put(x.name, x.queue));
        dispatcher = new EventDispatcher(reactorConnections);
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

    private void preventChange (String entity,
                                Object original,
                                Object value)
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

        public Connection queue;

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
                return loggerFactory.create(name);
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
        public ReactorSchema withLinearSharedQueue (final String group,
                                                    final int queueCapacity,
                                                    final int backlogCapacity)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ReactorSchema withLinearArrayQueue (final int queueCapacity)
        {
            final QueueType type = QueueType.LINEAR_ARRAY;
            preventChange("Queue Type", this.queueType, type);
            this.queueType = type;
            this.queueCapacity = queueCapacity;
            this.queue = new LinearArrayQueue(allocator, queueCapacity);
            return this;
        }

        @Override
        public ReactorSchema withLinearLinkedQueue (final int queueCapacity)
        {
            final QueueType type = QueueType.LINEAR_LINKED;
            preventChange("Queue Type", this.queueType, type);
            this.queueType = type;
            this.queueCapacity = queueCapacity;
            return this;
        }

        @Override
        public ReactorSchema withCircularArrayQueue (final int queueCapacity)
        {
            final QueueType type = QueueType.CIRCULAR_ARRAY;
            preventChange("Queue Type", this.queueType, type);
            this.queueType = type;
            this.queueCapacity = queueCapacity;
            return this;
        }

        @Override
        public ReactorSchema withCircularLinkedQueue (final int queueCapacity)
        {
            final QueueType type = QueueType.CIRCULAR_LINKED;
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

    public static void main (String[] args)
    {
        final CascadeSchema cs = new ConcreteSchema().named("Schema1");

        cs.addDynamicPool().named("default").withMinimumSize(0).withMaximumSize(256);
        cs.addFixedPool().named("pool2").withMinimumSize(512).withMaximumSize(768).withBufferCount(10);
        cs.addCompositePool().named("pool3").withMemberPool("default").withMemberPool("pool2");

        cs.addPump().named("pump1").withThreadCount(3);

        cs.usingPool("default").usingPump("pump1");

        cs.addReactor()
                .named("clock1")
                .withCore(Cores.newTicker().withPeriod(50, TimeUnit.MILLISECONDS).withFormatMonotonicNanos().sendTo("tickTock").build())
                .withLinearArrayQueue(4);

        cs.addReactor()
                .named("printer1")
                .withLinearArrayQueue(7)
                .withCore(Cores.from(x -> System.out.println("X = " + x.message().asString() + ", Thread = " + Thread.currentThread().getId())))
                .subscribeTo("tickTock");

        final Cascade cas = cs.build();

        cas.start();

        cas.reactors().get(CascadeToken.create("clock1")).broadcast(CascadeToken.create("toggle"), cas.allocator().newOperandStack());
    }
}
