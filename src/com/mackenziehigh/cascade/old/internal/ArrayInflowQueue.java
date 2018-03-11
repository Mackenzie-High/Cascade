package com.mackenziehigh.cascade.old.internal;

import com.google.common.base.Preconditions;
import com.mackenziehigh.cascade.old.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Array-based Inflow Queue.
 */
public final class ArrayInflowQueue
        extends AbstractInflowQueue
{
    /**
     * This queue stores the enqueued event-identifiers.
     */
    private final ArrayBlockingQueue<CascadeToken> eventQueue;

    /**
     * This queue stores the indices of the enqueued messages
     * that are stored in the message-storage object below.
     */
    private final LongSynchronizedQueue messageQueue;

    /**
     * This object efficiently stores the enqueued messages
     * and provides (integer) indices that are enqueued in
     * the message-queue above, so that the actual messages
     * can later be retrieved using those integers.
     */
    private final OperandStackStorage messageStorage;

    /**
     * Sole Constructor.
     *
     * @param allocator is needed to allocate requisite storage.
     * @param capacity will be the maximum size of the queue at any time.
     */
    public ArrayInflowQueue (final CascadeAllocator allocator,
                             final int capacity)
    {
        super(capacity);
        Preconditions.checkNotNull(allocator, "allocator");
        this.eventQueue = new ArrayBlockingQueue<>(capacity);
        this.messageQueue = new LongSynchronizedQueue(capacity);
        this.messageStorage = new OperandStackStorage(allocator, capacity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommit (final CascadeToken event,
                             final CascadeAllocator.OperandStack message)
    {
        // Enqueue the Event-ID.
        eventQueue.offer(event);

        // Enqueue the Message.
        final int pos = messageStorage.set(message);
        messageQueue.offer(pos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CascadeToken doPoll (final CascadeAllocator.OperandStack out)
    {
        // Dequeue the Event-ID.
        final CascadeToken event = eventQueue.poll();

        if (event != null)
        {
            // Dequeue the Message.
            final int idx = (int) messageQueue.poll();
            messageStorage.get(idx, out);

            return event;
        }
        else
        {
            return null; // No event-message available at this time.
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size ()
    {
        return eventQueue.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close ()
    {
        eventQueue.clear();
        messageStorage.close();
    }

}
