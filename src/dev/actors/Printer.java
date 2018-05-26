package dev.actors;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.Input;
import com.mackenziehigh.cascade.MutableInput;
import com.mackenziehigh.cascade.Reaction;
import com.mackenziehigh.cascade.Reactor;
import com.mackenziehigh.cascade.builder.ReactorBuilder;

/**
 *
 */
public final class Printer
{
    private final ReactorBuilder core = Cascade.newReactor();

    private final MutableInput<Integer> input = core.newArrayInput(Integer.class).build();

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
