package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.internal.cascade.ArrayInput;
import com.mackenziehigh.cascade.builder.OverflowPolicy;
import com.google.common.collect.Lists;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * TODO: Test pings in send and poll.
 *
 * @author mackenzie
 */
public final class ArrayInputTest
{
    private final MockReactor reactor = new MockReactor();

    private final ArrayInput<String> object = new ArrayInput<>(reactor, String.class);

    /**
     * Test: 20180525225637428064
     *
     * <p>
     * Method: <code>uuid()</code>
     * </p>
     */
    @Test
    public void test20180525225637428064 ()
    {
        /**
         * Before build().
         */
        final UUID uuid1 = object.uuid();

        /**
         * After build().
         */
        object.build();
        final UUID uuid2 = object.uuid();

        assertNotNull(uuid1);
        assertEquals(uuid1, uuid2);
    }

    /**
     * Test: 20180525225637428144
     *
     * <p>
     * Method: <code>name</code>
     * </p>
     *
     * <p>
     * Case: Before build().
     * </p>
     */
    @Test
    public void test20180525225637428144 ()
    {
        /**
         * Before build(), Before named().
         */
        assertEquals(object.uuid().toString(), object.name());

        /**
         * Before build(), After named().
         */
        object.named("Coal");
        assertEquals("Coal", object.name());
    }

    /**
     * Test: 20180525233352121667
     *
     * <p>
     * Method: <code>name</code>
     * </p>
     *
     * <p>
     * Case: After build(), Before named().
     * </p>
     */
    @Test
    public void test20180525233352121667 ()
    {
        object.build();
        assertEquals(object.uuid().toString(), object.name());
    }

    /**
     * Test: 20180525233556498166
     *
     * <p>
     * Method: <code>name</code>
     * </p>
     *
     * <p>
     * Case: After build(), After named().
     * </p>
     */
    @Test
    public void test20180525233556498166 ()
    {
        object.named("Coal");
        object.build();
        assertEquals("Coal", object.name());
    }

    /**
     * Test: 20180525233751021852
     *
     * <p>
     * Method: <code>named</code>
     * </p>
     *
     * <p>
     * Case: After build().
     * </p>
     */
    @Test (expected = IllegalStateException.class)
    public void test20180525233751021852 ()
    {
        object.build();
        object.named("Coal");
    }

    /**
     * Test: 20180525225637428172
     *
     * <p>
     * Method: <code>reactor()</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180525225637428172 ()
    {
        System.out.println("Test: 20180525225637428172");
        fail();
    }

    /**
     * Test: 20180525225637428198
     *
     * <p>
     * Method: <code>connect()</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180525225637428198 ()
    {
        System.out.println("Test: 20180525225637428198");
        fail();
    }

    /**
     * Test: 20180525225637428223
     *
     * <p>
     * Method: <code>disconnect()</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180525225637428223 ()
    {
        System.out.println("Test: 20180525225637428223");
        fail();
    }

    /**
     * Test: 20180525230353277838
     *
     * <p>
     * Method: <code>connections()</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180525230353277838 ()
    {
        System.out.println("Test: 20180525230353277838");
        fail();
    }

    /**
     * Test: 20180525230353277923
     *
     * <p>
     * Method: <code>capacity</code>
     * </p>
     */
    @Test
    public void test20180525230353277923 ()
    {
        /**
         * Before specified.
         */
        assertEquals(0, object.capacity());

        /**
         * After specified, Before build().
         */
        object.withCapacity(128);
        assertEquals(128, object.capacity());

        /**
         * After specified, After build().
         */
        object.build();
        assertEquals(128, object.capacity());
    }

    /**
     * Test: 20180525234811438774
     *
     * <p>
     * Method: <code>capacity</code>
     * </p>
     *
     * <p>
     * Case: Capacity Assignment, Duplicate.
     * </p>
     */
    @Test
    public void test20180525234811438774 ()
    {
        object.withCapacity(100);
        object.withCapacity(200);
        assertEquals(200, object.capacity());
    }

