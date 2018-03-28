package com.mackenziehigh.cascade.internal;

import com.google.common.collect.Lists;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit Test.
 */
public final class BoundedInflowQueueTest
{
    private final LinkedInflowQueue delegate = new LinkedInflowQueue(5);

    private BoundedInflowQueue queue = new BoundedInflowQueue(BoundedInflowQueue.OverflowPolicy.DROP_ALL, delegate);

    private final AtomicReference<CascadeToken> event = new AtomicReference<>();

    private final AtomicReference<CascadeStack> stack = new AtomicReference<>();

    /**
     * Test: 20180320001644031969
     *
     * <p>
     * Method: <code>offer</code>
     * </p>
     *
     * <p>
     * Case: Overflow, Drop All
     * </p>
     */
    @Test
    public void test20180320001644031969 ()
    {
        System.out.println("Test: 20180320001644031969");

        queue = new BoundedInflowQueue(BoundedInflowQueue.OverflowPolicy.DROP_ALL, delegate);

        assertEquals(0, queue.getOverflowCount());
        assertEquals(0, queue.size());
        assertTrue(queue.offer(CascadeToken.token("A"), CascadeStack.newStack().pushInt(100)));
        assertEquals(1, queue.size());
        assertTrue(queue.offer(CascadeToken.token("B"), CascadeStack.newStack().pushInt(200)));
        assertEquals(2, queue.size());
        assertTrue(queue.offer(CascadeToken.token("C"), CascadeStack.newStack().pushInt(300)));
        assertEquals(3, queue.size());
        assertTrue(queue.offer(CascadeToken.token("D"), CascadeStack.newStack().pushInt(400)));
        assertEquals(4, queue.size());
        assertTrue(queue.offer(CascadeToken.token("E"), CascadeStack.newStack().pushInt(500)));
        assertEquals(5, queue.size());
        assertTrue(queue.offer(CascadeToken.token("F"), CascadeStack.newStack().pushInt(600)));
        assertEquals(1, queue.size());
        assertTrue(queue.offer(CascadeToken.token("G"), CascadeStack.newStack().pushInt(700)));
        assertEquals(2, queue.size());
        assertTrue(queue.offer(CascadeToken.token("H"), CascadeStack.newStack().pushInt(800)));
        assertEquals(3, queue.size());
        assertTrue(queue.offer(CascadeToken.token("I"), CascadeStack.newStack().pushInt(900)));
        assertEquals(4, queue.size());
        assertTrue(queue.offer(CascadeToken.token("J"), CascadeStack.newStack().pushInt(1000)));
        assertEquals(5, queue.size());
        assertEquals(1, queue.getOverflowCount());

        assertEquals(5, queue.size());
        queue.removeOldest(event, stack);
        assertEquals("F", event.get().name());
        assertEquals(600, stack.get().peekAsInt());
        queue.removeOldest(event, stack);
        assertEquals("G", event.get().name());
        assertEquals(700, stack.get().peekAsInt());
        queue.removeOldest(event, stack);
        assertEquals("H", event.get().name());
        assertEquals(800, stack.get().peekAsInt());
        queue.removeOldest(event, stack);
        assertEquals("I", event.get().name());
        assertEquals(900, stack.get().peekAsInt());
        queue.removeOldest(event, stack);
        assertEquals("J", event.get().name());
        assertEquals(1000, stack.get().peekAsInt());
    }

