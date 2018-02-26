package com.mackenziehigh.cascade.redo2.internal;

import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;

/**
 * A thread-safe stopwatch.
 */
public final class SynchronizedStopwatch
{
    private final Stopwatch clock = Stopwatch.createStarted();

    public synchronized void reset ()
    {
        clock.reset();
    }

    public synchronized long elapsed (final TimeUnit unit)
    {
        return clock.elapsed(unit);
    }
}