    /**
     * Test: 20180525234913400670
     *
     * <p>
     * Method: <code>capacity</code>
     * </p>
     *
     * <p>
     * Case: Capacity Assignment, After build().
     * </p>
     */
    @Test (expected = IllegalStateException.class)
    public void test20180525234913400670 ()
    {
        object.build();
        object.withCapacity(100);
    }

    /**
     * Test: 20180525235046853903
     *
     * <p>
     * Method: <code>capacity</code>
     * </p>
     *
     * <p>
     * Case: Capacity Assignment, Negative.
     * </p>
     */
    @Test (expected = IllegalArgumentException.class)
    public void test20180525235046853903 ()
    {
        object.withCapacity(-1);
    }

    /**
     * Test: 20180525235221088739
     *
     * <p>
     * Method: <code>capacity</code>
     * </p>
     *
     * <p>
     * Case: Capacity Assignment, Zero.
     * </p>
     */
    @Test
    public void test20180525235221088739 ()
    {
        /**
         * Build the object.
         */
        object.withCapacity(0);
        assertEquals(0, object.capacity());
        object.build();

        /**
         * Verify that it was built correctly.
         */
        assertEquals(0, object.size());
        assertTrue(object.isEmpty());
        assertTrue(object.isFull());

        /**
         * Try to use the object.
         */
        object.send("Pong");
        assertEquals(0, object.size());
        assertTrue(object.isEmpty());
        assertTrue(object.isFull());
        assertEquals(0, reactor.pings.get());
    }

    /**
     * Test: 20180525230353277954
     *
     * <p>
     * Method: <code>size()</code>
     * </p>
     */
    @Test
    public void test20180525230353277954 ()
    {
        System.out.println("Test: 20180525230353277954");

        /**
         * Before build().
         */
        assertEquals(0, object.size());

        /**
         * After build(), Before Use.
         */
        object.withCapacity(100).build();
        assertEquals(0, object.size());

        /**
         * After build(), After Use.
         */
        object.send("Vulcan");
        assertEquals(1, object.size());
        object.send("Andoria");
        assertEquals(2, object.size());
    }

    /**
     * Test: 20180525230353277987
     *
     * <p>
     * Method: <code>isEmpty()</code>
     * </p>
     */
    @Test
    public void test20180525230353277987 ()
    {
        System.out.println("Test: 20180525230353277987");

        /**
         * Before build().
         */
        assertEquals(0, object.size());
        assertTrue(object.isEmpty());

        /**
         * After build(), Before Use.
         */
        object.withCapacity(100).build();
        assertEquals(0, object.size());
        assertTrue(object.isEmpty());

        /**
         * After build(), After Use.
         */
        object.send("Vulcan");
        assertEquals(1, object.size());
        assertFalse(object.isEmpty());
        object.send("Andoria");
        assertEquals(2, object.size());
        assertFalse(object.isEmpty());
    }

    /**
     * Test: 20180525230353278019
     *
     * <p>
     * Method: <code>isFull()</code>
     * </p>
     */
    @Test
    public void test20180525230353278019 ()
    {
        /**
         * Before build(), Before Capacity Assignment.
         */
        assertEquals(0, object.size());
        assertTrue(object.isEmpty());
        assertTrue(object.isFull());

        /**
         * Before build(), After Capacity Assignment.
         */
        object.withCapacity(5);
        assertEquals(0, object.size());
        assertTrue(object.isEmpty());
        assertFalse(object.isFull());

        /**
         * After build(), Before Use.
         */
        object.build();
        assertEquals(0, object.size());
        assertTrue(object.isEmpty());
        assertFalse(object.isFull());

        /**
         * After build(), After Use.
         */
        object.send("Vulcan");
        assertEquals(1, object.size());
        assertFalse(object.isEmpty());
        assertFalse(object.isFull());
        object.send("Andoria");
        assertEquals(2, object.size());
        assertFalse(object.isEmpty());
        assertFalse(object.isFull());
        object.send("Pluto");
        assertEquals(3, object.size());
        assertFalse(object.isEmpty());
        assertFalse(object.isFull());
        object.send("Saturn");
        assertEquals(4, object.size());
        assertFalse(object.isEmpty());
        assertFalse(object.isFull());
        object.send("Uranus");
        assertEquals(5, object.size());
        assertFalse(object.isEmpty());
        assertTrue(object.isFull());
    }