    /**
     * Test: 20180320002436263850
     *
     * <p>
     * Method: <code>offer</code>
     * </p>
     *
     * <p>
     * Case: Overflow, Drop Newest
     * </p>
     */
    @Test
    public void test20180320002436263850 ()
    {
        System.out.println("Test: 20180320002436263850");

        fail(); // This is broke I think

        queue = new BoundedInflowQueue(BoundedInflowQueue.OverflowPolicy.DROP_NEWEST, delegate);

        assertEquals(0, queue.getOverflowCount());
        assertEquals(0, queue.size());
        assertTrue(queue.offer(CascadeToken.token("A"), CascadeStack.newStack().pushInt(100)));
        assertEquals(1, queue.size());
        assertTrue(queue.offer(CascadeToken.token("B"), CascadeStack.newStack().pushInt(200)));
        assertEquals(2, queue.size());
        assertTrue(queue.offer(CascadeToken.token("C"), CascadeStack.newStack().pushInt(300)));
        assertEquals(3, queue.size());
        assertTrue(queue.offer(CascadeToken.token("D"), CascadeStack.newStack().pushInt(400)));
        assertEquals(4, queue.size());
        assertTrue(queue.offer(CascadeToken.token("E"), CascadeStack.newStack().pushInt(500)));
        assertEquals(5, queue.size());
        assertTrue(queue.offer(CascadeToken.token("F"), CascadeStack.newStack().pushInt(600)));
        assertEquals(5, queue.size());
        assertTrue(queue.offer(CascadeToken.token("G"), CascadeStack.newStack().pushInt(700)));
        assertEquals(5, queue.size());
        assertTrue(queue.offer(CascadeToken.token("H"), CascadeStack.newStack().pushInt(800)));
        assertEquals(5, queue.size());
        assertTrue(queue.offer(CascadeToken.token("I"), CascadeStack.newStack().pushInt(900)));
        assertEquals(5, queue.size());
        assertTrue(queue.offer(CascadeToken.token("J"), CascadeStack.newStack().pushInt(1000)));
        assertEquals(5, queue.size());
        assertEquals(5, queue.getOverflowCount());

        assertEquals(5, queue.size());
        queue.removeOldest(event, stack);
        assertEquals("A", event.get().name());
        assertEquals(100, stack.get().peekAsInt());
        queue.removeOldest(event, stack);
        assertEquals("B", event.get().name());
        assertEquals(200, stack.get().peekAsInt());
        queue.removeOldest(event, stack);
        assertEquals("C", event.get().name());
        assertEquals(300, stack.get().peekAsInt());
        queue.removeOldest(event, stack);
        assertEquals("D", event.get().name());
        assertEquals(400, stack.get().peekAsInt());
        queue.removeOldest(event, stack);
        assertEquals("J", event.get().name());
        assertEquals(1000, stack.get().peekAsInt());
    }

    /**
     * Test: 20180320002726081311
     *
     * <p>
     * Method: <code>offer</code>
     * </p>
     *
     * <p>
     * Case: Overflow, Drop Oldest
     * </p>
     */
    @Test
    public void test20180320002726081311 ()
    {
        System.out.println("Test: 20180320002726081311");

        queue = new BoundedInflowQueue(BoundedInflowQueue.OverflowPolicy.DROP_OLDEST, delegate);

        assertEquals(0, queue.getOverflowCount());
        assertEquals(0, queue.size());
        assertTrue(queue.offer(CascadeToken.token("A"), CascadeStack.newStack().pushInt(100)));
        assertEquals(1, queue.size());
        assertTrue(queue.offer(CascadeToken.token("B"), CascadeStack.newStack().pushInt(200)));
        assertEquals(2, queue.size());
        assertTrue(queue.offer(CascadeToken.token("C"), CascadeStack.newStack().pushInt(300)));
        assertEquals(3, queue.size());
        assertTrue(queue.offer(CascadeToken.token("D"), CascadeStack.newStack().pushInt(400)));
        assertEquals(4, queue.size());
        assertTrue(queue.offer(CascadeToken.token("E"), CascadeStack.newStack().pushInt(500)));
        assertEquals(5, queue.size());
        assertTrue(queue.offer(CascadeToken.token("F"), CascadeStack.newStack().pushInt(600)));
        assertEquals(5, queue.size());
        assertTrue(queue.offer(CascadeToken.token("G"), CascadeStack.newStack().pushInt(700)));
        assertEquals(5, queue.size());
        assertTrue(queue.offer(CascadeToken.token("H"), CascadeStack.newStack().pushInt(800)));
        assertEquals(5, queue.size());
        assertTrue(queue.offer(CascadeToken.token("I"), CascadeStack.newStack().pushInt(900)));
        assertEquals(5, queue.size());
        assertTrue(queue.offer(CascadeToken.token("J"), CascadeStack.newStack().pushInt(1000)));
        assertEquals(5, queue.size());
        assertEquals(5, queue.getOverflowCount());

        assertEquals(5, queue.size());
        queue.removeOldest(event, stack);
        assertEquals("F", event.get().name());
        assertEquals(600, stack.get().peekAsInt());
        queue.removeOldest(event, stack);
        assertEquals("G", event.get().name());
        assertEquals(700, stack.get().peekAsInt());
        queue.removeOldest(event, stack);
        assertEquals("H", event.get().name());
        assertEquals(800, stack.get().peekAsInt());
        queue.removeOldest(event, stack);
        assertEquals("I", event.get().name());
        assertEquals(900, stack.get().peekAsInt());
        queue.removeOldest(event, stack);
        assertEquals("J", event.get().name());
        assertEquals(1000, stack.get().peekAsInt());
    }

