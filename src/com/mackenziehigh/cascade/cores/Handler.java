package com.mackenziehigh.cascade.cores;

/**
 * A Consumer-like functional-interface that can throw all types of exceptions.
 *
 * @param <T>
 */
@FunctionalInterface
public interface Handler<T>
{
    public void accept (T value)
            throws Throwable;
}