    /**
     * Test: 20180525230353278044
     *
     * <p>
     * Method: <code>peekOrDefault()</code>
     * </p>
     */
    @Test
    public void test20180525230353278044 ()
    {
        /**
         * Before build(), Null.
         */
        assertNull(object.peekOrDefault(null));

        /**
         * Before build(), Not Null.
         */
        assertEquals("Mars", object.peekOrDefault("Mars"));

        /**
         * After build(), Default Value, Null.
         */
        object.withCapacity(100).build();
        assertNull(object.peekOrDefault(null));

        /**
         * After build(), Default Value, Not Null.
         */
        assertEquals("Mars", object.peekOrDefault("Mars"));

        /**
         * After build(), Non-Default Value.
         */
        object.send("T'Pol");
        assertEquals(1, object.size());
        assertEquals("T'Pol", object.peekOrDefault(null));

        /**
         * Peeking does not remove.
         */
        assertEquals(1, object.size());
        assertEquals("T'Pol", object.peekOrDefault(null));
        assertEquals(1, object.size());
    }

    /**
     * Test: 20180525230353278071
     *
     * <p>
     * Method: <code>peek</code>
     * </p>
     */
    @Test
    public void test20180525230353278071 ()
    {
        /**
         * Before build().
         */
        assertFalse(object.peek().isPresent());

        /**
         * After build(), Default Value.
         */
        object.withCapacity(100).build();
        assertFalse(object.peek().isPresent());

        /**
         * After build(), Non-Default Value.
         */
        object.send("T'Pol");
        assertEquals(1, object.size());
        assertEquals("T'Pol", object.peek().get());

        /**
         * Peeking does not remove.
         */
        assertEquals(1, object.size());
        assertEquals("T'Pol", object.peek().get());
        assertEquals(1, object.size());
    }

    /**
     * Test: 20180525230353278097
     *
     * <p>
     * Method: <code>pollOrDefault</code>
     * </p>
     */
    @Test
    public void test20180525230353278097 ()
    {
        System.out.println("Test: 20180525230353278097");

        /**
         * Before build(), Null.
         */
        assertNull(object.pollOrDefault(null));

        /**
         * Before build(), Not Null.
         */
        assertEquals("Mars", object.pollOrDefault("Mars"));

        /**
         * After build(), Default Value, Null.
         */
        object.withCapacity(100).build();
        assertNull(object.pollOrDefault(null));

        /**
         * After build(), Default Value, Not Null.
         */
        assertEquals("Mars", object.pollOrDefault("Mars"));

        /**
         * After build(), Non-Default Value.
         */
        object.send("T'Pol");
        assertEquals(1, object.size());
        assertEquals("T'Pol", object.pollOrDefault(null));
        assertEquals(0, object.size());
    }

    /**
     * Test: 20180525230353278124
     *
     * <p>
     * Method: <code>poll</code>
     * </p>
     */
    @Test
    public void test20180525230353278124 ()
    {
        System.out.println("Test: 20180525230353278124");

        /**
         * Before build().
         */
        assertFalse(object.poll().isPresent());

        /**
         * After build(), Default Value.
         */
        object.withCapacity(100).build();
        assertFalse(object.poll().isPresent());

        /**
         * After build(), Non-Default Value.
         */
        object.send("T'Pol");
        assertEquals(1, object.size());
        assertEquals("T'Pol", object.poll().get());
        assertEquals(0, object.size());
    }

