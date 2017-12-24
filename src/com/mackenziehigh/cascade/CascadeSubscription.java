package com.mackenziehigh.cascade;

import java.time.Duration;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * An instance of this interface describes an event-channel
 * that a given reactor is subscribed to, and can be used
 * to monitor that event-channel for event-messages coming
 * across it to the given reactor.
 *
 * <p>
 * Monitoring must be explicitly turned on.
 * By default, monitoring is turned off.
 * Toggling monitoring on/off/on will reset the underlying counters.
 * </p>
 */
public interface CascadeSubscription
{
    /**
     * Getter.
     *
     * @return the system that this subscription is part of.
     */
    public Cascade cascade ();

    /**
     * Getter.
     *
     * @return the reactor that contains this subscription.
     */
    public CascadeSubscription reactor ();

    /**
     * Use this method to turn on monitoring.
     *
     * @return this.
     */
    public CascadeSubscription enableMonitoring ();

    /**
     * Use this method to turn off monitoring.
     *
     * @return this.
     */
    public CascadeSubscription disableMonitoring ();

    /**
     * Getter.
     *
     * @return true, if monitoring is turned on.
     */
    public boolean isEnabled ();

    /**
     * Getter.
     *
     * @return the total number of messages enqueued,
     * or empty, if monitoring is turned off.
     */
    public OptionalLong getTotalEnqueues ();

    /**
     * Getter.
     *
     * @return the total number of messages dequeued,
     * or empty, if monitoring is turned off.
     */
    public OptionalLong getTotalDequeues ();

    /**
     * Getter.
     *
     * <p>
     * This method is *not* suitable for detecting rate spikes.
     * </p>
     *
     * @return the averaged number of messages per second,
     * or empty, if monitoring is turned off.
     */
    public OptionalLong getMessageRate ();

    /**
     * Getter.
     *
     * <p>
     * This method is *not* suitable for detecting rate spikes.
     * </p>
     *
     * @return the averaged number of bytes per second,
     * or empty, if monitoring is turned off.
     */
    public OptionalLong getBandwidth ();

    /**
     * Getter.
     *
     * @return the total number of bytes enqueued,
     * or empty, if monitoring is turned off.
     */
    public OptionalLong getTotalBytesEnqueued ();

    /**
     * Getter.
     *
     * @return the total number of bytes dequeued,
     * or empty, if monitoring is turned off.
     */
    public OptionalLong getTotalBytesDequeued ();

    /**
     * Getter.
     *
     * @return the maximum number of bytes enqueued in a single message,
     * or empty, if monitoring is turned off or no messages were received.
     */
    public OptionalLong getMaxBytes ();

    /**
     * Getter.
     *
     * @return the minimum number of bytes enqueued in a single message,
     * or empty, if monitoring is turned off or no messages were received.
     */
    public OptionalLong getMinBytes ();

    /**
     * Getter.
     *
     * @return the average number of bytes enqueued in a single message,
     * or empty, if monitoring is turned off or no messages were received.
     */
    public OptionalLong getAvgBytes ();

    /**
     * Getter.
     *
     * @return the number of nanoseconds between the first and last message,
     * or empty, if monitoring is turned off or no messages were received.
     */
    public Optional<Duration> getDuration ();

    /**
     * Getter.
     *
     * @return the maximum that backlogSize() has reached,
     * or empty, if monitoring is turned off or no messages were received.
     */
    public OptionalInt getMaxBacklogSize ();

    /**
     * Getter.
     *
     * @return the average that backlogSize() has reached,
     * or empty, if monitoring is turned off or no messages were received.
     */
    public OptionalInt getAvgBacklogSize ();

    /**
     * Getter.
     *
     * @return the maximum that queueSize() has reached,
     * or empty, if monitoring is turned off or no messages were received.
     */
    public OptionalInt getMaxQueueSize ();

    /**
     * Getter.
     *
     * @return the average that queueSize() has reached,
     * or empty, if monitoring is turned off or no messages were received.
     */
    public OptionalInt getAvgQueueSize ();
}
