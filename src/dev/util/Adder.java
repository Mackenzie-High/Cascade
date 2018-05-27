package dev.util;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.Input;
import com.mackenziehigh.cascade.MutableInput;
import com.mackenziehigh.cascade.Output;
import com.mackenziehigh.cascade.Reaction;
import com.mackenziehigh.cascade.Reactor;
import com.mackenziehigh.cascade.builder.ReactorBuilder;

/**
 *
 * @author mackenzie
 */
public final class Adder
{
    private final ReactorBuilder core = Cascade.newReactor();

    private final MutableInput<Long> left = core
            .newArrayInput(Long.class)
            .withCapacity(10)
            .build();

    private final MutableInput<Long> right = core
            .newArrayInput(Long.class)
            .withCapacity(10)
            .transform(x -> 1L)
            .build();

    private final Output<Long> result = core.newOutput(Long.class).build();

    private final Reaction adder = core
            .newReaction()
            .require(left)
            .require(right)
            .onMatch(() -> result.send(left.poll().get() + right.poll().get()))
            .build();

    public final Reactor reactor = core.build();

    public Input<Long> left ()
    {
        return left;
    }

    public Input<Long> right ()
    {
        return right;
    }

    public Output<Long> output ()
    {
        return result;
    }
}
