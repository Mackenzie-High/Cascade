package com.mackenziehigh.cascade.reactor;

import java.util.Optional;

/**
 *
 */
public interface MutableInput<T>
        extends Input<T>
{

    public MutableInput<T> clear ();

    public T pollOrDefault (T defaultValue);

    public Optional<T> poll ();

}
