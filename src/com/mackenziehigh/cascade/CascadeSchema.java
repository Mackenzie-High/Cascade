package com.mackenziehigh.cascade;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Use an instance of this class to create a Cascade object.
 */
public final class CascadeSchema
{
    /**
     * Builder.
     */
    public abstract class AbstractAllocatorSchema
    {
        private final String name;

        private AbstractAllocatorSchema (final String name)
        {
            Preconditions.checkNotNull(name, "name");
            this.name = name;
        }

        public String getName ()
        {
            return name;
        }
    }

    /**
     * Builder.
     */
    public final class DynamicAllocatorSchema
            extends AbstractAllocatorSchema
    {
        private DynamicAllocatorSchema (final String name)
        {
            super(name);
        }
    }

    /**
     * Builder.
     */
    public final class FixedAllocatorSchema
            extends AbstractAllocatorSchema
    {
        private int blockSize = 1024;

        private int blockCount = 1024;

        private FixedAllocatorSchema (final String name)
        {
            super(name);
        }

        public int getBlockSize ()
        {
            return blockSize;
        }

        public void setBlockSize (final int blockSize)
        {
            Preconditions.checkArgument(blockSize >= 0, "blockSize < 0");
            this.blockSize = blockSize;
        }

        public int getBlockCount ()
        {
            return blockCount;
        }

        public void setBlockCount (final int blockCount)
        {
            Preconditions.checkArgument(blockCount >= 0, "blockCount < 0");
            this.blockCount = blockCount;
        }
    }

    /**
     * Builder.
     */
    public abstract class AbstractPowerplantSchema
    {
        private final String name;

        private ThreadFactory threadFactory;

        private AbstractPowerplantSchema (final String name)
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

        public void setThreadFactory (final ThreadFactory threadFactory)
        {
            Preconditions.checkNotNull(threadFactory, "threadFactory");
            this.threadFactory = threadFactory;
        }
    }

    /**
     * Builder.
     */
    public final class DirectPowerplantSchema
            extends AbstractPowerplantSchema
    {
        private DirectPowerplantSchema (final String name)
        {
            super(name);
        }

    }

    /**
     * Builder.
     */
    public final class DedicatedPowerplantSchema
            extends AbstractPowerplantSchema
    {
        private int defaultThreadCount = 1;

        private final Map<String, Integer> threadCounts = Maps.newHashMap();

        private DedicatedPowerplantSchema (final String name)
        {
            super(name);
        }

        public DedicatedPowerplantSchema setDefaultThreadCount (final int count)
        {
            Preconditions.checkArgument(count >= 1, "count < 1");
            defaultThreadCount = count;
            return this;
        }

        public int getDefaultThreadCount ()
        {
            return defaultThreadCount;
        }

        public DedicatedPowerplantSchema setThreadCount (final String actorName,
                                                         final int count)
        {
            Preconditions.checkNotNull(actorName);
            Preconditions.checkArgument(count >= 1, "count < 1");
            threadCounts.put(actorName, count);
            return this;
        }

        public int getThreadCount (final String actorName)
        {
            return threadCounts.containsKey(actorName)
                    ? threadCounts.get(actorName)
                    : defaultThreadCount;
        }
    }

    /**
     * Builder.
     */
    public final class PooledPowerplantSchema
            extends AbstractPowerplantSchema
    {
        private int threadCount = 1;

        private PooledPowerplantSchema (final String name)
        {
            super(name);
        }

        public PooledPowerplantSchema setThreadCount (final int count)
        {
            Preconditions.checkArgument(count >= 1, "count < 1");
            threadCount = count;
            return this;
        }

        public int getThreadCount ()
        {
            return threadCount;
        }
    }

    /**
     * Builder.
     */
    public final class SpawningPowerplantSchema
            extends AbstractPowerplantSchema
    {
        private int minimumThreadCount = 1;

        private int maximumThreadCount = Integer.MAX_VALUE;

        private SpawningPowerplantSchema (final String name)
        {
            super(name);
        }

        public SpawningPowerplantSchema setMinimumThreadCount (final int count)
        {
            Preconditions.checkArgument(count >= 1, "count < 1");
            minimumThreadCount = count;
            return this;
        }

        public int getMinimumThreadCount ()
        {
            return minimumThreadCount;
        }

        public SpawningPowerplantSchema setMaximumThreadCount (final int count)
        {
            Preconditions.checkArgument(count >= 1, "count < 1");
            maximumThreadCount = count;
            return this;
        }

        public int getMaximumThreadCount ()
        {
            return maximumThreadCount;
        }
    }

