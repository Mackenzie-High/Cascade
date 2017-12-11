package com.mackenziehigh.cascade.internal.pumps;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * This class allows you to atomically acquire permits from a series of related semaphores.
 */
public final class SemaphoreSeries
{
    private final Semaphore[] members;

    /**
     * Sole Constructor.
     *
     * @param members provide the permits.
     */
    public SemaphoreSeries (final List<Semaphore> members)
    {
        this.members = ImmutableList.copyOf(members).toArray(new Semaphore[0]);
    }

    /**
     * Use this method to either acquire one permit from each semaphore,
     * or no permits at all when any of the semaphore(s) are exhausted.
     *
     * @return true, if the permits were acquired.
     */
    public boolean tryAcquire ()
    {
        for (int i = 0; i < members.length; i++)
        {
            /**
             * Release the subset that were acquired, if necessary.
             */
            if (members[i].tryAcquire() == false)
            {
                release(i);
                return false;
            }
        }

        return true;
    }

    /**
     * Use this method to either acquire one permit from each semaphore,
     * or no permits at all when any of the semaphore(s) are exhausted,
     * waiting up to the given timeout for the permit(s) to become available.
     *
     * <p>
     * The timeout is a goal, not a real-time guarantee.
     * </p>
     *
     * @param timeout is the maximum amount of time to wait.
     * @param timeoutUnits describes the timeout.
     * @return true, if the permits were actually acquired.
     * @throws InterruptedException if any semaphore throws the exception.
     */
    public boolean tryAcquire (final long timeout,
                               final TimeUnit timeoutUnits)
            throws InterruptedException
    {
        final long startTime = System.nanoTime();
        final long timeoutNanos = timeoutUnits.toNanos(timeout);

        Preconditions.checkArgument(timeoutNanos > 0, "Invalid Timeout");

        int index = 0;

        try
        {
            while (index < members.length)
            {
                /**
                 * If an operation took too long, then the remaining time may be negative.
                 * For the sake of simplicity (bug-freeness), round negative numbers to zero.
                 * Moreover, due to bugs on some systems (Windows XP, Virtual Machines, etc),
                 * the clock may not be monotonic or may make large jumps.
                 * In order to avoid bugs, ensure that the raining-time is strictly
                 * between zero and the user-provided value.
                 */
                final long elapsedTime = System.nanoTime() - startTime;
                final long diffTime = timeoutNanos - elapsedTime; // Limit(diffTime) -> 0
                final long remainingTime = Math.max(Math.min(diffTime, timeoutNanos), 0);

                if (remainingTime == 0)
                {
                    break;
                }

                index += (members[index].tryAcquire(timeout, timeoutUnits) ? 1 : 0);
            }
        }
        finally
        {
            if (index < members.length)
            {
                release(index);
            }
        }

        return true;
    }

    /**
     * Release the permits in reverse order.
     *
     * <p>
     * You are responsible for ensuring that you actually
     * hold the permits before invoking this method.
     * </p>
     */
    public void release ()
    {
        /**
         * Release the locks in reverse order.
         * The first lock to be locked is the last to be unlocked.
         */
        for (int i = members.length - 1; i >= 0; i--)
        {
            members[i].release();
        }
    }

    private void release (final int length)
    {
        for (int i = length - 1; i >= 0; i--)
        {
            members[i].release();
        }
    }
}
