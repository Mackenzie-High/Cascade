package com.mackenziehigh.cascade;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Use this interface to allocate operands.
 *
 * <p>
 * An operand is allocated within an allocation-pool,
 * which may recycle the operand when it is no longer in-use.
 * </p>
 *
 * <p>
 * In general, operands must be explicitly deallocated.
 * Operands use reference-counting in order to control deallocation.
 * When the reference-count reaches zero, the operand will be deallocated.
 * </p>
 *
 * <p>
 * An operand contains a reference to an operand <i>below</i> it.
 * Therefore, operands form spaghetti-stack data-structures.
 * </p>
 *
 * <p>
 * Unless stated explicitly otherwise, all the methods defined herein,
 * including those in inner classes, are required to be thread-safe.
 * </p>
 */
public interface CascadeAllocator
{
    /**
     * An instance of this interface describes a pool of allocations,
     * which may have specific finite user-defined bounds.
     */
    public interface AllocationPool
    {
        public enum AllocationCodes
        {
            /**
             * The allocation was successful.
             */
            OK,

            /**
             * The allocation failed due to insufficient memory or size() exceeds capacity().
             */
            OUT_OF_MEMORY,

            /**
             * The allocation failed, because the requested size is larger than maximumAllocationSize().
             */
            REQUEST_IS_TOO_LARGE,

            /**
             * The allocation failed, because the requested size is less than minimumAllocationSize().
             */
            REQUEST_IS_TOO_SMALL,
        }

        /**
         * Getter.
         *
         * @return the user-defined name of this pool.
         */
        public String name ();

        /**
         * Getter.
         *
         * @return true, if this is one of the default pool(s).
         */
        public boolean isDefault ();

        /**
         * Getter.
         *
         * @return true, if this pool is based on pre-allocated memory.
         */
        public boolean isPreallocated ();

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
         * Getter.
         *
         * @return the current number of allocations in this pool.
         */
        public int size ();

        /**
         * Getter.
         *
         * @return the maximum number of allocations in this pool.
         */
        public int capacity ();

        /**
         * Use this method to allocate an operand.
         *
         * @param stack points to the top of the operand-stack that
         * the newly allocated operand will be pushed onto.
         * @param buffer contains the content of the operand.
         * @param offset is the start position of the data in the buffer.
         * @param length is the length of the data in the buffer.
         * @throws IllegalArgumentException if out was not created by this allocator.
         * @throws IllegalArgumentException if (offset + length) exceeds length(buffer).
         * @throws IllegalArgumentException if length is less than minimumAllocationSize().
         * @throws IllegalArgumentException if length is greater than maximumAllocationSize().
         * @throws IllegalStateException if the pool is out-of-memory.
         */
        public default void alloc (OperandStack stack,
                                   byte[] buffer,
                                   int offset,
                                   int length)
        {
            Preconditions.checkArgument(length >= minimumAllocationSize(), AllocationCodes.REQUEST_IS_TOO_SMALL.toString());
            Preconditions.checkArgument(length <= maximumAllocationSize(), AllocationCodes.REQUEST_IS_TOO_LARGE.toString());
            final AllocationCodes retval = tryAlloc(stack, buffer, offset, length);
            Preconditions.checkState(retval != AllocationCodes.OUT_OF_MEMORY, AllocationCodes.OUT_OF_MEMORY.toString());
            Verify.verify(retval == AllocationCodes.OK);
        }

        /**
         * Use this method to allocate an operand.
         *
         * @param stack points to the top of the operand-stack that
         * the newly allocated operand will be pushed onto.
         * @param buffer contains the content of the operand.
         * @param offset is the start position of the data in the buffer.
         * @param length is the length of the data in the buffer.
         * @return a status-code indicating success or the reason for failure.
         * @throws IllegalArgumentException if out was not created by this allocator.
         * @throws IllegalArgumentException if (offset + length) exceeds length(buffer).
         */
        public AllocationCodes tryAlloc (OperandStack stack,
                                         byte[] buffer,
                                         int offset,
                                         int length);
    }

