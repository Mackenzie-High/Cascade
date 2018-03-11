package com.mackenziehigh.cascade.old.internal;

import com.mackenziehigh.cascade.old.internal.InflowQueue;
import com.mackenziehigh.cascade.old.internal.ConcreteAllocator;
import com.mackenziehigh.cascade.old.internal.ArrayInflowQueue;
import com.mackenziehigh.cascade.old.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static junit.framework.Assert.*;
import org.junit.Test;

/**
 * Unit Test.
 */
public final class ArrayInflowQueueTest
{
    private final ConcreteAllocator allocator = new ConcreteAllocator();


    {
        allocator.addDynamicPool(CascadeToken.create("default"), 0, Integer.MAX_VALUE);
    }

    /**
     * Test: 20171228012747604559
     *
     * <p>
     * Normal Cases.
     * </p>
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void test20171228012747604559 ()
            throws InterruptedException
    {
        System.out.println("Test: 20171228012747604559");

        final AtomicInteger callbacks = new AtomicInteger();
        final OperandStack message = allocator.newOperandStack();
        final InflowQueue inflow = new ArrayInflowQueue(allocator, 3);
        inflow.setCallback(x -> callbacks.incrementAndGet());
        Object accessKey;

        /**
         * Verify initial state.
         */
        assertEquals(0, callbacks.get());
        assertEquals(0, inflow.size());
        assertEquals(3, inflow.capacity());

        /**
         * Create a message to send.
         */
        message.push("Mercury").push("Venus");

        /**
         * Begin a transaction.
         */
        accessKey = inflow.lock(1, TimeUnit.MILLISECONDS);
        assertNotNull(accessKey);
        assertEquals(0, callbacks.get());
        assertEquals(0, inflow.size());
        assertEquals(3, inflow.capacity());

        /**
         * Since a transaction is in-progress,
         * no other transactions can start.
         */
        assertNull(inflow.lock(1, TimeUnit.MILLISECONDS)); // 1 Ms => Do *not* block in a unit-test.
        assertNull(inflow.lock(1, TimeUnit.MILLISECONDS));
        assertNull(inflow.lock(1, TimeUnit.MILLISECONDS));
        assertNull(inflow.lock(1, TimeUnit.MILLISECONDS));

        /**
         * Try committing the transaction with a null access-key,
         * which should be a no-op.
         */
        assertEquals(0, callbacks.get());
        assertEquals(0, inflow.size());
        assertEquals(3, inflow.capacity());
        inflow.commit(null, CascadeToken.create("Event1"), message);
        assertEquals(0, callbacks.get());
        assertEquals(0, inflow.size());
        assertEquals(3, inflow.capacity());

        /**
         * Commit the transaction (i.e. send the message).
         */
        assertEquals(0, callbacks.get());
        assertEquals(0, inflow.size());
        assertEquals(3, inflow.capacity());
        inflow.commit(accessKey, CascadeToken.create("Event2"), message);
        assertEquals(1, callbacks.get());
        assertEquals(1, inflow.size());
        assertEquals(3, inflow.capacity());

        /**
         * Since a transaction is *still* in-progress,
         * no other transactions can start.
         */
        assertNull(inflow.lock(1, TimeUnit.MILLISECONDS));
        assertNull(inflow.lock(1, TimeUnit.MILLISECONDS));
        assertNull(inflow.lock(1, TimeUnit.MILLISECONDS));
        assertNull(inflow.lock(1, TimeUnit.MILLISECONDS));

        /**
         * Try ending the transaction with a null access-key,
         * which should be a no-op.
         */
        assertEquals(1, callbacks.get());
        assertEquals(1, inflow.size());
        assertEquals(3, inflow.capacity());
        inflow.unlock(null);
        assertEquals(1, callbacks.get());
        assertEquals(1, inflow.size());
        assertEquals(3, inflow.capacity());

        /**
         * End the transaction.
         */
        assertEquals(1, callbacks.get());
        assertEquals(1, inflow.size());
        assertEquals(3, inflow.capacity());
        inflow.unlock(accessKey);
        assertEquals(1, callbacks.get());
        assertEquals(1, inflow.size());
        assertEquals(3, inflow.capacity());

