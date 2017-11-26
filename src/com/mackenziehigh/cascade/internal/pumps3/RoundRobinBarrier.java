package com.mackenziehigh.cascade.internal.pumps3;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class RoundRobinBarrier<E>
{
    private final RoundRobinSelector<E> selector;

    private final Semaphore permits = new Semaphore(Integer.MAX_VALUE);

    public RoundRobinBarrier (final List<E> elements)
    {
        this.selector = new RoundRobinSelector<>(elements);
        this.permits.drainPermits();
    }

    public void increment (final int index)
    {
        selector.increment(index);
        permits.release();
    }

    public E select (final int index,
                     final long timeout,
                     final TimeUnit timeoutUnits)
            throws InterruptedException
    {
        if (permits.tryAcquire(timeout, timeoutUnits))
        {
            return selector.select(index);
        }
        else
        {
            return null;
        }
    }
}
