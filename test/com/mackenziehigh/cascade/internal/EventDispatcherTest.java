package com.mackenziehigh.cascade.internal;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Map;
import static junit.framework.Assert.*;
import org.junit.Test;

/**
 * Unit Test.
 */
public final class EventDispatcherTest
{
    /**
     * Test: 20171228222253469448
     *
     * <p>
     * Case: Registration and De-registration.
     * </p>
     */
    @Test
    public void test20171228222253469448 ()
    {
        System.out.println("Test: 20171228222253469448");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        final Map<CascadeToken, InflowQueue> reactors = Maps.newHashMap();
        reactors.put(CascadeToken.create("A"), new ArrayInflowQueue(allocator, 128));
        reactors.put(CascadeToken.create("B"), new ArrayInflowQueue(allocator, 128));
        reactors.put(CascadeToken.create("C"), new ArrayInflowQueue(allocator, 128));
        final EventDispatcher dispatcher = new EventDispatcher(reactors);

        /**
         * Tokens.
         */
        final CascadeToken Event1 = CascadeToken.create("Event1");
        final CascadeToken Event2 = CascadeToken.create("Event2");
        final CascadeToken Event3 = CascadeToken.create("Event3");
        final CascadeToken Event4 = CascadeToken.create("Event4");
        final CascadeToken Event5 = CascadeToken.create("Event5");

        /**
         * Subscribe.
         */
        dispatcher.register(CascadeToken.create("A"), Event1);
        dispatcher.register(CascadeToken.create("A"), Event2);
        dispatcher.register(CascadeToken.create("A"), Event3); // Duplicate
        dispatcher.register(CascadeToken.create("A"), Event3);
        dispatcher.register(CascadeToken.create("B"), Event1);
        dispatcher.register(CascadeToken.create("B"), Event3);
        dispatcher.register(CascadeToken.create("B"), Event4);
        dispatcher.register(CascadeToken.create("C"), Event2);
        dispatcher.register(CascadeToken.create("C"), Event4);
        dispatcher.register(CascadeToken.create("C"), Event5);

        /**
         * Verify Subscriptions.
         */
        assertEquals(ImmutableSet.of(Event1, Event2, Event3), dispatcher.subscriptionsOf(CascadeToken.create("A")));
        assertEquals(ImmutableSet.of(Event1, Event3, Event4), dispatcher.subscriptionsOf(CascadeToken.create("B")));
        assertEquals(ImmutableSet.of(Event2, Event4, Event5), dispatcher.subscriptionsOf(CascadeToken.create("C")));
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.create("Event1")).size());
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.create("Event2")).size());
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.create("Event3")).size());
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.create("Event4")).size());
        assertEquals(1, dispatcher.subscribersOf(CascadeToken.create("Event5")).size());
        assertTrue(dispatcher.subscribersOf(CascadeToken.create("Event1")).contains(CascadeToken.create("A")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.create("Event1")).contains(CascadeToken.create("B")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.create("Event2")).contains(CascadeToken.create("A")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.create("Event2")).contains(CascadeToken.create("C")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.create("Event3")).contains(CascadeToken.create("A")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.create("Event3")).contains(CascadeToken.create("B")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.create("Event4")).contains(CascadeToken.create("B")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.create("Event4")).contains(CascadeToken.create("C")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.create("Event5")).contains(CascadeToken.create("C")));

        /**
         * Unsubscribe and Verify.
         */
        dispatcher.deregister(CascadeToken.create("A"), CascadeToken.create("Event1"));
        assertEquals(ImmutableSet.of(Event2, Event3), dispatcher.subscriptionsOf(CascadeToken.create("A")));
        assertEquals(ImmutableSet.of(Event1, Event3, Event4), dispatcher.subscriptionsOf(CascadeToken.create("B")));
        assertEquals(ImmutableSet.of(Event2, Event4, Event5), dispatcher.subscriptionsOf(CascadeToken.create("C")));
        assertFalse(dispatcher.subscribersOf(CascadeToken.create("Event1")).contains(CascadeToken.create("A")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.create("Event1")).contains(CascadeToken.create("B")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.create("Event2")).contains(CascadeToken.create("A")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.create("Event2")).contains(CascadeToken.create("C")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.create("Event3")).contains(CascadeToken.create("A")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.create("Event3")).contains(CascadeToken.create("B")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.create("Event4")).contains(CascadeToken.create("B")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.create("Event4")).contains(CascadeToken.create("C")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.create("Event5")).contains(CascadeToken.create("C")));
        assertEquals(1, dispatcher.subscribersOf(CascadeToken.create("Event1")).size());
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.create("Event2")).size());
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.create("Event3")).size());
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.create("Event4")).size());
        assertEquals(1, dispatcher.subscribersOf(CascadeToken.create("Event5")).size());

        /**
         * Unsubscribe and Verify.
         */
        dispatcher.deregister(CascadeToken.create("B"), CascadeToken.create("Event1"));
        assertEquals(ImmutableSet.of(Event2, Event3), dispatcher.subscriptionsOf(CascadeToken.create("A")));
        assertEquals(ImmutableSet.of(Event3, Event4), dispatcher.subscriptionsOf(CascadeToken.create("B")));
        assertEquals(ImmutableSet.of(Event2, Event4, Event5), dispatcher.subscriptionsOf(CascadeToken.create("C")));
        assertFalse(dispatcher.subscribersOf(CascadeToken.create("Event1")).contains(CascadeToken.create("A")));
        assertFalse(dispatcher.subscribersOf(CascadeToken.create("Event1")).contains(CascadeToken.create("B")));
        assertEquals(0, dispatcher.subscribersOf(CascadeToken.create("Event1")).size());
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.create("Event2")).size());
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.create("Event3")).size());
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.create("Event4")).size());
        assertEquals(1, dispatcher.subscribersOf(CascadeToken.create("Event5")).size());

        /**
         * Unsubscribe and Verify. The event does no exist.
         */
        dispatcher.deregister(CascadeToken.create("B"), CascadeToken.create("EventX"));
        assertFalse(dispatcher.subscribersOf(CascadeToken.create("Event1")).contains(CascadeToken.create("A")));
        assertFalse(dispatcher.subscribersOf(CascadeToken.create("Event1")).contains(CascadeToken.create("B")));
        assertEquals(0, dispatcher.subscribersOf(CascadeToken.create("Event1")).size());
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.create("Event2")).size());
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.create("Event3")).size());
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.create("Event4")).size());
        assertEquals(1, dispatcher.subscribersOf(CascadeToken.create("Event5")).size());
    }

    /**
     * Test: 20171228222253469537
     *
     * <p>
     * Case: Throughput.
     * </p>
     */
    @Test
    public void test20171228222253469537 ()
    {
        System.out.println("Test: 20171228222253469537");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        allocator.addDynamicPool(CascadeToken.create("default"), 0, 128);
        final OperandStack stack = allocator.newOperandStack();
        final Map<CascadeToken, InflowQueue> reactors = Maps.newHashMap();
        reactors.put(CascadeToken.create("A"), new ArrayInflowQueue(allocator, 128));
        reactors.put(CascadeToken.create("B"), new ArrayInflowQueue(allocator, 128));
        reactors.put(CascadeToken.create("C"), new ArrayInflowQueue(allocator, 128));
        final EventDispatcher dispatcher = new EventDispatcher(reactors);

        /**
         * Subscribe.
         */
        dispatcher.register(CascadeToken.create("A"), CascadeToken.create("Event1"));
        dispatcher.register(CascadeToken.create("B"), CascadeToken.create("Event2"));
        dispatcher.register(CascadeToken.create("C"), CascadeToken.create("Event3"));

        /**
         * Send the messages.
         */
        dispatcher.lookup(CascadeToken.create("A")).broadcast(CascadeToken.create("Event1"), stack.clear().push("Vulcan"));
        dispatcher.lookup(CascadeToken.create("B")).broadcast(CascadeToken.create("Event2"), stack.clear().push("Earth"));
        dispatcher.lookup(CascadeToken.create("C")).broadcast(CascadeToken.create("Event3"), stack.clear().push("Caprica"));

        /**
         * Verify the transmissions.
         */
        assertEquals("Event1", reactors.get(CascadeToken.create("A")).poll(stack).name());
        assertEquals("Vulcan", stack.asString());
        //
        assertEquals("Event2", reactors.get(CascadeToken.create("B")).poll(stack).name());
        assertEquals("Earth", stack.asString());
        //
        assertEquals("Event3", reactors.get(CascadeToken.create("C")).poll(stack).name());
        assertEquals("Caprica", stack.asString());
    }

    /**
     * Test: 20171229010604023927
     *
     * <p>
     * Method: <code>register(*)</code>
     * </p>
     *
     * <p>
     * Case: No Such Subscriber.
     * </p>
     */
    @Test (expected = IllegalArgumentException.class)
    public void test20171229010604023927 ()
    {
        System.out.println("Test: 20171229010604023927");

        final ConcreteAllocator allocator = new ConcreteAllocator();
        allocator.addDynamicPool(CascadeToken.create("default"), 0, 128);
        final Map<CascadeToken, InflowQueue> reactors = Maps.newHashMap();
        reactors.put(CascadeToken.create("A"), new ArrayInflowQueue(allocator, 128));
        reactors.put(CascadeToken.create("B"), new ArrayInflowQueue(allocator, 128));
        reactors.put(CascadeToken.create("C"), new ArrayInflowQueue(allocator, 128));
        final EventDispatcher dispatcher = new EventDispatcher(reactors);

        /**
         * Method Under Test.
         */
        dispatcher.register(CascadeToken.create("Z"), CascadeToken.create("Event1"));
    }
}