    /**
     * Builder.
     */
    public final class FunctionActorSchema
    {
        private final String name;

        private Function<OperandStack, OperandStack> function = x -> x;

        private FunctionActorSchema (final String name)
        {
            Preconditions.checkNotNull(name, "name");
            this.name = name;
        }

        public FunctionActorSchema setFunction (final Function<OperandStack, OperandStack> function)
        {
            Preconditions.checkNotNull(function, "function");
            this.function = function;
            return this;
        }

        public Function<OperandStack, OperandStack> getFunction ()
        {
            return function;
        }
    }

    /**
     * Builder.
     */
    public final class HeartbeatActorSchema
    {
        private final String name;

        private long periodMillis = 1000;

        private HeartbeatActorSchema (final String name)
        {
            Preconditions.checkNotNull(name, "name");
            this.name = name;
        }

        public HeartbeatActorSchema setPeriod (final long period,
                                               final TimeUnit units)
        {
            Preconditions.checkArgument(period > 0, "period <= 0");
            Preconditions.checkNotNull(units, "units");
            periodMillis = units.toMillis(period);
            return this;
        }

        public long getPeriodMillis ()
        {
            return periodMillis;
        }
    }

    /**
     * Builder.
     *
     * @param <T> the the type of the actor being built.
     */
    public final class ActorSchema<T extends CascadePlant>
    {
        private final String name;

        private final Class<T> type;

        private final T actor;

        private ActorSchema (final String name,
                             final Class<T> type,
                             final T actor)
        {
            Preconditions.checkNotNull(name, "name");
            this.name = name;
            this.type = type;
            this.actor = actor;
        }

        public String getName ()
        {
            return name;
        }

        public Class<T> getType ()
        {
            return type;
        }

        public T getActor ()
        {
            return actor;
        }

        public ActorSchema<T> setPowerplant (final String name)
        {
            return this;
        }

        public ActorSchema<T> setPowerplant (final AbstractPowerplantSchema value)
        {
            return this;
        }

        public AbstractPowerplantSchema getPowerplant ()
        {
            return null;
        }

        public ActorSchema<T> makeLogMessageSink ()
        {
            return this;
        }

        public boolean isLogMessageSink ()
        {
            return false;
        }

        public ActorSchema<T> makeUndeliveredMessageSink ()
        {
            return this;
        }

        public boolean isUndeliveredMessageSink ()
        {
            return false;
        }
    }

