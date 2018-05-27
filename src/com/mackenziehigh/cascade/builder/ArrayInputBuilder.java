package com.mackenziehigh.cascade.builder;

import com.mackenziehigh.cascade.internal.OverflowPolicy;
import java.util.function.Predicate;
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
    public ArrayInputBuilder<E> verify (Predicate<E> condition);

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayInputBuilder<E> named (String name);

}
