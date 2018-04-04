package com.mackenziehigh.cascade.internal.loggers;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Objects;

/**
 * Converts log message to event-messages and sends them.
 */
public final class MessageLogger
        implements CascadeLogger
{
    private final Cascade cascade;

    private final Object site;

    private final CascadeToken output;

    public MessageLogger (final Cascade cascade,
                          final CascadeToken output,
                          final Object site)
    {
        this.cascade = Objects.requireNonNull(cascade, "cascade");
        this.site = Objects.requireNonNull(site, "site");
        this.output = Objects.requireNonNull(output, "output");
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
        CascadeStack stack = CascadeStack.newStack();

        stack = stack.pushObject(site);
        stack = stack.pushObject(level);
        stack = stack.pushObject(message);

        for (Object arg : args)
        {
            stack = stack.pushObject(arg);
        }

        cascade.lookup(output).send(output, stack);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeLogger log (final LogLevel level,
                              final Throwable message)
    {
        CascadeStack stack = CascadeStack.newStack();

        stack = stack.pushObject(site);
        stack = stack.pushObject(level);
        stack = stack.pushObject(message);

        cascade.lookup(output).send(output, stack);

        return this;
    }
}
