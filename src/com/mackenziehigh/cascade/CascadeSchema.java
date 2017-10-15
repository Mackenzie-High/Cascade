package com.mackenziehigh.cascade;

import com.google.common.base.Preconditions;
import java.util.SortedMap;
import java.util.concurrent.ThreadFactory;

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
        private DedicatedPowerplantSchema (final String name)
        {
            super(name);
        }

    }

    /**
     * Builder.
     */
    public final class PooledPowerplantSchema
            extends AbstractPowerplantSchema
    {
        private PooledPowerplantSchema (final String name)
        {
            super(name);
        }

    }

    /**
     * Builder.
     */
    public final class SpawningPowerplantSchema
            extends AbstractPowerplantSchema
    {
        private SpawningPowerplantSchema (final String name)
        {
            super(name);
        }

    }

    /**
     * Builder.
     *
     * @param <T> the the type of the actor being built.
     */
    public final class ActorSchema<T extends Actor>
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

        public Powerplant getPowerplant ()
        {
            return null;
        }

        public ActorSchema<T> connect (final String neighbor)
        {
            return this;
        }

        public ActorSchema<T> connect (final ActorSchema<?> neighbor)
        {
            return connect(neighbor.name);
        }

        public ActorSchema<T> makeLogMessageSink ()
        {
            return this;
        }

        public boolean isLogMessageSink ()
        {
            return false;
        }

        public ActorSchema<T> makeUndeliveredMessageSick ()
        {
            return this;
        }

        public boolean isUndeliveredMessageSink ()
        {
            return false;
        }
    }

    public DynamicAllocatorSchema addDynamicAllocator (final String name)
    {
        final DynamicAllocatorSchema result = new DynamicAllocatorSchema(name);
        return result;
    }

    public FixedAllocatorSchema addFixedAllocator (final String name)
    {
        final FixedAllocatorSchema result = new FixedAllocatorSchema(name);
        return result;
    }

    public DirectPowerplantSchema addDirectPower (final String name)
    {
        final DirectPowerplantSchema result = new DirectPowerplantSchema(name);
        return result;
    }

    public DedicatedPowerplantSchema addDedicatedPower (final String name)
    {
        final DedicatedPowerplantSchema result = new DedicatedPowerplantSchema(name);
        return result;
    }

    public PooledPowerplantSchema addPooledPower (final String name)
    {
        final PooledPowerplantSchema result = new PooledPowerplantSchema(name);
        return result;
    }

    public SpawningPowerplantSchema addSpawningPower (final String name)
    {
        final SpawningPowerplantSchema result = new SpawningPowerplantSchema(name);
        return result;
    }

    public <T extends Actor> ActorSchema<T> addActor (final String name,
                                                      final Class<T> klass)
    {
        try
        {
            final T actor = klass.newInstance();
            final ActorSchema<T> result = new ActorSchema<>(name, klass, actor);
            return result;
        }
        catch (InstantiationException | IllegalAccessException ex)
        {
            throw new RuntimeException(ex); // TODO: Somethng else?????
        }
    }

    public SortedMap<String, Allocator> allocators ()
    {
        return null;
    }

    public SortedMap<String, Powerplant> powerplants ()
    {
        return null;
    }

    public SortedMap<String, Actor> actors ()
    {
        return null;
    }

    public Cascade build ()
    {
        return null;
    }

}
