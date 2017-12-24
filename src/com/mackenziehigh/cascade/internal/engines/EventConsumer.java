package com.mackenziehigh.cascade.internal.engines;

import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;

/**
 *
 */
public interface EventConsumer
{
    public void onMessage (CascadeToken event,
                           OperandStack message)
            throws Throwable;

    public default void onException (Throwable ex)
    {
        ex.printStackTrace(System.out); // TODO: temporary
    }
}
