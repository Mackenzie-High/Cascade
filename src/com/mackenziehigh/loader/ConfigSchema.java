package com.mackenziehigh.loader;

import com.mackenziehigh.loader.exceptions.InvalidConfigurationException;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Pattern Matching on ConfigObject.
 *
 * <p>
 * By default, all values are 'required'.
 * </p>
 *
 * <p>
 * A map-entry is 'required', if its value-part is 'required'.
 * A map-entry is 'optional', if its value-part is 'optional'.
 * </p>
 */
public final class ConfigSchema
{
    private Class type = null;

    private boolean required = true;

    private ConfigSchema elementSchema;

    private final Map<String, ConfigSchema> entrySchemas = new TreeMap<>();

    private final List<Predicate<ConfigObject>> predicates = new ArrayList<>();

    private final List<Consumer<ConfigObject>> onPresent = new ArrayList<>();

    /**
     * This method asserts that this schema will apply to a boolean value.
     *
     * @return this.
     * @throws IllegalStateException if the type has already been asserted.
     */
    public ConfigSchema requireBoolean ()
    {
        Preconditions.checkState(type == null);
        type = boolean.class;
        return this;
    }

    /**
     * This method asserts that this schema will apply to an integer value.
     *
     * @return this.
     * @throws IllegalStateException if the type has already been asserted.
     */
    public ConfigSchema requireInteger ()
    {
        Preconditions.checkState(type == null);
        type = int.class;
        return this;
    }

    /**
     * This method asserts that this schema will apply to a float value.
     *
     * @return this.
     * @throws IllegalStateException if the type has already been asserted.
     */
    public ConfigSchema requireFloat ()
    {
        Preconditions.checkState(type == null);
        type = float.class;
        return this;
    }

    /**
     * This method asserts that this schema will apply to a string value.
     *
     * @return this.
     * @throws IllegalStateException if the type has already been asserted.
     */
    public ConfigSchema requireString ()
    {
        Preconditions.checkState(type == null);
        type = String.class;
        return this;
    }

    /**
     * This method asserts that this schema will apply to a list value.
     *
     * @return this.
     * @throws IllegalStateException if the type has already been asserted.
     */
    public ConfigSchema requireList ()
    {
        Preconditions.checkState(type == null);
        type = List.class;
        elementSchema = new ConfigSchema();
        return this;
    }

    /**
     * This method asserts that this schema will apply to a map value.
     *
     * @return this.
     * @throws IllegalStateException if the type has already been asserted.
     */
    public ConfigSchema requireMap ()
    {
        Preconditions.checkState(type == null);
        type = Map.class;
        return this;
    }

    /**
     * This method asserts that this schema will apply to a class value.
     *
     * @return this.
     * @throws IllegalStateException if the type has already been asserted.
     */
    public ConfigSchema requireClass ()
    {
        Preconditions.checkState(type == null);
        type = Class.class;
        return this;
    }

    /**
     * If this schema will be applied to a map value,
     * then this method retrieves the schema that
     * will be applied to a given entry in the map.
     *
     * @param key identifies the map entry.
     * @return the schema to apply to the map entry.
     */
    public ConfigSchema entry (final String key)
    {
        Preconditions.checkState(type == Map.class);

        if (entrySchemas.containsKey(key) == false)
        {
            entrySchemas.put(key, new ConfigSchema());
        }
        return entrySchemas.get(key);
    }

    /**
     * If this schema will be applied to a list value,
     * then this method retrieves the schema that will
     * be applied to each element in the list.
     *
     * @return the schema of the list elements.
     */
    public ConfigSchema each ()
    {
        Preconditions.checkState(type == List.class);
        return elementSchema;
    }

    /**
     * This method asserts that the value is required.
     *
     * @return this.
     */
    public ConfigSchema required ()
    {
        required = true;
        return this;
    }

    /**
     * This method asserts that the value is optional.
     *
     * @return this.
     */
    public ConfigSchema optional ()
    {
        required = false;
        return this;
    }

    /**
     * This method asserts that a given predicate must hold true,
     * if the value is present.
     *
     * <p>
     * Note: The predicate will *not* be applied,
     * if no value is present.
     * </p>
     *
     * @param predicate is the requirement.
     * @return this.
     */
    public ConfigSchema require (final Predicate<ConfigObject> predicate)
    {
        Preconditions.checkNotNull(predicate);
        predicates.add(predicate);
        return this;
    }

