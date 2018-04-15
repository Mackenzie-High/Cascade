package dev;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeContext;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeStage;
import com.mackenziehigh.cascade.CascadeToken;
import static com.mackenziehigh.cascade.CascadeToken.*;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class Main2
{
    private static final CascadeToken X = token("x");

    private static final CascadeToken Y = token("y");

    public static void main (String[] args)
            throws InterruptedException
    {
        final Cascade cas = Cascade.newCascade();

        final CascadeStage stage = cas.newStage(Executors.newScheduledThreadPool(5));

        final CascadeActor actor1 = stage.newActor();
        actor1.script().appendToOnMessage(X, Main2::action1);
        actor1.script().appendToOnMessage(X, Main2::action2);
        actor1.script().appendToOnException((ctx, ex) -> ex.printStackTrace(System.err));
        actor1.start();

        final CascadeActor actor2 = stage.newActor();
        actor2.script().appendToOnMessage(Y, Main2::action3);
        actor2.script().appendToOnException((ctx, ex) -> ex.printStackTrace(System.err));
        actor2.start();

        actor1.awaitStart(Duration.ofSeconds(1));

        cas.send(X, CascadeStack.newStack().pushInt(0));

        cas.awaitClose(Duration.ofSeconds(1000));
    }

    private static final AtomicInteger counter = new AtomicInteger();

    private static void action1 (final CascadeContext ctx,
                                 final CascadeToken evt,
                                 final CascadeStack msg)
    {
        if (counter.incrementAndGet() % 1 == 0)
        {
            System.out.println("X = " + counter);
        }
    }

    private static void action2 (final CascadeContext ctx,
                                 final CascadeToken evt,
                                 final CascadeStack msg)
    {
        ctx.send(Y, msg);
    }

    private static void action3 (final CascadeContext ctx,
                                 final CascadeToken evt,
                                 final CascadeStack msg)
    {
//        final int top = msg.peekAsInt();
//        msg.pop();
//        ctx.send(X, CascadeStack.newStack().pushInt(top + 1));
        ctx.send(X, msg);
    }
}
