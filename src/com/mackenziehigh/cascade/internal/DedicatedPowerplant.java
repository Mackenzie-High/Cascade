package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.Powerplant;
import com.mackenziehigh.cascade.Reactor;
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
