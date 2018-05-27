package dev.util;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.Input;
import com.mackenziehigh.cascade.Output;
import com.mackenziehigh.cascade.PrivateInput;
import com.mackenziehigh.cascade.PrivateOutput;
import com.mackenziehigh.cascade.Reactor;
import com.mackenziehigh.cascade.builder.ReactorBuilder;

/**
 *
 * @author mackenzie
 */
public final class Splitter<E>
{
    private final PrivateInput<E> input;

    private final PrivateOutput<E> outputX;

    private final PrivateOutput<E> outputY;

    public final Reactor reactor;

    public Splitter (final Class<E> type)
    {
        final ReactorBuilder core = Cascade.newReactor();
        input = core.newArrayInput(type).withCapacity(8).build();
        outputX = core.newOutput(type).build();
        outputY = core.newOutput(type).build();
        core.newReaction()
                .require(input)
                .onMatch(this::run);
        reactor = core.build();
    }

    private void run ()
    {
        final E value = input.poll().get();
        outputX.send(value);
        outputY.send(value);
    }

    public Input<E> input ()
    {
        return input;
    }

    public Output<E> outputX ()
    {
        return outputX;
    }

    public Output<E> outputY ()
    {
        return outputY;
    }
}
