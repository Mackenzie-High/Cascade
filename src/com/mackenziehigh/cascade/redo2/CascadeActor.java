package com.mackenziehigh.cascade.redo2;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 *
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

    public UUID uniqueId ();

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

    public CascadeActor useLogger (CascadeLogger logger);

    public CascadeLogger logger ();

    public CascadeActor useAllocator (CascadeAllocator allocator);

    public CascadeAllocator allocator ();

    /**
     * Getter.
     *
     * @return the script that will be executed whenever messages are received.
     */
    public CascadeScript script ();

    public CascadeActor useArrayInflowQueue (int capacity);

    public CascadeActor useLinkedInflowQueue (int capacity);

    public CascadeActor useOverflowPolicyDropAll ();

    public CascadeActor useOverflowPolicyDropOldest ();

    public CascadeActor useOverflowPolicyDropNewest ();

    public CascadeActor useOverflowPolicyDropIncoming ();

    public CascadeActor useOverflowPolicyIncrementCapacity (int delta);

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
    public CascadeActor subscribe (String eventId);

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
    public CascadeActor unsubscribe (String eventId);

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
     */
    public void awaitClose ();

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
}
