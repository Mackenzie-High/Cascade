package com.mackenziehigh.cascade.redo2;

/**
 * Use this interface to issue log-messages.
 *
 * <p>
 * Many of the logging methods herein support argument substitution.
 * The substitutions will be performed using the <code>String.format(*)</code> method.
 * </p>
 */
public interface CascadeLogger
{
    /**
     * An instance of this interface creates a new
     * logger for an entity with a given name.
     */
    public interface Factory
    {
        /**
         * Getter.
         *
         * @param name is the name of the usage site().
         * @return a newly created logger.
         */
        public CascadeLogger create (CascadeToken name);
    }

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
     * Getter.
     *
     * @return the name of the entity that this logger applies to.
     */
    public CascadeToken site ();

    /**
     * Use this method to issue a log-message.
     *
     * @param level is the log-level of the log-message.
     * @param message is the log-message to issue.
     * @param args will be substituted into the log-message.
     * @return this.
     */
    public CascadeLogger log (final LogLevel level,
                              final String message,
                              final Object... args);

    /**
     * Use this method to issue a log-message.
     *
     * @param level is the log-level of the log-message.
     * @param message is the log-message to issue.
     * @return this.
     */
    public CascadeLogger log (final LogLevel level,
                              final Throwable message);

    /**
     * Use this method to issue a fatal-level log-message.
     *
     * @param message is the log-message to issue.
     * @param args will be substituted into the log-message.
     * @return this.
     */
    public default CascadeLogger fatal (final String message,
                                        final Object... args)
    {
        return log(LogLevel.FATAL, message, args);
    }

    /**
     * Use this method to issue a fatal-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public default CascadeLogger fatal (final Throwable message)
    {
        return log(LogLevel.FATAL, message);
    }

    /**
     * Use this method to issue a error-level log-message.
     *
     * @param message is the log-message to issue.
     * @param args will be substituted into the log-message.
     * @return this.
     */
    public default CascadeLogger error (final String message,
                                        final Object... args)
    {
        return log(LogLevel.ERROR, message, args);
    }

    /**
     * Use this method to issue a error-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public default CascadeLogger error (final Throwable message)
    {
        return log(LogLevel.ERROR, message);
    }

    /**
     * Use this method to issue a warning-level log-message.
     *
     * @param message is the log-message to issue.
     * @param args will be substituted into the log-message.
     * @return this.
     */
    public default CascadeLogger warn (final String message,
                                       final Object... args)
    {
        return log(LogLevel.WARNING, message, args);
    }

    /**
     * Use this method to issue a warning-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public default CascadeLogger warn (final Throwable message)
    {
        return log(LogLevel.WARNING, message);
    }

    /**
     * Use this method to issue a info-level log-message.
     *
     * @param message is the log-message to issue.
     * @param args will be substituted into the log-message.
     * @return this.
     */
    public default CascadeLogger info (final String message,
                                       final Object... args)
    {
        return log(LogLevel.INFO, message, args);
    }

    /**
     * Use this method to issue a info-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public default CascadeLogger info (final Throwable message)
    {
        return log(LogLevel.INFO, message);
    }

    /**
     * Use this method to issue a debug-level log-message.
     *
     * @param message is the log-message to issue.
     * @param args will be substituted into the log-message.
     * @return this.
     */
    public default CascadeLogger debug (final String message,
                                        final Object... args)
    {
        return log(LogLevel.DEBUG, message, args);
    }

    /**
     * Use this method to issue a debug-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public default CascadeLogger debug (final Throwable message)
    {
        return log(LogLevel.DEBUG, message);
    }

    /**
     * Use this method to issue a trace-level log-message.
     *
     * @param message is the log-message to issue.
     * @param args will be substituted into the log-message.
     * @return this.
     */
    public default CascadeLogger trace (final String message,
                                        final Object... args)
    {
        return log(LogLevel.TRACE, message, args);
    }

    /**
     * Use this method to issue a trace-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public default CascadeLogger trace (final Throwable message)
    {
        return log(LogLevel.TRACE, message);
    }
}