    /**
     * Test: 20180320003347425449
     *
     * <p>
     * Method: <code>offer</code>
     * </p>
     *
     * <p>
     * Case: Overflow, Drop Incoming
     * </p>
     */
    @Test
    public void test20180320003347425449 ()
    {
        System.out.println("Test: 20180320003347425449");

        queue = new BoundedInflowQueue(BoundedInflowQueue.OverflowPolicy.DROP_INCOMING, delegate);

        assertEquals(0, queue.getOverflowCount());
        assertEquals(0, queue.size());
        assertTrue(queue.offer(CascadeToken.token("A"), CascadeStack.newStack().pushInt(100)));
        assertEquals(1, queue.size());
        assertTrue(queue.offer(CascadeToken.token("B"), CascadeStack.newStack().pushInt(200)));
        assertEquals(2, queue.size());
        assertTrue(queue.offer(CascadeToken.token("C"), CascadeStack.newStack().pushInt(300)));
        assertEquals(3, queue.size());
        assertTrue(queue.offer(CascadeToken.token("D"), CascadeStack.newStack().pushInt(400)));
        assertEquals(4, queue.size());
        assertTrue(queue.offer(CascadeToken.token("E"), CascadeStack.newStack().pushInt(500)));
        assertEquals(5, queue.size());
        assertFalse(queue.offer(CascadeToken.token("F"), CascadeStack.newStack().pushInt(600)));
        assertEquals(5, queue.size());
        assertFalse(queue.offer(CascadeToken.token("G"), CascadeStack.newStack().pushInt(700)));
        assertEquals(5, queue.size());
        assertFalse(queue.offer(CascadeToken.token("H"), CascadeStack.newStack().pushInt(800)));
        assertEquals(5, queue.size());
        assertFalse(queue.offer(CascadeToken.token("I"), CascadeStack.newStack().pushInt(900)));
        assertEquals(5, queue.size());
        assertFalse(queue.offer(CascadeToken.token("J"), CascadeStack.newStack().pushInt(1000)));
        assertEquals(5, queue.size());
        assertEquals(5, queue.getOverflowCount());

        assertEquals(5, queue.size());
        queue.removeOldest(event, stack);
        assertEquals("A", event.get().name());
        assertEquals(100, stack.get().peekAsInt());
        queue.removeOldest(event, stack);
        assertEquals("B", event.get().name());
        assertEquals(200, stack.get().peekAsInt());
        queue.removeOldest(event, stack);
        assertEquals("C", event.get().name());
        assertEquals(300, stack.get().peekAsInt());
        queue.removeOldest(event, stack);
        assertEquals("D", event.get().name());
        assertEquals(400, stack.get().peekAsInt());
        queue.removeOldest(event, stack);
        assertEquals("E", event.get().name());
        assertEquals(500, stack.get().peekAsInt());
    }

    /**
     * Test: 20180318171426295782
     *
     * <p>
     * Method: <code>offer</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20180318171426295782 ()
    {
        System.out.println("Test: 20180318171426295782");

        assertEquals(0, queue.size());
        assertTrue(queue.offer(CascadeToken.token("A"), CascadeStack.newStack().pushInt(100)));
        assertEquals(1, queue.size());
        assertTrue(queue.offer(CascadeToken.token("B"), CascadeStack.newStack().pushInt(200)));
        assertEquals(2, queue.size());
        assertTrue(queue.offer(CascadeToken.token("C"), CascadeStack.newStack().pushInt(300)));
        assertEquals(3, queue.size());
        assertTrue(queue.offer(CascadeToken.token("D"), CascadeStack.newStack().pushInt(400)));
        assertEquals(4, queue.size());
        assertTrue(queue.offer(CascadeToken.token("E"), CascadeStack.newStack().pushInt(500)));
        assertEquals(5, queue.size());
        assertFalse(queue.offer(CascadeToken.token("F"), CascadeStack.newStack().pushInt(600)));
        assertEquals(5, queue.size());
    }

    /**
     * Test: 20180318175414337676
     *
     * <p>
     * Method: <code>offer</code>
     * </p>
     *
     * <p>
     * Case: Null Event
     * </p>
     */
    @Test (expected = NullPointerException.class)
    public void test20180318175414337676 ()
    {
        System.out.println("Test: 20180318175414337676");
        queue.offer(null, CascadeStack.newStack().pushInt(100));
    }

