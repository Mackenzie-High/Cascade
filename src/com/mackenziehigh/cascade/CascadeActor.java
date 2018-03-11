package com.mackenziehigh.cascade;

import com.mackenziehigh.cascade.Cascade;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Actors receive, process, and send event-messages.
 *
 * <p>
 * Actors are responsible for executing the onSetup(), onMessage(),
 * and onClose() handlers of the underlying script().
 * </p>
 *
 * <p>
 * An actor will never execute the same script() concurrently.
 * </p>
 *
 * <p>
 * By default, actors use effectively unbounded linked inflow-queues.
 * </p>
 *
 * <p>
 * By default, actors use the Drop Incoming overflow-policy.
 * </p>
 */
public interface CascadeActor
{
    /**
     * A builder that builds actor objects.
     */
    public interface Builder
    {
        /**
         * A factory that creates builders that build actor objects.
         *
         * @param <T> is the type of builder that will be created.
         */
        @FunctionalInterface
        public interface Factory<T extends Builder>
        {
            /**
             * Getter.
             *
             * @param stage will contain actors built via the builder.
             * @return the new builder.
             */
            public T newBuilder (CascadeStage stage);
        }

        /**
         * Getter.
         *
         * @return the stage that will contain the actor.
         */
        public CascadeStage stage ();

        /**
         * Getter.
         *
         * @return the newly built actor.
         */
        public CascadeActor build ();
    }

    public UUID uuid ();

    /**
     * Getter.
     *
     * @return the enclosing cascade.
     */
    public Cascade cascade ();

    /**
     * Getter.
     *
     * @return the enclosing stage.
     */
    public CascadeStage stage ();

    /**
     * Setter.
     *
     * <p>
     * In effect, this method overrides the default logger provided by
     * the default loggerFactory() that was specified by the enclosing
     * stage() when this actor was created.
     * </p>
     *
     * @param logger will be used as the logger() henceforth.
     * @return this.
     */
    public CascadeActor useLogger (CascadeLogger logger);

    /**
     * Getter.
     *
     * @return the current logger.
     */
    public CascadeLogger logger ();

    /**
     * Getter.
     *
     * @return the script that will be executed whenever messages are received.
     */
    public CascadeScript script ();

    /**
     * Causes this actor to switch to an fixed-size array-based
     * inflow-queue, which will be used to store the messages
     * that are pending processing by this actor.
     *
     * <p>
     * When backlogSize() reaches backlogCapacity().
     * the overflow-policy will dictate what happens
     * when new messages arrive for this actor.
     * </p>
     *
     * @param capacity will be the backlogCapacity() of the queue.
     * @return this.
     */
    public CascadeActor useArrayInflowQueue (int capacity);

    /**
     * Causes this actor to switch to an automatically expanding
     * array-based inflow-queue, which will be used to store the
     * messages that are pending processing by this actor.
     *
     * <p>
     * Whenever the queue becomes full, it will automatically
     * expand resize in order to facilitate storing more
     * messages, until the maximum capacity is reached.
     * At that point, the overflow-policy will dictate
     * how incoming messages affect the queue,
     * when the size is at capacity.
     * </p>
     *
     * <p>
     * When backlogSize() reaches backlogCapacity().
     * the overflow-policy will dictate what happens
     * when new messages arrive for this actor.
     * </p>
     *
     * @param size will be the initial size of the queue.
     * @param capacity will be the backlogCapacity() of the queue.
     * @param delta will be by how much the size of the queue
     * will increase automatically when the queue becomes too full.
     * @return this.
     */
    public CascadeActor useGrowableArrayInflowQueue (int size,
                                                     int capacity,
                                                     int delta);

    /**
     * Causes this actor to switch to an fixed-size array-based
     * inflow-queue, which will be used to store the messages
     * that are pending processing by this actor.
     *
     * <p>
     * When backlogSize() reaches backlogCapacity().
     * the overflow-policy will dictate what happens
     * when new messages arrive for this actor.
     * </p>
     *
     * @param capacity will be the backlogCapacity() of the queue.
     * @return this.
     */
    public CascadeActor useLinkedInflowQueue (int capacity);

    /**
     * Causes the overflow-policy to be changed to Drop All.
     *
     * <p>
     * After this method returns, whenever backlogSize() reaches
     * backlogCapacity() and a new message arrives for processing
     * by this actor, the inflow-queue will be cleared and then
     * the new message will be enqueued.
     * </p>
     *
     * @return this.
     */
    public CascadeActor useOverflowPolicyDropAll ();

