package com.mackenziehigh.cascade.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Provides a default ScheduledExecutorService.
 */
public final class CommonClock
{
    private static volatile ScheduledExecutorService service = null;

    public static synchronized ScheduledExecutorService clock ()
    {
        if (service == null)
        {
            addShutdownHook();
            service = Executors.newSingleThreadScheduledExecutor();
        }

        return service;
    }

    private static void addShutdownHook ()
    {
        final Runnable task = () ->
        {
            if (service != null)
            {
                service.shutdownNow();
            }
        };
    }

}