    /**
     * Test: 20180318175414337756
     *
     * <p>
     * Method: <code>offer</code>
     * </p>
     *
     * <p>
     * Case: Null Stack
     * </p>
     */
    @Test (expected = NullPointerException.class)
    public void test20180318175414337756 ()
    {
        System.out.println("Test: 20180318175414337756");
        queue.offer(CascadeToken.token("A"), null);
    }

    /**
     * Test: 20180318181222055125
     *
     * <p>
     * Method: <code>pollNewest</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20180318181222055125 ()
    {
        System.out.println("Test: 20180318181222055125");

        assertTrue(queue.offer(CascadeToken.token("A"), CascadeStack.newStack().pushInt(100)));
        assertTrue(queue.offer(CascadeToken.token("B"), CascadeStack.newStack().pushInt(200)));
        assertTrue(queue.offer(CascadeToken.token("C"), CascadeStack.newStack().pushInt(300)));
        assertTrue(queue.offer(CascadeToken.token("D"), CascadeStack.newStack().pushInt(400)));
        assertTrue(queue.offer(CascadeToken.token("E"), CascadeStack.newStack().pushInt(500)));
        assertEquals(5, queue.size());

        assertTrue(queue.removeNewest(event, stack));
        assertEquals("E", event.get().name());
        assertEquals(500, stack.get().peekAsInt());
        assertEquals(4, queue.size());

        assertTrue(queue.removeNewest(event, stack));
        assertEquals("D", event.get().name());
        assertEquals(400, stack.get().peekAsInt());
        assertEquals(3, queue.size());

        assertTrue(queue.removeNewest(event, stack));
        assertEquals("C", event.get().name());
        assertEquals(300, stack.get().peekAsInt());
        assertEquals(2, queue.size());

        assertTrue(queue.removeNewest(event, stack));
        assertEquals("B", event.get().name());
        assertEquals(200, stack.get().peekAsInt());
        assertEquals(1, queue.size());

        assertTrue(queue.removeNewest(event, stack));
        assertEquals("A", event.get().name());
        assertEquals(100, stack.get().peekAsInt());
        assertEquals(0, queue.size());

        assertFalse(queue.removeNewest(event, stack));
        assertNull(event.get());
        assertNull(stack.get());
        assertEquals(0, queue.size());
    }

    /**
     * Test: 20180318181222055214
     *
     * <p>
     * Method: <code>pollNewest</code>
     * </p>
     *
     * <p>
     * Case: Null Event
     * </p>
     */
    @Test (expected = NullPointerException.class)
    public void test20180318181222055214 ()
    {
        System.out.println("Test: 20180318181222055214");
        queue.removeNewest(null, stack);
    }

    /**
     * Test: 20180318181222055246
     *
     * <p>
     * Method: <code>pollNewest</code>
     * </p>
     *
     * <p>
     * Case: Null Stack
     * </p>
     */
    @Test (expected = NullPointerException.class)
    public void test20180318181222055246 ()
    {
        System.out.println("Test: 20180318181222055246");
        queue.removeNewest(event, null);
    }

