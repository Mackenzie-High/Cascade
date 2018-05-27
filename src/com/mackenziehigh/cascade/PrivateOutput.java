package com.mackenziehigh.cascade;

/**
 *
 * @author mackenzie
 */
public interface PrivateOutput<T>
        extends Output<T>
{
    public Output<T> send (T value);
}
