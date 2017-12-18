package com.mackenziehigh.cascade2;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public final class EventDispatcher
{
    /**
     * Conceptually, this is a transmission queue.
     *
     * <p>
     * The transaction-lock described herein is a conceptual entity.
     * In reality, the transaction-lock may consist of multiple
     * locks that must be obtained in sequence, etc.
     * </p>
     *
     * <p>
     * The transaction methods herein return or use an access-key.
     * The access-key is simply passed around in order to promote
     * proper usage of instances of this class, by requiring
     * that only the thread holding the access-key can manipulate
     * the transaction.
     * </p>
     */
    public interface Connection
    {
        /**
         * Use this method to attempt to obtain the transaction-lock,
         * blocking if necessary upto the given timeout.
         *
         * <p>
         * At some point after successfully obtaining an access-key
         * from this method, you must invoke either unlock(*).
         * </p>
         *
         * @param timeout is the maximum amount of time to wait.
         * @param timeoutUnits describes the timeout.
         * @return an access-key, if the lock was obtained; otherwise, return null.
         */
        public Object lock (long timeout,
                            TimeUnit timeoutUnits);

        /**
         * Use this method to attempt to obtain the transaction-lock,
         * without blocking if the lock cannot immediately be obtained.
         *
         * <p>
         * At some point after successfully obtaining an access-key
         * from this method, you must invoke either unlock(*).
         * </p>
         *
         * @return an access-key, if the lock was obtained; otherwise, return null.
         */
        public Object lock ();

        /**
         * Use this method to enqueue a message in this connection queue
         * after successfully obtaining the transaction-lock.
         *
         * @param key is the access-key obtained from lock().
         * @param message will be enqueued herein.
         */
        public void commit (Object key,
                            OperandStack message);

        /**
         * Use this method to release the transaction-lock.
         *
         * <p>
         * If the given key is null, then this method is a no-op.
         * </p>
         *
         * @param key is the access-key obtained from lock(), or null.
         */
        public void unlock (Object key);

        /**
         * Getter.
         *
         * @return the number of messages that are currently enqueued herein.
         */
        public int localSize ();

        /**
         * Getter.
         *
         * @return the maximum number of messages that can be enqueued herein.
         */
        public int localCapacity ();

        /**
         * Safely release any special resources herein.
         *
         * <p>
         * This method never throws any exceptions.
         * </p>
         */
        public void close ();
    }

    private final ImmutableSortedMap<Token, Connection> queues;

    private final ListMultimap<Token, Connection> subscriptions = Multimaps.newListMultimap(Maps.newConcurrentMap(), () -> Lists.newCopyOnWriteArrayList());

    /**
     * Sole constructor.
     *
     * @param queues
     */
    public EventDispatcher (final Map<Token, Connection> queues)
    {
        this.queues = ImmutableSortedMap.copyOf(queues);
    }

    /**
     * Register a message-queue to receive messages for a given event.
     *
     * @param subscriberId will now receive messages for (eventId).
     * @param eventId identifies the event to listen for.
     */
    public void register (final Token subscriberId,
                          final Token eventId)
    {
        final Connection handler = queues.get(subscriberId);

        if (handler != null)
        {
            subscriptions.put(eventId, handler);
        }
        else
        {
            throw new IllegalArgumentException("No Such Handler: " + subscriberId);
        }
    }

    /**
     * Deregister a message-queue from receiving message for a given event.
     *
     * @param subscriberId will no longer receive messages for (eventId).
     * @param eventId identifies the event to no longer listen for.
     */
    public void deregister (final Token subscriberId,
                            final Token eventId)
    {
        subscriptions.remove(eventId, subscriberId);
    }

    public boolean sendAsync (final Token eventId,
                              final OperandStack message)
    {
        return false;
    }

    public boolean sendSync (final Token eventId,
                             final OperandStack message,
                             final long timeout,
                             final TimeUnit timeoutUnit)
    {
        return false;
    }

    public boolean broadcast (final Token eventId,
                              final OperandStack message)
    {
        final List<Connection> subscribers = subscriptions.get(eventId);

        for (int i = 0; i < subscribers.size(); i++)
        {
            subscribers.get(i).lock();
        }

        return false;
    }
}
