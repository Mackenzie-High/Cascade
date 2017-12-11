package com.mackenziehigh.cascade.nodes.builders;

import com.mackenziehigh.cascade.CascadeNode;
import com.mackenziehigh.cascade.nodes.NodeBuilder;
import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * Node Builder for Printer nodes.
 */
public final class PrinterBuilder
        implements NodeBuilder<PrinterBuilder>
{
    private volatile Charset charset = Charset.forName("UTF-8");

    private volatile PrintStream stream = System.out;

    private volatile boolean newline = true;

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeNode.Core build ()
    {
        return new CascadeNode.Core()
        {
            @Override
            public void onMessage (final CascadeNode.Context context)
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
