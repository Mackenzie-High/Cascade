package com.mackenziehigh.cascade.internal.pumps3;

import com.google.common.base.Preconditions;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.internal.pumps3.Connector.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

/**
 * This class provides the algorithms needed to send
 * a message atomically to multiple independent pipes.
 *
 * <p>
 * In this context, atomicity means that given an
 * attempt (S) to send a message (M) to a set of
 * outputs (X1, ..., XN), then either (M) will be
 * enqueued in all of the outputs (X) or (M)
 * will not be enqueued in any of the outputs.
 * </p>
 *
 * <p>
 * In this context, orderliness means that given
 * two concurrent attempts (S1) and (S2) to send
 * messages (M1) and (M2) to the exact same
 * outputs (X1, ..., XN) using the exact same
 * instance of this object, then the order in
 * which the messages are enqueued in the outputs
 * will be the same. For example, this means that,
 * if (X1) receives (M1) and then (M2), then
 * (X2) will receive (M1) and then (M2), and
 * (X3) will receive (M1) and then (M2), etc.
 * (X3) would not receive (M2) and then (M1),
 * since (X1) received them in the reverse order.
 * </p>
 *
 * <p>
 * Orderliness is only guaranteed when the concurrent
 * send attempts are made through the same instance
 * of this object!
 * </p>
 */
public final class OrderlyAtomicSender
{
    /**
     * This lock ensures that no two send attempts
     * are modifying the underlying outputs simultaneously,
     * which in-turn ensures orderliness as defined above.
     */
    private final Lock transactionLock = new ReentrantLock();

    private final ArrayList<Connection> outputs;

    private final ArrayList<Object> keys;

    public OrderlyAtomicSender (final Collection<Connection> outputs)
    {
        this.outputs = new ArrayList<>(outputs);
        this.keys = new ArrayList<>(outputs.size());
        IntStream.range(0, outputs.size()).forEach(i -> keys.add(null));
        this.keys.trimToSize();
        this.outputs.trimToSize();
    }

    public int broadcast (final OperandStack message)
    {
        return 0; // TODO
    }

    /**
     * Use this method to atomically send a message to all of the outputs,
     * without blocking if any of the outputs are unable to accept the message.
     *
     * @param message is the message to send.
     * @return true, if the message was sent.
     */
    public boolean sendAsync (final OperandStack message)
    {
        if (transactionLock.tryLock() == false)
        {
            return false;
        }

        int commitCount = 0;

        try
        {
            boolean hasAllLocks = true;

            /**
             * Acquire the locks.
             */
            for (int i = 0; i < keys.size(); i++)
            {
                final Object key = outputs.get(i).lock();
                hasAllLocks &= key != null;
                keys.set(i, key);
            }

            /**
             * If we successfully obtained the locks,
             * then commit the message to each output.
             */
            for (int i = 0; hasAllLocks && i < keys.size(); i++)
            {
                final Object key = keys.get(i);
                outputs.get(i).commit(key, message);
                ++commitCount;
            }

            /**
             * Release the locks.
             */
            for (int i = 0; i < keys.size(); i++)
            {
                outputs.get(i).unlock(keys.get(i));
                keys.set(i, null);
            }
        }
        finally
        {
            transactionLock.unlock();
        }

        final boolean result = commitCount == keys.size();
        return result;
    }

    /**
     * Use this method to atomically send a message to all of the outputs,
     * blocking upto the given timeout, if any of the outputs are unable
     * to accept the message immediately.
     *
     * <p>
     * The timeout is a goal, not a real-time guarantee.
     * </p>
     *
     * @param message is the message to send.
     * @param timeout is the maximum amount of time to wait.
     * @param timeoutUnits describes the timeout.
     * @return true, if the message was sent.
     * @throws java.lang.InterruptedException
     */
    public boolean sendSync (final OperandStack message,
                             final long timeout,
                             final TimeUnit timeoutUnits)
            throws InterruptedException
    {
        Preconditions.checkArgument(timeoutUnits.toNanos(timeout) > 0, "Invalid Timeout");

        /**
         * The asynchronous version of this method is usually faster,
         * because fewer system-calls are needed, etc; therefore,
         * we will try that method first in hopes of getting lucky.
         */
        if (sendAsync(message))
        {
            return true;
        }

        final long timeoutNanos = timeoutUnits.toNanos(timeout);
        final long startTime = System.nanoTime();
        int lockCount = 0;

        if (transactionLock.tryLock(timeoutNanos, TimeUnit.NANOSECONDS) == false)
        {
            return false;
        }

        try
        {
            /**
             * Acquire the locks.
             */
            for (int i = 0; i < keys.size(); i++)
            {
                final long elapsedTime = System.nanoTime() - startTime;
                final long diffTime = timeoutNanos - elapsedTime; // Limit (diffTime) -> 0
                final long remainingTime = Math.max(Math.min(diffTime, timeoutNanos), 0);

                if (remainingTime == 0)
                {
                    break;
                }

                final Object key = outputs.get(i).lock(remainingTime, TimeUnit.NANOSECONDS);
                if (key == null)
                {
                    break;
                }
                else
                {
                    keys.set(i, outputs.get(i).lock());
                    ++lockCount;
                }
            }

            final boolean hasAllLocks = lockCount == keys.size();

            /**
             * If we successfully obtained the locks,
             * then commit the message to each output.
             */
            int commitCount = 0;
            for (int i = 0; hasAllLocks && i < keys.size(); i++)
            {
                final Object key = keys.get(i);
                outputs.get(i).commit(key, message);
                ++commitCount;
            }

            /**
             * Release the locks.
             */
            for (int i = 0; i < keys.size(); i++)
            {
                outputs.get(i).unlock(keys.get(i));
                keys.set(i, null);
            }

            final boolean result = commitCount == keys.size();
            return result;
        }
        finally
        {
            transactionLock.lock();
        }
    }

}
