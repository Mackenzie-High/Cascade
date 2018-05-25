package com.mackenziehigh.cascade.reactor.builder;

import com.google.common.base.Preconditions;
import com.mackenziehigh.cascade.reactor.CheckedRunnable;
import com.mackenziehigh.cascade.reactor.Input;
import com.mackenziehigh.cascade.reactor.Output;
import com.mackenziehigh.cascade.reactor.Reaction;
import java.time.temporal.ChronoUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/**
 *
 */
public interface ReactionBuilder
{
    public ReactionBuilder named (String name);

    public ReactionBuilder require (BooleanSupplier condition);

    public default ReactionBuilder require (Input<?> input)
    {
        Preconditions.checkNotNull(input, "input");
        return require(() -> !input.isEmpty());
    }

    public default <T> ReactionBuilder require (Input<T> input,
                                                Predicate<T> head)
    {
        Preconditions.checkNotNull(input, "input");
        Preconditions.checkNotNull(head, "head");
        return require(() -> head.test(input.peekOrDefault(null)));
    }

    public default ReactionBuilder require (Output<?> output)
    {
        Preconditions.checkNotNull(output, "output");
        return require(() -> !output.isFull());
    }

    public default ReactionBuilder rateLimit (int permits,
                                              ChronoUnit unit)
    {
        // TODO
        return this;
    }

    public default ReactorBuilder limit (long count)
    {
        return null;
    }

    public ReactionBuilder onMatch (CheckedRunnable task);

    public ReactionBuilder orElse (CheckedRunnable task);

    public ReactionBuilder onError (CheckedRunnable handler);

    public Reaction build ();
}
