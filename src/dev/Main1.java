package dev;

import com.google.common.base.Verify;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeContext;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeStage;
import com.mackenziehigh.cascade.CascadeToken;
import static com.mackenziehigh.cascade.CascadeToken.*;
import java.time.Duration;
import java.util.concurrent.Executors;

/**
 *
 */
public class Main1
{
    public static void main (String[] args)
            throws InterruptedException
    {
        final Cascade cas = Cascade.newCascade();

        final CascadeStage stage = cas.newStage(Executors.newFixedThreadPool(1));

        final CascadeActor actor1 = stage.newActor();
        actor1.script().appendToOnSetup((ctx) -> System.out.println("Z = " + cas.channelOf(token("out"))));
        actor1.script().appendToOnMessage(token("out"), Main1::action1);
        actor1.start();

        actor1.awaitStart(Duration.ofSeconds(1));

        Verify.verify(cas.channelOf(token("out")).isPresent());
        cas.send(token("out"), CascadeStack.newStack().pushObject("Mercury"));
        cas.send(token("out"), CascadeStack.newStack().pushObject("Venus"));
        cas.send(token("out"), CascadeStack.newStack().pushObject("Earth"));
        cas.send(token("out"), CascadeStack.newStack().pushObject("Mars"));
        cas.send(token("out"), CascadeStack.newStack().pushObject("Jupiter"));

        System.out.println("S = " + actor1.subscriptions());

        cas.awaitClose(Duration.ofSeconds(1000));
    }

    private static void action1 (final CascadeContext ctx,
                                 final CascadeToken evt,
                                 final CascadeStack msg)
    {
        System.out.println("X = " + msg.peekAsString());
    }
}
