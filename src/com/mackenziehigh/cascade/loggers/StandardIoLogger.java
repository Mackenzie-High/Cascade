package com.mackenziehigh.cascade.loggers;

import com.mackenziehigh.cascade.CascadeLogger;
import java.util.logging.Logger;

/**
 *
 */
public final class StandardIoLogger
        implements CascadeLogger
{
    private final Logger logger;

    private final Object site;

    public StandardIoLogger (final Object site)
    {
        this.site = site;
        this.logger = Logger.getLogger(site.getClass().getName());
    }

    @Override
    public Object site ()
    {
        return site;
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
        message.printStackTrace(System.err);
        return this;
    }
}
