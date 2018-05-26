package com.mackenziehigh.cascade.builder;

import com.mackenziehigh.cascade.internal.OverflowPolicy;

/**
 *
 *
 * @param <E>
 * @param <T>
 */
public interface BoundedInputBuilder<E>
{
    public BoundedInputBuilder<E> withCapacity (int capacity);

    public BoundedInputBuilder<E> withOverflowPolicy (OverflowPolicy policy);
}
