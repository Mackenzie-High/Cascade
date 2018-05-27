package com.mackenziehigh.cascade.builder;

import com.mackenziehigh.cascade.internal.OverflowPolicy;
import java.time.temporal.ChronoUnit;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import com.mackenziehigh.cascade.PrivateInput;

/**
 *
 */
public interface ArrayInputBuilder<E>
        extends InputBuilder<E>,
                BoundedInputBuilder<E>
{
    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayInputBuilder<E> withOverflowPolicy (OverflowPolicy policy);

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayInputBuilder<E> withCapacity (int capacity);

    /**
     * {@inheritDoc}
     */
    @Override
    public PrivateInput<E> build ();

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayInputBuilder<E> limit (long count);

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayInputBuilder<E> rateLimit (int permits,
                                           ChronoUnit unit);

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayInputBuilder<E> verify (Predicate<E> condition);

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayInputBuilder<E> transform (UnaryOperator<E> transform);

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayInputBuilder<E> filter (Predicate<E> filter);

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayInputBuilder<E> named (String name);

}