    /**
     * Causes the overflow-policy to be changed to Drop Oldest.
     *
     * <p>
     * After this method returns, whenever backlogSize() reaches
     * backlogCapacity() and a new message arrives for processing
     * by this actor, the message that has been in the inflow-queue
     * for the longest period of time will be removed and then
     * the new message will be enqueued.
     * </p>
     *
     * @return this.
     */
    public CascadeActor useOverflowPolicyDropOldest ();

    /**
     * Causes the overflow-policy to be changed to Drop Newest.
     *
     * <p>
     * After this method returns, whenever backlogSize() reaches
     * backlogCapacity() and a new message arrives for processing
     * by this actor, the message that has been in the inflow-queue
     * for the shortest period of time will be removed and then
     * the new message will be enqueued.
     * </p>
     *
     * @return this.
     */
    public CascadeActor useOverflowPolicyDropNewest ();

    /**
     * Causes the overflow-policy to be changed to Drop Incoming.
     *
     * <p>
     * After this method returns, whenever backlogSize() reaches
     * backlogCapacity() and a new message arrives for processing
     * by this actor, the message that has just arrived will be
     * dropped and no message will be enqueued. In short, messages
     * will only be enqueued when there is actually space available.
     * </p>
     *
     * @return this.
     */
    public CascadeActor useOverflowPolicyDropIncoming ();

    /**
     * This method causes this actor to begin receiving messages for the given event.
     *
     * @param eventId identifies the event to listen for.
     * @return this.
     */
    public CascadeActor subscribe (CascadeToken eventId);

    /**
     * This method causes this actor to begin receiving messages for the given event.
     *
     * @param eventId identifies the event to listen for.
     * @return this.
     */
    public default CascadeActor subscribe (final String eventId)
    {
        return subscribe(CascadeToken.create(eventId));
    }

    /**
     * This method causes this actor to stop receiving messages for the given event.
     *
     * <p>
     * If this actor is not currently subscribed to the given event,
     * then this method is simply a no-op.
     * </p>
     *
     * @param eventId identifies the event to no longer listen for.
     * @return this.
     */
    public CascadeActor unsubscribe (CascadeToken eventId);

    /**
     * This method causes this actor to stop receiving messages for the given event.
     *
     * <p>
     * If this actor is not currently subscribed to the given event,
     * then this method is simply a no-op.
     * </p>
     *
     * @param eventId identifies the event to no longer listen for.
     * @return this.
     */
    public default CascadeActor unsubscribe (final String eventId)
    {
        return unsubscribe(CascadeToken.create(eventId));
    }

    /**
     * Getter.
     *
     * @return the identities of the events that this actor is listening for.
     */
    public Set<CascadeToken> subscriptions ();

    /**
     * Getter.
     *
     * @return true, if and only if, the script() is currently being executed.
     */
    public boolean isActing ();

    /**
     * Getter.
     *
     * @return true, if and only if, this actor is still on the stage.
     */
    public boolean isAlive ();

    /**
     * Getter.
     *
     * @return true, if and only if, this actor is leaving the stage.
     */
    public boolean isClosing ();

    /**
     * This method kills this actor, which causes it to stop listening
     * for incoming messages, remove itself from the stage, etc.
     */
    public void close ();

    /**
     * This method blocks, until this actor dies.
     *
     * @param timeout is the maximum amount of time to wait.
     * @param timeoutUnit describes the timeout.
     * @throws java.lang.InterruptedException
     */
    public void awaitClose (final long timeout,
                            final TimeUnit timeoutUnit)
            throws InterruptedException;

    /**
     * Getter.
     *
     * @return the time that this actor was created.
     */
    public Instant creationTime ();

    /**
     * Getter.
     *
     * @return how long this actor has been alive.
     */
    public default Duration age ()
    {
        return Duration.between(creationTime(), Instant.now());
    }

    /**
     * Getter.
     *
     * @return the current number of messages enqueued in the inflow queue.
     */
    public int backlogSize ();

    /**
     * Getter.
     *
     * @return the current capacity of the inflow queue.
     */
    public int backlogCapacity ();

    /**
     * Getter.
     *
     * @return the total number of messages sent to this actor,
     * thus far, including messages that had to be dropped.
     */
    public long receivedMessageCount ();

    /**
     * Getter.
     *
     * @return the total number of messages that this actor
     * has dropped upon receiving, thus far.
     */
    public long droppedMessageCount ();

    /**
     * Getter,
     *
     * @return the total number of messages that this actor
     * has actually processed using the script(), thus far.
     */
    public long consumedMessageCount ();

    /**
     * Getter.
     *
     * @return the total number of messages that this actor
     * has sent to other actors and/or itself, thus far.
     */
    public long producedMessageCount ();

