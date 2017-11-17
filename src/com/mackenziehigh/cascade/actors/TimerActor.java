package com.mackenziehigh.cascade.actors;

import com.mackenziehigh.cascade.CascadePlant.Context;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import com.mackenziehigh.cascade.CascadePlant;

/**
 *
 * @author mackenzie
 */
public final class TimerActor
        implements CascadePlant
{

    private final ScheduledExecutorService clock = Executors.newScheduledThreadPool(1);

    @Override
    public void onStart (final Context ctx)
            throws Throwable
    {

    }

}
