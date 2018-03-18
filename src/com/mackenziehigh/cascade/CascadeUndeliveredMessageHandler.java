package com.mackenziehigh.cascade;

/**
 *
 */
@FunctionalInterface
public interface CascadeUndeliveredMessageHandler
{
    public void onUndeliveredMessage (CascadeToken event,
                                      CascadeStack stack);
}
