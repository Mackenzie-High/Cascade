package com.mackenziehigh.cascade;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Instances of this interface receive messages from other reactors,
 * process those messages, and send messages to yet other reactors.
 */
public interface CascadeReactor
{
    /**
     * This type of exception indicates that a indefinitely
     * blocking send operation failed to send a message.
     */
    public class SendFailureException
            extends Exception
    {
        private final CascadeReactor source;

        public SendFailureException (final CascadeReactor source)
        {
            this.source = Objects.requireNonNull(source, "source");
        }

        public CascadeReactor getSource ()
        {
            return source;
        }
    }

    /**
     * An instances of this interface can be used to instantiate
     * instances of the business-logic event-handlers,
     * which provides the behavior of the reactor.
     */
    public interface CoreBuilder
    {
        public Core build ();
    }

    /**
     * Instances of this interface provide the business-logic of the reactor.
     *
     * <p>
     * Instances of this class must be made to be thread-safe; however,
     * there are some simplifying assumptions that can be made.
     * Cascade will only use threads owned by the corresponding pump
     * to invoke the methods herein, unless stated explicitly otherwise.
     * Cascade will only invoke the methods herein using one thread at a time.
     * However, Cascade does *not* guarantee that the same thread will always
     * be used to invoke the methods herein.
     * </p>
     */
    public interface Core
    {
        /**
         * Getter.
         *
         * <p>
         * This method returns the subscriptions that were known
         * by the <code>CoreBuilder</code> at the time that this
         * object was constructed, which may not be the full set
         * of subscriptions applicable to this object.
         * </p>
         *
         * <p>
         * This method will be invoked by one of the startup thread(s),
         * rather than a pump related thread.
         * </p>
         *
         * @return an immutable set containing the names of event-channels.
         */
        public default Set<CascadeToken> initialSubscriptions ()
        {
            return ImmutableSet.of();
        }

        /**
         * This is the first life-cycle event-handler to be invoked.
         *
         * <p>
         * Only the controller should cause this method to be invoked.
         * </p>
         *
         * <p>
         * The controller shall only cause this method to be invoked once.
         * </p>
         *
         * @param context provides access to the current state.
         * @throws java.lang.Throwable if something goes wrong.
         */
        public default void onSetup (final Context context)
                throws Throwable
        {
            // Pass.
        }

        /**
         * This is the second life-cycle event-handler to be invoked.
         *
         * <p>
         * When this method is invoked, onSetup(*) has already been
         * invoked on all of the reactors, including this reactor.
         * </p>
         *
         * <p>
         * Only the controller should cause this method to be invoked.
         * </p>
         *
         * <p>
         * The controller shall only cause this method to be invoked once.
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
         * This is the third life-cycle event-handler to be invoked.
         *
         * <p>
         * When this method is invoked, onStart(*) has already been
         * invoked on all of the reactors, including this reactor.
         * </p>
         *
         * <p>
         * This method may be repeatedly invoked during the lifetime
         * of the reactor in order to handle processing messages.
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
         * This is the fourth life-cycle event-handler to be invoked.
         *
         * <p>
         * This method is *not* guaranteed to be invoked during abnormal shutdowns.
         * </p>
         *
         * <p>
         * Only the controller should cause this method to be invoked.
         * </p>
         *
         * <p>
         * The controller shall only cause this method to be invoked once.
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
         * This is the fifth life-cycle event-handler to be invoked.
         *
         * <p>
         * When this method is invoked, onStop(*) has already been
         * invoked on all of the reactors, including this reactor.
         * </p>
         *
         * <p>
         * During a normal shutdown, this method will be periodically polled,
         * until it returns true in order to indicate stopping has completed.
         * The controller will not proceed to destroying the reactors,
         * until all reactors have acknowledged that they have stopped.
         * Therefore, the system will not be able to shutdown,
         * unless this method eventually returns true, for each reactor.
         * </p>
         *
         * <p>
         * The onMessage(*) method will not be invoked on any reactor,
         * including this reactor, once this method returns true
         * to the controller during a normal shutdown.
         * </p>
         *
         * <p>
         * This method is *not* guaranteed to be invoked during abnormal shutdowns.
         * </p>
         *
         * <p>
         * Only the controller should cause this method to be invoked.
         * </p>
         *
         * <p>
         * This method must be safe to invoke anytime and/or repeatedly
         * without causing any meaningful state change within the reactor.
         * </p>
         *
         * <p>
         * Implementations of this method should merely check a flag.
         * Do *not* perform long-running computations herein.
         * </p>
         *
         * @return true, if this is ready to begin the destruction phase.
         * @throws java.lang.Throwable if something goes wrong.
         */
        public default boolean isDestroyable ()
                throws Throwable
        {
            return true; // Because, pure reactors do not care.
        }

