package com.mackenziehigh.cascade;

import com.mackenziehigh.cascade.CascadeReactor.Core;
import java.util.concurrent.ThreadFactory;

/**
 * Use an instance of this interface in order to
 * create a Cascade object using a fluent API.
 */
public interface CascadeSchema
{
    /**
     * Describes an allocation-pool that uses on-demand memory allocation.
     */
    public interface DynamicPoolSchema
    {
        /**
         * Setter.
         *
         * @param name will be the name of the pool.
         * @return this.
         * @throws IllegalStateException if this method was already invoked.
         */
        public DynamicPoolSchema named (String name);

        /**
         * Setter.
         *
         * @param bound will be the minimum-size of allocations in the pool.
         * @return this.
         * @throws IllegalStateException if this method was already invoked.
         * @throws IllegalArgumentException if bound is less than zero.
         * @throws IllegalArgumentException if the maximum-size was already
         * set and the given bound is larger than that value.
         */
        public DynamicPoolSchema withMinimumSize (int bound);

        /**
         * Setter.
         *
         * @param bound will be the maximum-size of allocations in the pool.
         * @return this.
         * @throws IllegalStateException if this method was already invoked.
         * @throws IllegalArgumentException if bound is less than zero.
         * @throws IllegalArgumentException if the minimum-size was already
         * set and the given bound is smaller than that value.
         */
        public DynamicPoolSchema withMaximumSize (int bound);
    }

    /**
     * Describes an allocation-pool that uses preallocated memory.
     */
    public interface FixedPoolSchema
    {
        /**
         * Setter.
         *
         * @param name will be the name of the pool.
         * @return this.
         * @throws IllegalStateException if this method was already invoked.
         */
        public FixedPoolSchema named (String name);

        /**
         * Setter.
         *
         * @param bound will be the minimum-size of allocations in the pool.
         * @return this.
         * @throws IllegalStateException if this method was already invoked.
         * @throws IllegalArgumentException if bound is less than zero.
         * @throws IllegalArgumentException if the maximum-size was already
         * set and the given bound is larger than that value.
         */
        public FixedPoolSchema withMinimumSize (int bound);

        /**
         * Setter.
         *
         * @param bound will be the maximum-size of allocations in the pool.
         * @return this.
         * @throws IllegalStateException if this method was already invoked.
         * @throws IllegalArgumentException if bound is less than zero.
         * @throws IllegalArgumentException if the minimum-size was already
         * set and the given bound is smaller than that value.
         */
        public FixedPoolSchema withMaximumSize (int bound);

        /**
         * Setter.
         *
         * @param count will be the number of preallocated memory buffers,
         * which is the maximum number of operands that will be allocated
         * within the allocation-pool at one one time.
         * @return this.
         * @throws IllegalStateException if this method was already invoked.
         * @throws IllegalArgumentException if count is less than zero.
         */
        public FixedPoolSchema withCapacity (int count);
    }

    /**
     * Describes an allocation-pool that delegates to other allocation-pools.
     */
    public interface CompositePoolSchema
    {
        /**
         * Setter.
         *
         * @param name will be the name of the pool.
         * @return this.
         * @throws IllegalStateException if this method was already invoked.
         */
        public CompositePoolSchema named (String name);

        /**
         * Setter.
         *
         * @param name is the name of an allocation-pool to use,
         * if none of the other allocation-pools are appropriate.
         * @return this.
         * @throws IllegalStateException if this method was already invoked.
         */
        public CompositePoolSchema withFallbackPool (String name);

        /**
         * Setter.
         *
         * <p>
         * This method may be invoked multiple times.
         * </p>
         *
         * @param name is the name of an allocation-pool that this pool
         * will delegate to in order to perform allocations.
         * @return this.
         * @throws IllegalArgumentException if the bounds of the given pool
         * overlap with any other member pool that has already been added hereto.
         */
        public CompositePoolSchema withMemberPool (String name);
    }

