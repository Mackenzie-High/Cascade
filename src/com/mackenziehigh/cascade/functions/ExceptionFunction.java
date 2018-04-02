package com.mackenziehigh.cascade.functions;

import com.mackenziehigh.cascade.CascadeContext;

/**
 * Lambda function whose signature is the same as the onUnhandledException() event-handler.
 */
@FunctionalInterface
public interface ExceptionFunction
{
    public void accept (CascadeContext ctx,
                        Throwable cause)
            throws Throwable;
}