    /**
     * Getter.
     *
     * @return the total number of messages that this actor has sent,
     * but no actors were listening for.
     */
    public long undeliveredMessageCount ();

    /**
     * Getter.
     *
     * @return the number of unhandled exceptions that
     * have been thrown by the script(), thus far.
     */
    public long unhandledExceptionCount ();

    /**
     * Causes the timing of script execution to be turned on or off.
     *
     * <p>
     * Timing only measures how long is spent processing messages.
     * The setup and close operations are now timed.
     * </p>
     *
     * @param state is true, if the timing should be turned on.
     * @return this.
     */
    public CascadeActor toggleStopwatch (boolean state);

    /**
     * Causes the stopwatch to be reset.
     *
     * @return a counter counting how many times this method has been called.
     */
    public long resetStopwatch ();

    /**
     * Getter.
     *
     * @return how long since the last call to resetStopwatch(),
     * or empty, if the stopwatch is currently turned off.
     */
    public Optional<Duration> elapsedTime ();

    /**
     * Getter,
     *
     * @return the total number of messages that this actor
     * has actually processed using the script(),
     * since the last call to resetStopwatch, or empty,
     * if the stopwatch is currently turned off.
     */
    public OptionalLong timedMessageCount ();

    /**
     * Getter.
     *
     * @return the maximum time spent processing a single message,
     * since the last call to resetStopwatch(), or empty,
     * if the stopwatch is currently turned off.
     */
    public Optional<Duration> maximumTime ();

    /**
     * Getter.
     *
     * @return the minimum time spent processing a single message,
     * since the last call to resetStopwatch(), or empty,
     * if the stopwatch is currently turned off.
     */
    public Optional<Duration> minimumTime ();

    /**
     * Getter.
     *
     * @return the total time spent processing messages,
     * since the last call to resetStopwatch(), or empty,
     * if the stopwatch is currently turned off.
     */
    public Optional<Duration> totalTime ();

    /**
     * Getter.
     *
     * @return the average time spent processing a single message,
     * since the last call to resetStopwatch(), or empty,
     * if the stopwatch is currently turned off.
     */
    public Optional<Duration> averageTime ();

    /**
     * Convenience method overload.
     *
     * @param dest is the given destination to forward the message to.
     * @return this.
     */
    public default CascadeActor monitorInput (final String dest)
    {
        return monitorInput(CascadeToken.create(dest));
    }

    /**
     * Causes all messages processed by this actor to be forwarded
     * to another event-stream as well, which is useful for
     * debugging purposes.
     *
     * <p>
     * This method facilitates the monitoring of inputs to this actor,
     * by other actors that are interested in listening.
     * </p>
     *
     * <p>
     * First, the event-identifier will be pushed on the operand-stack.
     * Second, the sequence-number of the message relative to this
     * actor will be pushed onto the operand-stack. This is the index
     * of the message in the history of messages received by this actor.
     * Third, the current Instant will be pushed onto the operand-stack.
     * Fourth, this actor will be pushed onto the operand-stack.
     * Finally, the operand-stack will be sent to the given destination.
     * </p>
     *
     * <p>
     * This method causes a message to be forwarded when it is removed
     * from the inflow-queue and begins being processed by the actor.
     * Thus, messages will not be forwarded while still in the inflow-queue.
     * Likewise, messages that are dropped due to the overflow-policy
     * will not be forwarded at all.
     * </p>
     *
     * @param dest is the given destination to forward the message to.
     * @return this.
     */
    public CascadeActor monitorInput (CascadeToken dest);

    /**
     * Convenience method overload.
     *
     * @param dest is the given destination to forward the message to.
     * @return this.
     */
    public default CascadeActor monitorOutput (final String dest)
    {
        return monitorInput(CascadeToken.create(dest));
    }

    /**
     * Causes all messages sent-from this actor to be forwarded
     * to another event-stream as well, which is useful for
     * debugging purposes.
     *
     * <p>
     * This method facilitates the monitoring of outputs from this actor,
     * by other actors that are interested in listening.
     * </p>
     *
     * <p>
     * First, the event-identifier will be pushed on the operand-stack.
     * Second, the sequence-number of the message relative to this
     * actor will be pushed onto the operand-stack. This is the index
     * of the message in the history of messages published by this actor.
     * Third, the current Instant will be pushed onto the operand-stack.
     * Fourth, this actor will be pushed onto the operand-stack.
     * Finally, the operand-stack will be sent to the given destination.
     * </p>
     *
     * @param dest is the given destination to forward the message to.
     * @return this.
     */
    public CascadeActor monitorOutput (CascadeToken dest);
}
