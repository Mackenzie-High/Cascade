package com.mackenziehigh.internal.cascade;

import com.google.common.collect.Lists;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.OverflowPolicy;
import com.mackenziehigh.cascade.Reactor;
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
    private final Reactor reactor = Cascade.newReactor();

    private InternalInput<String> object = InternalInput.newLinkedInput(reactor, String.class, 8, OverflowPolicy.DROP_INCOMING);

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
        final UUID uuid1 = object.uuid();
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
     * Case: Default.
     * </p>
     */
    @Test
    public void test20180525225637428144 ()
    {
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
     * Case: After Assignment.
     * </p>
     */
    @Test
    public void test20180525233556498166 ()
    {
        object.named("Coal");
        assertEquals("Coal", object.name());
    }

    /**
     * Test: 20180525225637428172
     *
     * <p>
     * Method: <code>reactor()</code>
     * </p>
     *
     * <p>
     * Case: Normal.
     * </p>
     */
    @Test
    public void test20180525225637428172 ()
    {
        assertEquals(reactor, object.reactor());
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
        assertEquals(8, object.capacity());
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

        assertEquals(0, object.size());
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
        assertEquals(0, object.size());
        assertTrue(object.isEmpty());
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
        assertEquals(8, object.capacity());

        assertEquals(0, object.size());
        assertTrue(object.isEmpty());
        assertFalse(object.isFull());

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
        assertFalse(object.isFull());
        object.send("Romulus");
        assertEquals(6, object.size());
        assertFalse(object.isEmpty());
        assertFalse(object.isFull());
        object.send("Mars");
        assertEquals(7, object.size());
        assertFalse(object.isEmpty());
        assertFalse(object.isFull());
        object.send("Venus");
        assertEquals(8, object.size());
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
         * Empty Queue.
         */
        assertNull(object.peekOrDefault(null));
        assertEquals("Mars", object.peekOrDefault("Mars"));

        /**
         * Non-Empty Queue.
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
         * Empty Queue.
         */
        assertFalse(object.peek().isPresent());

        /**
         * Non-Empty Queue.
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
         * Empty Queue.
         */
        assertNull(object.pollOrDefault(null));
        assertEquals("Mars", object.pollOrDefault("Mars"));

        /**
         * Non-Empty Queue.
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
         * Empty Queue.
         */
        assertFalse(object.poll().isPresent());

        /**
         * Non-Empty Queue.
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
         * Empty Queue.
         */
        object.forEach(x -> out.add(x));
        assertTrue(out.isEmpty());

        /**
         * Non-Empty Queue.
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
     * Case: Null Argument.
     * </p>
     */
    @Test (expected = NullPointerException.class)
    public void test20180525230353278173 ()
    {
        object.send(null);
    }

    /**
     * Test: 20180525230353278223
     *
     * <p>
     * Method: <code>send</code>
     * </p>
     *
     * <p>
     * Case: Overflow Policy - Drop Oldest.
     * </p>
     */
    @Test
    public void test20180525230353278223 ()
    {
        /**
         * Build the input and fill it up to capacity.
         */
        object = InternalInput.newLinkedInput(reactor, String.class, 3, OverflowPolicy.DROP_OLDEST);
        object.send("A");
        object.send("B");
        object.send("C");
        assertEquals(3, object.size());
        assertTrue(object.isFull());
        assertEquals(OverflowPolicy.DROP_OLDEST, object.overflowPolicy());

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
        object = InternalInput.newLinkedInput(reactor, String.class, 3, OverflowPolicy.DROP_NEWEST);
        object.send("A");
        object.send("B");
        object.send("C");
        assertEquals(3, object.size());
        assertTrue(object.isFull());
        assertEquals(OverflowPolicy.DROP_NEWEST, object.overflowPolicy());

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
        object = InternalInput.newLinkedInput(reactor, String.class, 3, OverflowPolicy.DROP_PENDING);
        object.send("A");
        object.send("B");
        object.send("C");
        assertEquals(3, object.size());
        assertTrue(object.isFull());
        assertEquals(OverflowPolicy.DROP_PENDING, object.overflowPolicy());

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
        object = InternalInput.newLinkedInput(reactor, String.class, 3, OverflowPolicy.DROP_INCOMING);
        object.send("A");
        object.send("B");
        object.send("C");
        assertEquals(3, object.size());
        assertTrue(object.isFull());
        assertEquals(OverflowPolicy.DROP_INCOMING, object.overflowPolicy());

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
        object = InternalInput.newLinkedInput(reactor, String.class, 3, OverflowPolicy.DROP_ALL);
        object.send("A");
        object.send("B");
        object.send("C");
        assertEquals(3, object.size());
        assertTrue(object.isFull());
        assertEquals(OverflowPolicy.DROP_ALL, object.overflowPolicy());

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
         * Empty Queue.
         */
        assertTrue(object.isEmpty());
        object.clear();
        assertTrue(object.isEmpty());

        /**
         * Non-Empty Queue.
         */
        object.send("A");
        object.send("B");
        object.send("C");
        object.clear();
        assertTrue(object.isEmpty());
    }

}
