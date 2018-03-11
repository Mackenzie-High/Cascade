package com.mackenziehigh.cascade.allocators;

import java.util.OptionalLong;

/**
 *
 */
public interface CascadeAllocator
{
    /**
     * This type of exception means that an allocation failed,
     * because the relevant allocation-pool is full.
     */
    public class ExhaustedAllocationPoolException
            extends IllegalStateException
    {
        // Pass
    }

    /**
     * Getter.
     *
     * @return true, if this pool is based on pre-allocated memory.
     */
    public boolean isFixed ();

    /**
     * Getter.
     *
     * <p>
     * This method only returns a value, if isFixed(),
     * because maintaining counts can be a performance hit
     * in some implementations due to thread contention.
     * Only the other hand, fixed pools must maintain
     * the counts anyway due to the recycling of operands.
     * </p>
     *
     * @return the number of active allocations.
     */
    public default OptionalLong size ()
    {
        return OptionalLong.empty();
    }

    /**
     * Getter.
     *
     * <p>
     * This method only returns a value, if isFixed(),
     * because maintaining counts can be a performance hit
     * in some implementations due to thread contention.
     * Only the other hand, fixed pools must maintain
     * the counts anyway due to the recycling of operands.
     * </p>
     *
     * @return the maximum number of concurrent active allocations herein.
     */
    public default OptionalLong capacity ()
    {
        return OptionalLong.empty();
    }

    /**
     * Getter.
     *
     * @return the minimum size of a single allocation.
     */
    public int minimumAllocationSize ();

    /**
     * Getter.
     *
     * @return the maximum size of a single allocation.
     */
    public int maximumAllocationSize ();

    /**
     * Use this method to allocate an operand.
     *
     * @param stack points to the top of the operand-stack that
     * the newly allocated operand will be pushed onto.
     * @param buffer contains the content of the operand.
     * @param offset is the start position of the data in the buffer.
     * @param length is the length of the data in the buffer.
     * @throws AllocatorMismatchException if stack was not created by this allocator.
     * @throws IllegalArgumentException if (offset + length) exceeds length(buffer).
     * @throws IllegalArgumentException if length is less than minimumAllocationSize().
     * @throws IllegalArgumentException if length is greater than maximumAllocationSize().
     * @throws ExhaustedAllocationPoolException if the pool is out-of-memory.
     */
    public default void alloc (final com.mackenziehigh.cascade.old.CascadeAllocator.OperandStack stack,
                               final byte[] buffer,
                               final int offset,
                               final int length)
    {
        if (tryAlloc(stack, buffer, offset, length) == false)
        {
            throw new ExhaustedAllocationPoolException();
        }
    }

    /**
     * Use this method to allocate an operand.
     *
     * <p>
     * The only difference between this method and alloc(*)
     * is that this method will return a boolean flag,
     * rather than throwing an exception, when an allocation
     * would fail due to pool/memory exhaustion.
     * </p>
     *
     * @param stack points to the top of the operand-stack that
     * the newly allocated operand will be pushed onto.
     * @param buffer contains the content of the operand.
     * @param offset is the start position of the data in the buffer.
     * @param length is the length of the data in the buffer.
     * @return true, iff the allocation succeeds.
     * @throws AllocatorMismatchException if stack was not created by this allocator.
     * @throws IllegalArgumentException if (offset + length) exceeds length(buffer).
     * @throws IllegalArgumentException if length is less than minimumAllocationSize().
     * @throws IllegalArgumentException if length is greater than maximumAllocationSize().
     */
    public boolean tryAlloc (com.mackenziehigh.cascade.old.CascadeAllocator.OperandStack stack,
                             byte[] buffer,
                             int offset,
                             int length);
}
