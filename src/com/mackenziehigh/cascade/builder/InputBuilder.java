package com.mackenziehigh.cascade.builder;

import java.time.temporal.ChronoUnit;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import com.mackenziehigh.cascade.PrivateInput;

/**
 *
 */
public interface InputBuilder<E>

{

    public InputBuilder<E> named (String name);

    public InputBuilder<E> filter (Predicate<E> filter);

    public InputBuilder<E> transform (UnaryOperator<E> transform);

    public InputBuilder<E> verify (Predicate<E> condition);

    public InputBuilder<E> rateLimit (final int permits,
                                      final ChronoUnit unit);

    public InputBuilder<E> limit (final long count);

    public PrivateInput<E> build ();
}
