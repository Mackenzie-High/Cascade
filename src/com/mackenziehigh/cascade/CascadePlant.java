package com.mackenziehigh.cascade;

import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Instances of this interface receive messages from neighboring actors,
 * process those messages, send then messages to neighboring actors.
 *
 * <p>
 * Implementations of this class must have a nullary constructor.
 * </p>
 */
public interface CascadePlant
{
    /**
     * An instance of this interface provides access to important objects
     * during invocations of the event-handlers in an event-handler.
     */
    public interface Context
    {
        /**
         * Getter.
         *
         * <p>
         * For a given actor instance, this method always returns the same object.
         * Thus, the actor can safely use the result between event-handler invocations.
         * </p>
         *
         * @return the system that this actor is part of.
         */
        public Cascade cascade ();

        /**
         * Getter.
         *
         * <p>
         * For a given actor instance, this method always returns the same object.
         * Thus, the actor can safely use the result between event-handler invocations.
         * </p>
         *
         * @return the logger for use by this actor.
         */
        public CascadeLogger logger ();

        /**
         * Getter.
         *
         * @return the allocator intended for use by this actor.
         */
        public CascadeAllocator allocator ();

        /**
         * Getter.
         *
         * @return the allocation-pool intended for use by this actor.
         */
        public CascadeAllocator.AllocationPool pool ();

        /**
         * Getter.
         *
         * @return the powerplant that powers this actor.
         */
        public CascadePump powerplant ();

        /**
         * Getter.
         *
         * <p>
         * For a given actor instance, this method always returns the same object.
         * Thus, the actor can safely use the result between event-handler invocations.
         * </p>
         *
         * @return this actor.
         */
        public CascadePlant actor ();

        /**
         * Getter.
         *
         * <p>
         * For a given actor instance, this method always returns the same object.
         * Thus, the actor can safely use the result between event-handler invocations.
         * </p>
         *
         * @return the name of this actor.
         */
        public String name ();

        /**
         * Getter.
         *
         * <p>
         * The list is ordered lexicographically based on
         * the names of the consuming actors.
         * </p>
         *
         * <p>
         * Note: Using a List here, rather than a Set,
         * avoids the need to allocate an Iterator object
         * whenever an iteration is performed.
         * </p>
         *
         * <p>
         * For a given actor instance, this method always returns the same object.
         * Thus, the actor can safely use the result between event-handler invocations.
         * </p>
         *
         * @return the inputs that supply message to this actor.
         */
        public List<CascadePipe> inputs ();

        /**
         * Getter.
         *
         * <p>
         * The list is ordered lexicographically based on
         * the names of the consuming actors.
         * </p>
         *
         * <p>
         * Note: Using a List here, rather than a Set,
         * avoids the need to allocate an Iterator object
         * whenever an iteration is performed.
         * </p>
         *
         * <p>
         * For a given actor instance, this method always returns the same object.
         * Thus, the actor can safely use the result between event-handler invocations.
         * </p>
         *
         * @return the pipelines that carry messages from this actor.
         */
        public List<CascadePipe> outputs ();

        /**
         * Getter.
         *
         * @return the message that needs to be processed, if any.
         */
        public OperandStack message ();

        /**
         * Send a message to the outputs().
         *
         * <p>
         * If the backlog() of any output has reached the queueCapacity(),
         * then this method will simply return false.
         * </p>
         *
         * <p>
         * This method is atomic; therefore, the message will be enqueued
         * in all of the output pipelines or none of them.
         * </p>
         *
         * <p>
         * This method must maintain the ordering of messages in the face
         * of multiple threads invoking this method concurrently given
         * the same actor. In other words, this method is thread-safe.
         * </p>
         *
         * <p>
         * The message cannot be null or empty.
         * </p>
         *
         * <p>
         * This method does not block.
         * </p>
         *
         * @param message will be enqueued in each output pipeline.
         * @return this.
         */
        public boolean async (OperandStack message);

