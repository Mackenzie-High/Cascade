package com.mackenziehigh.cascade;

import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
         * @return the message that needs to be processed.
         */
        public OperandStack message ();

        /**
         * Send a message to the outputs().
         *
         * <p>
         * If the backlog() of any output has reached the queueCapacity(),
         * then this method will simply return false, if atomic is true.
         * </p>
         *
         * <p>
         * The message cannot be null.
         * </p>
         *
         * <p>
         * This method does not block.
         * </p>
         *
         * @param message will be enqueued in each output pipeline.
         * @param atomic true, iff all outputs must accept the message.
         * @return true, iff all of the outputs accepted the message.
         */
        public boolean send (OperandStack message,
                             boolean atomic);

        /**
         * Send a message to the outputs().
         *
         * <p>
         * Equivalent: send(message, true)
         * </p>
         *
         * <p>
         * This method does not block.
         * </p>
         *
         * @param message will be enqueued in each output pipeline.
         * @return true, iff all of the outputs accepted the message.
         */
        public default boolean send (final OperandStack message)
        {
            return send(message, true);
        }

        /**
         * Send a message, blocking if necessary.
         *
         * @param message will be sent.
         * @param timeout is the maximum amount of time to block.
         * @param units are the units of the timeout.
         * @return true, if the message was sent.
         */
        public boolean send (final OperandStack message,
                             final long timeout,
                             final TimeUnit units);
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
