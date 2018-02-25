package com.mackenziehigh.cascade.redo;

import java.util.Objects;
import java.util.Optional;

/**
 *
 */
public final class Param
{
    private final String name;

    private final Optional<Object> value;

    public Param (final String name,
                  final Object value)
    {
        this.name = Objects.requireNonNull(name, "name");
        this.value = Optional.ofNullable(value);
    }

}
