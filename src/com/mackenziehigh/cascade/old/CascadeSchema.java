package com.mackenziehigh.cascade.old;

import com.mackenziehigh.cascade.old.CascadeReactor;
import com.mackenziehigh.cascade.old.Cascade;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.old.CascadeReactor.Core;
import com.mackenziehigh.cascade.old.internal.ConcreteSchema;
import java.util.concurrent.ThreadFactory;

/**
 * Use an instance of this interface in order to
 * create a Cascade object using a fluent API.
 */
public interface CascadeSchema
{
    /**
     * Describes an allocation-pool.
     */
    public interface PoolSchema
    {
        /**
         * This method causes the pool to become the global default allocation-pool,
         * which will be used by any algorithms that choose to not use a use-site
         * specific allocation-pool.
         *
         * @return this.
         */
        public PoolSchema makeGlobalDefault ();
    }

    /**
     * Describes an allocation-pool that uses on-demand memory allocation.
     *
     * <p>
     * This type of allocation-pool simply allocates a new <code>byte[]</code>
     * whenever an allocation request is made.
     * </p>
     *
     * <p>
     * <b>Advantages:</b>
     * <ul>
     * <li>
     * Only the amount of memory that is actually needed is allocated per operand;
     * therefore, operands never have wasted padding.
     * </li>
     * <li>
     * The amount of memory allocated by the pool may grow and shrink as needed,
     * rather than requiring a pre-allocated amount based on programmer expectations.
     * </li>
     * </ul>
     * </p>
     *
     * <p>
     * <b>Disadvantages:</b>
     * <ul>
     * <li>
     * Allocations put pressure on the garbage-collector. In particular, frequent large
     * allocations may have a negative performance impact, by causing the garbage-collector
     * to have to perform more frequent invasive collections. For example, the (G1) garbage
     * collector automatically promotes large objects to the old-generation.
     * </li>
     * </ul>
     * </p>
     *
     * <p>
     * The default minimum-size is zero and the default maximum-size is <code>Integer.MAX_VALUE</code>.
     * </p>
     */
    public interface DynamicPoolSchema
            extends PoolSchema
    {
        /**
         * Setter.
         *
         * @param bound will be the minimum-size of allocations in the pool.
         * @return this.
         */
        public DynamicPoolSchema withMinimumSize (int bound);

        /**
         * Setter.
         *
         * @param bound will be the maximum-size of allocations in the pool.
         * @return this.
         */
        public DynamicPoolSchema withMaximumSize (int bound);

        /**
         * {@inheritDoc}
         */
        @Override
        public DynamicPoolSchema makeGlobalDefault ();
    }

    /**
     * Describes an allocation-pool that uses preallocated memory.
     *
     * <p>
     * This type of allocation-pool simply allocates a a fixed number
     * of <code>byte[]</code> buffers at startup. Whenever an allocation
     * request is made, a free buffer is selected and used to fulfill
     * the request. When the buffer is no longer in-use, the buffer
     * is returned to the pool of available buffers.
     * </p>
     *
     * <p>
     * <b>Advantages:</b>
     * <ul>
     * <li>
     * Since the buffers are reused, no pressure is put on the garbage-collector.
     * </li>
     * <li>
     * The amount of memory allocated by the pool is effectively constant.
     * </li>
     * </ul>
     * </p>
     *
     * <p>
     * <b>Disadvantages:</b>
     * <ul>
     * <li>
     * The preallocated buffers have a fixed size; therefore,
     * memory will be wasted, if the requested allocation-size
     * is smaller than the size of the buffer.
     * </li>
     * <li>
     * Since each allocation consumes one whole preallocated buffer,
     * there is a strict limit on the number of simultaneous allocations.
     * Specifically, no more allocations can be made than there are buffers
     * in the pool of preallocated buffers, even if wasted space is present.
     * </li>
     * <li>
     * Programmers must carefully tune the number of preallocated buffers,
     * based solely upon their expectations of runtime behavior,
     * in order to ensure that an adequate number of buffers will be available
     * without exceeding the available system resources.
     * </li>
     * </ul>
     * </p>
     */
    public interface FixedPoolSchema
            extends PoolSchema
    {
        /**
         * Setter.
         *
         * @param bound will be the minimum-size of allocations in the pool.
         * @return this.
         */
        public FixedPoolSchema withMinimumSize (int bound);

        /**
         * Setter.
         *
         * @param bound will be the maximum-size of allocations in the pool.
         * @return this.
         */
        public FixedPoolSchema withMaximumSize (int bound);

        /**
         * Setter.
         *
         * @param count will be the number of preallocated memory buffers,
         * which is the maximum number of operands that will be allocated
         * within the allocation-pool at one one time.
         * @return this.
         */
        public FixedPoolSchema withBufferCount (int count);

        /**
         * {@inheritDoc}
         */
        @Override
        public FixedPoolSchema makeGlobalDefault ();
    }

