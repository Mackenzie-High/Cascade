package com.mackenziehigh.cascade.old.allocators;

import java.util.Set;

/**
 *
 */
public interface Allocator
        extends AutoCloseable
{

    /**
     * This type of exception means that an allocation failed,
     * because the relevant allocation-pool is full.
     */
    public class ExhaustedAllocatorException
            extends IllegalStateException
    {
        private final Allocator allocator;

        public ExhaustedAllocatorException (final Allocator allocator)
        {
            super("Allocator is completely full!");
            this.allocator = allocator;
        }

        public Allocator allocator ()
        {
            return allocator;
        }
    }

    public interface BinaryOperand
    {
        public void incrementRefCount ();

        public void decrementRefCount ();

        public int size ();

        public byte byteAt (int index);

        public int copyTo (byte[] buffer,
                           int offset,
                           int length);
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

    public Set<Allocator> delegates ();

    /**
     * Use this method to allocate an operand.
     *
     * @param stack points to the top of the operand-stack that
     * the newly allocated operand will be pushed onto.
     * @param buffer contains the content of the operand.
     * @param offset is the start position of the data in the buffer.
     * @param length is the length of the data in the buffer.
     * @return the modified immutable operand-stack.
     * @throws IllegalArgumentException if (offset + length) exceeds length(buffer).
     * @throws IllegalArgumentException if length is less than minimumAllocationSize().
     * @throws IllegalArgumentException if length is greater than maximumAllocationSize().
     * @throws ExhaustedAllocatorException if the pool is out-of-memory.
     */
    public default OperandStack alloc (final OperandStack stack,
                                       final byte[] buffer,
                                       final int offset,
                                       final int length)
    {
        final OperandStack result = tryAlloc(stack, buffer, offset, length);

        if (result == null)
        {
            throw new ExhaustedAllocatorException(this);
        }

        return result;
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
     * @return the modified immutable operand-stack, if allocations succeeds; otherwise return null.
     * @throws IllegalArgumentException if (offset + length) exceeds length(buffer).
     * @throws IllegalArgumentException if length is less than minimumAllocationSize().
     * @throws IllegalArgumentException if length is greater than maximumAllocationSize().
     */
    public OperandStack tryAlloc (OperandStack stack,
                                  byte[] buffer,
                                  int offset,
                                  int length);

}
