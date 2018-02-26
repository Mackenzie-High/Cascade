package com.mackenziehigh.cascade.redo2;

import com.mackenziehigh.cascade.redo2.util.actors.CommonActors;
import java.time.Duration;

/**
 * For Testing Only.
 */
public final class Main
{

    public static void main (String[] args)
    {
        final Cascade cas = Cascade.create();

        final CascadeStage r = cas.newStage();
        final CascadeActor ra = r.newActor(CommonActors.TICKER)
                .setPeriod(1000)
                .setOutput("ticks")
                .setDelay(Duration.ofDays(1))
                .useFixedDelay()
                .build();

        System.out.println(r.actors());

    }

}
