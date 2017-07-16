package com.mackenziehigh.loader;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Optional;

/**
 * Configuration Object.
 */
public interface ConfigObject
{
    /**
     * This method determines the type of value contained herein.
     *
     * @return true, if this object stores a class-reference.
     */
    public boolean isClass ();

    /**
     * This method determines the type of value contained herein.
     *
     * @return true, if this object stores a boolean.
     */
    public boolean isBoolean ();

    /**
     * This method determines the type of value contained herein.
     *
     * @return true, if this object stores an integer.
     */
    public boolean isInteger ();

    /**
     * This method determines the type of value contained herein.
     *
     * @return true, if this object stores a float.
     */
    public boolean isFloat ();

    /**
     * This method determines the type of value contained herein.
     *
     * @return true, if this object stores a string.
     */
    public boolean isString ();

    /**
     * This method determines the type of value contained herein.
     *
     * @return true, if this object stores a list.
     */
    public boolean isList ();

    /**
     * This method determines the type of value contained herein.
     *
     * @return true, if this object stores a map.
     */
    public boolean isMap ();

    /**
     * This method determines whether the value is present.
     *
     * @return true, if the value is present.
     */
    public boolean isPresent ();

    /**
     * This method retrieves the class identified by this value.
     *
     * @return the class, if it is found.
     */
    public Optional<Class> asClass ();

    /**
     * This method retrieves this value, as a boolean.
     *
     * @return the value, if possible.
     */
    public Optional<Boolean> asBoolean ();

    /**
     * This method retrieves this value, as an integer.
     *
     * @return the value, if possible.
     */
    public Optional<Long> asInteger ();

    /**
     * This method retrieves this value, as a float.
     *
     * @return the value, if possible.
     */
    public Optional<Double> asFloat ();

    /**
     * This method retrieves this value, as a string.
     *
     * @return the value, if possible.
     */
    public Optional<String> asString ();

    /**
     * This method retrieves this value, as a list.
     *
     * @return the value, if possible.
     */
    public Optional<ImmutableList<ConfigObject>> asList ();

    /**
     * This method retrieves this value, as a map.
     *
     * @return the value, if possible.
     */
    public Optional<ImmutableSortedMap<String, ConfigObject>> asMap ();
}