        /**
         * Add another message to the queue.
         */
        message.clear().push("Earth").push("Mars");
        accessKey = inflow.lock(1, TimeUnit.MILLISECONDS);
        assertNotNull(accessKey);
        inflow.commit(accessKey, CascadeToken.create("Event3"), message);
        inflow.unlock(accessKey);
        assertEquals(2, callbacks.get());
        assertEquals(2, inflow.size());
        assertEquals(3, inflow.capacity());

        /**
         * Add another message to the queue.
         * Then, the queue will be full.
         */
        message.clear().push("Jupiter").push("Saturn");
        accessKey = inflow.lock(1, TimeUnit.MILLISECONDS);
        assertNotNull(accessKey);
        inflow.commit(accessKey, CascadeToken.create("Event4"), message);
        inflow.unlock(accessKey);
        assertEquals(3, callbacks.get());
        assertEquals(3, inflow.size());
        assertEquals(3, inflow.capacity());

        /**
         * Try adding another message to the queue, which is already full.
         * This should fail, because the capacity has been reached.
         */
        message.clear().push("Uranus").push("Neptune");
        accessKey = inflow.lock(1, TimeUnit.MILLISECONDS);
        assertNull(accessKey);
        assertEquals(3, callbacks.get());
        assertEquals(3, inflow.size());
        assertEquals(3, inflow.capacity());

        /**
         * Dequeue a message.
         */
        message.clear();
        assertEquals(CascadeToken.create("Event2"), inflow.poll(message));
        assertEquals(3, callbacks.get());
        assertEquals(2, inflow.size());
        assertEquals(3, inflow.capacity());
        assertEquals("Venus", message.asString());
        message.pop();
        assertEquals("Mercury", message.asString());
        message.pop();

        /**
         * Add another message to the queue using the nullary lock() method.
         * Then, the queue will be full again.
         */
        message.clear().push("Pluto").push("Vulcan");
        accessKey = inflow.lock();
        assertNotNull(accessKey);
        inflow.commit(accessKey, CascadeToken.create("Event5"), message);
        inflow.unlock(accessKey);
        assertEquals(4, callbacks.get());
        assertEquals(3, inflow.size());
        assertEquals(3, inflow.capacity());

        /**
         * Try to add another message to the queue using the nullary lock() method.
         * This should fail, because the queue is already full.
         */
        message.clear().push("Andoria").push("Caprica");
        accessKey = inflow.lock();
        assertNull(accessKey);
        assertEquals(4, callbacks.get());
        assertEquals(3, inflow.size());
        assertEquals(3, inflow.capacity());

        /**
         * Dequeue the next message and verify its contents.
         */
        message.clear();
        assertEquals(CascadeToken.create("Event3"), inflow.poll(message));
        assertEquals(4, callbacks.get());
        assertEquals(2, inflow.size());
        assertEquals(3, inflow.capacity());
        assertEquals("Mars", message.asString());
        message.pop();
        assertEquals("Earth", message.asString());
        message.pop();

        /**
         * Dequeue the next message and verify its contents.
         */
        message.clear();
        assertEquals(CascadeToken.create("Event4"), inflow.poll(message));
        assertEquals(4, callbacks.get());
        assertEquals(1, inflow.size());
        assertEquals(3, inflow.capacity());
        assertEquals("Saturn", message.asString());
        message.pop();
        assertEquals("Jupiter", message.asString());
        message.pop();

        /**
         * Dequeue the next message and verify its contents.
         */
        message.clear();
        assertEquals(CascadeToken.create("Event5"), inflow.poll(message));
        assertEquals(4, callbacks.get());
        assertEquals(0, inflow.size());
        assertEquals(3, inflow.capacity());
        assertEquals("Vulcan", message.asString());
        message.pop();
        assertEquals("Pluto", message.asString());
        message.pop();

        /**
         * Try to dequeue the next message, which should fail,
         * because the queue is currently empty.
         */
        message.clear();
        assertEquals(null, inflow.poll(message));
        assertEquals(4, callbacks.get());
        assertEquals(0, inflow.size());
        assertEquals(3, inflow.capacity());
        assertTrue(message.isStackEmpty());

        /**
         * Begin a transaction using the nullary lock() method.
         */
        accessKey = inflow.lock();
        assertNotNull(accessKey);
        assertEquals(4, callbacks.get());
        assertEquals(0, inflow.size());
        assertEquals(3, inflow.capacity());

