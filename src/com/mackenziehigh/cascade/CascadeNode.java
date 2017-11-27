package com.mackenziehigh.cascade;

import com.google.common.base.Verify;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Instances of this interface receive messages from neighboring nodes,
 * process those messages, and send messages to neighboring nodes.
 */
public interface CascadeNode
{
    /**
     * This type of exception indicates that a indefinitely
     * blocking send operation failed to send a message.
     */
    public class SendFailureException
            extends Exception
    {
        private final CascadeNode source;

        public SendFailureException (final CascadeNode source)
        {
            this.source = Objects.requireNonNull(source, "source");
        }

        public CascadeNode getSource ()
        {
            return source;
        }
    }

    /**
     * Instances of this interface provide the business-logic of the node.
     *
     * <p>
     * Implementations of this class should have a nullary constructor,
     * if possible, in order to facilitate easy reflective instantiation.
     * </p>
     */
    public interface EventHandler
    {
        /**
         * Getter.
         *
         * <p>
         * Default Value = (1)
         * </p>
         *
         * @return the maximum number of threads that can concurrently
         * using the event-handler of this node to process events.
         */
        public default int concurrentCapacity ()
        {
            return 1;
        }

        /**
         * This is the first event-handler to be invoked.
         *
         * <p>
         * Only the controller should invoke this method.
         * </p>
         *
         * <p>
         * The controller shall only invoke this method once.
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
         * When this method is invoked, onSetup(*) has already been
         * invoked on all of the nodes, including this node itself.
         * </p>
         *
         * <p>
         * Only the controller should invoke this method.
         * </p>
         *
         * <p>
         * The controller shall only invoke this method once.
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
         * When this method is invoked, onStart(*) has already been
         * invoked on all of the nodes, including this node itself.
         * </p>
         *
         * <p>
         * As a general assumption, if concurrentCapacity() is one,
         * then this method will only be invoked by one thread at a time.
         * </p>
         *
         * <p>
         * This method may be repeatedly invoked during the lifetime
         * of the node in order to handle processing messages.
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
         * The onMessage(*) method will not be invoked on
         * any node ,including this node, once this method
         * begins execution, because shutdown has begun.
         * </p>
         *
         * <p>
         * This method is *not* guaranteed to be invoked during abnormal shutdowns.
         * </p>
         *
         * <p>
         * Only the controller should invoke this method.
         * </p>
         *
         * <p>
         * The controller shall only invoke this method once.
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
         * When this method is invoked, onStop(*) has already been
         * invoked on all of the nodes, including this node itself.
         * </p>
         *
         * <p>
         * This method is *not* guaranteed to be invoked during abnormal shutdowns.
         * </p>
         *
         * <p>
         * Only the controller should invoke this method.
         * </p>
         *
         * <p>
         * The controller shall only invoke this method once.
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
         * This event-handler will be invoked whenever an unhandled
         * exception is thrown by one of the other event-handlers.
         *
         * @param context provides access to the current state.
         * @throws java.lang.Throwable if something goes wrong.
         */
        public default void onException (final Context context)
                throws Throwable
        {
            Verify.verify(context.exception() != null);
            context.logger().error(context.exception());
        }
    }

    /**
     * An instance of this interface provides access to important values
     * during invocations of the event-handlers.
     *
     * <p>
     * Some values may be absent depending on the exact situation.
     * </p>
     */
    public interface Context
    {
        /**
         * Getter.
         *
         * <p>
         * For a given node instance, this method always returns the same object.
         * Thus, the node can safely use the result between event-handler invocations.
         * </p>
         *
         * <p>
         * This method never returns null.
         * </p>
         *
         * @return the system that this node is part of.
         */
        public Cascade cascade ();

        /**
         * Getter.
         *
         * <p>
         * For a given node instance, this method always returns the same object.
         * Thus, the node can safely use the result between event-handler invocations.
         * </p>
         *
         * <p>
         * This method never returns null.
         * </p>
         *
         * @return the logger intended for use by this actor.
         */
        public CascadeLogger logger ();

