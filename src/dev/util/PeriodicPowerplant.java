package dev.util;

import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.Powerplant;
import com.mackenziehigh.cascade.Reactor;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
public class PeriodicPowerplant
        implements Powerplant
{
    private final Set<Reactor> reactors = Sets.newConcurrentHashSet();

    private final Thread thread = new Thread(this::run);

    private final Semaphore wait = new Semaphore(0);

    public void add (final Reactor reactor)
    {
        reactors.add(reactor);
    }

    public void start ()
    {
        thread.start();
    }

    private void run ()
    {
        try
        {
            while (true)
            {
                reactors.stream().forEach(x -> x.crank());
                wait.tryAcquire(250, TimeUnit.MILLISECONDS);
            }
        }
        catch (Throwable ex)
        {
            ex.printStackTrace(System.err);
        }
    }

    @Override
    public void onStart (final Reactor reactor,
                         final AtomicReference<?> meta)
    {
        reactors.add(reactor);
    }

    @Override
    public void onStop (final Reactor reactor,
                        final AtomicReference<?> meta)
    {
        // Pass.
    }

    @Override
    public void onPing (final Reactor reactor,
                        final AtomicReference<?> meta)
    {
        // Pass.
    }

    @Override
    public void close ()
            throws Exception
    {
        // Pass.
    }

}
