package com.mackenziehigh.cascade.cores;

import com.mackenziehigh.cascade.CascadeReactor;
import com.mackenziehigh.cascade.CascadeReactor.Context;
import com.mackenziehigh.cascade.cores.builders.ForwarderBuilder;
import com.mackenziehigh.cascade.cores.builders.PrinterBuilder;
import com.mackenziehigh.cascade.cores.builders.TickerBuilder;
import java.util.function.Consumer;

/**
 *
 */
public final class Cores
{
    public static ForwarderBuilder newForwarder ()
    {
        return new ForwarderBuilder();
    }

    public static TickerBuilder newTicker ()
    {
        return new TickerBuilder();
    }

    public static PrinterBuilder newPrinter ()
    {
        return new PrinterBuilder();
    }

    /**
     * Returns a core that functions as a no-op.
     *
     * @return the new reactor core.
     */
    public static CascadeReactor.Core nop ()
    {
        return from(x ->
        {
            // Pass
        });
    }

    /**
     * Converts a consumer to a reactor core.
     *
     * @param action will be converted.
     * @return the new reactor core.
     */
    public static CascadeReactor.Core from (final Consumer<Context> action)
    {
        return new CascadeReactor.Core()
        {
            @Override
            public void onMessage (Context context)
                    throws Throwable
            {
                action.accept(context);
            }
        };
    }
}
