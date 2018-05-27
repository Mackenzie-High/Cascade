package dev.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.Input;
import com.mackenziehigh.cascade.Output;
import com.mackenziehigh.cascade.PrivateInput;
import com.mackenziehigh.cascade.PrivateOutput;
import com.mackenziehigh.cascade.Reactor;
import com.mackenziehigh.cascade.builder.ReactorBuilder;
import java.util.List;

/**
 *
 */
public final class Bus<T>
{
    private final List<PrivateInput<T>> inputs;

    private final List<PrivateOutput<T>> outputs;

    public final Reactor reactor;

    public Bus (final Class<T> type,
                final int inputCount,
                final int outputCount)
    {
        final List<PrivateInput<T>> ins = Lists.newArrayList();
        final List<PrivateOutput<T>> outs = Lists.newArrayList();

        final ReactorBuilder core = Cascade.newReactor();

        for (int i = 0; i < inputCount; i++)
        {
            final int k = i;

            final PrivateInput<T> input = core
                    .newArrayInput(type)
                    .withCapacity(8)
                    .build();

            ins.add(input);

            core.newReaction().require(input).onMatch(() -> run(k)).build();
        }

        for (int i = 0; i < outputCount; i++)
        {
            final PrivateOutput<T> output = core.newOutput(type).build();
            outs.add(output);
        }

        reactor = core.build();

        this.inputs = ImmutableList.copyOf(ins);
        this.outputs = ImmutableList.copyOf(outs);
    }

    private void run (final int index)
    {
        final T value = inputs.get(index).pollOrDefault(null);

        final int len = outputs.size();
        for (int i = 0; i < len; i++)
        {
            outputs.get(i).send(value);
        }
    }

    public Input<T> input (final int index)
    {
        return inputs.get(index);
    }

    public Output<T> output (final int index)
    {
        return outputs.get(index);
    }
}