        /**
         * Getter.
         *
         * <p>
         * For a given node instance, this method always returns the same object.
         * Thus, the node can safely use the result between event-handler invocations.
         * </p>
         *
         * <p>
         * This method never returns null.
         * </p>
         *
         * @return the allocator intended for use by this actor.
         */
        public CascadeAllocator allocator ();

        /**
         * Getter.
         *
         * <p>
         * For a given node instance, this method always returns the same object.
         * Thus, the node can safely use the result between event-handler invocations.
         * </p>
         *
         * <p>
         * This method never returns null.
         * </p>
         *
         * @return the allocation-pool intended for use by this actor.
         */
        public CascadeAllocator.AllocationPool pool ();

        /**
         * Getter.
         *
         * <p>
         * This method returns the pump that is designated to power this node;
         * however, in some unique situations, this may not be the pump that
         * is actually powering the node. For example, assume that this node
         * is designated to be powered by a <i>direct</i> pump. In that case,
         * this method will return the <i>direct</i> pump, but the node will
         * actually be powered by the pump associated directly/indirectly
         * with the node that is sending messages to this node.
         * Moreover, in that case, this node may actually be powered by
         * multiple different pumps throughout its life-cycle due
         * to different nodes, powered by distinct pumps, sending messages
         * to this node.
         * </p>
         *
         * <p>
         * For a given node instance, this method always returns the same object.
         * Thus, the node can safely use the result between event-handler invocations.
         * </p>
         *
         * <p>
         * This method never returns null.
         * </p>
         *
         * @return the pump that powers this node.
         */
        public CascadePump pump ();

        /**
         * Getter.
         *
         * <p>
         * For a given node instance, this method always returns the same object.
         * Thus, the node can safely use the result between event-handler invocations.
         * </p>
         *
         * <p>
         * This method never returns null.
         * </p>
         *
         * @return this node.
         */
        public CascadeNode node ();

        /**
         * Getter.
         *
         * <p>
         * For a given node instance, this method always returns the same object.
         * Thus, the node can safely use the result between event-handler invocations.
         * </p>
         *
         * <p>
         * This method never returns null.
         * </p>
         *
         * @return the name of this node.
         */
        public String name ();

        /**
         * Getter.
         *
         * <p>
         * The list is ordered lexicographically based on
         * the names of the supplying nodes, ignoring case.
         * This node is the consumer in each of the edges.
         * </p>
         *
         * <p>
         * Note: Using a List here, rather than a Set,
         * avoids the need to allocate an Iterator object
         * whenever an iteration is performed.
         * </p>
         *
         * <p>
         * For a given node instance, this method always returns the same object.
         * Thus, the node can safely use the result between event-handler invocations.
         * </p>
         *
         * <p>
         * This method never returns null.
         * </p>
         *
         * @return the inputs that supply message to this actor.
         */
        public List<CascadeEdge> inputs ();

        /**
         * Getter.
         *
         * <p>
         * The list is ordered lexicographically based on
         * the names of the consuming actors, ignoring case.
         * This node is the supplier in each of the edges.
         * </p>
         *
         * <p>
         * Note: Using a List here, rather than a Set,
         * avoids the need to allocate an Iterator object
         * whenever an iteration is performed.
         * </p>
         *
         * <p>
         * For a given node instance, this method always returns the same object.
         * Thus, the node can safely use the result between event-handler invocations.
         * </p>
         *
         * <p>
         * This method never returns null.
         * </p>
         *
         * @return the pipelines that carry messages from this actor.
         */
        public List<CascadeEdge> outputs ();

        /**
         * Getter.
         *
         * <p>
         * This method will return null, unless a message is available.
         * </p>
         *
         * <p>
         * In particular, this method will return null inside
         * of the onSetup(*), onStart(*), onStop(*), and onDestroy(*)
         * event-handlers.
         * </p>
         *
         * <p>
         * Inside of the onException(*) event-handler, this method will return null,
         * unless the event-handler is handling an exception that was thrown by
         * the onMessage(*) event-handler. In that case, this method will return
         * an operand-stack containing the same message that was passed to onMessage(*)
         * when the event-handler threw the exception causing the onException(*) call.
         * The OperandStack objects themselves may have different identity though.
         * </p>
         *
         * @return the message that needs to be processed, if any.
         */
        public OperandStack message ();

