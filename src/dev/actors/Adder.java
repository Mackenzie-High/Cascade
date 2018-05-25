package dev.actors;

import com.mackenziehigh.cascade.reactor.Cascade;
import com.mackenziehigh.cascade.reactor.Input;
import com.mackenziehigh.cascade.reactor.MutableInput;
import com.mackenziehigh.cascade.reactor.Output;
import com.mackenziehigh.cascade.reactor.Reaction;
import com.mackenziehigh.cascade.reactor.Reactor;
import com.mackenziehigh.cascade.reactor.builder.ReactorBuilder;

/**
 *
 * @author mackenzie
 */
public final class Adder
{
    private final ReactorBuilder core = Cascade.newReactor();

    private final MutableInput<Integer> left = core.newInput(Integer.class).build();

    private final MutableInput<Integer> right = core.newInput(Integer.class).build();

    private final Output<Integer> result = core.newOutput(Integer.class).build();

    private final Reaction adder = core
            .newReaction()
            .require(left)
            .require(right)
            .onMatch(() -> result.send(left.poll().get() + right.poll().get()))
            .build();

    public final Reactor reactor = core.build();

    public Input<Integer> left ()
    {
        return left;
    }

    public Input<Integer> right ()
    {
        return right;
    }

    public Output<Integer> result ()
    {
        return result;
    }
}
