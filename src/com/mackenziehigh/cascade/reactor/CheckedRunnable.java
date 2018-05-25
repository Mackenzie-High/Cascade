package com.mackenziehigh.cascade.reactor;

import com.google.common.base.Preconditions;

/**
 *
 */
@FunctionalInterface
public interface CheckedRunnable
{
    public void run ()
            throws Throwable;

    public default CheckedRunnable andThen (final CheckedRunnable next)
    {
        Preconditions.checkNotNull(next, "next");
        return () ->
        {
            this.run();
            next.run();
        };
    }
}
