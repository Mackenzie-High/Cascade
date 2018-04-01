package com.mackenziehigh.cascade.loggers;

import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Objects;

/**
 * This is a logger that simply discards all log-messages.
 */
public final class NullLogger
        implements CascadeLogger
{
    private final CascadeToken site;

    public NullLogger (final CascadeToken site)
    {
        this.site = Objects.requireNonNull(site, "site");
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
        // Pass
        return this;
    }

    @Override
    public CascadeLogger log (final LogLevel level,
                              final Throwable message)
    {
        // Pass
        return this;
    }

}