    /**
     * Use this method to add an allocator that allocates operands
     * on-demand and supports automated garbage-collection.
     *
     * @param name is the name of the new allocator.
     * @return the schema of the new allocator.
     */
    public DynamicAllocatorSchema addDynamicAllocator (final String name)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(dynamicAllocators.containsKey(name) == false, "Duplicate Allocator: " + name);
        final DynamicAllocatorSchema result = new DynamicAllocatorSchema(name);
        dynamicAllocators.put(name, result);
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
    public FixedAllocatorSchema addFixedAllocator (final String name)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(fixedAllocators.containsKey(name) == false, "Duplicate Allocator: " + name);
        final FixedAllocatorSchema result = new FixedAllocatorSchema(name);
        fixedAllocators.put(name, result);
        return result;
    }

    /**
     * Use this method to add a powerplant that powers (consumer) actor(s)
     * using the thread(s) that are powering the (supplier) actor(s).
     *
     * @param name is the name of the new powerplant.
     * @return the schema of the new powerplant.
     */
    public DirectPowerplantSchema addDirectPowerplant (final String name)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(directPower.containsKey(name) == false, "Duplicate Powerplant: " + name);
        final DirectPowerplantSchema result = new DirectPowerplantSchema(name);
        directPower.put(name, result);
        return result;
    }

    /**
     * Use this method to add a powerplant that powers (consumer) actor(s)
     * using dedicated threads per actor.
     *
     * @param name is the name of the new powerplant.
     * @return the schema of the new powerplant.
     */
    public DedicatedPowerplantSchema addDedicatedPowerplant (final String name)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(dedicatedPower.containsKey(name) == false, "Duplicate Powerplant: " + name);
        final DedicatedPowerplantSchema result = new DedicatedPowerplantSchema(name);
        dedicatedPower.put(name, result);
        return result;
    }

    /**
     * Use this method to add a powerplant that powers (consumer) actor(s)
     * using a fixed-size pool of threads.
     *
     * @param name is the name of the new powerplant.
     * @return the schema of the new powerplant.
     */
    public PooledPowerplantSchema addPooledPowerplant (final String name)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(pooledPower.containsKey(name) == false, "Duplicate Powerplant: " + name);
        final PooledPowerplantSchema result = new PooledPowerplantSchema(name);
        pooledPower.put(name, result);
        return result;
    }

    /**
     * Use this method to add a powerplant that powers (consumer) actor(s)
     * using a pool of threads that resizes on-demand as needed.
     *
     * @param name is the name of the new powerplant.
     * @return the schema of the new powerplant.
     */
    public SpawningPowerplantSchema addSpawningPowerplant (final String name)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(spawningPower.containsKey(name) == false, "Duplicate Powerplant: " + name);
        final SpawningPowerplantSchema result = new SpawningPowerplantSchema(name);
        spawningPower.put(name, result);
        return result;
    }

    /**
     * Use this method to add an actor to the system.
     *
     * @param <T> is the type of the actor class.
     * @param name is the name of the new actor object.
     * @param klass is the fully-qualified name of the actor class.
     * @return the schema of the new actor.
     */
    public <T extends CascadePlant> ActorSchema<T> addActor (final String name,
                                                             final Class<T> klass)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(actors.containsKey(name) == false, "Duplicate Actor: " + name);

        try
        {
            final T actor = klass.newInstance();
            final ActorSchema<T> result = new ActorSchema<>(name, klass, actor);
            actors.put(name, result);
            return result;
        }
        catch (InstantiationException | IllegalAccessException ex)
        {
            throw new RuntimeException(ex); // TODO: Somethng else?????
        }
    }

    /**
     * Use this method to add an actor to the system.
     *
     * @param name is the name of the new actor object.
     * @param function is the transformation that the actor performs.
     * @return the schema of the new actor.
     */
    public FunctionActorSchema addActor (final String name,
                                         final Function<OperandStack, OperandStack> function)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(actors.containsKey(name) == false, "Duplicate Actor: " + name);
        final FunctionActorSchema actor = new FunctionActorSchema(name);
        actor.setFunction(function);
        // actors.put(name, actor); // TODO
        return actor;
    }

    /**
     * Use this method to add an actor to the system.
     *
     * @param name is the name of the new actor object.
     * @param period is how often the heart issues heartbeat messages.
     * @param units are the units of the period.
     * @return the schema of the new actor.
     */
    public HeartbeatActorSchema addActor (final String name,
                                          final long period,
                                          final TimeUnit units)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(actors.containsKey(name) == false, "Duplicate Actor: " + name);
        final HeartbeatActorSchema actor = new HeartbeatActorSchema(name);
        actor.setPeriod(period, units);
        // actors.put(name, actor); // TODO
        return actor;
    }

    /**
     * Use this method to connect two actors using a directed pipeline.
     *
     * @param supplier is the supplying actor.
     * @param consumer is the consuming actor.
     * @return this.
     */
    public CascadeSchema connect (final String supplier,
                                  final String consumer)
    {
        return this;
    }

    private final Map<String, DynamicAllocatorSchema> dynamicAllocators = Maps.newHashMap();

    private final Map<String, FixedAllocatorSchema> fixedAllocators = Maps.newHashMap();

    private final Map<String, DirectPowerplantSchema> directPower = Maps.newHashMap();

    private final Map<String, DedicatedPowerplantSchema> dedicatedPower = Maps.newHashMap();

    private final Map<String, PooledPowerplantSchema> pooledPower = Maps.newHashMap();

    private final Map<String, SpawningPowerplantSchema> spawningPower = Maps.newHashMap();

    private final Map<String, ActorSchema> actors = Maps.newHashMap();

    /**
     * Use this method to construct the new system.
     *
     * @return the new system.
     */
    public Cascade build ()
    {

        return null;
    }

    public static void main (String[] args)
    {
        final CascadeSchema cs = new CascadeSchema();
        cs.addDedicatedPowerplant("SteamLoco").setDefaultThreadCount(1);
        cs.addDynamicAllocator("default");
        cs.addActor("liver", 1, TimeUnit.SECONDS);

        cs.connect("liver", "adder");
        cs.build().start();
    }
}
