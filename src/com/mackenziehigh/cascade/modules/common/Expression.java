package com.mackenziehigh.cascade.modules.common;

import com.mackenziehigh.cascade.AbstractModule;
import com.mackenziehigh.sexpr.SexprSchema;

/**
 * An instance of this class is a module that computes
 * the result of a mathematical expression based on
 * data that is lazily obtained from message queues.
 */
public final class Expression
        extends AbstractModule
{
    @Override
    public void setup ()
            throws Throwable
    {
        SexprSchema.fromResource("/com/mackenziehigh/cascade/resources/Expression.txt")
                .pass("INIT")
                .build()
                .match(configuration());
    }

}
