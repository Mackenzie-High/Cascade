package com.mackenziehigh.cascade;

import com.mackenziehigh.cascade.CascadeStage;
import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.util.actors.CommonActors;
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
