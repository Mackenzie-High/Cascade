package com.mackenziehigh.cascade.functions;

import com.mackenziehigh.cascade.CascadeContext;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;

/**
 * Lambda function whose signature is the same as the onMessage() event-handler.
 */
@FunctionalInterface
public interface MessageFunction
{
    public void accept (CascadeContext ctx,
                        CascadeToken event,
                        CascadeStack stack)
            throws Throwable;
}