    /**
     * Test: 20180525230353278150
     *
     * <p>
     * Method: <code>forEach</code>
     * </p>
     */
    @Test
    public void test20180525230353278150 ()
    {
        final List<String> out = new LinkedList<>();

        /**
         * Before build().
         */
        object.forEach(x -> out.add(x));
        assertTrue(out.isEmpty());

        /**
         * After build(), Empty.
         */
        object.withCapacity(100).build();
        object.forEach(x -> out.add(x));
        assertTrue(out.isEmpty());

        /**
         * After build(), Not Empty.
         */
        object.send("Vancouver");
        object.send("Toronto");
        object.send("Montreal");
        assertEquals(3, object.size());
        assertTrue(out.isEmpty());
        object.forEach(x -> out.add(x));
        assertEquals(Lists.newArrayList("Vancouver", "Toronto", "Montreal"), out);
    }

    /**
     * Test: 20180525230353278173
     *
     * <p>
     * Method: <code>send</code>
     * </p>
     *
     * <p>
     * Case: Null Argument
     * </p>
     */
    @Test (expected = NullPointerException.class)
    public void test20180525230353278173 ()
    {
        object.withCapacity(10).build().send(null);
    }

    /**
     * Test: 20180525230353278199
     *
     * <p>
     * Method: <code>send</code>
     * </p>
     *
     * <p>
     * Case: Not Built Yet
     * </p>
     */
    @Test
    public void test20180525230353278199 ()
    {
        assertTrue(object.withCapacity(10).send("Vulcan").isEmpty());
        assertEquals(0, reactor.pings.get());
    }

    /**
     * Test: 20180525230353278223
     *
     * <p>
     * Method: <code>send</code>
     * </p>
     *
     * <p>
     * Case: Overflow Policy - Drop Oldest
     * </p>
     */
    @Test
    public void test20180525230353278223 ()
    {
        /**
         * Build the input and fill it up to capacity.
         */
        object.withCapacity(3).withOverflowPolicy(OverflowPolicy.DROP_OLDEST).build();
        object.send("A");
        object.send("B");
        object.send("C");
        assertEquals(3, object.size());
        assertTrue(object.isFull());

        /**
         * Try to insert another element, which will overflow the capacity.
         */
        object.send("X");

        /**
         * Verify the the overflow-policy properly resolved the overflow.
         */
        assertEquals(3, object.size());
        assertEquals("B", object.poll().get());
        assertEquals("C", object.poll().get());
        assertEquals("X", object.poll().get());
        assertTrue(object.isEmpty());
    }

    /**
     * Test: 20180525230353278248
     *
     * <p>
     * Method: <code>send</code>
     * </p>
     *
     * <p>
     * Case: Overflow Policy - Drop Newest
     * </p>
     */
    @Test
    public void test20180525230353278248 ()
    {
        /**
         * Build the input and fill it up to capacity.
         */
        object.withCapacity(3).withOverflowPolicy(OverflowPolicy.DROP_NEWEST).build();
        object.send("A");
        object.send("B");
        object.send("C");
        assertEquals(3, object.size());
        assertTrue(object.isFull());

        /**
         * Try to insert another element, which will overflow the capacity.
         */
        object.send("X");

        /**
         * Verify the the overflow-policy properly resolved the overflow.
         */
        assertEquals(3, object.size());
        assertEquals("A", object.poll().get());
        assertEquals("B", object.poll().get());
        assertEquals("X", object.poll().get());
        assertTrue(object.isEmpty());
    }

