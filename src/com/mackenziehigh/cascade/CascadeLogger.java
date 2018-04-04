package com.mackenziehigh.cascade;

import java.util.Objects;

/**
 * Use this interface to issue log-messages.
 *
 * <p>
 * Many of the logging methods herein support argument substitution.
 * The substitutions will be performed using the <code>format()</code> method herein.
 * </p>
 */
public interface CascadeLogger
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
     * Getter.
     *
     * @return where this logger is being used.
     */
    public Object site ();

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

    /**
     * This method substitutes the given arguments into the given format string.
     *
     * <p>
     * This method will replace occurrences of the substring, "{}",
     * with the equivalently indexed arguments. For example,
     * <code>
     * format("{} = {} + {}", "A", "B", "C") => "A = B + C"
     * </code>
     * </p>
     *
     * @param format will have the given arguments substituted into it.
     * @param arguments will be substituted into the given format string.
     * @return the result of the substitution.
     */
    public static String format (final String format,
                                 final Object... arguments)
    {
        final StringBuilder result = new StringBuilder();

        boolean opened = false;
        int idx = 0;

        for (int i = 0; i < format.length(); i++)
        {
            final char chr = format.charAt(i);

            if (chr == '{' && i < format.length() - 1)
            {
                opened = true;
            }
            else if (chr == '{')
            {
                result.append(chr);
                opened = false;
            }
            else if (chr == '}' && opened)
            {
                final Object argument = idx < arguments.length ? arguments[idx] : "";
                final String value = Objects.toString(argument);
                ++idx;
                result.append('{');
                result.append(value);
                result.append('}');
                opened = false;
            }
            else if (chr == '}' && !opened)
            {
                result.append(chr);
                opened = false;
            }
            else
            {
                result.append(chr);
                opened = false;
            }
        }

        return result.toString();
    }
}
