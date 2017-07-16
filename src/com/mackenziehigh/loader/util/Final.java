package com.mackenziehigh.loader.util;

import java.util.Objects;
import java.util.Optional;

/**
 * An instance of this class is a reference that can only be set once.
 */
public final class Final<T>
{
    private Optional<T> value = Optional.empty();

    public synchronized Final set (final T value)
    {
        if (this.value.isPresent() == false)
        {
            this.value = Optional.of(Objects.requireNonNull(value));
        }
        return this;
    }

    public Optional<T> get ()
    {
        return value;
    }
}
