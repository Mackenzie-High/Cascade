package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.internal.ConcreteSchema;
import com.mackenziehigh.cascade.CascadeReactor;

/**
 *
 */
public interface CascadeSchema1
{
    /**
     * Use this method to add a powerplant that powers (consumer) actor(s)
     * using dedicated threads per actor.
     *
     * @param name is the name of the new powerplant.
     * @return the schema of the new powerplant.
     */
    public DedicatedPumpSchema addDedicatedPump (final String name);

    /**
     * Use this method to add a powerplant that powers (consumer) actor(s)
     * using the thread(s) that are powering the (supplier) actor(s).
     *
     * @param name is the name of the new powerplant.
     * @return the schema of the new powerplant.
     */
    public DirectPumpSchema addDirectPump (final String name);

    /**
     * Use this method to add an allocator that allocates operands
     * on-demand and supports automated garbage-collection.
     *
     * @param name is the name of the new allocator.
     * @return the schema of the new allocator.
     */
    public DynamicPoolSchema addDynamicPool (final String name);

    /**
     * Use this method to add an allocator that allocates operands
     * using pre-allocated buffers that do <b>not</b> support
     * automatic garbage-collection.
     *
     * @param name is the name of the new allocator.
     * @return the schema of the new allocator.
     */
    public FixedPoolSchema addFixedPool (final String name);

    /**
     * Use this method to add an actor to the system.
     *
     * @param <T> is the type of the actor class.
     * @param name is the name of the new actor object.
     * @param builder will create the underlying implementation of the node.
     * @return the schema of the new actor.
     */
    public <T extends NodeSchema> NodeSchema<T> addNode (final String name,
                                                         final CascadeReactor.CoreBuilder builder);

    /**
     * Use this method to add a powerplant that powers (consumer) actor(s)
     * using a fixed-size pool of threads.
     *
     * @param name is the name of the new powerplant.
     * @return the schema of the new powerplant.
     */
    public PooledPumpSchema addPooledPump (final String name);

    /**
     * Use this method to add a powerplant that powers (consumer) actor(s)
     * using a pool of threads that resizes on-demand as needed.
     *
     * @param name is the name of the new powerplant.
     * @return the schema of the new powerplant.
     */
    public SpawningPumpSchema addSpawningPump (final String name);

    /**
     * Use this method to construct the new system.
     *
     * @return the new system.
     */
    public Cascade build ();

    /**
     * Use this method to connect two actors using a directed pipeline.
     *
     * @param supplier is the supplying actor.
     * @param consumer is the consuming actor.
     * @return the schema of the new edge.
     */
    public EdgeSchema connect (final String supplier,
                               final String consumer);

    /**
     * Use this method to connect two actors using a directed pipeline.
     *
     * @param supplier is the supplying actor.
     * @param consumer is the consuming actor.
     * @return the schema of the new edge.
     */
    public EdgeSchema connect (final NodeSchema supplier,
                               final NodeSchema consumer);

    /**
     * Use this method to enter a name-space.
     *
     * @param name will become part of the name-space path.
     * @return this.
     */
    public ConcreteSchema enter (final String name);

    /**
     * Use this method to exit a name-space.
     *
     * @return this.
     */
    public ConcreteSchema exit ();

    /**
     * Getter.
     *
     * @return the default logger.
     */
    public CascadeLogger getDefaultLogger ();

    public String getNamespace ();

    /**
     * Setter.
     *
     * @param logger will be provided as the default logger.
     * @return this.
     */
    public ConcreteSchema setDefaultLogger (final CascadeLogger logger);

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
    public ConcreteSchema usingLogger (final CascadeLogger logger);

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
    public ConcreteSchema usingPool (final String name);

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
    public ConcreteSchema usingPool (final CascadeAllocator.AllocationPool pool);

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
    public ConcreteSchema usingPump (final String name);

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
    public ConcreteSchema usingPump (final PumpSchema pump);

}
