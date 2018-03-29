package com.mackenziehigh.cascade;

import static com.mackenziehigh.cascade.CascadeToken.token;
import com.mackenziehigh.cascade.util.actors.CommonActors;
import java.time.Duration;
import com.mackenziehigh.cascade.util.CascadeActors;

/**
 * For Testing Only.
 */
public final class Main
{

    public static void main (String[] args)
    {
        final Cascade cas = Cascades.newCascade();

        final CascadeStage r = cas.newStage();
        r.incrementThreadCount();

        final CascadeActor ra = r.newActor(CommonActors.TICKER)
                .setPeriod(1000)
                .setOutput("ticks")
                .setDelay(Duration.ofDays(1))
                .useFixedDelay()
                .build();

        final CascadeActor rb = r.newActor(CommonActors.STDOUT)
                .setInput("ticks")
                .build();

        final CascadeActors ops = null;

        ops.of(token("A"), "A");
        ops.map(token("A"), token("B"), x -> x.pop());

    }

}
