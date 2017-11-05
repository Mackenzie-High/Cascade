package com.mackenziehigh.cascade.actors;

import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeActor.Context;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 *
 * @author mackenzie
 */
public final class TimerActor
        implements CascadeActor
{

    private final ScheduledExecutorService clock = Executors.newScheduledThreadPool(1);

    @Override
    public void onStart (final Context ctx)
            throws Throwable
    {

    }

}
