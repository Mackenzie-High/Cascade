package com.mackenziehigh.cascade.internal.loggers;

import com.mackenziehigh.cascade.CascadeLogger;
import java.io.PrintStream;
import java.util.Objects;

/**
 * Synchronously writes log messages to a print-stream.
 */
public final class PrintLogger
        implements CascadeLogger
{

    private final PrintStream out;

    private final Object site;

    public PrintLogger (final Object site,
                        final PrintStream stream)
    {
        this.site = site;
        this.out = Objects.requireNonNull(stream, "stream");
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
        CascadeLogger.format("[{}][{}]", args);
        return this;
    }

    @Override
    public CascadeLogger log (LogLevel level,
                              Throwable message)
    {

        message.printStackTrace(System.err);
        return this;
    }
}
