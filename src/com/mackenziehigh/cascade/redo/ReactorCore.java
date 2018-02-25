package com.mackenziehigh.cascade.redo;

import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.allocators.OperandStack;

/**
 *
 */
public interface ReactorCore
{
    public static interface Builder
    {
        public ReactorCore build ();
    }

    public default void onLoad (Reactor context)
            throws Throwable
    {
        // Pass
    }

    public default void onMessage (Reactor context,
                                   CascadeToken channel,
                                   OperandStack message)
            throws Throwable
    {
        // Pass
    }

    public default void onUnload (Reactor context)
            throws Throwable
    {
        // Pass
    }
}