    /**
     * An instance of this class is a space-efficient array of pointers to operand-stacks.
     *
     * <p>
     * You must call close() in order to free the operand-stacks referenced by the array;
     * otherwise, a memory-leak will occur, because the operands may be pooled.
     * </p>
     */
    public interface OperandStackArray
            extends AutoCloseable
    {
        /**
         * Getter.
         *
         * @return the allocator that created this pointer.
         */
        public CascadeAllocator allocator ();

        /**
         * Getter.
         *
         * @return the length of this array.
         */
        public OperandStackArray size ();

        /**
         * Use this method to assign an operand-stack to an element in this array.
         *
         * @param index identifies the element to set.
         * @param value is the new value of the array element, or null, to clear the element.
         * @return this.
         */
        public OperandStackArray set (int index,
                                      OperandStack value);

        /**
         * {@inheritDoc}
         */
        @Override
        public void close ();
    }

    /**
     * An instance of this class is a pointer to the top of an operand-stack.
     *
     * <p>
     * You must call close() in order to free the referenced operand-stack;
     * otherwise, a memory-leak will occur, because the operands may be pooled.
     * </p>
     */
    public interface OperandStack
            extends AutoCloseable
    {
        /**
         * Getter.
         *
         * @return the allocator that created this pointer.
         */
        public CascadeAllocator allocator ();

        /**
         * Getter.
         *
         * @return the allocation-pool of the top operand on the operand-stack,
         * or the anonymous-pool, if the operand-stack is empty.
         */
        public AllocationPool pool ();

        /**
         * Use this method to cause this object to refer to a different operand-stack.
         *
         * @param value points to the other operand-stack.
         * @return this.
         */
        public OperandStack set (OperandStack value);

        /**
         * Use this method to cause this object to refer to a different operand-stack.
         *
         * <p>
         * If the relevant array element is null,
         * then this method is equivalent to clear().
         * </p>
         *
         * @param array contains the other operand-stack.
         * @param index identifies the relevant array element.
         * @return this.
         * @throws IndexOutOfBoundsException if the index is out-of-bounds.
         */
        public OperandStack set (OperandStackArray array,
                                 int index);

        /**
         * Copy this operand-stack.
         *
         * <p>
         * This is a constant-time operation.
         * </p>
         *
         * @return a new operand-stack object.
         */
        public default OperandStack copy ()
        {
            final OperandStack retval = allocator().newOperandStack();
            retval.set(this);
            return retval;
        }

        /**
         * Use this method to cause this method to no longer point to any operand-stack.
         * In other words, this pointer will become a null-pointer.
         * Here a null-pointer is equivalent to an empty operand-stack.
         *
         * @return this.
         */
        public default OperandStack clear ()
        {
            return set(null);
        }

        /**
         * Equivalent: clear().
         */
        @Override
        public default void close ()
        {
            clear();
        }

        /**
         * Getter.
         *
         * @return the size of the top operand on the operand-stack in bytes.
         */
        public int operandSize ();

        /**
         * Getter.
         *
         * @return the maximum size of this operand in bytes.
         */
        public int operandCapacity ();

        /**
         * Getter.
         *
         * @return the number of operands that are on this operand-stack.
         */
        public int stackSize ();

        /**
         * Getter.
         *
         * @return true, iff the operandSize() is zero.
         */
        public default boolean isOperandEmpty ()
        {
            return operandSize() == 0;
        }

        /**
         * Getter.
         *
         * @return true, iff the stackSize() is zero.
         */
        public default boolean isStackEmpty ()
        {
            return stackSize() == 0;
        }

        /**
         * Copy an operand from the top of one operand-stack
         * onto the top of this operand-stack.
         *
         * @param values are the operand(s) to push.
         * @return this.
         */
        public OperandStack push (OperandStack values);

        /**
         * Push an operand onto the top of the stack.
         *
         * <p>
         * True, becomes a single byte equal to one.
         * False, becomes a single byte equal to zero.
         * </p>
         *
         * @param value is the operand to push.
         * @return this.
         */
        public default OperandStack push (boolean value)
        {
            return push((byte) (value ? 1 : 0));
        }

        /**
         * Push an operand onto the top of the stack.
         *
         * @param value is the operand to push.
         * @return this.
         */
        public default OperandStack push (byte value)
        {
            final byte[] array = new byte[1];
            array[0] = value;
            return push(array);
        }

        /**
         * Push an operand onto the top of the stack.
         *
         * @param value is the operand to push.
         * @return this.
         */
        public default OperandStack push (short value)
        {
            return push(Shorts.toByteArray(value));
        }

        /**
         * Push an operand onto the top of the stack.
         *
         * @param value is the operand to push.
         * @return this.
         */
        public default OperandStack push (int value)
        {
            return push(Ints.toByteArray(value));
        }

        /**
         * Push an operand onto the top of the stack.
         *
         * @param value is the operand to push.
         * @return this.
         */
        public default OperandStack push (long value)
        {
            return push(Longs.toByteArray(value));
        }

        /**
         * Push an operand onto the top of the stack.
         *
         * @param value is the operand to push.
         * @return this.
         */
        public default OperandStack push (float value)
        {
            return push(Float.floatToIntBits(value));
        }

        /**
         * Push an operand onto the top of the stack.
         *
         * @param value is the operand to push.
         * @return this.
         */
        public default OperandStack push (double value)
        {
            return push(Longs.toByteArray(Double.doubleToLongBits(value)));
        }

        /**
         * Push an operand onto the top of the stack.
         *
         * <p>
         * This method converts the string to a UTF-8
         * encoded byte-array and then pushes the array.
         * </p>
         *
         * @param value is the operand to push.
         * @return this.
         */
        public default OperandStack push (String value)
        {
            Preconditions.checkNotNull(value, "value");
            return push(value.getBytes(Charset.forName("UTF-8")));
        }

        /**
         * Push a operand onto the top of the stack.
         *
         * @param value is the operand to push.
         * @return this.
         */
        public default OperandStack push (byte[] value)
        {
            return push(value, 0, value.length);
        }

        /**
         * Push a operand onto the top of the stack.
         *
         * @param buffer is the operand to push.
         * @param offset is the start location of the data in the buffer.
         * @param length is the length of the data.
         * @return this.
         * @throws IndexOutOfBoundsException if offset is less-than zero.
         * @throws IllegalArgumentException if length is less-than zero.
         */
        public OperandStack push (byte[] buffer,
                                  int offset,
                                  int length);

        /**
         * Use this method to efficiently retrieve a
         * byte at a given index within this operand.
         *
         * @param index identifies the byte in this operand.
         * @return the byte at the given index.
         * @throws IndexOutOfBoundsException if index is invalid.
         * @throws IllegalStateException if stackSize() is zero.
         */
        public byte byteAt (int index);

        /**
         * Use this method to copy the content of this operand into a given buffer.
         *
         * @param buffer is the buffer that will receive the content.
         * @return the number of bytes copied into the buffer,
         * which may be less than operandSize(), if the buffer is too small.
         * @throws IndexOutOfBoundsException if index is invalid.
         * @throws IllegalStateException if stackSize() is zero.
         */
        public default int copyTo (byte[] buffer)
        {
            return copyTo(buffer, 0);
        }

        /**
         * Use this method to copy the content of this operand into a given buffer,
         * starting at a given offset in the buffer.
         *
         * @param buffer is the buffer that will receive the content.
         * @param offset is an index into the buffer.
         * @return the number of bytes copied into the buffer,
         * which may be less than operandSize(), if the buffer is too small.
         * @throws IndexOutOfBoundsException if index is invalid.
         * @throws IllegalStateException if stackSize() is zero.
         */
        public default int copyTo (byte[] buffer,
                                   int offset)
        {
            return copyTo(buffer, offset, operandSize());
        }

        /**
         * Use this method to copy the content of this operand into a given buffer,
         * starting at a given offset in the buffer.
         *
         * @param buffer is the buffer that will receive the content.
         * @param offset is an index into the buffer.
         * @param length is the maximum number of bytes to copy.
         * @return the number of bytes copied into the buffer,
         * which may be less than operandSize(), if the buffer is too small.
         * @throws IndexOutOfBoundsException if index is invalid.
         * @throws IllegalStateException if stackSize() is zero.
         */
        public int copyTo (byte[] buffer,
                           int offset,
                           int length);

        /**
         * Data Conversion: byte[] to boolean.
         *
         * @return the converted value.
         * @throws IllegalStateException if operandSize() &ne (1).
         * @throws IllegalStateException if stackSize() is zero.
         */
        public default boolean asBoolean ()
        {
            return asByte() != 0;
        }

        /**
         * Data Conversion: byte[] to byte.
         *
         * @return the converted value.
         * @throws IllegalStateException if operandSize() &ne (1).
         * @throws IllegalStateException if stackSize() is zero.
         */
        public default byte asByte ()
        {
            Preconditions.checkState(operandSize() == 1, "Wrong Size");
            return byteAt(0);
        }

        /**
         * Data Conversion: byte[] to short.
         *
         * <p>
         * The bytes must be in big-endian byte-order.
         * </p>
         *
         * @return the converted value.
         * @throws IllegalStateException if operandSize() &ne (2).
         * @throws IllegalStateException if stackSize() is zero.
         */
        public default short asShort ()
        {
            Preconditions.checkState(operandSize() == 2, "Wrong Size");
            return Shorts.fromBytes(byteAt(0), byteAt(1));
        }

        /**
         * Data Conversion: byte[] to int.
         *
         * <p>
         * The bytes must be in big-endian byte-order.
         * </p>
         *
         * @return the converted value.
         * @throws IllegalStateException if operandSize() &ne (4).
         * @throws IllegalStateException if stackSize() is zero.
         */
        public default int asInt ()
        {
            Preconditions.checkState(operandSize() == 4, "Wrong Size");
            return Ints.fromBytes(byteAt(0),
                                  byteAt(1),
                                  byteAt(2),
                                  byteAt(3));
        }

        /**
         * Data Conversion: byte[] to long.
         *
         * <p>
         * The bytes must be in big-endian byte-order.
         * </p>
         *
         * @return the converted value.
         * @throws IllegalStateException if operandSize() &ne (8).
         * @throws IllegalStateException if stackSize() is zero.
         */
        public default long asLong ()
        {
            Preconditions.checkState(operandSize() == 8, "Wrong Size");
            return Longs.fromBytes(byteAt(0),
                                   byteAt(1),
                                   byteAt(2),
                                   byteAt(3),
                                   byteAt(4),
                                   byteAt(5),
                                   byteAt(6),
                                   byteAt(7));
        }

        /**
         * Data Conversion: byte[] to float.
         *
         * @return the converted value.
         * @throws IllegalStateException if operandSize() &ne (4).
         * @throws IllegalStateException if stackSize() is zero.
         */
        public default float asFloat ()
        {
            Preconditions.checkState(operandSize() == 4, "Wrong Size");
            return Float.intBitsToFloat(asInt());
        }

        /**
         * Data Conversion: byte[] to double.
         *
         * @return the converted value.
         * @throws IllegalStateException if operandSize() &ne (8).
         * @throws IllegalStateException if stackSize() is zero.
         */
        public default double asDouble ()
        {
            Preconditions.checkState(operandSize() == 8, "Wrong Size");
            return Double.longBitsToDouble(asLong());
        }

        /**
         * Data Conversion: Encoded byte[] to String.
         *
         * <p>
         * The bytes must be UTF-8 encoded.
         * </p>
         *
         * <p>
         * All of the bytes are considered to be part of the string.
         * No size header, etc, is present in the byte[].
         * </p>
         *
         * @return the converted value.
         * @throws IllegalStateException if stackSize() is zero.
         */
        public default String asString ()
        {
            return new String(asByteArray(), Charset.forName("UTF-8"));
        }

        /**
         * Getter.
         *
         * @return the content of the top operand on the operand-stack.
         * @throws IllegalStateException if stackSize() is zero.
         */
        public default byte[] asByteArray ()
        {
            final byte[] array = new byte[operandSize()];
            for (int i = 0; i < array.length; i++)
            {
                array[i] = byteAt(i);
            }
            return array;
        }

        /**
         * Pop a single operand off the top of the operand-stack.
         *
         * @return this.
         * @throws IllegalStateException if stackSize() is zero.
         */
        public OperandStack pop ();
    }

    /**
     * Use this method to create a new operand-stack.
     *
     * @return a new empty operand-stack.
     */
    public OperandStack newOperandStack ();

    /**
     * Use this method to create a new operand-stack-array.
     *
     * @param size is the length of the new array.
     * @return a new empty operand-stack-array.
     */
    public OperandStackArray newOperandStackArray (int size);

    /**
     * Getter.
     *
     * @return a map that maps the names of pools to the pools themselves.
     */
    public Map<String, AllocationPool> pools ();

    /**
     * Use this method to retrieve the anonymous-pool.
     *
     * @return a pool that routes allocation requests to the best-matching default pool.
     */
    public AllocationPool anon ();
}