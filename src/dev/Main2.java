package dev;

import com.mackenziehigh.cascade.Output;
import com.mackenziehigh.cascade.Reaction;
import com.mackenziehigh.cascade.Reactor;
import com.mackenziehigh.cascade.builder.ReactorBuilder;
import com.mackenziehigh.internal.cascade.InternalReactor;
import com.mackenziehigh.cascade.PrivateInput;

/**
 *
 * @author mackenzie
 */
public final class Main2
{
    private final ReactorBuilder reactor = new InternalReactor();

    private final PrivateInput<Integer> fin = reactor
            .newArrayInput(Integer.class)
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
