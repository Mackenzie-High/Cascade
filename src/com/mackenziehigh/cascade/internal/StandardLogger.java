package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.redo2.CascadeLogger;
import com.mackenziehigh.cascade.redo2.CascadeToken;
import java.util.logging.Logger;

/**
 *
 */
public final class StandardLogger
        implements CascadeLogger
{
    private final Logger logger;

    private final CascadeToken site;

    public StandardLogger (final CascadeToken site)
    {
        this.logger = Logger.getLogger(site.name());
        this.site = site;
    }

    @Override
    public CascadeToken site ()
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
