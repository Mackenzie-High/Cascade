package com.mackenziehigh.cascade;

import com.mackenziehigh.cascade.Input;
import java.util.Optional;

/**
 *
 */
public interface MutableInput<T>
        extends Input<T>
{

    public MutableInput<T> clear ();

    public T pollOrDefault (T defaultValue);

    public default Optional<T> poll ()
    {
        final T head = pollOrDefault(null);
        return Optional.ofNullable(head);
    }

}
