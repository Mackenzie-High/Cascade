package com.mackenziehigh.cascade.cores.builders;

import com.mackenziehigh.cascade.CascadeReactor;
import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * Node Builder for Printer nodes.
 */
public final class Printer
        implements CascadeReactor.CoreBuilder
{
    private volatile Charset charset = Charset.forName("UTF-8");

    private volatile PrintStream stream = System.out;

    private volatile boolean newline = true;

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeReactor.Core build ()
    {
        return new CascadeReactor.Core()
        {
            @Override
            public void onMessage (final CascadeReactor.Context context)
                    throws Throwable
            {
                final byte[] bytes = context.message().asByteArray();
                context.message().pop();
                final String string = new String(bytes, charset);

                if (newline)
                {
                    stream.println(string);
                }
                else
                {
                    stream.print(string);
                }
            }
        };
    }

}
