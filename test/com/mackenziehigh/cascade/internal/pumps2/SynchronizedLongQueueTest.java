package com.mackenziehigh.cascade.internal.pumps2;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import static junit.framework.Assert.*;
import org.junit.Test;

public final class SynchronizedLongQueueTest
{
    /**
     * Test: 20171122032828423331
     *
     * <p>
     * Case: Normal without clear()
     * </p>
     */
    @Test
    public void test20171122032828423331 ()
    {
        System.out.println("Test: 20171122032828423331");

        final int capacity = 8;
        final SynchronizedLongQueue queue1 = new SynchronizedLongQueue(capacity);
        final ArrayBlockingQueue<Long> queue2 = new ArrayBlockingQueue<>(capacity);
        final AtomicLong out = new AtomicLong();

        assertEquals(capacity, queue1.capacity());

        for (int c = 0; c < 4; c++)
        {
            for (int k = 0; k < capacity + 4; k++)
            {
                for (int i = 0; i <= k; i++)
                {
                    assertEquals(i < capacity, queue1.offer(i * 1000 + 1L));
                    assertEquals(i < capacity, queue2.offer(i * 1000 + 1L));
                    assertEquals(queue1.size(), queue2.size());
                    assertEquals(Math.min(i + 1, capacity), queue1.size());
                }

                for (int i = 0; i <= k; i++)
                {
                    out.set(0);
                    final boolean actual = queue1.poll(out);
                    final Long expected = queue2.poll();
                    assertEquals(expected != null, actual);
                    assertEquals((expected == null ? 0 : expected), out.get());
                    assertEquals(queue1.size(), queue2.size());
                }
            }
        }
    }

    /**
     * Test: 20171122032828423393
     *
     * <p>
     * Case: Normal with clear()
     * </p>
     */
    @Test
    public void test20171122032828423393 ()
    {
        System.out.println("Test: 20171122032828423393");

        final int capacity = 8;
        final SynchronizedLongQueue queue1 = new SynchronizedLongQueue(capacity);
        final ArrayBlockingQueue<Long> queue2 = new ArrayBlockingQueue<>(capacity);
        final AtomicLong out = new AtomicLong();

        assertEquals(capacity, queue1.capacity());

        for (int c = 0; c < 4; c++)
        {
            for (int k = 0; k < capacity + 4; k++)
            {
                for (int i = 0; i <= k; i++)
                {
                    assertEquals(i < capacity, queue1.offer(i * 1000 + 1L));
                    assertEquals(i < capacity, queue2.offer(i * 1000 + 1L));
                    assertEquals(queue1.size(), queue2.size());
                    assertEquals(Math.min(i + 1, capacity), queue1.size());
                }

                for (int i = 0; i <= k; i++)
                {
                    out.set(0);
                    final boolean actual = queue1.poll(out);
                    final Long expected = queue2.poll();
                    assertEquals(expected != null, actual);
                    assertEquals((expected == null ? 0 : expected), out.get());
                    assertEquals(queue1.size(), queue2.size());
                }

                queue1.clear();
            }
        }
    }

    /**
     * Test: 20171122041309084760
     *
     * <p>
     * Case: All methods must be synchronized.
     * </p>
     */
    @Test
    public void test20171122041309084760 ()
    {
        System.out.println("Test: 20171122041309084760");

        assertTrue(Modifier.isFinal(SynchronizedLongQueue.class.getModifiers()));

        for (Method x : SynchronizedLongQueue.class.getDeclaredMethods())
        {
            assertTrue(Modifier.isSynchronized(x.getModifiers()));
        }
    }
}