    /**
     * Test: 20180525230353278273
     *
     * <p>
     * Method: <code>send</code>
     * </p>
     *
     * <p>
     * Case: Overflow Policy - Drop Pending
     * </p>
     */
    @Test
    public void test20180525230353278273 ()
    {
        /**
         * Build the input and fill it up to capacity.
         */
        object.withCapacity(3).withOverflowPolicy(OverflowPolicy.DROP_PENDING).build();
        object.send("A");
        object.send("B");
        object.send("C");
        assertEquals(3, object.size());
        assertTrue(object.isFull());

        /**
         * Try to insert another element, which will overflow the capacity.
         */
        object.send("X");

        /**
         * Verify the the overflow-policy properly resolved the overflow.
         */
        assertEquals(1, object.size());
        assertEquals("X", object.poll().get());
        assertTrue(object.isEmpty());
    }

    /**
     * Test: 20180525230353278297
     *
     * <p>
     * Method: <code>send</code>
     * </p>
     *
     * <p>
     * Case: Overflow Policy - Drop Incoming
     * </p>
     */
    @Test
    public void test20180525230353278297 ()
    {
        /**
         * Build the input and fill it up to capacity.
         */
        object.withCapacity(3).withOverflowPolicy(OverflowPolicy.DROP_INCOMING).build();
        object.send("A");
        object.send("B");
        object.send("C");
        assertEquals(3, object.size());
        assertTrue(object.isFull());

        /**
         * Try to insert another element, which will overflow the capacity.
         */
        object.send("X");

        /**
         * Verify the the overflow-policy properly resolved the overflow.
         */
        assertEquals(3, object.size());
        assertEquals("A", object.poll().get());
        assertEquals("B", object.poll().get());
        assertEquals("C", object.poll().get());
        assertTrue(object.isEmpty());
    }

    /**
     * Test: 20180525230353278317
     *
     * <p>
     * Method: <code>send</code>
     * </p>
     *
     * <p>
     * Case: Overflow Policy - Drop All
     * </p>
     */
    @Test
    public void test20180525230353278317 ()
    {
        /**
         * Build the input and fill it up to capacity.
         */
        object.withCapacity(3).withOverflowPolicy(OverflowPolicy.DROP_ALL).build();
        object.send("A");
        object.send("B");
        object.send("C");
        assertEquals(3, object.size());
        assertTrue(object.isFull());

        /**
         * Try to insert another element, which will overflow the capacity.
         */
        object.send("X");

        /**
         * Verify the the overflow-policy properly resolved the overflow.
         */
        assertTrue(object.isEmpty());
    }

    /**
     * Test: 20180525230353278337
     *
     * <p>
     * Method: <code>send</code>
     * </p>
     *
     * <p>
     * Case: Overflow Policy - Unspecified
     * </p>
     */
    @Test
    public void test20180525230353278337 ()
    {
        System.out.println("Test: 20180525230353278337");
        fail();
    }

    /**
     * Test: 20180525230353278358
     *
     * <p>
     * Method: <code>verify</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180525230353278358 ()
    {
        System.out.println("Test: 20180525230353278358");
        fail();
    }

    /**
     * Test: 20180525231117106175
     *
     * <p>
     * Method: <code>type</code>
     * </p>
     */
    @Test
    public void test20180525231117106175 ()
    {
        /**
         * Before build().
         */
        assertEquals(String.class, object.type());

        /**
         * After build().
         */
        object.withCapacity(10).build();
        assertEquals(String.class, object.type());
    }

    /**
     * Test: 20180525231117106210
     *
     * <p>
     * Method: <code>clear</code>
     * </p>
     */
    @Test
    public void test20180525231117106210 ()
    {
        /**
         * Case: Before build().
         */
        object.clear();
        assertTrue(object.isEmpty());

        /**
         * Case: After build(), Before Use.
         */
        object.withCapacity(3).build();
        object.clear();
        assertTrue(object.isEmpty());

        /**
         * After build(), After Use.
         */
        object.send("A");
        object.send("B");
        object.send("C");
        object.clear();
        assertTrue(object.isEmpty());
    }

}
