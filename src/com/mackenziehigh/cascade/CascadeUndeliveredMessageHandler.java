package com.mackenziehigh.cascade;

import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.CascadeStack;

/**
 *
 */
@FunctionalInterface
public interface CascadeUndeliveredMessageHandler
{
    public void onUndeliveredMessage (CascadeToken event,
                                      CascadeStack stack);
}
