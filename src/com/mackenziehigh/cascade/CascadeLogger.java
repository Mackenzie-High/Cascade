package com.mackenziehigh.cascade;

import com.mackenziehigh.sexpr.SAtom;
import com.mackenziehigh.sexpr.SList;

/**
 * TODO!!! Allow printf like strings in log-message functions for ease of use.
 *
 * Use this interface to issue log-messages.
 */
public final class CascadeLogger
{
    /**
     * Log Levels.
     *
     * <p>
     * The order of these constants is significant.
     * The lowest levels have the lowest ordinal values.
     * The highest levels have the highest ordinal values.
     * </p>
     */
    public enum LogLevel
    {
        TRACE,
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        FATAL
    }

    /**
     * Constructor.
     *
     */
    public CascadeLogger ()
    {

    }

    /**
     * This method issues a log message.
     *
     * @param level is the severity of the message.
     * @param message is the human-readable error-message.
     * @param exception is an exception, if applicable.
     * @return this.
     */
    private CascadeLogger log (final LogLevel level,
                               final Object content)
    {
        final SAtom part1 = new SAtom(level.toString());
        final SAtom part2 = new SAtom(String.valueOf(content));
        final SList list = SList.of(part1, part2);

//        final Message message = Message.newMessage(sourceName, sourceID, messageCounter.incrementAndGet(), list);
//
//        queue().send(message);
        return this;
    }

    /**
     * Use this method to issue a fatal-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public CascadeLogger fatal (final String message)
    {
        return log(LogLevel.FATAL, message);
    }

    /**
     * Use this method to issue a fatal-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public CascadeLogger fatal (final Throwable message)
    {
        return log(LogLevel.FATAL, message);
    }

    /**
     * Use this method to issue a error-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public CascadeLogger error (final String message)
    {
        return log(LogLevel.ERROR, message);
    }

    /**
     * Use this method to issue a error-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public CascadeLogger error (final Throwable message)
    {
        return log(LogLevel.ERROR, message);
    }

    /**
     * Use this method to issue a warning-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public CascadeLogger warn (final String message)
    {
        return log(LogLevel.WARNING, message);
    }

    /**
     * Use this method to issue a warning-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public CascadeLogger warn (final Throwable message)
    {
        return log(LogLevel.WARNING, message);
    }

    /**
     * Use this method to issue a info-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public CascadeLogger info (final String message)
    {
        return log(LogLevel.INFO, message);
    }

    /**
     * Use this method to issue a info-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public CascadeLogger info (final Throwable message)
    {
        return log(LogLevel.INFO, message);
    }

    /**
     * Use this method to issue a debug-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public CascadeLogger debug (final String message)
    {
        return log(LogLevel.DEBUG, message);
    }

    /**
     * Use this method to issue a debug-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public CascadeLogger debug (final Throwable message)
    {
        return log(LogLevel.DEBUG, message);
    }

    /**
     * Use this method to issue a trace-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public CascadeLogger trace (final String message)
    {
        return log(LogLevel.TRACE, message);
    }

    /**
     * Use this method to issue a trace-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public CascadeLogger trace (final Throwable message)
    {
        return log(LogLevel.TRACE, message);
    }
}