        /**
         * Send a message to the outputs().
         *
         * <p>
         * This method is atomic; therefore, the message will be enqueued
         * in all of the output pipelines or none of them.
         * </p>
         *
         * <p>
         * This method must maintain the ordering of messages in the face
         * of multiple threads invoking this method concurrently given
         * the same actor. In other words, this method is thread-safe.
         * </p>
         *
         * <p>
         * This method will block until all of the outputs enqueue the message.
         * </p>
         *
         * @param message will be enqueued in each output pipeline.
         * @return true.
         */
        public boolean sync (final OperandStack message);
    }

    /**
     * This method determines whether this actor supports parallel execution.
     *
     * @return
     */
    public default boolean isParallelAllowed ()
    {
        return false;
    }

    /**
     * Can a direct powerplant be used?
     *
     * @return
     */
    public default boolean isDirectAllowed ()
    {
        return true;
    }

    /**
     * This is the first event-handler to be invoked.
     *
     * <p>
     * Only the controller shall invoke this method.
     * </p>
     *
     * <p>
     * This method will only be invoked once.
     * </p>
     *
     * @param context provides access to the current state.
     * @throws java.lang.Throwable if something goes wrong.
     */
    public default void onSetup (final Context context)
            throws Throwable
    {
        // Pass
    }

    /**
     * This is the second event-handler to be invoked.
     *
     * <p>
     * When this method is invoked, setup(*) has already been
     * invoked on all of the modules that are being loaded,
     * including this module itself.
     * </p>
     *
     * <p>
     * Only the controller shall invoke this method.
     * </p>
     *
     * <p>
     * This method will only be invoked once.
     * </p>
     *
     * @param context provides access to the current state.
     * @throws java.lang.Throwable if something goes wrong.
     */
    public default void onStart (final Context context)
            throws Throwable
    {
        // Pass
    }

    /**
     * This is the third event-handler to be invoked.
     *
     * <p>
     * As a general assumption, if isParallelizable() is false,
     * then this method will only be invoked by one thread at a time.
     * </p>
     *
     * @param context provides access to the current state.
     * @throws java.lang.Throwable if something goes wrong.
     */
    public default void onMessage (final Context context)
            throws Throwable
    {
        // Pass
    }

    /**
     * This is the fourth event-handler to be invoked.
     *
     * <p>
     * This method is not guaranteed to be invoked during abnormal shutdowns.
     * </p>
     *
     * <p>
     * Only the controller shall invoke this method.
     * </p>
     *
     * <p>
     * This method will only be invoked once.
     * </p>
     *
     * @param context provides access to the current state.
     * @throws java.lang.Throwable if something goes wrong.
     */
    public default void onStop (final Context context)
            throws Throwable
    {
        // Pass
    }

    /**
     * This is the fifth event-handler to be invoked.
     *
     * <p>
     * This method is not guaranteed to be invoked during abnormal shutdowns.
     * </p>
     *
     * <p>
     * When this method is invoked, stop() has already been
     * invoked on all of the modules that are loaded,
     * including this module itself.
     * </p>
     *
     * <p>
     * Only the controller shall invoke this method.
     * </p>
     *
     * <p>
     * This method will only be invoked once.
     * </p>
     *
     * @param context provides access to the current state.
     * @throws java.lang.Throwable if something goes wrong.
     */
    public default void onDestroy (final Context context)
            throws Throwable
    {
        // Pass
    }

    /**
     * This is the sixth event-handler to be invoked.
     *
     * <p>
     * This method is not guaranteed to be invoked during abnormal shutdowns.
     * </p>
     *
     * <p>
     * When this method is invoked, destroy() has already been
     * invoked on all of the modules that are loaded,
     * including this module itself.
     * </p>
     *
     * <p>
     * Only the controller shall invoke this method.
     * </p>
     *
     * <p>
     * This method will only be invoked once.
     * </p>
     *
     * @return any objects that need to be closed.
     */
    public default Set<Closeable> closeables ()
    {
        return Collections.emptySet();
    }
}
