package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.redo2.CascadeToken;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class provides the algorithms needed to send
 * a message atomically to multiple independent queues.
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
public abstract class OrderlyAtomicSender
{
    public abstract void resolveConnections (CascadeToken event,
                                             ArrayList<InflowQueue> out);

    /**
     * This lock ensures that no two send attempts
     * are modifying the underlying outputs simultaneously,
     * which in-turn ensures orderliness as defined above.
     */
    private final Lock transactionLock = new ReentrantLock();

    private final ArrayList<InflowQueue> outputs = new ArrayList<>(16);

    private final ArrayList<Object> keys = new ArrayList<>(16);

    /**
     * Use this method to *non* atomically send a message to all of the outputs,
     * without blocking if any of the outputs are unable to accept the message.
     *
     * @param event identifies the event-channel that the message will be sent to.
     * @param message is the message to send.
     * @return the number of outputs that enqueued the message.
     */
    public int broadcast (final CascadeToken event,
                          final OperandStack message)
    {
        if (transactionLock.tryLock() == false)
        {
            return 0;
        }
        else
        {
            resolveConnections(event);
        }

        int commitCount = 0;

        try
        {
            /**
             * Acquire the locks.
             */
            for (int i = 0; i < outputs.size(); i++)
            {
                final Object key = outputs.get(i).lock();
                keys.add(key);
            }

            /**
             * If we successfully obtained the locks,
             * then commit the message to each output.
             */
            for (int i = 0; i < keys.size(); i++)
            {
                final Object key = keys.get(i);
                if (key != null)
                {
                    outputs.get(i).commit(key, event, message);
                    ++commitCount;
                }
            }
        }
        finally
        {
            /**
             * Release the locks.
             */
            for (int i = 0; i < keys.size(); i++)
            {
                outputs.get(i).unlock(keys.get(i));
                keys.set(i, null);
            }

            transactionLock.unlock();
        }

        return commitCount;
    }

    /**
     * Use this method to atomically send a message to all of the outputs,
     * without blocking if any of the outputs are unable to accept the message.
     *
     * @param event identifies the event-channel that the message will be sent to.
     * @param message is the message to send.
     * @return true, if the message was sent.
     */
    public boolean sendAsync (final CascadeToken event,
                              final OperandStack message)
    {
        if (transactionLock.tryLock() == false)
        {
            return false;
        }
        else
        {
            resolveConnections(event);
        }

        int commitCount = 0;

        try
        {
            boolean hasAllLocks = true;

            /**
             * Acquire the locks.
             */
            for (int i = 0; i < outputs.size(); i++)
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
                outputs.get(i).commit(key, event, message);
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
     * @param event identifies the event-channel that the message will be sent to.
     * @param message is the message to send.
     * @param timeout is the maximum amount of time to wait.
     * @param timeoutUnits describes the timeout.
     * @return true, if the message was sent.
     * @throws java.lang.InterruptedException
     */
    public boolean sendSync (final CascadeToken event,
                             final OperandStack message,
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
        if (sendAsync(event, message))
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
        else
        {
            resolveConnections(event);
        }

        try
        {
            /**
             * Acquire the locks.
             */
            for (int i = 0; i < outputs.size(); i++)
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
                outputs.get(i).commit(key, event, message);
                ++commitCount;
            }

            /**
             * Release the locks.
             */
            for (int i = 0; i < keys.size(); i++)
            {
                outputs.get(i).unlock(keys.size());
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

    private void resolveConnections (final CascadeToken event)
    {
        resolveConnections(event, outputs);
        keys.ensureCapacity(outputs.size());
        keys.clear();
    }

}
