package com.mackenziehigh.cascade.redo2.internal;

import com.mackenziehigh.cascade.redo2.CascadeOperand;
import com.mackenziehigh.cascade.redo2.CascadeToken;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
public final class RoundRobinScheduler
{
    public void add (final InflowQueue queue,
                     final Runnable handler)
    {

    }

    public void remove (final InflowQueue queue)
    {

    }

    public void update (final InflowQueue queue)
    {

    }

    public boolean poll (AtomicReference<CascadeToken> eventOut,
                         AtomicReference<CascadeOperand> stackOut)
    {
        return false;
    }
}