    /**
     * Describes a pump consisting of a pool of threads.
     */
    public interface PumpSchema
    {
        /**
         * Setter.
         *
         * @param name will be the name of the pump.
         * @return this.
         * @throws IllegalStateException if this method was already invoked.
         */
        public PumpSchema named (String name);

        /**
         * Setter.
         *
         * @param factory will be used to create the threads in the thread-pool.
         * @return this.
         * @throws IllegalStateException if this method was already invoked.
         */
        public PumpSchema usingThreadFactory (ThreadFactory factory);

        /**
         * Setter.
         *
         * @param count will be the number of threads in the thread-pool.
         * @return this.
         * @throws IllegalStateException if this method was already invoked.
         */
        public PumpSchema withThreadCount (int count);

    }

    /**
     * Describes a reactor, its core, and its queue.
     */
    public interface ReactorSchema
    {
        /**
         * Setter.
         *
         * @param name will be the name of the reactor.
         * @return this.
         * @throws IllegalStateException if this method was already invoked.
         */
        public ReactorSchema named (String name);

        /**
         * Setter.
         *
         * @param core will be the core of the reactor.
         * @return this.
         * @throws IllegalStateException if this method was already invoked.
         */
        public ReactorSchema withCore (Core core);

        /**
         * Setter.
         *
         * @param factory will be used to create a logger for the reactor.
         * @return this.
         * @throws IllegalStateException if this method was already invoked.
         * @throws IllegalStateException if an actual logger was already specified.
         */
        public ReactorSchema usingLogger (CascadeLogger.Factory factory);

        /**
         * Setter.
         *
         * @param logger will be the logger used by the reactor.
         * @return this.
         * @throws IllegalStateException if this method was already invoked.
         * @throws IllegalStateException if a logger factory was already specified.
         */
        public ReactorSchema usingLogger (CascadeLogger logger);

        /**
         * Setter.
         *
         * @param name identifies the allocation-pool for the reactor to use.
         * @return this.
         * @throws IllegalStateException if this method was already invoked.
         */
        public ReactorSchema usingPool (String name);

        /**
         * Setter.
         *
         * @param name identifies the pump for the reactor to use.
         * @return this.
         * @throws IllegalStateException if this method was already invoked.
         */
        public ReactorSchema usingPump (String name);

        public ReactorSchema withLinearSharedQueue (String group,
                                                    int queueCapacity,
                                                    int backlogCapacity);

        public ReactorSchema withLinearArrayQueue (int queueCapacity);

        public ReactorSchema withLinearLinkedQueue (int queueCapacity);

        public ReactorSchema withCircularSharedQueue (String group,
                                                      int queueCapacity,
                                                      int backlogCapacity);

        public ReactorSchema withCircularArrayQueue (int queueCapacity);

        public ReactorSchema withCircularLinkedQueue (int queueCapacity);

        public ReactorSchema subscribeTo (String event);
    }

    /**
     * Setter.
     *
     * @param name will be the name of the Cascade object itself.
     * @return this.
     * @throws IllegalStateException if this method was already invoked.
     */
    public CascadeSchema named (String name);

    public CascadeSchema begin (String name);

    public CascadeSchema end ();

    /**
     * Use this method to add a dynamic allocation-pool.
     *
     * @return this.
     */
    public DynamicPoolSchema addDynamicPool ();

    /**
     * Use this method to add a fixed allocation-pool.
     *
     * @return this.
     */
    public FixedPoolSchema addFixedPool ();

    /**
     * Use this method to add a composite allocation-pool.
     *
     * @return this.
     */
    public CompositePoolSchema addCompositePool ();

    /**
     * Use this method to add a pump.
     *
     * @return this.
     */
    public PumpSchema addPump ();

    /**
     * Use this method to add a reactor.
     *
     * @return this.
     */
    public ReactorSchema addReactor ();

    /**
     * Use this method to construct a Cascade object from this schema.
     *
     * @return the new object.
     */
    public Cascade build ();
}
