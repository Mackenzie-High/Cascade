package com.mackenziehigh.cascade.internal.loggers;

import com.mackenziehigh.cascade.CascadeLogger;
import java.util.Objects;

/**
 * Combines two loggers.
 */
public final class CompositeLogger
        implements CascadeLogger
{
    private final Object site;

    private final CascadeLogger delegate1;

    private final CascadeLogger delegate2;

    /**
     * Sole constructor.
     *
     * @param site is subject location of the logger.
     * @param logger1 will be delegated to.
     * @param logger2 will be delegated to.
     */
    public CompositeLogger (final Object site,
                            final CascadeLogger logger1,
                            final CascadeLogger logger2)
    {
        this.site = Objects.requireNonNull(site, "site");
        this.delegate1 = Objects.requireNonNull(logger1, "logger1");
        this.delegate2 = Objects.requireNonNull(logger2, "logger2");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object site ()
    {
        return site;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeLogger log (final LogLevel level,
                              final String message,
                              final Object... args)
    {
        delegate1.log(level, message, args);
        delegate2.log(level, message, args);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeLogger log (final LogLevel level,
                              final Throwable message)
    {
        delegate1.log(level, message);
        delegate2.log(level, message);
        return this;
    }
}
