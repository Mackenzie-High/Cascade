package com.mackenziehigh.cascade.dev2;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.Cascade.Stage.Actor;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class Clock
{
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    private final Actor<Instant, Instant> actor;

    public final Actor.Output<Instant> clockOut;

    public Clock ()
    {
        actor = Cascade.newStage().newActor().withScript((Instant x) -> x).create();
        clockOut = actor.output();
        service.scheduleAtFixedRate(() -> actor.accept(Instant.now()), 0, 1, TimeUnit.SECONDS);
    }

}
