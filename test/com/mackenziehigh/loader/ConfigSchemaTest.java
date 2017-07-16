package com.mackenziehigh.loader;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mackenziehigh.loader.exceptions.InvalidConfigurationException;
import com.mackenziehigh.loader.internal.parser.ConfigObjectFactory;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import static org.junit.Assert.*;
import org.junit.Test;

public class ConfigSchemaTest
{
    private final ConfigObjectFactory factory = new ConfigObjectFactory();

    private final ConfigObject BOOLEAN = factory.fromBoolean(true);

    private final ConfigObject CLASS = factory.fromClass(String.class.getName());

    private final ConfigObject FLOAT = factory.fromFloat(12.34);

    private final ConfigObject INTEGER = factory.fromInteger(56);

    private final ConfigObject STRING = factory.fromString("Vulcan");

    private final ConfigObject LIST = factory.fromList(ImmutableList.of());

    private final ConfigObject MAP = factory.fromMap(ImmutableMap.of());

    /**
     * Test: 20170611190530171277
     *
     * <p>
     * Case: data-type enforcement
     * </p>
     */
    @Test
    public void test20170611190530171277 ()
    {
        System.out.println("Test: 20170611190530171277");

        final Map<String, ConfigSchema> schemas = new HashMap<>();
        schemas.put("boolean", new ConfigSchema().requireBoolean());
        schemas.put("class", new ConfigSchema().requireClass());
        schemas.put("float", new ConfigSchema().requireFloat());
        schemas.put("integer", new ConfigSchema().requireInteger());
        schemas.put("string", new ConfigSchema().requireString());
        schemas.put("list", new ConfigSchema().requireList());
        schemas.get("list").each().requireInteger();
        schemas.put("map", new ConfigSchema().requireMap());

        final Map<String, ConfigObject> objects = new HashMap<>();
        objects.put("boolean", BOOLEAN);
        objects.put("class", CLASS);
        objects.put("float", FLOAT);
        objects.put("integer", INTEGER);
        objects.put("string", STRING);
        objects.put("list", LIST);
        objects.put("map", MAP);

        for (Entry<String, ConfigSchema> schema : schemas.entrySet())
        {
            for (Entry<String, ConfigObject> object : objects.entrySet())
            {
                // Case: schema matches type
                if (schema.getKey().equals(object.getKey()))
                {
                    // This should not throw any exception.
                    schema.getValue().apply(object.getValue());
                    continue;
                }

                // Case: schema does not match type
                try
                {
                    schema.getValue().apply(object.getValue());
                    fail();
                }
                catch (InvalidConfigurationException ex)
                {
                    // Pass.
                }
            }
        }
    }

    /**
     * Test: 20170611190530171298
     *
     * <p>
     * Case: missing required map entry
     * </p>
     */
    @Test (expected = InvalidConfigurationException.class)
    public void test20170611190530171298 ()
    {
        System.out.println("Test: 20170611190530171298");

        final ConfigSchema schema = new ConfigSchema();
        schema.requireMap();
        schema.entry("name").required();

        final ConfigObject object = factory.fromMap(ImmutableMap.of());

        schema.apply(object);
    }

    /**
     * Test: 20170611190530171317
     *
     * <p>
     * Case: missing optional map entry
     * </p>
     */
    @Test
    public void test20170611190530171317 ()
    {
        System.out.println("Test: 20170611190530171317");

        final ConfigSchema schema = new ConfigSchema();
        schema.requireMap();
        schema.entry("name").optional();

        final ConfigObject object = factory.fromMap(ImmutableMap.of());

        schema.apply(object);
    }

    /**
     * Test: 20170611190530171333
     *
     * <p>
     * Case: invalid map entry
     * </p>
     */
    @Test (expected = InvalidConfigurationException.class)
    public void test20170611190530171333 ()
    {
        System.out.println("Test: 20170611190530171333");

        final ConfigSchema schema = new ConfigSchema();
        schema.requireMap();
        schema.entry("name").required().requireBoolean();

        final ConfigObject object = factory.fromMap(ImmutableMap.of("name", INTEGER));

        schema.apply(object);
    }

