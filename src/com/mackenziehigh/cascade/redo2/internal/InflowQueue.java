package com.mackenziehigh.cascade.redo2.internal;

import com.mackenziehigh.cascade.redo2.CascadeOperand;
import com.mackenziehigh.cascade.redo2.CascadeToken;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
public interface InflowQueue
        extends AutoCloseable
{
    public void push (CascadeToken event,
                      CascadeOperand operand);

    public boolean pop (AtomicReference<CascadeToken> eventOut,
                        AtomicReference<CascadeOperand> stackOut);

    public int size ();

    public int capacity ();

    @Override
    public void close ();

}
