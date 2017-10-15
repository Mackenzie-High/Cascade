package com.mackenziehigh.cascade.actors;

import com.mackenziehigh.cascade.AbstractActor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 *
 * @author mackenzie
 */
public final class TimerActor
        extends AbstractActor
{

    private final ScheduledExecutorService clock = Executors.newScheduledThreadPool(1);

    @Override
    public void start ()
            throws Throwable
    {

    }

}