    /**
     * Describes an allocation-pool that delegates to other allocation-pools.
     *
     * <p>
     * A composite-pool consists of a series of member-pools and an optional fallback-pool.
     * </p>
     *
     * <p>
     * When an allocation request is made for (X) bytes, the composite-pool will attempt
     * to locate a member-pool whose minimum allocation-size is less-or-equal-to (X)
     * and whose maximum allocation-size is greater-or-equal-to (X). If such a member-pool
     * is found, then the allocation request will be forwarded to that member-pool.
     * If no such member-pool is found and a fallback-pool was specified,
     * then the allocation request will be forwarded to the fallback-pool.
     * </p>
     *
     * <p>
     * As usual, each member-pool has its own minimum and maximum allocation-sizes.
     * None of the member-pools can have overlapping allocation-sizes.
     * No such restriction is placed onto the fallback-pool.
     * The member-pools are not required to be contiguous.
     * </p>
     *
     * <p>
     * An logarithmic-time, O(log N), search algorithm is used to locate the matching member-pool.
     * Thus, using a composite-pool should not add a meaningful amount of allocation overhead.
     * </p>
     *
     * <p>
     * The member-pools and the fallback-pool cannot themselves be composite-pools.
     * This avoids the possibility of recursively-defined composite-pools,
     * which would be prima facia erroneous.
     * </p>
     */
    public interface CompositePoolSchema
            extends PoolSchema
    {

        /**
         * Setter.
         *
         * <p>
         * If the given name is a simple-name, when treated as a token,
         * then this method will prepend the current namespace onto the name.
         * </p>
         *
         * @param name is the name of an allocation-pool to use,
         * if none of the other allocation-pools are appropriate.
         * @return this.
         */
        public CompositePoolSchema withFallbackPool (String name);

        /**
         * Setter.
         *
         * <p>
         * This method may be invoked multiple times.
         * </p>
         *
         * <p>
         * If the given name is a simple-name, when treated as a token,
         * then this method will prepend the current namespace onto the name.
         * </p>
         *
         * @param name is the name of an allocation-pool that this pool
         * will delegate to in order to perform allocations.
         * @return this.
         */
        public CompositePoolSchema withMemberPool (String name);

        /**
         * {@inheritDoc}
         */
        @Override
        public CompositePoolSchema makeGlobalDefault ();
    }

    /**
     * Describes a pump consisting of a pool of threads.
     *
     * <p>
     * By default, the pool will have one thread,
     * which will be a non-daemon thread.
     * </p>
     */
    public interface PumpSchema
    {

        /**
         * Setter.
         *
         * @param factory will be used to create the threads in the thread-pool.
         * @return this.
         */
        public PumpSchema usingThreadFactory (ThreadFactory factory);

        /**
         * Setter.
         *
         * @param count will be the number of threads in the thread-pool.
         * @return this.
         */
        public PumpSchema withThreadCount (int count);

    }

    /**
     * Describes a reactor, its core, and its queue.
     *
     * <p>
     * If no queue, logger, pool, or pump settings are explicitly specified,
     * then the reactor will use the default setting from the enclosing scope.
     * </p>
     */
    public interface ReactorSchema
    {

        /**
         * Setter.
         *
         * @param core will be the core of the reactor.
         * @return this.
         */
        public ReactorSchema withCore (Core core);

        /**
         * Setter.
         *
         * @param core will produce the core of the reactor.
         * @return this.
         */
        public ReactorSchema withCore (CascadeReactor.CoreBuilder core);

        /**
         * Setter.
         *
         * @param factory will be used to create a logger for the reactor.
         * @return this.
         */
        public ReactorSchema usingLogger (CascadeLogger.Factory factory);

        /**
         * Setter.
         *
         * <p>
         * If the given name is a simple-name, when treated as a token,
         * then this method will prepend the current namespace onto the name.
         * </p>
         *
         * @param name identifies the allocation-pool for the reactor to use.
         * @return this.
         */
        public ReactorSchema usingPool (String name);

        /**
         * Setter.
         *
         * <p>
         * If the given name is a simple-name, when treated as a token,
         * then this method will prepend the current namespace onto the name.
         * </p>
         *
         * @param name identifies the pump for the reactor to use.
         * @return this.
         */
        public ReactorSchema usingPump (String name);

        /**
         * This method will cause the reactor to use a non-circular
         * array-queue in order to store pending event-messages.
         *
         * @param capacity will be the maximum-size of the queue.
         * @return this.
         */
        public ReactorSchema withArrayQueue (int capacity);

        /**
         * This method will cause the reactor to use a non-circular
         * linked-queue in order to store pending event-messages.
         *
         * @param capacity will be the maximum-size of the queue.
         * @return this.
         */
        public ReactorSchema withLinkedQueue (int capacity);

