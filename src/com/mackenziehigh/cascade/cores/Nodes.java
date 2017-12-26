package com.mackenziehigh.cascade.cores;

import com.mackenziehigh.cascade.cores.builders.ForwarderBuilder;
import com.mackenziehigh.cascade.cores.builders.PrinterBuilder;
import com.mackenziehigh.cascade.CascadeReactor;
import com.mackenziehigh.cascade.CascadeReactor.Context;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *
 */
public interface Nodes
{
    public final Supplier<ForwarderBuilder> FORWARDER = () -> new ForwarderBuilder();

    public final Supplier<PrinterBuilder> PRINTER = () -> new PrinterBuilder();

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
