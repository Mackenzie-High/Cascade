package com.mackenziehigh.cascade.cores.builders;

import com.mackenziehigh.cascade.CascadeReactor;

/**
 *
 */
public final class RunnerBuilder
        implements CascadeReactor.CoreBuilder
{

    @Override
    public CascadeReactor.Core build ()
    {
        return new CascadeReactor.Core()
        {
        };
    }

}