        /**
         * Since a transaction is in-progress,
         * no other transactions can start.
         */
        assertNull(inflow.lock(1, TimeUnit.MILLISECONDS)); // 1 Ms => Do *not* block in a unit-test.
        assertNull(inflow.lock(1, TimeUnit.MILLISECONDS));
        assertNull(inflow.lock());
        assertNull(inflow.lock());

        /**
         * Commit the message.
         */
        message.clear().push("Vega");
        inflow.commit(accessKey, CascadeToken.create("Event6"), message);
        assertEquals(5, callbacks.get());
        assertEquals(1, inflow.size());
        assertEquals(3, inflow.capacity());

        /**
         * End the transaction.
         */
        assertEquals(5, callbacks.get());
        assertEquals(1, inflow.size());
        assertEquals(3, inflow.capacity());
        inflow.unlock(accessKey);
        assertEquals(5, callbacks.get());
        assertEquals(1, inflow.size());
        assertEquals(3, inflow.capacity());

        /**
         * Dequeue the next message and verify its contents.
         */
        message.clear();
        assertEquals(CascadeToken.create("Event6"), inflow.poll(message));
        assertEquals(5, callbacks.get());
        assertEquals(0, inflow.size());
        assertEquals(3, inflow.capacity());
        assertEquals("Vega", message.asString());
        message.pop();
    }

    /**
     * Test: 20171228012924826340
     *
     * <p>
     * Method: <code>commit()</code>
     * </p>
     *
     * <p>
     * Case: Wrong Key
     * </p>
     */
    @Test (expected = IllegalArgumentException.class)
    public void test20171228012924826340 ()
    {
        System.out.println("Test: 20171228012924826340");

        final AtomicInteger callbacks = new AtomicInteger();
        final OperandStack message = allocator.newOperandStack();
        final InflowQueue inflow = new ArrayInflowQueue(allocator, 3);
        inflow.setCallback(x -> callbacks.incrementAndGet());

        assertNotNull(inflow.lock());

        /**
         * This was not obtained from the lock() method.
         */
        final Object accessKey = new Object();

        inflow.commit(accessKey, CascadeToken.create("Event1"), message);
    }

    /**
     * Test: 20171228012924826372
     *
     * <p>
     * Method: <code>unlock()</code>
     * </p>
     *
     * <p>
     * Case: Wrong Key
     * </p>
     */
    @Test (expected = IllegalArgumentException.class)
    public void test20171228012924826372 ()
    {
        System.out.println("Test: 20171228012924826372");

        final AtomicInteger callbacks = new AtomicInteger();
        final InflowQueue inflow = new ArrayInflowQueue(allocator, 3);
        inflow.setCallback(x -> callbacks.incrementAndGet());

        assertNotNull(inflow.lock());

        /**
         * This was not obtained from the lock() method.
         */
        final Object accessKey = new Object();

        inflow.unlock(accessKey);
    }

    /**
     * Test: 20171228012924826400
     *
     * <p>
     * Method: <code>close()</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20171228012924826400 ()
    {
        System.out.println("Test: 20171228012924826400");

        final ConcreteAllocator alloc = new ConcreteAllocator();
        alloc.addFixedPool(CascadeToken.create("default"), 0, 100, 10);

        final AtomicInteger callbacks = new AtomicInteger();
        final OperandStack message = alloc.newOperandStack();
        final InflowQueue inflow = new ArrayInflowQueue(alloc, 3);
        inflow.setCallback(x -> callbacks.incrementAndGet());
        Object accessKey;

        /**
         * Add a message to the queue, which will consume
         * two buffers in the default allocation-pool.
         */
        assertEquals(0, alloc.defaultPool().size().getAsLong());
        message.clear().push("Earth").push("Mars");
        accessKey = inflow.lock();
        assertNotNull(accessKey);
        inflow.commit(accessKey, CascadeToken.create("Event1"), message);
        inflow.unlock(accessKey);

        /**
         * Clear the message first, since it also holds references.
         */
        message.clear();

        /**
         * Method Under Test.
         */
        assertEquals(2, alloc.defaultPool().size().getAsLong());
        inflow.close();
        assertEquals(0, alloc.defaultPool().size().getAsLong());
    }
}