    /**
     * Test: 20180318181222055276
     *
     * <p>
     * Method: <code>pollOldest</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20180318181222055276 ()
    {
        System.out.println("Test: 20180318181222055276");

        assertTrue(queue.offer(CascadeToken.token("A"), CascadeStack.newStack().pushInt(100)));
        assertTrue(queue.offer(CascadeToken.token("B"), CascadeStack.newStack().pushInt(200)));
        assertTrue(queue.offer(CascadeToken.token("C"), CascadeStack.newStack().pushInt(300)));
        assertTrue(queue.offer(CascadeToken.token("D"), CascadeStack.newStack().pushInt(400)));
        assertTrue(queue.offer(CascadeToken.token("E"), CascadeStack.newStack().pushInt(500)));
        assertEquals(5, queue.size());

        assertTrue(queue.removeOldest(event, stack));
        assertEquals("A", event.get().name());
        assertEquals(100, stack.get().peekAsInt());
        assertEquals(4, queue.size());

        assertTrue(queue.removeOldest(event, stack));
        assertEquals("B", event.get().name());
        assertEquals(200, stack.get().peekAsInt());
        assertEquals(3, queue.size());

        assertTrue(queue.removeOldest(event, stack));
        assertEquals("C", event.get().name());
        assertEquals(300, stack.get().peekAsInt());
        assertEquals(2, queue.size());

        assertTrue(queue.removeOldest(event, stack));
        assertEquals("D", event.get().name());
        assertEquals(400, stack.get().peekAsInt());
        assertEquals(1, queue.size());

        assertTrue(queue.removeOldest(event, stack));
        assertEquals("E", event.get().name());
        assertEquals(500, stack.get().peekAsInt());
        assertEquals(0, queue.size());

        assertFalse(queue.removeOldest(event, stack));
        assertNull(event.get());
        assertNull(stack.get());
        assertEquals(0, queue.size());
    }

    /**
     * Test: 20180318181222055305
     *
     * <p>
     * Method: <code>pollOldest</code>
     * </p>
     *
     * <p>
     * Case: Null Event
     * </p>
     */
    @Test (expected = NullPointerException.class)
    public void test20180318181222055305 ()
    {
        System.out.println("Test: 20180318181222055305");
        queue.removeOldest(null, stack);
    }

    /**
     * Test: 20180318184611815574
     *
     * <p>
     * Method: <code>pollOldest</code>
     * </p>
     *
     * <p>
     * Case: Null Stack
     * </p>
     */
    @Test (expected = NullPointerException.class)
    public void test20180318184611815574 ()
    {
        System.out.println("Test: 20180318184611815574");
        queue.removeOldest(event, null);
    }

