package com.mackenziehigh.cascade;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

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
 * Moreover, an actor will synchronize execution of the script()
 * in order to ensure memory-consistency.
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

    /**
     * Getter.
     *
     * @return a UUID that uniquely identifies this actor in time and space.
     */
    public UUID uuid ();

    /**
     * Getter.
     *
     * @return the enclosing cascade.
     */
    public default Cascade cascade ()
    {
        return stage().cascade();
    }

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
     * In effect, this method overrides the default that was provided
     * by the enclosing stage() when this actor was created.
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
     * Causes this actor to switch to a fixed-size array-based
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
     * increase its capacity in order to facilitate storing more
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
     * by this actor, the inflow-queue will be cleared
     * and the new message will also be dropped.
     * </p>
     *
     * @return this.
     */
    public CascadeActor useOverflowPolicyDropAll ();

    /**
     * Causes the overflow-policy to be changed to Drop Pending.
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
    public CascadeActor useOverflowPolicyDropPending ();

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
     * @param event identifies the event to listen for.
     * @return this.
     */
    public CascadeActor subscribe (CascadeToken event);

    /**
     * This method causes this actor to begin receiving messages for the given event.
     *
     * @param event identifies the event to listen for.
     * @return this.
     */
    public default CascadeActor subscribe (final String event)
    {
        return subscribe(CascadeToken.token(event));
    }

    /**
     * This method causes this actor to stop receiving messages for the given event.
     *
     * <p>
     * If this actor is not currently subscribed to the given event,
     * then this method is simply a no-op.
     * </p>
     *
     * @param event identifies the event to no longer listen for.
     * @return this.
     */
    public CascadeActor unsubscribe (CascadeToken event);

    /**
     * This method causes this actor to stop receiving messages for the given event.
     *
     * <p>
     * If this actor is not currently subscribed to the given event,
     * then this method is simply a no-op.
     * </p>
     *
     * @param event identifies the event to no longer listen for.
     * @return this.
     */
    public default CascadeActor unsubscribe (final String event)
    {
        return unsubscribe(CascadeToken.token(event));
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
     * @return true, if and only if, this actor is not closed or closing.
     */
    public boolean isActive ();

    /**
     * Getter.
     *
     * @return true, if and only if, this actor is leaving the stage.
     */
    public boolean isClosing ();

    /**
     * Getter.
     *
     * @return true, if and only if, this actor has left the stage.
     */
    public boolean isClosed ();

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
    public long receivedMessages ();

    /**
     * Getter.
     *
     * @return the total number of messages that this actor
     * has dropped upon receiving them, thus far.
     */
    public long droppedMessages ();

    /**
     * Getter.
     *
     * @return the total number of messages that this actor
     * has actually processed using the script(), thus far.
     */
    public long consumedMessages ();

    /**
     * Getter.
     *
     * @return the total number of messages that this actor
     * has sent to other actors and/or itself,
     * including undelivered messages, thus far.
     */
    public long producedMessages ();

    /**
     * Getter.
     *
     * @return the total number of messages that this actor has sent,
     * but no actors were listening for.
     */
    public long undeliveredMessages ();

    /**
     * Getter.
     *
     * @return the number of unhandled exceptions that
     * have been thrown by the script(), thus far.
     */
    public long unhandledExceptions ();

    /**
     * Causes this actor to be monitored by the given director.
     *
     * @param director will monitor this actor.
     * @return this.
     */
    public CascadeActor registerDirector (CascadeDirector director);

    /**
     * Causes this actor will no-longer be monitored by the given director.
     *
     * <p>
     * This method is a no-op, if the given director does not monitor this actor.
     * </p>
     *
     * @param director will no-longer monitor this actor.
     * @return this.
     */
    public CascadeActor deregisterDirector (CascadeDirector director);

    /**
     * This method kills this actor, which causes it to stop listening
     * for incoming messages, remove itself from the stage, etc.
     *
     * <p>
     * This method returns immediately; however, the actor will not close
     * until it has finished any work that it is currently performing.
     * </p>
     */
    public void close ();

    /**
     * This method blocks, until this actor dies.
     *
     * @param timeout is the maximum amount of time to wait.
     * @throws java.lang.InterruptedException
     */
    public void awaitClose (Duration timeout)
            throws InterruptedException;
}
