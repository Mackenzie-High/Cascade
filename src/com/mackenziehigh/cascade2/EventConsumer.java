package com.mackenziehigh.cascade2;

import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;

/**
 *
 */
public interface EventConsumer
{
    public void onMessage (Token event,
                           OperandStack message)
            throws Throwable;

    public default void onException (Throwable ex)
    {
        ex.printStackTrace(System.out); // TODO: temporary
    }
}
