package com.mackenziehigh.cascade.internal.pumps3;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeAllocator.OperandArray;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;

/**
 * An instance of this class allows one to place operand-stacks
 * into a storage slot identified by a primitive-int key and
 * later retrieve the stack using the same key.
 */
public final class OperandStackStorage
        implements AutoCloseable
{
    private final LongSynchronizedQueue freeKeys;

    private final OperandArray storage;

    public OperandStackStorage (final CascadeAllocator allocator,
                                final int capacity)
    {
        Preconditions.checkArgument(capacity >= 0, "capacity");
        this.freeKeys = new LongSynchronizedQueue(capacity);
        this.storage = allocator.newOperandArray(capacity);

        for (int i = 0; i < capacity; i++)
        {
            Verify.verify(freeKeys.offer(i));
        }
    }

    public synchronized int set (final OperandStack stack)
    {
        if (freeKeys.isEmpty())
        {
            throw new IllegalStateException("No Free Key");
        }
        else
        {
            final int key = (int) freeKeys.poll();
            storage.set(key, stack);
            return key;
        }
    }

    public synchronized void get (final int key,
                                  final OperandStack out)
    {
        out.set(storage, key);
        storage.set(key, null); // Prevent memory leaks!
        Verify.verify(freeKeys.offer(key));
    }

    @Override
    public void close ()
    {
        storage.close();
    }
}
