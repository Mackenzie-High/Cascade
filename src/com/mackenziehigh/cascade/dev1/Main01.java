package com.mackenziehigh.cascade.dev1;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.Cascade.Stage;
import com.mackenziehigh.cascade.Cascade.Stage.Actor;

/**
 *
 */
public final class Main01
{
    public static void main (String[] args)
            throws InterruptedException
    {
        final Stage stage = Cascade.newStage(4);

        final Actor<String, String> erin = stage.newActor()
                .withScript((String x) -> x + " World ")
                .withLinkedInflowQueue()
                .create();

        final Actor<String, String> anna = stage.newActor()
                .withScript((String x) -> x + " Alien ")
                .withLinkedInflowQueue()
                .create();

        final Actor<String, String> emma = stage.newActor()
                .withScript((String x) -> System.out.println("X = " + x))
                .withLinkedInflowQueue()
                .create();

        erin.output().connect(anna.input());
        anna.output().connect(emma.input());

        erin.input().send(" Hello ");

        Thread.sleep(10_000);
    }
}
