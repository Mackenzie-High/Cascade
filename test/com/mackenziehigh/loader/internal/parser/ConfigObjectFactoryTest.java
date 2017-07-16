package com.mackenziehigh.loader.internal.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.mackenziehigh.loader.ConfigObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit Test.
 */
public class ConfigObjectFactoryTest
{
    /**
     * Test: 20170610234343413278
     *
     * <p>
     * Method: <code>fromBoolean(*)</code>
     * </p>
     *
     * <p>
     * Case: normal
     * </p>
     */
    @Test
    public void test20170610234343413278 ()
    {
        System.out.println("Test: 20170610234343413278");

        final ConfigObjectFactory factory = new ConfigObjectFactory();
        final ConfigObject object1 = factory.fromBoolean(false);

        assertTrue(object1.isPresent());

        assertTrue(object1.isBoolean());
        assertFalse(object1.isClass());
        assertFalse(object1.isFloat());
        assertFalse(object1.isInteger());
        assertFalse(object1.isList());
        assertFalse(object1.isMap());
        assertFalse(object1.isString());

        assertEquals(false, object1.asBoolean().get());

        assertTrue(object1.asBoolean().isPresent());
        assertFalse(object1.asClass().isPresent());
        assertFalse(object1.asFloat().isPresent());
        assertFalse(object1.asInteger().isPresent());
        assertFalse(object1.asList().isPresent());
        assertFalse(object1.asMap().isPresent());
        assertFalse(object1.asString().isPresent());

        final ConfigObject object2 = factory.fromBoolean(true);

        assertTrue(object2.isPresent());

        assertTrue(object2.isBoolean());
        assertFalse(object2.isClass());
        assertFalse(object2.isFloat());
        assertFalse(object2.isInteger());
        assertFalse(object2.isList());
        assertFalse(object2.isMap());
        assertFalse(object2.isString());

        assertEquals(true, object2.asBoolean().get());

        assertTrue(object2.asBoolean().isPresent());
        assertFalse(object2.asClass().isPresent());
        assertFalse(object2.asFloat().isPresent());
        assertFalse(object2.asInteger().isPresent());
        assertFalse(object2.asList().isPresent());
        assertFalse(object2.asMap().isPresent());
        assertFalse(object2.asString().isPresent());
    }

    /**
     * Test: 20170610234343413372
     *
     * <p>
     * Method: <code>fromInteger(*)</code>
     * </p>
     *
     * <p>
     * Case: normal
     * </p>
     */
    @Test
    public void test20170610234343413372 ()
    {
        System.out.println("Test: 20170610234343413372");

        final ConfigObjectFactory factory = new ConfigObjectFactory();
        final ConfigObject object1 = factory.fromInteger(13);

        assertTrue(object1.isPresent());

        assertFalse(object1.isBoolean());
        assertFalse(object1.isClass());
        assertFalse(object1.isFloat());
        assertTrue(object1.isInteger());
        assertFalse(object1.isList());
        assertFalse(object1.isMap());
        assertFalse(object1.isString());

        assertEquals(13, (long) object1.asInteger().get());

        assertFalse(object1.asBoolean().isPresent());
        assertFalse(object1.asClass().isPresent());
        assertFalse(object1.asFloat().isPresent());
        assertTrue(object1.asInteger().isPresent());
        assertFalse(object1.asList().isPresent());
        assertFalse(object1.asMap().isPresent());
        assertFalse(object1.asString().isPresent());
    }

    /**
     * Test: 20170610234343413404
     *
     * <p>
     * Method: <code>fromFloat(*)</code>
     * </p>
     *
     * <p>
     * Case: normal
     * </p>
     */
    @Test
    public void test20170610234343413404 ()
    {
        System.out.println("Test: 20170610234343413404");

        final ConfigObjectFactory factory = new ConfigObjectFactory();
        final ConfigObject object1 = factory.fromFloat(17);

        assertTrue(object1.isPresent());

        assertFalse(object1.isBoolean());
        assertFalse(object1.isClass());
        assertTrue(object1.isFloat());
        assertFalse(object1.isInteger());
        assertFalse(object1.isList());
        assertFalse(object1.isMap());
        assertFalse(object1.isString());

        assertEquals(17, object1.asFloat().get(), 0.1);

        assertFalse(object1.asBoolean().isPresent());
        assertFalse(object1.asClass().isPresent());
        assertTrue(object1.asFloat().isPresent());
        assertFalse(object1.asInteger().isPresent());
        assertFalse(object1.asList().isPresent());
        assertFalse(object1.asMap().isPresent());
        assertFalse(object1.asString().isPresent());
    }

    /**
     * Test: 20170610234343413435
     *
     * <p>
     * Method: <code>fromString(*)</code>
     * </p>
     *
     * <p>
     * Case: normal
     * </p>
     */
    @Test
    public void test20170610234343413435 ()
    {
        System.out.println("Test: 20170610234343413435");

        final ConfigObjectFactory factory = new ConfigObjectFactory();
        final ConfigObject object1 = factory.fromString("Emma");

        assertTrue(object1.isPresent());

        assertFalse(object1.isBoolean());
        assertFalse(object1.isClass());
        assertFalse(object1.isFloat());
        assertFalse(object1.isInteger());
        assertFalse(object1.isList());
        assertFalse(object1.isMap());
        assertTrue(object1.isString());

        assertEquals("Emma", object1.asString().get());

        assertFalse(object1.asBoolean().isPresent());
        assertFalse(object1.asClass().isPresent());
        assertFalse(object1.asFloat().isPresent());
        assertFalse(object1.asInteger().isPresent());
        assertFalse(object1.asList().isPresent());
        assertFalse(object1.asMap().isPresent());
        assertTrue(object1.asString().isPresent());
    }

