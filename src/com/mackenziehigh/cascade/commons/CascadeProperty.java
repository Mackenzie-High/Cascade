package com.mackenziehigh.cascade.commons;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 *
 */
public final class CascadeProperty<E>
{
    private final Builder<E> builder;

    private CascadeProperty (final Builder<E> builder)
    {
        this.builder = builder;
    }

    public synchronized CascadeProperty<E> set (final E value)
    {
        if (builder.readonly && builder.assigned)
        {
            throw new IllegalStateException("Illegal Assignment to Final Property: " + builder.name);
        }
        else if (builder.nullable == false && value == null)
        {
            throw new NullPointerException("Assigning Null to Non-Nullable Property: " + builder.name);
        }
        else if (builder.requirements.stream().allMatch(x -> x.test(value)) == false)
        {
            throw new IllegalArgumentException("Invalid Value for Property: " + builder.name);
        }
        else
        {
            builder.assigned = true;
            builder.value = value;
        }
        return this;
    }

    public E get ()
    {
        if (builder.assigned)
        {
            return builder.value;
        }
        else
        {
            throw new IllegalStateException("No Value Available for Property: " + builder.name);
        }
    }

    public E getOrDefault (final E defaultValue)
    {
        if (builder.assigned)
        {
            return builder.value;
        }
        else
        {
            return defaultValue;
        }
    }

    public boolean isSet ()
    {
        return builder.assigned;
    }

    public boolean isFinal ()
    {
        return builder.readonly;
    }

    public boolean isNullable ()
    {
        return builder.nullable;
    }

    public String name ()
    {
        return builder.name;
    }

    @Override
    public String toString ()
    {
        return name() + " = " + getOrDefault(null);
    }

    /**
     * Getter.
     *
     * @param <T>
     * @param name will be the name of the new property.
     * @return a new builder that can create properties.
     */
    public static <T> Builder<T> newBuilder (final String name)
    {
        return new Builder<>(name);
    }

    /**
     * Builder.
     *
     * @param <T> is the type of the value stored in the property.
     */
    public static final class Builder<T>
    {
        private volatile boolean built = false;

        private final String name;

        private volatile T value = null;

        private volatile boolean assigned = false;

        private volatile boolean readonly = false;

        private volatile boolean nullable = false;

        private final List<Predicate<T>> requirements = Collections.synchronizedList(Lists.newArrayList());

        private Builder (final String name)
        {
            this.name = Objects.requireNonNull(name, "name");
        }

        public synchronized Builder<T> setValue (final T initial)
        {
            Preconditions.checkState(built == false, "Already Built!");
            assigned = true;
            value = initial;
            return this;
        }

        public synchronized Builder<T> makeFinal ()
        {
            Preconditions.checkState(built == false, "Already Built!");
            readonly = true;
            return this;
        }

        public synchronized Builder<T> makeNullable ()
        {
            Preconditions.checkState(built == false, "Already Built!");
            nullable = true;
            return this;
        }

        public synchronized Builder<T> require (final Predicate<T> condition)
        {
            Preconditions.checkNotNull(condition, "condition");
            Preconditions.checkState(built == false, "Already Built!");
            requirements.add(condition);
            return this;
        }

        public synchronized CascadeProperty<T> build ()
        {
            Preconditions.checkState(built == false, "Already Built!");
            return new CascadeProperty<>(this);
        }
    };
}
