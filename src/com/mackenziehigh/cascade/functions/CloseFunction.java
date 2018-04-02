package com.mackenziehigh.cascade.functions;

import com.mackenziehigh.cascade.CascadeContext;

/**
 * Lambda function whose signature is the same as the onClose() event-handler.
 */
@FunctionalInterface
public interface CloseFunction
{
    public void accept (CascadeContext ctx)
            throws Throwable;
}
