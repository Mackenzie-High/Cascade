package com.mackenziehigh.cascade.internal.pumps3;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class implements a multi-queue consisting of inner queues
 * that provide transactional *insertion* of elements thereto.
 *
 * <p>
 * All of the inner queues in an instance of this class
 * share a single underlying finite capacity data-structure.
 * We will call the capacity of the data-structure the global-capacity.
 * In addition, each inner queue may be assigned its own capacity,
 * which we will call the local-capacity of the queue.
 * The globalSize() will always be less-than-or-equal the globalCapacity().
 * The sum of the localCapacity() of the inner queues may exceed globalCapacity();
 * however, the sum of the localSize() will never exceed globalCapacity().
 * </p>
 */
public final class LongTransactionalMultiQueue
{
    private final List<TransactionQueue> innerQueues;

    /**
     * The sum of the sizes of the nested queues will
     * be less-than-or-equal-to this value at all times.
     */
    private final int globalCapacity;

    /**
     * All of the transaction-queues herein share a single underlying
     * data-structure of fixed finite capacity, such that the capacity
     * of that data-structure equals the number of permits available
     * from this semaphore. Thus, this semaphore prevents the insertion
     * of elements into the underlying data-structure,
     * unless space is actually available.
     */
    private final Semaphore globalSpace;

    /**
     * Sole Constructor.
     *
     * <p>
     * Each element in the localCapacity array declares a single inner queue.
     * Thus, the inner queue with id() (5) corresponds to localCapacity[5].
     * </p>
     *
     * @param globalCapacity is the shared capacity of all the inner queues.
     * @param localCapacity contains the local-capacity of each inner queue.
     */
    public LongTransactionalMultiQueue (final int globalCapacity,
                                        final int[] localCapacity)
    {
        this.globalCapacity = globalCapacity;
        this.globalSpace = new Semaphore(globalCapacity);

        final List<TransactionQueue> list = new LinkedList<>();

        for (int i = 0; i < localCapacity.length; i++)
        {
            final TransactionQueue queue = new TransactionQueue(i, localCapacity[i]);
            list.add(queue);
        }
        this.innerQueues = Collections.unmodifiableList(new CopyOnWriteArrayList<>(list));
    }

    public int globalSize ()
    {
        // TODO: thread-safety
        return innerQueues.stream().mapToInt(x -> x.localSize()).sum();
    }

    public int globalCapacity ()
    {
        return globalCapacity;
    }

    public List<TransactionQueue> members ()
    {
        return innerQueues;
    }

    public final class TransactionQueue
    {
        private final int id;

        /**
         * This is the maximum number of elements in the queue,
         * under best-case assumptions. If the underlying queue
         * has a smaller capacity than this capacity value,
         * then the real capacity is lesser of the two values
         * due to the classic Pigeon Hole Principle.
         */
        private final int localCapacity;

        /**
         * This lock ensures that the thread that called
         * begin(*) is the same thread that is calling
         * either rollback() or commit(*).
         */
        private final Lock transactionLock = new ReentrantLock();

        /**
         * This semaphore regulates the insertion of elements
         * into this queue in order to ensure size() is always
         * less-than-or-equal to capacity(), by issuing permits
         * only when space is available in the queue.
         * However, this semaphore does *not* take into account
         * the sizes of other queues in this multi-queue;
         * therefore, one must also obtain a permit from
         * the global-space semaphore.
         */
        private final Semaphore localSpace;

        private final LongSynchronizedQueue queue;

        /**
         * This is the number of times that begin(*) returned true.
         */
        private final AtomicLong beginCount = new AtomicLong();

        /**
         * This is the number of times that commit(*) has been called.
         */
        private final AtomicLong commitCount = new AtomicLong();

        /**
         * This is the number of times that rollback(*) has been called.
         */
        private final AtomicLong rollbackCount = new AtomicLong();

        private TransactionQueue (final int id,
                                  final int localCapacity)
        {
            this.id = id;
            this.localCapacity = Math.min(localCapacity, globalCapacity);
            this.localSpace = new Semaphore(localCapacity);
            this.queue = new LongSynchronizedQueue(localCapacity);
        }

        /**
         * Getter.
         *
         * @return the identifier of this inner-queue.
         */
        public int id ()
        {
            return id;
        }

