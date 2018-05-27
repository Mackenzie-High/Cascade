package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.Powerplant;
import com.mackenziehigh.cascade.Reactor;
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
    public void onPing (final Reactor reactor,
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
