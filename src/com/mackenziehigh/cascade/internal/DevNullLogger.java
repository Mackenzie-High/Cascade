package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.redo2.CascadeLogger;
import com.mackenziehigh.cascade.redo2.CascadeToken;
import java.util.Objects;

/**
 * This is a logger that simply discards all log-messages.
 */
public final class DevNullLogger
        implements CascadeLogger
{
    private final CascadeToken site;

    public DevNullLogger (final CascadeToken site)
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
