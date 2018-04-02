package com.mackenziehigh.cascade;

import com.mackenziehigh.cascade.scripts.LambdaScript;
import com.mackenziehigh.cascade.util.actors.CommonActors;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * For Testing Only.
 */
public final class Main
{

    public static void main (String[] args)
    {
        final Cascade cas = Cascades.newCascade();

        final CascadeToken ns = CascadeToken.token("com.mackenzie.high");
        final CascadeToken ticks = ns.append("ticks");

        final CascadeStage r = cas.newStage();
        r.incrementThreadCount();

        final CascadeActor ra = r.newActor(CommonActors.CLOCK)
                .setPeriod(Duration.ofMillis(1000))
                .setDataOutput(ticks)
                .setDelay(Duration.ofSeconds(1))
                .useFixedDelay()
                .sendElapsed(TimeUnit.SECONDS)
                .build();

        final CascadeActor rb = r.newActor(CommonActors.STDOUT)
                .setInput(ticks)
                .build();

        final LambdaScript.Builder script = LambdaScript.newBuilder();
        script.bindOnMessage(ticks, (ctx, evt, msg) -> System.out.println("Y = " + msg.peekAsObject()));

    }

}