    /**
     * Test: 20180318191113468824
     *
     * <p>
     * Method: <code>clear</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20180318191113468824 ()
    {
        System.out.println("Test: 20180318191113468824");

        assertTrue(queue.offer(CascadeToken.token("A"), CascadeStack.newStack().pushInt(100)));
        assertTrue(queue.offer(CascadeToken.token("B"), CascadeStack.newStack().pushInt(200)));
        assertTrue(queue.offer(CascadeToken.token("C"), CascadeStack.newStack().pushInt(300)));
        assertTrue(queue.offer(CascadeToken.token("D"), CascadeStack.newStack().pushInt(400)));
        assertTrue(queue.offer(CascadeToken.token("E"), CascadeStack.newStack().pushInt(500)));
        assertEquals(5, queue.size());

        queue.clear(); // Method Under Test

        assertEquals(0, queue.size());

        queue.clear(); // Method Under Test

        assertEquals(0, queue.size());
    }

    /**
     * Test: 20180318191113468902
     *
     * <p>
     * Method: <code>size</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20180318191113468902 ()
    {
        System.out.println("Test: 20180318191113468902");

        assertEquals(0, queue.size());
        assertTrue(queue.offer(CascadeToken.token("A"), CascadeStack.newStack().pushInt(100)));
        assertEquals(1, queue.size());
        assertTrue(queue.offer(CascadeToken.token("B"), CascadeStack.newStack().pushInt(200)));
        assertEquals(2, queue.size());
        assertTrue(queue.offer(CascadeToken.token("C"), CascadeStack.newStack().pushInt(300)));
        assertEquals(3, queue.size());
        assertTrue(queue.offer(CascadeToken.token("D"), CascadeStack.newStack().pushInt(400)));
        assertEquals(4, queue.size());
        assertTrue(queue.offer(CascadeToken.token("E"), CascadeStack.newStack().pushInt(500)));
        assertEquals(5, queue.size());
    }

    /**
     * Test: 20180318191113468930
     *
     * <p>
     * Method: <code>isEmpty</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20180318191113468930 ()
    {
        System.out.println("Test: 20180318191113468930");

        assertTrue(queue.isEmpty());
        assertTrue(queue.offer(CascadeToken.token("A"), CascadeStack.newStack().pushInt(100)));
        assertFalse(queue.isEmpty());
        assertTrue(queue.offer(CascadeToken.token("B"), CascadeStack.newStack().pushInt(200)));
        assertFalse(queue.isEmpty());
        assertTrue(queue.offer(CascadeToken.token("C"), CascadeStack.newStack().pushInt(300)));
        assertFalse(queue.isEmpty());
        assertTrue(queue.offer(CascadeToken.token("D"), CascadeStack.newStack().pushInt(400)));
        assertFalse(queue.isEmpty());
        assertTrue(queue.offer(CascadeToken.token("E"), CascadeStack.newStack().pushInt(500)));
        assertFalse(queue.isEmpty());
        queue.clear();
        assertTrue(queue.isEmpty());
    }

    /**
     * Test: 20180318191932911422
     *
     * <p>
     * Method: <code>forEach</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20180318191932911422 ()
    {
        System.out.println("Test: 20180318191932911422");

        final List<CascadeToken> events = Lists.newLinkedList();
        final List<CascadeStack> stacks = Lists.newLinkedList();

        final BiConsumer<CascadeToken, CascadeStack> functor = (x, y) ->
        {
            events.add(x);
            stacks.add(y);
        };

        assertTrue(events.isEmpty());
        assertTrue(stacks.isEmpty());
        queue.forEach(functor);
        assertTrue(events.isEmpty());
        assertTrue(stacks.isEmpty());

        assertTrue(queue.offer(CascadeToken.token("A"), CascadeStack.newStack().pushInt(100)));
        assertTrue(queue.offer(CascadeToken.token("B"), CascadeStack.newStack().pushInt(200)));
        assertTrue(queue.offer(CascadeToken.token("C"), CascadeStack.newStack().pushInt(300)));
        assertTrue(queue.offer(CascadeToken.token("D"), CascadeStack.newStack().pushInt(400)));
        assertTrue(queue.offer(CascadeToken.token("E"), CascadeStack.newStack().pushInt(500)));
        assertEquals(5, queue.size());

        assertTrue(events.isEmpty());
        assertTrue(stacks.isEmpty());
        queue.forEach(functor);
        assertEquals(5, events.size());
        assertEquals(CascadeToken.token("A"), events.get(0));
        assertEquals(CascadeToken.token("B"), events.get(1));
        assertEquals(CascadeToken.token("C"), events.get(2));
        assertEquals(CascadeToken.token("D"), events.get(3));
        assertEquals(CascadeToken.token("E"), events.get(4));
        assertEquals(5, stacks.size());
        assertEquals(100, stacks.get(0).peekAsInt());
        assertEquals(200, stacks.get(1).peekAsInt());
        assertEquals(300, stacks.get(2).peekAsInt());
        assertEquals(400, stacks.get(3).peekAsInt());
        assertEquals(500, stacks.get(4).peekAsInt());
    }

    /**
     * Test: 20180318193714500384
     *
     * <p>
     * Method: <code>capacity</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20180318193714500384 ()
    {
        System.out.println("Test: 20180318193714500384");

        assertEquals(0, queue.size());
        assertEquals(5, queue.capacity());
        assertTrue(queue.offer(CascadeToken.token("A"), CascadeStack.newStack().pushInt(100)));
        assertEquals(1, queue.size());
        assertEquals(5, queue.capacity());
        assertTrue(queue.offer(CascadeToken.token("B"), CascadeStack.newStack().pushInt(200)));
        assertEquals(2, queue.size());
        assertEquals(5, queue.capacity());
        assertTrue(queue.offer(CascadeToken.token("C"), CascadeStack.newStack().pushInt(300)));
        assertEquals(3, queue.size());
        assertEquals(5, queue.capacity());
        assertTrue(queue.offer(CascadeToken.token("D"), CascadeStack.newStack().pushInt(400)));
        assertEquals(4, queue.size());
        assertEquals(5, queue.capacity());
        assertTrue(queue.offer(CascadeToken.token("E"), CascadeStack.newStack().pushInt(500)));
        assertEquals(5, queue.size());
        assertEquals(5, queue.capacity());
    }
}