        /**
         * Getter.
         *
         * <p>
         * This method returns null, except in the onException(*) event-handler.
         * In onException(*), this method returns the unhandled exception
         * that caused the event-handler to be invoked.
         * </p>
         *
         * @return the exception that needs to be processed, if any.
         */
        public Throwable exception ();

        /**
         * Atomically send a message to all of the outputs() without blocking.
         *
         * <p>
         * If the queueSize() of any outputs has reached its queueCapacity(),
         * then this method will simply return false, because the output
         * will be unable to accept the message. In other words, this method
         * will fail to send the message, if any of the outputs is at 100%
         * back-pressure.
         * </p>
         *
         * <p>
         * This method is atomic; therefore, the message will be enqueued
         * in all of the output edges or none of them.
         * </p>
         *
         * <p>
         * This method must maintain the ordering of messages in the face
         * of multiple threads invoking this method concurrently from
         * the exact same node.
         * </p>
         *
         * <p>
         * This method does not block indefinitely.
         * </p>
         *
         * @param message will be enqueued in each output edge.
         * @return true, if the message was actually sent.
         */
        public boolean async (OperandStack message);

        /**
         * Atomically send a message to the outputs(), blocking if necessary
         * until the outputs are able to enqueue the message.
         *
         * <p>
         * If the queueSize() of any outputs has reached its queueCapacity(),
         * then this method will simply return false, because the output
         * will be unable to accept the message. In other words, this method
         * will fail to send the message, if any of the outputs is at 100%
         * back-pressure.
         * </p>
         *
         * <p>
         * This method is atomic; therefore, the message will be enqueued
         * in all of the output edges or none of them.
         * </p>
         *
         * <p>
         * This method must maintain the ordering of messages in the face
         * of multiple threads invoking this method concurrently from
         * the exact same node.
         * </p>
         *
         * <p>
         * This method will block until all of the outputs enqueue the message,
         * or the given timeout is reached.
         * </p>
         *
         * <p>
         * This method will make an effort to wait up to the given timeout,
         * even in the face of unexpected internal faults.
         * </p>
         *
         * <p>
         * The timeout is a goal, not a real-time guarantee.
         * </p>
         *
         * @param message will be enqueued in each output edge.
         * @param timeout is the maximum amount of time to wait.
         * @param timeoutUnits describes the timeout.
         * @return true, if the message was actually sent.
         */
        public boolean sync (final OperandStack message,
                             final long timeout,
                             final TimeUnit timeoutUnits);

        /**
         * Atomically send a message to the outputs(), blocking if necessary
         * until the outputs are able to enqueue the message.
         *
         * <p>
         * This method is exactly like sync(*), except that it will keep
         * trying to send the message until either the message is sent
         * of normal shutdown begins.
         * </p>
         *
         * <p>
         * If normal shutdown begins, this this method will throw a SendFailureException.
         * Usually, you will simply want to log the exception, since shutdown has begun.
         * </p>
         *
         * @param message will be enqueued in each output edge.
         * @throws SendFailureException if shutdown has begun and the message cannot be sent.
         */
        public void send (final OperandStack message)
                throws SendFailureException;

        /**
         * Non-Atomically send a message to all of the outputs() without blocking.
         *
         * <p>
         * This method is non-atomic; therefore, the message will be enqueued
         * in as many of the outputs as will accept the message,
         * but not necessarily all of them.
         * </p>
         *
         * <p>
         * This method must maintain the ordering of messages in the face
         * of multiple threads invoking this method concurrently from
         * the exact same node.
         * </p>
         *
         * <p>
         * This method does not block indefinitely.
         * </p>
         *
         * @param message will be enqueued in each output edge.
         * @return the number of outputs that accepted the message.
         */
        public int broadcast (final OperandStack message);
    }

    /**
     * Getter.
     *
     * @return the name of this node.
     */
    public default String name ()
    {
        return protoContext().name();
    }

    /**
     * Getter.
     *
     * @return the base context from which others are derived.
     */
    public Context protoContext ();

    /**
     * Getter.
     *
     * @return the event-handler that implements the business-logic.
     */
    public EventHandler eventHandler ();
}
