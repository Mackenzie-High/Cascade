package com.mackenziehigh.cascade.internal;

/**
 * A Consumer-like functional-interface that can throw exceptions.
 *
 * @param <T>
 */
@FunctionalInterface
public interface UnsafeConsumer<T>
{
    public void accept (T value)
            throws Throwable;
}