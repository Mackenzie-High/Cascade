package dev.util;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.Input;
import com.mackenziehigh.cascade.Reaction;
import com.mackenziehigh.cascade.Reactor;
import com.mackenziehigh.cascade.builder.ReactorBuilder;
import com.mackenziehigh.cascade.PrivateInput;

/**
 *
 */
public final class Printer
{
    private final ReactorBuilder core = Cascade.newReactor();

    private final PrivateInput<Long> input = core
            .newArrayInput(Long.class)
            .withCapacity(10)
            .build();

    private final Reaction action = core
            .newReaction()
            .require(input)
            .onMatch(() -> System.out.println("X = " + input.poll().get()))
            .build();

    public final Reactor reactor = core.build();

    public Input<Long> input ()
    {
        return input;
    }
}
