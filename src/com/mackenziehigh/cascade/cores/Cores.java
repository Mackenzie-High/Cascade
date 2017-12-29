package com.mackenziehigh.cascade.cores;

import com.mackenziehigh.cascade.CascadeReactor;
import com.mackenziehigh.cascade.CascadeReactor.Context;
import com.mackenziehigh.cascade.cores.builders.Clock;
import com.mackenziehigh.cascade.cores.builders.Printer;

/**
 *
 */
public final class Cores
{

    public static Clock newClock ()
    {
        return new Clock();
    }

    public static Printer newPrinter ()
    {
        return new Printer();
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
    public static CascadeReactor.Core from (final Handler<Context> action)
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
