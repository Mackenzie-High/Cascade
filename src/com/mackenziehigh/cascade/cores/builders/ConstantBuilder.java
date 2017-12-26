package com.mackenziehigh.cascade.cores.builders;

import com.mackenziehigh.cascade.CascadeReactor;
import com.mackenziehigh.cascade.cores.NodeBuilder;
import java.util.Arrays;

/**
 *
 */
public final class ConstantBuilder
        implements NodeBuilder<ConstantBuilder>
{
    private volatile boolean clearStack = false;

    private byte[] constant = new byte[0];

    public ConstantBuilder clearStack ()
    {
        clearStack = false;
        return this;
    }

    public ConstantBuilder pushOnly ()
    {
        clearStack = false;
        return this;
    }

    public ConstantBuilder set (final byte[] value)
    {
        this.constant = Arrays.copyOf(value, 0);
        return this;
    }

    @Override
    public CascadeReactor.Core build ()
    {
        return null;
    }

}
