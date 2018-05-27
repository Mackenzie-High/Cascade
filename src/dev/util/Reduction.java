package dev.util;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.Input;
import com.mackenziehigh.cascade.Output;
import com.mackenziehigh.cascade.PrivateInput;
import com.mackenziehigh.cascade.PrivateOutput;
import com.mackenziehigh.cascade.Reactor;
import com.mackenziehigh.cascade.builder.ReactorBuilder;
import dev.util.functions.Function2;

/**
 * Functional reduce().
 * TODO: How to reset?
 */
public final class Reduction<E>
{
    private final PrivateInput<E> input;

    private final PrivateOutput<E> output;

    public final Reactor reactor;

    private volatile E lastResult;

    private final Function2<E, E, E> operation;

    private Reduction (final Class<E> type,
                       final E initial,
                       final Function2<E, E, E> task)
    {
        this.lastResult = initial;
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
        final E left = lastResult;
        final E right = input.pollOrDefault(null);
        lastResult = operation.call(left, right);
        output.send(lastResult);
    }

    public Input<E> input ()
    {
        return input;
    }

    public Output<E> output ()
    {
        return output;
    }

    public static <T> Reduction<T> create (final Class<T> type,
                                           final T initialValue,
                                           final Function2<T, T, T> operation)
    {
        return new Reduction<>(type, initialValue, operation);
    }

    public static Reduction<Long> summationLong ()
    {
        return new Reduction<>(Long.class, 0L, (x, y) -> x + y);
    }
}
