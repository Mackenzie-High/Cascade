package com.mackenziehigh.cascade.functions;

import com.mackenziehigh.cascade.CascadeContext;

/**
 * Lambda function whose signature is the same as the onSetup() event-handler.
 */
@FunctionalInterface
public interface SetupFunction
{
    public void accept (CascadeContext ctx)
            throws Throwable;
}
