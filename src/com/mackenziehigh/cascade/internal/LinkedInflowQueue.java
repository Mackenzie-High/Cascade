package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Linked-List based Inflow Queue.
 */
public final class LinkedInflowQueue
        extends AbstractInflowQueue
{
    private static final class Entry
    {
        public final CascadeToken event;

        public final OperandStack message;

        public Entry (final CascadeToken event,
                      final OperandStack message)
        {
            this.event = event;
            this.message = message;
        }
    }

    /**
     * This queue contains the event-messages.
     */
    private final Queue<Entry> queue = new LinkedBlockingQueue<>();

    /**
     * Sole Constructor.
     *
     * @param allocator is needed to allocate requisite storage.
     * @param capacity will be the maximum size of the queue at any time.
     */
    public LinkedInflowQueue (final CascadeAllocator allocator,
                              final int capacity)
    {
        super(capacity);
        Preconditions.checkNotNull(allocator, "allocator");
        Preconditions.checkArgument(capacity >= 0, "capacity < 0");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommit (final CascadeToken event,
                             final CascadeAllocator.OperandStack message)
    {
        // Enqueue the Event-ID and the Message.
        final OperandStack stack = message.allocator().newOperandStack();
        stack.set(message);
        final Entry entry = new Entry(event, stack);
        queue.add(entry);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CascadeToken doPoll (final CascadeAllocator.OperandStack out)
    {
        // Dequeue the Event-ID and the Message.
        final Entry entry = queue.poll();

        if (entry != null)
        {
            // Output the Message.
            out.set(entry.message);

            // Deallocate the operand-stack.
            entry.message.close();

            return entry.event;
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
        return queue.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close ()
    {
        while (queue.isEmpty() == false)
        {
            final Entry entry = queue.poll();

            if (entry != null)
            {
                entry.message.close();
            }
        }
    }

}
