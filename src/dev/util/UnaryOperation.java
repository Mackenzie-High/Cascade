package dev.util;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.Input;
import com.mackenziehigh.cascade.Output;
import com.mackenziehigh.cascade.PrivateInput;
import com.mackenziehigh.cascade.PrivateOutput;
import com.mackenziehigh.cascade.Reactor;
import com.mackenziehigh.cascade.builder.ReactorBuilder;
import dev.util.functions.Operator1;

/**
 * TODO: Add builder and exception handling.
 * TODO: require output capacity (for pipelines)?
 */
public final class UnaryOperation<E>
{
    private final PrivateInput<E> input;

    private final PrivateOutput<E> output;

    public final Reactor reactor;

    private final Operator1<E> operation;

    private UnaryOperation (final Class<E> type,
                            final Operator1 task)
    {
        this.operation = task;
        final ReactorBuilder core = Cascade.newReactor();
        input = core.newArrayInput(type).withCapacity(8).build();
        output = core.newOutput(type).build();
        core.newReaction()
                .require(input)
                .onMatch(this::run)
                .build();
        reactor = core.build();
    }

    private void run ()
            throws Throwable
    {
        final E result = operation.call(input.poll().get());
        output.send(result);
    }

    public Input<E> input ()
    {
        return input;
    }

    public Output<E> output ()
    {
        return output;
    }

    public static <T> UnaryOperation<T> create (final Class<T> type,
                                                final Operator1<T> operation)
    {
        return new UnaryOperation<>(type, operation);
    }

    public static UnaryOperation<Integer> negateInt ()
    {
        return create(Integer.class, x -> -x);
    }

    public static UnaryOperation<Long> negateLong ()
    {
        return create(Long.class, x -> -x);
    }
}
