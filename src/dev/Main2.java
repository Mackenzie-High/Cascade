package dev;

import com.mackenziehigh.cascade.reactor.MutableInput;
import com.mackenziehigh.cascade.reactor.Output;
import com.mackenziehigh.cascade.reactor.Reaction;
import com.mackenziehigh.cascade.reactor.Reactor;
import com.mackenziehigh.cascade.reactor.builder.ReactorBuilder;
import com.mackenziehigh.cascade.reactor.internal.InternalReactor;

/**
 *
 * @author mackenzie
 */
public final class Main2
{
    private final ReactorBuilder reactor = new InternalReactor();

    private final MutableInput<Integer> fin = reactor
            .newInput(Integer.class)
            .build();

    private final Output<Integer> fout = reactor
            .newOutput(Integer.class)
            .build();

    private final Reaction ract = reactor
            .newReaction()
            .require(fin)
            .require(fin, x -> x % 2 == 0)
            .onMatch(() -> System.out.println("X = " + fin.poll().get()))
            .build();

    private final Reactor core = reactor.build();

    public static void main (String[] args)
    {
        final Main2 actor = new Main2();
        actor.fin.send(100);
        actor.core.crank();
    }
}
