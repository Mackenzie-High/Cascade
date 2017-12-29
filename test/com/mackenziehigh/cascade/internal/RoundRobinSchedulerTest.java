package com.mackenziehigh.cascade.internal;

import com.google.common.collect.ImmutableList;
import java.util.concurrent.TimeUnit;
import static junit.framework.Assert.*;
import org.junit.Test;

/**
 * Unit Test.
 */
public final class RoundRobinSchedulerTest
{
    /**
     * Test: 20171228174027645340
     *
     * <p>
     * Case: Normal
     * </p>
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void test20171228174027645340 ()
            throws InterruptedException
    {
        System.out.println("Test: 20171228174027645340");

        Scheduler.TaskStream<String> tasks;

        final RoundRobinScheduler<String> sch = new RoundRobinScheduler<>(ImmutableList.of("X", "Y", "Z"));

        /**
         * Verify the initialization.
         */
        assertEquals(3, sch.streams().size());
        assertTrue(sch.streams().get("X").source().equals("X"));
        assertTrue(sch.streams().get("Y").source().equals("Y"));
        assertTrue(sch.streams().get("Z").source().equals("Z"));

        /**
         * No tasks have been added yet, so none are available.
         */
        assertNull(sch.pollTask(1, TimeUnit.MILLISECONDS)); // 1 Ms => Do *NOT* block in a unit-test.

        /**
         * Add a single task.
         */
        sch.addTask(sch.streams().get("X"));

        /**
         * Now one task should be available.
         */
        tasks = sch.pollTask(1, TimeUnit.MILLISECONDS);
        assertNotNull(tasks);
        assertEquals("X", tasks.source());

        /**
         * We still hold the lock on the (X) stream.
         * Let us go ahead and add another task to that stream
         * and then try to poll for another task.
         * We should not be able to obtain the new task
         * via poll() yet, because we still hold the lock.
         */
        sch.addTask(sch.streams().get("X"));
        assertNull(sch.pollTask(1, TimeUnit.MILLISECONDS));

        /**
         * We still hold the lock on the (X) stream.
         * Let us go ahead and add a task to the (Y) stream.
         * The task should then be immediately available,
         * even though we still hold the lock on (X).
         */
        sch.addTask(sch.streams().get("Y"));
        tasks = sch.pollTask(1, TimeUnit.MILLISECONDS);
        assertNotNull(tasks);
        assertEquals("Y", tasks.source());

        /**
         * Release the lock on the (X) stream,
         * which we are still holding. Then,
         * the still pending task from the (X)
         * stream should be available.
         * Release its lock too.
         */
        sch.streams().get("X").release();
        tasks = sch.pollTask(1, TimeUnit.MILLISECONDS);
        assertNotNull(tasks);
        assertEquals("X", tasks.source());
        sch.streams().get("X").release();

        /**
         * Release the lock on the (Y) stream.
         */
        sch.streams().get("Y").release();

        /**
         * No more tasks are available.
         */
        assertNull(sch.pollTask(1, TimeUnit.MILLISECONDS));
    }

    /**
     * Test: 20171228192609772502
     *
     * <p>
     * Case: Too Many Releases
     * </p>
     */
    @Test (expected = IllegalStateException.class)
    public void test20171228192609772502 ()
    {
        System.out.println("Test: 20171228192609772502");

        final RoundRobinScheduler<String> sch = new RoundRobinScheduler<>(ImmutableList.of("X", "Y", "Z"));

        /**
         * We do not currently hold the lock,
         * so release() has nothing to release.
         */
        sch.streams().get("X").release();
    }

    /**
     * Test: 20171228193044641358
     *
     * <p>
     * Case: No Input Sources.
     * </p>
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void test20171228193044641358 ()
            throws InterruptedException
    {
        System.out.println("Test: 20171228193044641358");

        final RoundRobinScheduler<String> sch = new RoundRobinScheduler<>(ImmutableList.of("X", "Y", "Z"));

        assertNull(sch.pollTask(1, TimeUnit.MILLISECONDS)); // 1 Ms => Do *NOT* block in a unit-test.
    }
}