    /**
     * This methods asserts that the value is a string
     * that must match the given regular expression.
     *
     * @param pattern is the given regular expression.
     * @return this.
     */
    public ConfigSchema requireMatch (final String pattern)
    {
        Objects.requireNonNull(pattern);
        Preconditions.checkState(type == String.class);
        predicates.add(x -> x.asString().get().matches(pattern));
        return this;
    }

    /**
     * This method binds an action to perform,
     * when the value is present.
     *
     * @param action is the action to perform.
     * @return this.
     */
    public ConfigSchema onPresent (final Consumer<ConfigObject> action)
    {
        Preconditions.checkNotNull(action);
        onPresent.add(action);
        return this;
    }

    /**
     * This method applies this schema to the given object.
     *
     * @param config is the object that must obey this schema.
     */
    public void apply (final ConfigObject config)
    {
        final List<Runnable> actions = new LinkedList<>();
        apply(config, actions);
        actions.forEach(action -> action.run());
    }

    /**
     * This method applies this schema to the given object.
     *
     * @param config is the object that must obey this schema.
     */
    private void apply (final ConfigObject config,
                        final List<Runnable> actions)
    {
        Preconditions.checkState(type != null, "You must specify a data-type.");

        /**
         * The value must be the correct data-type.
         */
        if (type == boolean.class && !config.isBoolean())
        {
            throw new InvalidConfigurationException("Wrong Type: expected = boolean");
        }
        else if (type == int.class && !config.isInteger())
        {
            throw new InvalidConfigurationException("Wrong Type: expected = integer");
        }
        else if (type == float.class && !config.isFloat())
        {
            throw new InvalidConfigurationException("Wrong Type: expected = float");
        }
        else if (type == String.class && !config.isString())
        {
            throw new InvalidConfigurationException("Wrong Type: expected = string");
        }
        else if (type == List.class && !config.isList())
        {
            throw new InvalidConfigurationException("Wrong Type: expected = list");
        }
        else if (type == Map.class && !config.isMap())
        {
            throw new InvalidConfigurationException("Wrong Type: expected = map");
        }
        else if (type == Class.class && !config.isClass())
        {
            throw new InvalidConfigurationException("Wrong Type: expected = class");
        }

        final boolean present = config.isPresent();

        /**
         * If this value is required, then it must be present.
         */
        if (required && !present)
        {
            throw new InvalidConfigurationException("A required value is missing or unobtainable.");
        }

        /**
         * The following tests only apply when a value is present,
         * so go ahead and return now, if no value is present.
         */
        if (present == false)
        {
            return;
        }

        /**
         * All of the predicates must hold true.
         */
        if (predicates.stream().anyMatch(p -> !p.test(config)))
        {
            throw new InvalidConfigurationException("A predicate failed.");
        }

        /**
         * If the value is a list, then the element-schema must
         * hold true for each of the elements in the list.
         */
        if (type == List.class)
        {
            config.asList().get().forEach(element -> elementSchema.apply(element));
        }

        /**
         * If the value is a map, then all of the required
         * entries must be present in the map.
         */
        if (type == Map.class && config.asMap().get().keySet().containsAll(getRequiredKeys()) == false)
        {
            final Set<String> missing = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            missing.addAll(getRequiredKeys());
            missing.removeAll(config.asMap().get().keySet());
            throw new InvalidConfigurationException("Missing entries: " + new LinkedList<>(missing));
        }

        /**
         * If the value is a map, then no extraneous
         * entries can be present in the map.
         */
        if (type == Map.class && entrySchemas.keySet().containsAll(config.asMap().get().keySet()) == false)
        {
            final Set<String> extraneous = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            extraneous.addAll(config.asMap().get().keySet());
            extraneous.removeAll(entrySchemas.keySet());
            throw new InvalidConfigurationException("Extraneous entries: " + new LinkedList<>(extraneous));
        }

        /**
         * If the value is a map, then each value in the map
         * must match the relevant schema.
         */
        if (type == Map.class)
        {
            config.asMap().get().entrySet().forEach(entry -> entrySchemas.get(entry.getKey()).apply(entry.getValue()));
        }

        /**
         * Since the value is present and passed the tests,
         * then execute any associated actions.
         *
         * We will execute the actions later,
         * once the whole configuration is validated.
         */
        onPresent.forEach(action -> actions.add(() -> action.accept(config)));
    }

    private Set<String> getRequiredKeys ()
    {
        return entrySchemas.entrySet()
                .stream()
                .filter(x -> x.getValue().required)
                .map(x -> x.getKey())
                .collect(Collectors.toSet());
    }
}