        /**
         * This method causes the reactor to be subscribed to a named event-channel; therefore,
         * the reactor will receive any event-messages sent to that channel at runtime.
         *
         * <p>
         * This method may be invoked multiple times in order to subscribe to multiple event-channels.
         * </p>
         *
         * <p>
         * Subsequent invocations of this method, given the same argument, are no-ops.
         * </p>
         *
         * @param event is the exact name of the event-channel.
         * @return this.
         */
        public ReactorSchema subscribeTo (String event);
    }

    /**
     * Use this method to enter a new scope.
     *
     * <p>
     * The default logger-factory, pool, and pump
     * of the current scope, if any, will be inherited by
     * the new scope.
     * </p>
     *
     *
     * @param name is the namespace to enter.
     * @return this.
     */
    public CascadeSchema enter (String name);

    /**
     * This method causes reactors subsequently added to this scope
     * to use the given logger-factory, unless another logger-factory
     * is explicitly specified for the reactor.
     *
     * @param factory will be the default logger-factory for reactors in this scope.
     * @return this.
     */
    public CascadeSchema usingLogger (CascadeLogger.Factory factory);

    /**
     * This method causes reactors subsequently added to this scope
     * to use the allocation-pool with the given name, unless another
     * allocation-pool is explicitly specified for the reactor.
     *
     * <p>
     * If the given name is a simple-name, when treated as a token,
     * then this method will prepend the current namespace onto the name.
     * </p>
     *
     * @param name identifies the default allocation-pool for reactors in this scope.
     * @return this.
     */
    public CascadeSchema usingPool (String name);

    /**
     * This method causes reactors subsequently added to this scope
     * to use the pump with the given name, unless another pump
     * is explicitly specified for the reactor.
     *
     * <p>
     * If the given name is a simple-name, when treated as a token,
     * then this method will prepend the current namespace onto the name.
     * </p>
     *
     * @param name identifies the default pump for reactors in this scope.
     * @return this.
     */
    public CascadeSchema usingPump (String name);

    /**
     * This method causes reactors subsequently added to this scope
     * to use a linked-queue with the given capacity,
     * unless explicitly specified otherwise.
     *
     * @param capacity will be the default queue-capacity.
     * @return this.
     */
    public CascadeSchema usingLinkedQueues (int capacity);

    /**
     * This method causes reactors subsequently added to this scope
     * to use an array-queue with the given capacity,
     * unless explicitly specified otherwise.
     *
     * @param capacity will be the default queue-capacity.
     * @return this.
     */
    public CascadeSchema usingArrayQueues (int capacity);

    /**
     * Use this method to add a dynamic allocation-pool.
     *
     * <p>
     * If the given name is a simple-name, when treated as a token,
     * then this method will prepend the current namespace onto the name.
     * </p>
     *
     * @param name will be the name of the new pool.
     * @return this.
     */
    public DynamicPoolSchema addDynamicPool (String name);

    /**
     * Use this method to add a fixed allocation-pool.
     *
     *      * <p>
     * If the given name is a simple-name, when treated as a token,
     * then this method will prepend the current namespace onto the name.
     * </p>
     *
     * @param name will be the name of the new pool.
     * @return this.
     */
    public FixedPoolSchema addFixedPool (String name);

    /**
     * Use this method to add a composite allocation-pool.
     *
     * <p>
     * If the given name is a simple-name, when treated as a token,
     * then this method will prepend the current namespace onto the name.
     * </p>
     *
     * @param name will be the name of the new pool.
     * @return this.
     */
    public CompositePoolSchema addCompositePool (String name);

    /**
     * Use this method to add a pump.
     *
     * <p>
     * If the given name is a simple-name, when treated as a token,
     * then this method will prepend the current namespace onto the name.
     * </p>
     *
     * @param name will be the name of the new pump.
     * @return this.
     */
    public PumpSchema addPump (String name);

    /**
     * Use this method to add a reactor.
     *
     * <p>
     * If the given name is a simple-name, when treated as a token,
     * then this method will prepend the current namespace onto the name.
     * </p>
     *
     * @param name will be the name of the new reactor.
     * @return this.
     */
    public ReactorSchema addReactor (String name);

    /**
     * Use this method to construct a Cascade object from this schema.
     *
     * @return the new object.
     */
    public Cascade build ();

    /**
     * Factory Method.
     *
     * @param name will be the name of the new Cascade object.
     * @return a new instance of this interface.
     */
    public static CascadeSchema create (final String name)
    {
        return new ConcreteSchema(name);
    }

    /**
     * Factory Method.
     *
     * @param name will be the name of the new Cascade object.
     * @return a new instance of this interface with default settings.
     */
    public static CascadeSchema createSimple (final String name)
    {
        // TODO: Finish
        final CascadeSchema schema = create(name);
        schema.addDynamicPool("default").withMinimumSize(0).withMaximumSize(Integer.MAX_VALUE);
        schema.addPump("pump").withThreadCount(Runtime.getRuntime().availableProcessors() + 1);
        schema.usingPool("default");
        schema.usingPump("pump");
        return schema;
    }
}