        /**
         * This is the sixth life-cycle event-handler to be invoked.
         *
         * <p>
         * When this method is invoked, onStop(*) has already been
         * invoked on all of the reactors, including this reactor,
         * and isDestroyable() has returned true for each reactor.
         * </p>
         *
         * <p>
         * This method is *not* guaranteed to be invoked during abnormal shutdowns.
         * </p>
         *
         * <p>
         * Only the controller should cause this method to be invoked.
         * </p>
         *
         * <p>
         * The controller shall only cause this method to be invoked once.
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
            context.reactor().logger().warn(context.exception());
        }

        /**
         * This method returns implementation-specific and time-specific
         * information that a programmer may find helpful during
         * development and/or debugging.
         *
         * @return an immutable map.
         */
        public default Map<String, String> debug ()
        {
            return ImmutableMap.of();
        }
    }

    /**
     * An instance of this interface provides access to important values
     * during invocations of the life-cycle event-handlers.
     *
     * <p>
     * Some values may be absent depending on the exact situation.
     * </p>
     *
     * <p>
     * Most of the methods herein simply delegate to the enclosing reactor.
     * </p>
     */
    public interface Context
            extends CascadeReactor
    {
        /**
         * Getter.
         *
         * @return the reactor that contains this context.
         */
        public CascadeReactor reactor ();

        /**
         * Getter.
         *
         * <p>
         * This method will return null, unless an event is available.
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
         * the same event-token that was passed to onMessage(*) when the event-handler
         * threw the exception causing the onException(*) call.
         * </p>
         *
         * @return the event that needs to be processed, if any.
         */
        public CascadeToken event ();

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
    }

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
     * @return the system that this reactor is part of.
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
     * @return the full-name of this reactor.
     */
    public CascadeToken name ();

    /**
     * Getter.
     *
     * <p>
     * This method never returns null.
     * </p>
     *
     * @return the event-handler that implements the business-logic.
     */
    public Core core ();

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
     * @return the logger intended for use by this reactor.
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
     * @return the allocator intended for use by this reactor.
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
     * @return the allocation-pool intended for use by this reactor.
     */
    public CascadeAllocator.AllocationPool pool ();

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
     * @return the pump that powers this reactor.
     */
    public CascadePump pump ();

    /**
     * This method retrieves the number of messages that
     * are currently enqueued awaiting processing.
     *
     * <p>
     * If this message-queue is based upon an underlying
     * queue that is shared with other message-queues,
     * then this result will be the sum of the sizes
     * of the message-queues. In other words, this method
     * retrieves the size of the underlying queue.
     * Thus, this result may be larger than queueSize().
     * </p>
     *
     * @return the total number of back-logged messages.
     */
    public int backlogSize ();

    /**
     * This method retrieves the maximum number of messages
     * that can be simultaneously enqueued awaiting processing.
     *
     * <p>
     * If this message-queue is based upon an underlying
     * queue that is shared with other message-queues,
     * then this result will be the capacity of the
     * Thus, this result may be larger than queueCapacity().
     * </p>
     *
     * @return the maximum number of back-logged messages.
     */
    public int backlogCapacity ();

    /**
     * This method retrieves the number of messages that
     * are currently enqueued awaiting processing in this
     * particular queue only.
     *
     * @return the total number of messages in this queue.
     */
    public int queueSize ();

    /**
     * This method retrieves the maximum number of messages
     * that can be enqueued at one time awaiting processing.
     *
     * <p>
     * The queueCapacity() is always upper-bounded by the backlogCapacity().
     * </p>
     *
     * <p>
     * The queueCapacity() is a best-case upper-bound.
     * In practice, the actual available capacity may be lower.
     * For example, if this message-queue is based upon
     * a underlying finite data-structure that is shared
     * with other message-queues, then the available capacity
     * at any single moment in time will be affected by
     * the size of each of the message-queues,
     * even if each individual message-queue has
     * a queueCapacity() equal to the backlogCapacity().
     * </p>
     *
     * <p>
     * In the situation where this message-queue cannot accept
     * more messages, because backlogSize() equals backlogCapacity(),
     * then we will say that the underlying queue is <i>overcommitted</i>.
     * </p>
     *
     * @return the maximum queue size.
     */
    public int queueCapacity ();