        /**
         * Use this method to begin a transaction without blocking.
         *
         * @return true, only if a transaction has been begun.
         */
        public boolean begin ()
        {
            Preconditions.checkState(beginCount.get() == commitCount.get() + rollbackCount.get(), "Usage Error!");

            final boolean hasTransactionLock = transactionLock.tryLock();

            if (hasTransactionLock == false)
            {
                return false;
            }

            final boolean hasLocalSpace = localSpace.tryAcquire();

            if (hasLocalSpace == false)
            {
                transactionLock.unlock();
                return false;
            }

            final boolean hasGlobalSpace = globalSpace.tryAcquire();

            if (hasGlobalSpace == false)
            {
                localSpace.release();
                transactionLock.unlock();
                return false;
            }

            beginCount.incrementAndGet();
            return true;
        }

        /**
         * Use this method to begin a transaction,
         * blocking if necessary upto a given timeout.
         *
         * <p>
         * The timeout is a goal, not a real-time guarantee.
         * </p>
         *
         * @param timeout is the amount of time to block.
         * @param timeoutUnits describes the timeout.
         * @return true, only if a transaction has been begun.
         */
        public boolean begin (final long timeout,
                              final TimeUnit timeoutUnits)
        {
            /**
             * The asynchronous version of this method is usually faster,
             * because it does not have to call nanoTime(), etc,
             * but may fail due to thread-contention.
             */
            if (begin())
            {
                return true;
            }

            final long startTime = System.nanoTime();
            final long timeoutNanos = timeoutUnits.toNanos(timeout);

            boolean hasTransactionLock = false;
            boolean hasLocalSpace = false;
            boolean hasGlobalSpace = false;
            boolean failure = true;

            Preconditions.checkArgument(timeoutNanos > 0, "Invalid Timeout");

            while (failure)
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

                try
                {
                    if (hasTransactionLock == false)
                    {
                        hasTransactionLock = transactionLock.tryLock(remainingTime, TimeUnit.NANOSECONDS);
                        continue;
                    }

                    if (hasLocalSpace == false)
                    {
                        localSpace.tryAcquire(remainingTime, TimeUnit.NANOSECONDS);
                        continue;
                    }

                    if (hasGlobalSpace == false)
                    {
                        globalSpace.tryAcquire(remainingTime, TimeUnit.NANOSECONDS);
                        continue;
                    }

                    failure = !hasTransactionLock || !hasLocalSpace || !hasGlobalSpace;
                }
                catch (InterruptedException ex)
                {
                    // Pass
                }
            }

            if (failure && hasTransactionLock)
            {
                transactionLock.unlock();
            }

            if (failure && hasLocalSpace)
            {
                localSpace.release();
            }

            if (failure && hasGlobalSpace)
            {
                globalSpace.release();
            }

            if (failure)
            {
                return false;
            }
            else
            {
                beginCount.incrementAndGet();
                return true;
            }
        }

        /**
         * After successfully calling begin(*), you can use this method
         * in order to add a single value to this queue without fear of
         * violating the capacity restrictions due to thread competition.
         *
         * @param value will be added to the queue.
         */
        public void commit (final long value)
        {
            Verify.verify(queue.offer(value), "Bug!");

            /**
             * Only release the transaction-lock.
             * The global-space and local-space semaphores
             * will be released by the poll() operation
             * performed by the consumer thread.
             */
            transactionLock.unlock();

            /**
             * Track call-counts in order to detect accidental misuse.
             */
            rollbackCount.incrementAndGet();
        }

        /**
         * After successfully calling begin(*), you can use this method
         * in order to cancel the transaction without adding a value
         * to the underlying queue.
         */
        public void rollback ()
        {
            /**
             * Since nothing was added to the queue,
             * release everything.
             */
            globalSpace.release();
            localSpace.release();
            transactionLock.unlock();

            /**
             * Track call-counts in order to detect accidental misuse.
             */
            rollbackCount.incrementAndGet();
        }

        public int localSize ()
        {
            return queue.size();
        }

        public int localCapacity ()
        {
            return localCapacity;
        }

        /**
         * Use this method to retrieve and remove an element from this queue.
         *
         * <p>
         * You are responsible for only invoking this method
         * when an element is actually available in the queue.
         * </p>
         *
         * @return the retrieved value.
         */
        public long poll ()
        {
            /**
             * Perform a sanity check; however, this method is not synchronized.
             * Therefore, this will not always work.
             * Thus, this check is not part of the method contract.
             */
            Preconditions.checkState(localSize() > 0, "Empty Queue");

            final long value = queue.poll();
            globalSpace.release();
            localSpace.release();
            return value;
        }

    }
}
