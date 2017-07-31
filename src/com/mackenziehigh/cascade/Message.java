package com.mackenziehigh.cascade;

import com.google.common.base.Preconditions;
import com.mackenziehigh.sexpr.Sexpr;
import java.time.Instant;

/**
 * Base Message Interface.
 */
public final class Message
{
    private final String sourceName;

    private final UniqueID sourceID;

    private final long sequenceNumber;

    private final UniqueID uniqueID;

    private final UniqueID correlationID;

    private final long creationTimeMillis;

    private final long referenceTimeNano;

    private final Sexpr content;

    private Message (final String sourceName,
                     final UniqueID sourceID,
                     final long sequenceNumber,
                     final UniqueID uniqueID,
                     final UniqueID correlationID,
                     final long creationTimeMillis,
                     final long referenceTimeNano,
                     final Sexpr content)
    {
        Preconditions.checkNotNull(sourceName, "sourceName");
        Preconditions.checkNotNull(sourceID, "sourceID");
        Preconditions.checkArgument(sequenceNumber >= 0, "sequenceNumber < 0");
        Preconditions.checkNotNull(uniqueID, "uniqueID");
        Preconditions.checkNotNull(correlationID, "correlationID");
        Preconditions.checkNotNull(content, "content");
        this.sourceName = sourceName;
        this.sourceID = sourceID;
        this.sequenceNumber = sequenceNumber;
        this.uniqueID = uniqueID;
        this.correlationID = correlationID;
        this.creationTimeMillis = creationTimeMillis;
        this.referenceTimeNano = referenceTimeNano;
        this.content = content;
    }

    /**
     * This is the user-defined name of the source that created this message.
     *
     * @return the name of the source.
     */
    public String sourceName ()
    {
        return sourceName;
    }

    /**
     * This is the unique-ID of the source that created this message.
     *
     * @return the unique-ID of the source.
     */
    public UniqueID sourceID ()
    {
        return sourceID;
    }

    /**
     * This method retrieves the index of this message
     * relative to other messages from the same exact source.
     *
     * @return the sequence-number of this message.
     */
    public long sequenceNumber ()
    {
        return sequenceNumber;
    }

    /**
     * This method retrieves the unique-ID of this message.
     *
     * @return the unique-ID of this message.
     */
    public UniqueID uniqueID ()
    {
        return uniqueID;
    }

    /**
     * This method retrieves the correlation-ID of this message.
     *
     * <p>
     * If a message does not have an explicitly specified correlation-ID,
     * then this will simply be the unique-ID of the message itself.
     * </p>
     *
     * @return the correlation-ID.
     */
    public UniqueID correlationID ()
    {
        return correlationID;
    }

    /**
     * This is when the message was created.
     *
     * <p>
     * Equivalent To: Instant.ofEpochMilli(creationTimeMillis())
     * </p>
     *
     * @return the creation time.
     */
    public Instant creationTime ()
    {
        return Instant.ofEpochMilli(creationTimeMillis());
    }

    /**
     * This is when the message was created.
     *
     * <p>
     * The result is expressed in milliseconds from the epoch.
     * This method is equivalent to System.currentTimeMillis().
     * </p>
     *
     * <p>
     * If this object is the result of deserialization,
     * then this is the creation-time <b>before</b> serialization.
     * </p>
     *
     * @return the creation time.
     */
    public long creationTimeMillis ()
    {
        return creationTimeMillis;
    }

    /**
     * This is the number of nanoseconds from an arbitrary point in time.
     *
     * <p>
     * This method is equivalent to System.nanoTime().
     * </p>
     *
     * <p>
     * This value can only be meaningfully compared
     * to values from the exact same source.
     * </p>
     *
     * <p>
     * If this object is the result of deserialization,
     * then this is the reference-time <b>before</b> serialization.
     * </p>
     *
     * @return the nanoseconds from the arbitrary reference point.
     */
    public long referenceTimeNano ()
    {
        return referenceTimeNano;
    }

    /**
     * This is user-defined content of this message.
     *
     * @return the content.
     */
    public Sexpr content ()
    {
        return content;
    }

    @Override
    public String toString ()
    {
        return "Message: sourceName = '" + sourceName + "'"
               + ", sourceID = " + sourceID
               + ", sequenceNumber = " + sequenceNumber
               + ", uniqueID = " + uniqueID
               + ", correlationID = " + correlationID
               + ", creationTimeMillis = '" + creationTime() + "'"
               + ", creationTimeMillis = " + creationTimeMillis
               + ", referenceTimeNano = " + referenceTimeNano
               + ", content = " + content;
    }

    /**
     * This method creates a a new message.
     *
     * @param sourceName is the value for sourceName().
     * @param sourceID is the value for sourceID().
     * @param sequenceNumber is the value for sequenceNumber().
     * @param content is the value for content().
     * @return a new message-builder.
     */
    public static Message newMessage (final String sourceName,
                                      final UniqueID sourceID,
                                      final long sequenceNumber,
                                      final Sexpr content)
    {
        final UniqueID id = UniqueID.random();
        return new Message(sourceName,
                           sourceID,
                           sequenceNumber,
                           id,
                           id,
                           System.currentTimeMillis(),
                           System.nanoTime(),
                           content);
    }

    /**
     * This method creates a a new message.
     *
     * @param sourceName is the value for sourceName().
     * @param sourceID is the value for sourceID().
     * @param sequenceNumber is the value for sequenceNumber().
     * @param correlationID is the value for correlationID().
     * @param content is the value for content().
     * @return a new message-builder.
     */
    public static Message newMessage (final String sourceName,
                                      final UniqueID sourceID,
                                      final long sequenceNumber,
                                      final UniqueID correlationID,
                                      final Sexpr content)
    {
        return new Message(sourceName,
                           sourceID,
                           sequenceNumber,
                           UniqueID.random(),
                           correlationID,
                           System.currentTimeMillis(),
                           System.nanoTime(),
                           content);
    }

    /**
     * This method creates a a new message.
     *
     * @param sourceName is the value for sourceName().
     * @param sourceID is the value for sourceID().
     * @param sequenceNumber is the value for sequenceNumber().
     * @param uniqueID is the value for uniqueID().
     * @param correlationID is the value for correlationID().
     * @param creationTimeMillis is the value for creationTimeMillis().
     * @param referenceTimeNano is the value for referenceTimeNano().
     * @param content is the value for content().
     * @return a new message-builder.
     */
    public static Message newMessage (final String sourceName,
                                      final UniqueID sourceID,
                                      final long sequenceNumber,
                                      final UniqueID uniqueID,
                                      final UniqueID correlationID,
                                      final long creationTimeMillis,
                                      final long referenceTimeNano,
                                      final Sexpr content)
    {
        return new Message(sourceName,
                           sourceID,
                           sequenceNumber,
                           uniqueID,
                           correlationID,
                           creationTimeMillis,
                           referenceTimeNano,
                           content);
    }

}