    /**
     * This method computes the current amount of back-pressure
     * in this message-queue, which is conceptually the percentage
     * of the queue that is currently full.
     *
     * <p>
     * If this message-queue is based upon an underlying
     * queue that is shared with other message-queues,
     * then the fullness of the other queues may affect
     * the back-pressure as well.
     * </p>
     *
     * @return the current back-pressure reading (0 &#8804 X &#8804 100.0).
     */
    public default double backpressure ()
    {
        if (queueCapacity() == 0 || backlogCapacity() == 0)
        {
            return 100.0;
        }
        else
        {
            final double queueBP = queueSize() / (double) queueCapacity();
            final double backlogBP = backlogSize() / (double) backlogCapacity();
            final double bp = 100.0 * Math.max(queueBP, backlogBP);
            return bp;
        }
    }

    /**
     * Getter.
     *
     * <p>
     * This method returns an immutable set.
     * </p>
     *
     * @return an immutable map that maps the full-names of event-channels
     * to objects that describe them and can be used to monitor them.
     */
    public Map<CascadeToken, CascadeSubscription> subscriptions ();

    /**
     * Atomically send an event-message without blocking.
     *
     * <p>
     * If the queueSize() of any receivers has reached its queueCapacity(),
     * then this method will simply return false, because the output
     * will be unable to accept the message. In other words, this method
     * will fail to send the message, if any of the outputs is at 100%
     * back-pressure.
     * </p>
     *
     * <p>
     * This method is atomic; therefore, the message will be enqueued
     * in all of the receivers or none of them.
     * </p>
     *
     * <p>
     * This method must maintain the ordering of messages in the face
     * of multiple threads invoking this method concurrently from
     * the exact same reactor, which is important, because reactors
     * are free to create private internal threads.
     * </p>
     *
     * <p>
     * This method does not block indefinitely.
     * </p>
     *
     * @param event identifies the event-channel to send the message to.
     * @param message will be enqueued in each receiver.
     * @return true, if the message was actually sent.
     */
    public boolean async (CascadeToken event,
                          OperandStack message);

    /**
     * Atomically send an event-message, blocking if necessary
     * until the receivers are able to enqueue the message.
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
     * in all of the receivers or none of them.
     * </p>
     *
     * <p>
     * This method must maintain the ordering of messages in the face
     * of multiple threads invoking this method concurrently from
     * the exact same reactor, which is important, because reactors
     * are free to create private internal threads.
     * </p>
     *
     * <p>
     * This method will block until all of the receivers enqueue the message,
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
     * @param event identifies the event-channel to send the message to.
     * @param message will be enqueued in each receiver.
     * @param timeout is the maximum amount of time to wait.
     * @param timeoutUnits describes the timeout.
     * @return true, if the message was actually sent.
     */
    public boolean sync (CascadeToken event,
                         OperandStack message,
                         long timeout,
                         TimeUnit timeoutUnits);

    /**
     * Atomically send an event-message, blocking if necessary
     * until the receivers are able to enqueue the message.
     *
     * <p>
     * This method is exactly like sync(*), except that it will keep
     * trying to send the message until either the message is sent
     * or normal shutdown begins.
     * </p>
     *
     * <p>
     * If normal shutdown begins, this this method will throw a SendFailureException.
     * Usually, you will simply want to log the exception, since shutdown has begun.
     * </p>
     *
     * @param event identifies the event-channel to send the message to.
     * @param message will be enqueued in each receiver.
     * @throws SendFailureException if shutdown has begun and the message cannot be sent.
     */
    public void send (CascadeToken event,
                      OperandStack message)
            throws SendFailureException;

    /**
     * Non-Atomically send a message without blocking.
     *
     * <p>
     * This method is non-atomic; therefore, the message will be enqueued
     * in as many of the receivers as will accept the message,
     * but not necessarily all of them.
     * </p>
     *
     * <p>
     * This method must maintain the ordering of messages in the face
     * of multiple threads invoking this method concurrently from
     * the exact same reactor, which is important, because reactors
     * are free to create private internal threads.
     * </p>
     *
     * <p>
     * This method does not block indefinitely.
     * </p>
     *
     * @param event identifies the event-channel to send the message to.
     * @param message will be enqueued in each receiver.
     * @return the number of outputs that accepted the message.
     */
    public int broadcast (CascadeToken event,
                          OperandStack message);
}
