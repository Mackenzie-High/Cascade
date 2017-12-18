package com.mackenziehigh.cascade2;

import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;

/**
 * Invoked by the engine.
 */
public interface MessageHandler
{
    public void onMessage (Token event,
                           OperandStack message);
}
