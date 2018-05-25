package com.mackenziehigh.cascade.reactor.internal;

import com.mackenziehigh.cascade.reactor.Powerplant;
import com.mackenziehigh.cascade.reactor.Reactor;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
public final class NopPowerplant
        implements Powerplant
{
    @Override
    public void onStart (final Reactor reactor,
                         final AtomicReference<?> meta)
    {
        // Pass
    }

    @Override
    public void onStop (final Reactor reactor,
                        final AtomicReference<?> meta)
    {
        // Pass
    }

    @Override
    public void onReady (final Reactor reactor,
                         final AtomicReference<?> meta)
    {
        // Pass
    }

    @Override
    public void close ()
            throws Exception
    {
        // Pass
    }

}
