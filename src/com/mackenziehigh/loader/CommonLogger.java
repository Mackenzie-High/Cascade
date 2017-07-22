package com.mackenziehigh.loader;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Use this interface to issue log-messages.
 */
public final class CommonLogger
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
     * Log Messages.
     */
    public interface LogMessage
            extends Message
    {
        /**
         * This is the level of the log-message.
         *
         * @return the log-level.
         */
        public LogLevel level ();

        /**
         * This is the user-supplied log-message.
         *
         * @return the error-message.
         */
        public String message ();
    }

    private final UniqueID loggerID = UniqueID.random();

    private final AtomicLong messageCounter = new AtomicLong();

    private final AbstractModule module;

    private final MessageQueue queue;

    private final ImmutableMap<String, String> tags;

    /**
     * Constructor.
     *
     * @param queue is where log-messages will be sent.
     */
    public CommonLogger (final MessageQueue queue)
    {
        this(null, queue);
    }

    /**
     * Constructor.
     *
     * @param module contains this logger.
     * @param queue is where log-messages will be sent.
     */
    public CommonLogger (final AbstractModule module,
                         final MessageQueue queue)
    {
        this(module, queue, ImmutableMap.of());
    }

    /**
     * Constructor.
     *
     * @param module contains this logger.
     * @param queue is where log-messages will be sent.
     * @param tags are applied to this logger and its messages.
     */
    private CommonLogger (final AbstractModule module,
                          final MessageQueue queue,
                          final ImmutableMap<String, String> tags)
    {
        Preconditions.checkNotNull(queue, "queue");
        this.module = module;
        this.queue = queue;
        this.tags = tags;
    }

    /**
     * This method adds a tag to this logger.
     *
     * @param tag is the key-part of the tag to add.
     * @return a modified copy of this logger.
     */
    public CommonLogger tag (final String tag)
    {
        return tag(tag, "");
    }

    /**
     * This method adds a tag to this logger.
     *
     * <p>
     * This is a linear-time operation,
     * because the tag-map must be copied.
     * </p>
     *
     * @param key is the key-part of the tag to add.
     * @param value is the value-part of the tag to add.
     * @return a modified copy of this logger.
     */
    public CommonLogger tag (final String key,
                             final String value)
    {
        final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        builder.putAll(tags);
        builder.put(key, value);
        return new CommonLogger(module, queue, builder.build());
    }

    /**
     * These are the tags applied to this logger.
     *
     * @return the tags, if any.
     */
    public ImmutableMap<String, String> tags ()
    {
        return tags;
    }

    /**
     * These are the message queue(s) that will receive log messages.
     *
     * @return an immutable
     */
    public MessageQueue queue ()
    {
        return queue;
    }

    /**
     * This method issues a log message.
     *
     * @param level is the severity of the message.
     * @param message is the human-readable error-message.
     * @param exception is an exception, if applicable.
     * @return this.
     */
    private CommonLogger log (final LogLevel level,
                              final Object content)
    {
        final Instant creationTime = Instant.now();

        final UniqueID prefix = loggerID;
        final UniqueID suffix = UniqueID.fromBytes(messageCounter.incrementAndGet());
        final UniqueID messageID = UniqueID.combine(prefix, suffix);

        final LogMessage message = new LogMessage()
        {
            @Override
            public LogLevel level ()
            {
                return level;
            }

            @Override
            public String message ()
            {
                return content instanceof Throwable
                        ? ((Throwable) content).getMessage()
                        : (String) content;
            }

            @Override
            public String controllerName ()
            {
                return queue().processor().controller().name();
            }

            @Override
            public UniqueID controllerID ()
            {
                return queue().processor().controller().uniqueID();
            }

            @Override
            public String processorName ()
            {
                return queue().processor().name();
            }

            @Override
            public UniqueID processorID ()
            {
                return queue().processor().uniqueID();
            }

            @Override
            public String moduleName ()
            {
                return module == null ? null : module.name();
            }

            @Override
            public UniqueID moduleID ()
            {
                return module == null ? null : module.uniqueID();
            }

            @Override
            public UniqueID uniqueID ()
            {
                return messageID;
            }

            @Override
            public Instant creationTime ()
            {
                return creationTime;
            }

            @Override
            public Object content ()
            {
                return content;
            }

            @Override
            public UniqueID correlationID ()
            {
                return null;
            }
        };

        queue().send(message);

        return this;
    }

    /**
     * Use this method to issue a fatal-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public CommonLogger fatal (final String message)
    {
        return log(LogLevel.FATAL, message);
    }

    /**
     * Use this method to issue a fatal-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public CommonLogger fatal (final Throwable message)
    {
        return log(LogLevel.FATAL, message);
    }

    /**
     * Use this method to issue a error-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public CommonLogger error (final String message)
    {
        return log(LogLevel.ERROR, message);
    }

    /**
     * Use this method to issue a error-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public CommonLogger error (final Throwable message)
    {
        return log(LogLevel.ERROR, message);
    }

    /**
     * Use this method to issue a warning-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public CommonLogger warn (final String message)
    {
        return log(LogLevel.WARNING, message);
    }

    /**
     * Use this method to issue a warning-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public CommonLogger warn (final Throwable message)
    {
        return log(LogLevel.WARNING, message);
    }

    /**
     * Use this method to issue a info-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public CommonLogger info (final String message)
    {
        return log(LogLevel.INFO, message);
    }

    /**
     * Use this method to issue a info-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public CommonLogger info (final Throwable message)
    {
        return log(LogLevel.INFO, message);
    }

    /**
     * Use this method to issue a debug-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public CommonLogger debug (final String message)
    {
        return log(LogLevel.DEBUG, message);
    }

    /**
     * Use this method to issue a debug-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public CommonLogger debug (final Throwable message)
    {
        return log(LogLevel.DEBUG, message);
    }

    /**
     * Use this method to issue a trace-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public CommonLogger trace (final String message)
    {
        return log(LogLevel.TRACE, message);
    }

    /**
     * Use this method to issue a trace-level log-message.
     *
     * @param message is the log-message to issue.
     * @return this.
     */
    public CommonLogger trace (final Throwable message)
    {
        return log(LogLevel.TRACE, message);
    }
}
