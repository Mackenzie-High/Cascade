package com.mackenziehigh.cascade.reactor.internal;

import com.mackenziehigh.cascade.reactor.Cascade;
import com.mackenziehigh.cascade.reactor.MutableInput;
import com.mackenziehigh.cascade.reactor.Output;
import com.mackenziehigh.cascade.reactor.builder.ReactionBuilder;
import com.mackenziehigh.cascade.reactor.builder.ReactorBuilder;
import java.time.temporal.ChronoUnit;

/**
 *
 * @author mackenzie
 */
public final class Main
{
    private final ReactorBuilder reactor = Cascade
            .newReactor()
            .named("Anna");

    private final MutableInput<String> input1 = reactor
            .newInput(String.class)
            .withArrayQueue(128)
            .withOverflowPolicyDropOldest()
            .filter(x -> x.contains("XX"))
            .transform(x -> "XX = " + x)
            .build();

    private final Output<String> output1 = reactor
            .newOutput(String.class)
            .verify(x -> x.matches(""))
            .build();

    private final ReactionBuilder action1 = reactor
            .newReaction()
            .require(input1)
            .require(input1, x -> x.contains("Autumn"))
            .require(() -> System.currentTimeMillis() % 2 == 0)
            .rateLimit(1, ChronoUnit.SECONDS)
            .onMatch(() -> System.out.println("X = " + input1.poll().get()))
            .onMatch(this::action1)
            .orElse(() -> System.out.println("Y"));

    public Main ()
    {
        reactor.build().start();
    }

    private void action1 ()
    {
        // Pass
    }

    public static void main (String[] args)
    {

    }
}
