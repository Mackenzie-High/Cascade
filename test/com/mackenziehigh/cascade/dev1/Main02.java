package com.mackenziehigh.cascade.dev1;

import com.mackenziehigh.cascade.Cascade;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
public final class Main02
{
    public static void main (String[] args)
            throws InterruptedException
    {
        final Cascade.Stage stage = Cascade.newStage(5);

        final Cascade.Stage.Actor<AtomicLong, AtomicLong> counter = stage.newActor()
                .withScript(Main02::counter)
                .withLinkedInflowQueue()
                .create();

        counter.input().connect(counter.output());

        counter.input().send(new AtomicLong());

        Thread.sleep(10_000);
    }

    private static AtomicLong counter (final AtomicLong count)
    {
        if (count.get() % 100_000L == 0)
        {
            System.out.println("Count = " + count.get());
        }

        count.incrementAndGet();

        return count;
    }
}
