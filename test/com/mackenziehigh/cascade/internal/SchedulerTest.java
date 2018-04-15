package com.mackenziehigh.cascade.internal;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit Test.
 */
public final class SchedulerTest
{
    private final StringBuilder proof = new StringBuilder();

    private final Scheduler<String> scheduler = new Scheduler<>(() -> proof.append('T'));

    private final Scheduler.Process<String> taskA = scheduler.newProcess();

    private final Scheduler.Process<String> taskB = scheduler.newProcess();

    public SchedulerTest ()
    {
        taskA.userObject().set("A");
        taskB.userObject().set("B");
    }

    private boolean work ()
    {
        final Scheduler.Process<String> task = scheduler.poll();

        if (task == null)
        {
            return false;
        }

        try (Scheduler.Process<String> proc = task)
        {
            proof.append(proc.userObject().get());
        }

        return true;
    }

    /**
     * Test: 20180415003706478467
     *
     * <p>
     * Case: Ordering of Scheduled Tasks
     * </p>
     */
    @Test
    public void test20180415003706478467 ()
    {
        System.out.println("Test: 20180415003706478467");

        /**
         * Randomly schedule the two tasks many times.
         */
        final int taskCount = 2_000;
        final Random rand = new Random(System.currentTimeMillis());
        final List<Scheduler.Process<String>> tasks = IntStream.rangeClosed(1, taskCount)
                .mapToObj(x -> rand.nextBoolean() ? taskA : taskB)
                .collect(Collectors.toList());
        tasks.forEach(x -> x.schedule());

        /**
         * Poll each pending tasks and simulate work by appending
         * the user-object (A|B) to the proof string.
         */
        int count = 0;
        while (work())
        {
            ++count;
        }
        assertEquals(tasks.size(), count);

        /**
         * Because the tasks were all scheduled before any work was performed
         * and the scheduler uses a round-robin algorithm in that case,
         * the proof string should be a series of "AB" or "BA" substrings.
         * Since the tasks were created randomly, the number of "A" and "B"
         * substrings will likely be unbalanced; therefore, the tail of the
         * proof string will only be "A" or "B" substrings.
         */
        final String tasksOnly = proof.toString().replace("T", "");
        assertTrue(tasksOnly.matches("((AB)+|(BA)+)(A+|B+)"));
        assertEquals(taskCount, tasksOnly.length());

        /**
         * Every time that a task becomes available for execution, the callback is executed.
         * Thus, the number of callbacks must equal the number of scheduled tasks.
         * However, the callback is not executed every time the task is scheduled.
         */
        final String callbacksOnly = proof.toString().replaceAll("A|B", "");
        assertTrue(callbacksOnly.matches("T+"));
        assertEquals(taskCount, callbacksOnly.length());
    }

}
