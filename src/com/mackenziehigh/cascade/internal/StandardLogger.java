package com.mackenziehigh.cascade.internal;



import com.mackenziehigh.cascade.CascadeLogger;
import java.util.logging.Logger;

/**
 *
 */
public final class StandardLogger
        implements CascadeLogger
{
    private final Logger logger;

    public StandardLogger (final String name)
    {
        this.logger = Logger.getLogger(name);
    }

    @Override
    public CascadeLogger log (final LogLevel level,
                              final String message,
                              final Object... args)
    {
        logger.warning(message); // TODO
        return this;
    }

    @Override
    public CascadeLogger log (LogLevel level,
                              Throwable message)
    {
        logger.warning(message.getMessage()); // TODO
        return this;
    }

}
