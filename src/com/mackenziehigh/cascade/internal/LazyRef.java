package com.mackenziehigh.cascade.internal;

import java.util.Objects;
import java.util.function.Supplier;

/**
 *
 */
public final class LazyRef<E>
{
    private volatile E value = null;

    private final Supplier<E> supplier;

    private final Object lockObject = new Object();

    private LazyRef (final Supplier<E> supplier)
    {
        this.supplier = Objects.requireNonNull(supplier, "supplier");
    }

    // TODO: Only a subsection of this needs to be synchronized, actually.
    public synchronized E get ()
    {
        if (value == null)
        {
            value = Objects.requireNonNull(supplier.get(), "Lazy value is null!");
        }

        return value;
    }

    public static synchronized <T> LazyRef<T> create (final Supplier<T> supplier)
    {
        return new LazyRef<>(supplier);
    }
}
