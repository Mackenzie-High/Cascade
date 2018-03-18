package com.mackenziehigh.cascade.old.internal;

import com.mackenziehigh.cascade.old.internal.EventDispatcher;
import com.mackenziehigh.cascade.old.internal.InflowQueue;
import com.mackenziehigh.cascade.old.internal.ConcreteAllocator;
import com.mackenziehigh.cascade.old.internal.ArrayInflowQueue;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mackenziehigh.cascade.old.CascadeAllocator.OperandStack;
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
        reactors.put(CascadeToken.token("A"), new ArrayInflowQueue(allocator, 128));
        reactors.put(CascadeToken.token("B"), new ArrayInflowQueue(allocator, 128));
        reactors.put(CascadeToken.token("C"), new ArrayInflowQueue(allocator, 128));
        final EventDispatcher dispatcher = new EventDispatcher(reactors);

        /**
         * Tokens.
         */
        final CascadeToken Event1 = CascadeToken.token("Event1");
        final CascadeToken Event2 = CascadeToken.token("Event2");
        final CascadeToken Event3 = CascadeToken.token("Event3");
        final CascadeToken Event4 = CascadeToken.token("Event4");
        final CascadeToken Event5 = CascadeToken.token("Event5");

        /**
         * Subscribe.
         */
        dispatcher.register(CascadeToken.token("A"), Event1);
        dispatcher.register(CascadeToken.token("A"), Event2);
        dispatcher.register(CascadeToken.token("A"), Event3); // Duplicate
        dispatcher.register(CascadeToken.token("A"), Event3);
        dispatcher.register(CascadeToken.token("B"), Event1);
        dispatcher.register(CascadeToken.token("B"), Event3);
        dispatcher.register(CascadeToken.token("B"), Event4);
        dispatcher.register(CascadeToken.token("C"), Event2);
        dispatcher.register(CascadeToken.token("C"), Event4);
        dispatcher.register(CascadeToken.token("C"), Event5);

        /**
         * Verify Subscriptions.
         */
        assertEquals(ImmutableSet.of(Event1, Event2, Event3), dispatcher.subscriptionsOf(CascadeToken.token("A")));
        assertEquals(ImmutableSet.of(Event1, Event3, Event4), dispatcher.subscriptionsOf(CascadeToken.token("B")));
        assertEquals(ImmutableSet.of(Event2, Event4, Event5), dispatcher.subscriptionsOf(CascadeToken.token("C")));
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.token("Event1")).size());
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.token("Event2")).size());
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.token("Event3")).size());
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.token("Event4")).size());
        assertEquals(1, dispatcher.subscribersOf(CascadeToken.token("Event5")).size());
        assertTrue(dispatcher.subscribersOf(CascadeToken.token("Event1")).contains(CascadeToken.token("A")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.token("Event1")).contains(CascadeToken.token("B")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.token("Event2")).contains(CascadeToken.token("A")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.token("Event2")).contains(CascadeToken.token("C")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.token("Event3")).contains(CascadeToken.token("A")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.token("Event3")).contains(CascadeToken.token("B")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.token("Event4")).contains(CascadeToken.token("B")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.token("Event4")).contains(CascadeToken.token("C")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.token("Event5")).contains(CascadeToken.token("C")));

        /**
         * Unsubscribe and Verify.
         */
        dispatcher.deregister(CascadeToken.token("A"), CascadeToken.token("Event1"));
        assertEquals(ImmutableSet.of(Event2, Event3), dispatcher.subscriptionsOf(CascadeToken.token("A")));
        assertEquals(ImmutableSet.of(Event1, Event3, Event4), dispatcher.subscriptionsOf(CascadeToken.token("B")));
        assertEquals(ImmutableSet.of(Event2, Event4, Event5), dispatcher.subscriptionsOf(CascadeToken.token("C")));
        assertFalse(dispatcher.subscribersOf(CascadeToken.token("Event1")).contains(CascadeToken.token("A")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.token("Event1")).contains(CascadeToken.token("B")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.token("Event2")).contains(CascadeToken.token("A")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.token("Event2")).contains(CascadeToken.token("C")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.token("Event3")).contains(CascadeToken.token("A")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.token("Event3")).contains(CascadeToken.token("B")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.token("Event4")).contains(CascadeToken.token("B")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.token("Event4")).contains(CascadeToken.token("C")));
        assertTrue(dispatcher.subscribersOf(CascadeToken.token("Event5")).contains(CascadeToken.token("C")));
        assertEquals(1, dispatcher.subscribersOf(CascadeToken.token("Event1")).size());
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.token("Event2")).size());
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.token("Event3")).size());
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.token("Event4")).size());
        assertEquals(1, dispatcher.subscribersOf(CascadeToken.token("Event5")).size());

        /**
         * Unsubscribe and Verify.
         */
        dispatcher.deregister(CascadeToken.token("B"), CascadeToken.token("Event1"));
        assertEquals(ImmutableSet.of(Event2, Event3), dispatcher.subscriptionsOf(CascadeToken.token("A")));
        assertEquals(ImmutableSet.of(Event3, Event4), dispatcher.subscriptionsOf(CascadeToken.token("B")));
        assertEquals(ImmutableSet.of(Event2, Event4, Event5), dispatcher.subscriptionsOf(CascadeToken.token("C")));
        assertFalse(dispatcher.subscribersOf(CascadeToken.token("Event1")).contains(CascadeToken.token("A")));
        assertFalse(dispatcher.subscribersOf(CascadeToken.token("Event1")).contains(CascadeToken.token("B")));
        assertEquals(0, dispatcher.subscribersOf(CascadeToken.token("Event1")).size());
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.token("Event2")).size());
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.token("Event3")).size());
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.token("Event4")).size());
        assertEquals(1, dispatcher.subscribersOf(CascadeToken.token("Event5")).size());

        /**
         * Unsubscribe and Verify. The event does no exist.
         */
        dispatcher.deregister(CascadeToken.token("B"), CascadeToken.token("EventX"));
        assertFalse(dispatcher.subscribersOf(CascadeToken.token("Event1")).contains(CascadeToken.token("A")));
        assertFalse(dispatcher.subscribersOf(CascadeToken.token("Event1")).contains(CascadeToken.token("B")));
        assertEquals(0, dispatcher.subscribersOf(CascadeToken.token("Event1")).size());
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.token("Event2")).size());
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.token("Event3")).size());
        assertEquals(2, dispatcher.subscribersOf(CascadeToken.token("Event4")).size());
        assertEquals(1, dispatcher.subscribersOf(CascadeToken.token("Event5")).size());
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
        allocator.addDynamicPool(CascadeToken.token("default"), 0, 128);
        final OperandStack stack = allocator.newOperandStack();
        final Map<CascadeToken, InflowQueue> reactors = Maps.newHashMap();
        reactors.put(CascadeToken.token("A"), new ArrayInflowQueue(allocator, 128));
        reactors.put(CascadeToken.token("B"), new ArrayInflowQueue(allocator, 128));
        reactors.put(CascadeToken.token("C"), new ArrayInflowQueue(allocator, 128));
        final EventDispatcher dispatcher = new EventDispatcher(reactors);

        /**
         * Subscribe.
         */
        dispatcher.register(CascadeToken.token("A"), CascadeToken.token("Event1"));
        dispatcher.register(CascadeToken.token("B"), CascadeToken.token("Event2"));
        dispatcher.register(CascadeToken.token("C"), CascadeToken.token("Event3"));

        /**
         * Send the messages.
         */
        dispatcher.lookup(CascadeToken.token("A")).broadcast(CascadeToken.token("Event1"), stack.clear().push("Vulcan"));
        dispatcher.lookup(CascadeToken.token("B")).broadcast(CascadeToken.token("Event2"), stack.clear().push("Earth"));
        dispatcher.lookup(CascadeToken.token("C")).broadcast(CascadeToken.token("Event3"), stack.clear().push("Caprica"));

        /**
         * Verify the transmissions.
         */
        assertEquals("Event1", reactors.get(CascadeToken.token("A")).poll(stack).name());
        assertEquals("Vulcan", stack.asString());
        //
        assertEquals("Event2", reactors.get(CascadeToken.token("B")).poll(stack).name());
        assertEquals("Earth", stack.asString());
        //
        assertEquals("Event3", reactors.get(CascadeToken.token("C")).poll(stack).name());
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
        allocator.addDynamicPool(CascadeToken.token("default"), 0, 128);
        final Map<CascadeToken, InflowQueue> reactors = Maps.newHashMap();
        reactors.put(CascadeToken.token("A"), new ArrayInflowQueue(allocator, 128));
        reactors.put(CascadeToken.token("B"), new ArrayInflowQueue(allocator, 128));
        reactors.put(CascadeToken.token("C"), new ArrayInflowQueue(allocator, 128));
        final EventDispatcher dispatcher = new EventDispatcher(reactors);

        /**
         * Method Under Test.
         */
        dispatcher.register(CascadeToken.token("Z"), CascadeToken.token("Event1"));
    }
}
