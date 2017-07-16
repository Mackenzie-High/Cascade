package com.mackenziehigh.loader.internal.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.mackenziehigh.loader.ConfigObject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Configuration Object Factory.
 */
public final class ConfigObjectFactory
{

    private static final class Config
            implements ConfigObject
    {
        public static final Config NO_SUCH_CLASS = new Config(new Object());

        private final Object value;

        public Config (final Object value)
        {
            this.value = value;
        }

        @Override
        public Optional<Boolean> asBoolean ()
        {
            return isBoolean() ? Optional.of((Boolean) value) : Optional.empty();
        }

        @Override
        public Optional<Class> asClass ()
        {
            return isClass() && this != NO_SUCH_CLASS ? Optional.of((Class) value) : Optional.empty();
        }

        @Override
        public Optional<Double> asFloat ()
        {
            return isFloat() ? Optional.of((Double) value) : Optional.empty();
        }

        @Override
        public Optional<Long> asInteger ()
        {
            return isInteger() ? Optional.of((Long) value) : Optional.empty();
        }

        @Override
        public Optional<ImmutableList<ConfigObject>> asList ()
        {
            return isList() ? Optional.of((ImmutableList) value) : Optional.empty();
        }

        @Override
        public Optional<ImmutableSortedMap<String, ConfigObject>> asMap ()
        {
            return isMap() ? Optional.of((ImmutableSortedMap) value) : Optional.empty();
        }

        @Override
        public Optional<String> asString ()
        {
            return isString() ? Optional.of((String) value) : Optional.empty();
        }

        @Override
        public boolean isBoolean ()
        {
            return value instanceof Boolean;
        }

        @Override
        public boolean isClass ()
        {
            return value instanceof Class || this == NO_SUCH_CLASS;
        }

        @Override
        public boolean isFloat ()
        {
            return value instanceof Double;
        }

        @Override
        public boolean isInteger ()
        {
            return value instanceof Long;
        }

        @Override
        public boolean isList ()
        {
            return value instanceof List;
        }

        @Override
        public boolean isMap ()
        {
            return value instanceof Map;
        }

        @Override
        public boolean isPresent ()
        {
            final boolean present = asBoolean().isPresent()
                                    || asClass().isPresent()
                                    || asFloat().isPresent()
                                    || asInteger().isPresent()
                                    || asList().isPresent()
                                    || asMap().isPresent()
                                    || asString().isPresent();

            return present;
        }

        @Override
        public boolean isString ()
        {
            return value instanceof String;
        }

        @Override
        public String toString ()
        {
            return value.toString();
        }
    }

    public ConfigObject fromBoolean (final boolean value)
    {
        return new Config(value);
    }

    public ConfigObject fromInteger (final long value)
    {
        return new Config(value);
    }

    public ConfigObject fromFloat (final double value)
    {
        return new Config(value);
    }

    public ConfigObject fromString (final String value)
    {
        Objects.requireNonNull(value);
        return new Config(value);
    }

    public ConfigObject fromClass (final String name)
    {
        Objects.requireNonNull(name);

        try
        {
            final Class klass = Class.forName(name);
            return new Config(klass);
        }
        catch (ClassNotFoundException ex)
        {
            return Config.NO_SUCH_CLASS;
        }
    }

    public ConfigObject fromList (final List<ConfigObject> value)
    {
        Objects.requireNonNull(value);
        return new Config(ImmutableList.copyOf(value));
    }

    public ConfigObject fromMap (final Map<String, ConfigObject> value)
    {
        Objects.requireNonNull(value);
        return new Config(ImmutableSortedMap.orderedBy(String.CASE_INSENSITIVE_ORDER).putAll(value).build());
    }
}
