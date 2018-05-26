package com.mackenziehigh.cascade.internal;

/**
 * What to do when overflow occurs.
 */
public enum OverflowPolicy
{
    /**
     * The overflow-policy depends on the queue implementation.
     */
    UNSPECIFIED,

    /**
     * Drop the oldest message that is already in the queue.
     */
    DROP_OLDEST,

    /**
     * Drop the newest message that is already in the queue.
     */
    DROP_NEWEST,

    /**
     * Drop the message that is being inserted into the queue,
     * rather than removing an element that is already in the queue.
     */
    DROP_INCOMING,

    /**
     * Drop everything already in the queue, but accept the incoming message.
     */
    DROP_PENDING,

    /**
     * Drop everything already in the queue and the incoming message.
     */
    DROP_ALL
}
