package com.mackenziehigh.cascade.reactor.internal;

import com.mackenziehigh.cascade.reactor.Powerplant;
import com.mackenziehigh.cascade.reactor.Reactor;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author mackenzie
 */
public final class DedicatedPowerplant
        implements Powerplant
{
    private final Thread thread = new Thread(this::run);

    public DedicatedPowerplant createAndStart ()
    {
        final DedicatedPowerplant plant = new DedicatedPowerplant();
        plant.thread.start();
        return plant;
    }

    private void run ()
    {

    }

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
