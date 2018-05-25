package dev.actors;

import com.mackenziehigh.cascade.reactor.Cascade;
import com.mackenziehigh.cascade.reactor.Input;
import com.mackenziehigh.cascade.reactor.MutableInput;
import com.mackenziehigh.cascade.reactor.Reaction;
import com.mackenziehigh.cascade.reactor.Reactor;
import com.mackenziehigh.cascade.reactor.builder.ReactorBuilder;

/**
 *
 */
public final class Printer
{
    private final ReactorBuilder core = Cascade.newReactor();

    private final MutableInput<Integer> input = core.newInput(Integer.class).build();

    private final Reaction action = core
            .newReaction()
            .require(input)
            .onMatch(() -> System.out.println("X = " + input.poll().get()))
            .build();

    public final Reactor reactor = core.build();

    public Input<Integer> input ()
    {
        return input;
    }
}
