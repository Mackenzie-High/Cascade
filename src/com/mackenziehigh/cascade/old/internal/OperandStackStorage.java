package com.mackenziehigh.cascade.old.internal;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.mackenziehigh.cascade.old.CascadeAllocator;
import com.mackenziehigh.cascade.old.CascadeAllocator.OperandArray;
import com.mackenziehigh.cascade.old.CascadeAllocator.OperandStack;

/**
 * An instance of this class allows one to place operand-stacks
 * into storage slots identified by a primitive-int keys and
 * later retrieve the stacks using the same keys.
 *
 * <p>
 * Since operand-stacks effectively use manual memory management,
 * you must invoke close() on this object once you are done
 * using it in order to allow the operand-stacks to be freed.
 * </p>
 */
public final class OperandStackStorage
        implements AutoCloseable
{

    /**
     * This array stores the operand-stacks.
     */
    private final OperandArray storage;

    /**
     * These are the indexes of the available slots in the storage array.
     */
    private final LongSynchronizedQueue freeKeys;

    /**
     * Sole Constructor.
     *
     * @param allocator owns the stacks that we will store herein.
     * @param capacity is the maximum number of stacks stored at once.
     */
    public OperandStackStorage (final CascadeAllocator allocator,
                                final int capacity)
    {
        Preconditions.checkNotNull(allocator, "allocator");
        Preconditions.checkArgument(capacity >= 0, "capacity");
        this.freeKeys = new LongSynchronizedQueue(capacity);
        this.storage = allocator.newOperandArray(capacity);

        for (int i = 0; i < capacity; i++)
        {
            Verify.verify(freeKeys.offer(i));
        }
    }

    /**
     * Use this method to add a operand-stack to storage.
     *
     * @param stack will be stored.
     * @return the key identifying the storage location.
     * @throws IllegalStateException if capacity limitations prevent insertion.
     */
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

    /**
     * Use this method to retrieve an operand-stack from storage.
     *
     * @param key identifies where herein the operand-stack is stored.
     * @param out will receive the operand-stack.
     */
    public synchronized void get (final int key,
                                  final OperandStack out)
    {
        out.set(storage, key);
        storage.set(key, null); // Prevent memory leaks!
        Verify.verify(freeKeys.offer(key));
    }

    /**
     * Use this method to release all of the operand-stacks herein.
     */
    @Override
    public void close ()
    {
        storage.close();
    }
}