    /**
     * Test: 20170610234343413463
     *
     * <p>
     * Method: <code>fromClass(*)</code>
     * </p>
     *
     * <p>
     * Case: normal
     * </p>
     */
    @Test
    public void test20170610234343413463 ()
    {
        System.out.println("Test: 20170610234343413463");

        final ConfigObjectFactory factory = new ConfigObjectFactory();
        final ConfigObject object1 = factory.fromClass(Math.class.getName());

        assertTrue(object1.isPresent());

        assertFalse(object1.isBoolean());
        assertTrue(object1.isClass());
        assertFalse(object1.isFloat());
        assertFalse(object1.isInteger());
        assertFalse(object1.isList());
        assertFalse(object1.isMap());
        assertFalse(object1.isString());

        assertEquals(Math.class, object1.asClass().get());

        assertFalse(object1.asBoolean().isPresent());
        assertTrue(object1.asClass().isPresent());
        assertFalse(object1.asFloat().isPresent());
        assertFalse(object1.asInteger().isPresent());
        assertFalse(object1.asList().isPresent());
        assertFalse(object1.asMap().isPresent());
        assertFalse(object1.asString().isPresent());
    }

    /**
     * Test: 20170611001513915004
     *
     * <p>
     * Method: <code>fromClass(*)</code>
     * </p>
     *
     * <p>
     * Case: no such class
     * </p>
     */
    @Test
    public void test20170611001513915004 ()
    {
        System.out.println("Test: 20170611001513915004");

        final ConfigObjectFactory factory = new ConfigObjectFactory();
        final ConfigObject object1 = factory.fromClass(UUID.randomUUID().toString());

        assertFalse(object1.isPresent());

        assertFalse(object1.isBoolean());
        assertTrue(object1.isClass());
        assertFalse(object1.isFloat());
        assertFalse(object1.isInteger());
        assertFalse(object1.isList());
        assertFalse(object1.isMap());
        assertFalse(object1.isString());

        assertFalse(object1.asBoolean().isPresent());
        assertFalse(object1.asClass().isPresent());
        assertFalse(object1.asFloat().isPresent());
        assertFalse(object1.asInteger().isPresent());
        assertFalse(object1.asList().isPresent());
        assertFalse(object1.asMap().isPresent());
        assertFalse(object1.asString().isPresent());
    }

    /**
     * Test: 20170610234343413486
     *
     * <p>
     * Method: <code>fromList(*)</code>
     * </p>
     *
     * <p>
     * Case: normal
     * </p>
     */
    @Test
    public void test20170610234343413486 ()
    {
        System.out.println("Test: 20170610234343413486");

        final ConfigObjectFactory factory = new ConfigObjectFactory();
        final ConfigObject element1 = factory.fromInteger(100);
        final ConfigObject element2 = factory.fromInteger(200);
        final List<ConfigObject> list = new LinkedList<>();
        list.add(element1);
        list.add(element2);
        final ConfigObject object1 = factory.fromList(list);

        assertTrue(object1.isPresent());

        assertFalse(object1.isBoolean());
        assertFalse(object1.isClass());
        assertFalse(object1.isFloat());
        assertFalse(object1.isInteger());
        assertTrue(object1.isList());
        assertFalse(object1.isMap());
        assertFalse(object1.isString());

        assertEquals(list, object1.asList().get());
        assertTrue(object1.asList().get() instanceof ImmutableList);

        assertFalse(object1.asBoolean().isPresent());
        assertFalse(object1.asClass().isPresent());
        assertFalse(object1.asFloat().isPresent());
        assertFalse(object1.asInteger().isPresent());
        assertTrue(object1.asList().isPresent());
        assertFalse(object1.asMap().isPresent());
        assertFalse(object1.asString().isPresent());
    }

    /**
     * Test: 20170610234343413509
     *
     * <p>
     * Method: <code>fromMap(*)</code>
     * </p>
     *
     * <p>
     * Case: normal
     * </p>
     */
    @Test
    public void test20170610234343413509 ()
    {
        System.out.println("Test: 20170610234343413509");

        final ConfigObjectFactory factory = new ConfigObjectFactory();
        final Map<String, ConfigObject> map = new HashMap<>();
        map.put("Erin", factory.fromString("Burnett"));
        map.put("Emma", factory.fromString("Watson"));
        final ConfigObject object1 = factory.fromMap(map);

        assertTrue(object1.isPresent());

        assertFalse(object1.isBoolean());
        assertFalse(object1.isClass());
        assertFalse(object1.isFloat());
        assertFalse(object1.isInteger());
        assertFalse(object1.isList());
        assertTrue(object1.isMap());
        assertFalse(object1.isString());

        assertEquals(map, object1.asMap().get());
        assertTrue(object1.asMap().get() instanceof ImmutableSortedMap);

        assertFalse(object1.asBoolean().isPresent());
        assertFalse(object1.asClass().isPresent());
        assertFalse(object1.asFloat().isPresent());
        assertFalse(object1.asFloat().isPresent());
        assertFalse(object1.asInteger().isPresent());
        assertFalse(object1.asList().isPresent());
        assertTrue(object1.asMap().isPresent());
        assertFalse(object1.asString().isPresent());
    }
}
