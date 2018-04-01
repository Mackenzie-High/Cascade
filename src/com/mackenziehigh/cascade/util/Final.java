package com.mackenziehigh.cascade.util;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A facade around a value that can only be assigned-to once.
 *
 * <p>
 * This class is designed to mimic the final keyword.
 * A final can only be set in a constructor; however,
 * a Final can be set anywhere, once.
 * </p>
 *
 * <p>
 * A Final cannot store a null value.
 * </p>
 *
 * @param <T> describes the value stored herein.
 */
public final class Final<T>
{
    private volatile Supplier<? extends T> lazySource = null;

    private volatile Optional<T> element;

    private volatile boolean locked = false;

    /**
     * No public constructors.
     */
    private Final ()
    {
        // Pass
    }

    /**
     * Constructs a non-assigned Final.
     *
     * @param <U> describes the value stored in the result.
     * @return the new Final that does not contain a value yet.
     */
    public static <U> Final<U> empty ()
    {
        final Final<U> result = new Final<>();
        result.lazySource = null;
        result.element = Optional.empty();
        return result;
    }

    /**
     * Constructs a Final with the given value assigned thereto.
     *
     * @param <U> describes the value stored in the result.
     * @param value will be stored in the new Final.
     * @return the new Final that contains the given value.
     */
    public static <U> Final<U> of (final U value)
    {
        Objects.requireNonNull(value, "value");
        final Final<U> result = new Final<>();
        result.lazySource = null;
        result.element = Optional.of(value);
        return result;
    }

    /**
     * Constructs a Final that will either be assigned-to explicitly
     * via the set() method or implicitly via the given supplier.
     *
     * <p>
     * If get() is called for the first time, and no value is stored herein,
     * then the given supplier will be invoked in order to obtain the value
     * and then the value will be assigned to this object using set().
     * </p>
     *
     * @param <U> describes the value stored in the result.
     * @param supplier may supply the value to store herein.
     * @return the new Final that does not contain a value yet.
     */
    public static <U> Final<U> lazyOf (final Supplier<? extends U> supplier)
    {
        Objects.requireNonNull(supplier, "supplier");
        final Final<U> result = new Final<>();
        result.lazySource = supplier;
        result.element = Optional.empty();
        return result;
    }

    /**
     * Assigns a value to this Final, when no value was already assigned,
     * or throws an exception when already assigned.
     *
     * @param value will be stored herein henceforth.
     * @return this.
     * @throws IllegalStateException if this Final was already set.
     * @throws NullPointerException if the value is null.
     */
    public synchronized Final<T> set (final T value)
    {
        final boolean notPresent = !element.isPresent();
        final boolean notLocked = !locked;

        if (value == null)
        {
            throw new NullPointerException("value");
        }
        else if (notLocked && notPresent)
        {
            locked = true;
            element = Optional.of(value);
            return this;
        }
        else
        {
            throw new IllegalStateException("Already Set");
        }
    }

    public Final<T> lock ()
    {
        locked = true;
        return this;
    }

    /**
     * Assigns a value to this Final, when no value was already assigned,
     * or is a no-op when already assigned.
     *
     * @param value will be stored herein henceforth.
     * @return this.
     * @throws NullPointerException if the value is null.
     */
    public synchronized Final<T> setIfNotSet (final T value)
    {
        return isNotSet() ? set(value) : this;
    }

    /**
     * Retrieve the value stored herein, if one is present.
     *
     * <p>
     * If no value is present herein, but a lazy supplier was specified,
     * then this method will invoke the supplier and assign the result hereto.
     * </p>
     *
     * @return the value herein, if present; otherwise, return empty.
     */
    public synchronized Optional<T> get ()
    {
        if (element.isPresent())
        {
            return element;
        }
        else if (lazySource != null)
        {
            set(lazySource.get());
            return element;
        }
        else
        {
            return Optional.empty();
        }
    }

    /**
     * Determines whether this Final has a lazy supplier.
     *
     * @return true, if this Final can-be/was initialized using a supplier.
     */
    public boolean isLazy ()
    {
        return lazySource != null;
    }

    /**
     * Determines whether a value was already assigned hereto.
     *
     * @return true, if a value was already assigned.
     */
    public boolean isSet ()
    {
        return element.isPresent() || locked;
    }

    /**
     * Determines whether a value was already assigned hereto.
     *
     * @return false, if a value was already assigned.
     */
    public boolean isNotSet ()
    {
        return !isSet();
    }

    /**
     * Return a string representation of this Final.
     *
     * <p>
     * If no value is assigned hereto, then "Not Set Yet" will be returned.
     * Otherwise, the string representation of the value itself will be returned.
     * </p>
     *
     * @return a string representation of this object.
     */
    @Override
    public String toString ()
    {
        return element.map(x -> x.toString()).orElse("Not Set Yet");
    }

}
