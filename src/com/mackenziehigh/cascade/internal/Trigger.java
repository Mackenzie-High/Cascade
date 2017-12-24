package com.mackenziehigh.cascade.internal;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public final class Trigger
{
    private final Semaphore permit = new Semaphore(1);

    private final AtomicBoolean awakenNeeded = new AtomicBoolean();

    public Trigger ()
    {
        permit.drainPermits();
        awakenNeeded.set(true);
    }

    public void await (final long timeout,
                       final TimeUnit timeoutUnit)
            throws InterruptedException
    {
        permit.tryAcquire(timeout, timeoutUnit);
        awakenNeeded.set(true);
    }

    public void signal ()
    {
        if (awakenNeeded.compareAndSet(true, false))
        {
            permit.release();
        }
    }
}