    /**
     * Test: 20170611190530171349
     *
     * <p>
     * Case: valid map entry
     * </p>
     */
    @Test
    public void test20170611190530171349 ()
    {
        System.out.println("Test: 20170611190530171349");

        final ConfigSchema schema = new ConfigSchema();
        schema.requireMap();
        schema.entry("name").required().requireInteger();

        final ConfigObject object = factory.fromMap(ImmutableMap.of("name", INTEGER));

        schema.apply(object);
    }

    /**
     * Test: 20170611190530171365
     *
     * <p>
     * Case: invalid list entry
     * </p>
     */
    @Test (expected = InvalidConfigurationException.class)
    public void test20170611190530171365 ()
    {
        System.out.println("Test: 20170611190530171365");

        final ConfigSchema schema = new ConfigSchema();
        schema.requireList();
        schema.each().requireBoolean();

        final ConfigObject object = factory.fromList(ImmutableList.of(INTEGER));

        schema.apply(object);
    }

    /**
     * Test: 20170611190530171383
     *
     * <p>
     * Case: predicate enforcement
     * </p>
     */
    @Test (expected = InvalidConfigurationException.class)
    public void test20170611190530171383 ()
    {
        System.out.println("Test: 20170611190530171383");

        final ConfigSchema schema = new ConfigSchema();
        schema.requireInteger();
        schema.require(x -> x.asInteger().get() < 5);

        final ConfigObject object = factory.fromInteger(17);

        schema.apply(object);
    }

    /**
     * Test: 20170611190530171400
     *
     * <p>
     * Case: predicates do not apply to missing elements
     * </p>
     */
    @Test
    public void test20170611190530171400 ()
    {
        System.out.println("Test: 20170611190530171400");

        final ConfigSchema schema = new ConfigSchema();
        schema.requireClass();
        schema.optional();
        schema.require(x -> false);

        final ConfigObject object = factory.fromClass(UUID.randomUUID().toString());

        schema.apply(object);
    }

    /**
     * Test: 20170611190530171424
     *
     * <p>
     * Case: string pattern predicate enforcement
     * </p>
     */
    @Test (expected = InvalidConfigurationException.class)
    public void test20170611190530171424 ()
    {
        System.out.println("Test: 20170611190530171424");

        final ConfigSchema schema = new ConfigSchema();
        schema.requireString().requireMatch("[A-Z]+");

        final ConfigObject object = factory.fromString("A1Z");

        schema.apply(object);
    }

    /**
     * Test: 20170611190530171441
     *
     * <p>
     * Case: action execution
     * </p>
     */
    @Test
    public void test20170611190530171441 ()
    {
        System.out.println("Test: 20170611190530171441");

        final List<Object> out = new LinkedList<>();

        final ConfigSchema schema = new ConfigSchema();
        schema.requireList();
        schema.each().requireInteger().onPresent(x -> out.add(x.asInteger().get()));

        final ConfigObject element1 = factory.fromInteger(17);
        final ConfigObject element2 = factory.fromInteger(23);
        final ConfigObject object = factory.fromList(ImmutableList.of(element1, element2));

        schema.apply(object);

        assertEquals(ImmutableList.of(17L, 23L), out);
    }

    /**
     * Test: 20170611190530171456
     *
     * <p>
     * Case: actions only execute on successful match
     * </p>
     */
    @Test
    public void test20170611190530171456 ()
    {
        System.out.println("Test: 20170611190530171456");

        final List<Object> out = new LinkedList<>();

        final ConfigSchema schema = new ConfigSchema();
        schema.requireList();
        schema.each().requireInteger().require(x -> x.asInteger().get() < 10).onPresent(x -> out.add(x.asInteger().get()));

        final ConfigObject element1 = factory.fromInteger(17);
        final ConfigObject element2 = factory.fromInteger(23);
        final ConfigObject object = factory.fromList(ImmutableList.of(element1, element2));

        try
        {
            schema.apply(object);
            fail();
        }
        catch (InvalidConfigurationException e)
        {
            // Pass
        }

        assertTrue(out.isEmpty());
    }
}
