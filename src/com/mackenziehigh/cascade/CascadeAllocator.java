package com.mackenziehigh.cascade;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Use this interface to allocate operands.
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
     * An instance of this class is a pointer to the top of an operand-stack.
     *
     * <p>
     * This interface implements AutoCloseable,
     * so that try-with-resources statements
     * can be used to clear the pointer.
     * </p>
     *
     * <p>
     * <b>Warning:</b> Unless explicitly stated otherwise,
     * instances of this interface are not thread-safe.
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
         * Use this method to cause this pointer to refer to a different operand.
         *
         * @param value is a pointer to the other operand.
         * @return this.
         */
        public OperandStack assign (OperandStack value);

        /**
         * Use this method to cause this method to no longer point to any operand.
         * In other words, this pointer will become a null-pointer.
         *
         * @return this.
         */
        public OperandStack clear ();

        /**
         * Equivalent: clear().
         */
        @Override
        public void close ();

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
        public boolean isOperandEmpty ();

        /**
         * Getter.
         *
         * @return true, iff the stackSize() is zero.
         */
        public boolean isStackEmpty ();

        /**
         * Use this method to efficiently retrieve a
         * byte at a given index within this operand.
         *
         * @param index identifies the byte in this operand.
         * @return the byte at the given index.
         * @throws IndexOutOfBoundsException if index is invalid.
         */
        public byte byteAt (int index);

        /**
         * Use this method to copy the content of this operand into a given buffer.
         *
         * @param buffer is the buffer that will receive the content.
         * @return the number of bytes copied into the buffer,
         * which may be less than the size() of the operand,
         * if the buffer is too small.
         */
        public int memcpy (byte[] buffer);

        /**
         * Use this method to copy the content of this operand into a given buffer,
         * starting at a given offset in the buffer.
         *
         * @param buffer is the buffer that will receive the content.
         * @param offset is an index into the buffer.
         * @return (-1) if the offset is out-of-range; otherwise,
         * return the number of bytes copied into the buffer,
         * which may be less than the size() of the operand,
         * if the buffer is too small.
         */
        public int memcpy (byte[] buffer,
                           int offset);

        /**
         * Data Conversion: byte[] to boolean.
         *
         * @return the converted value.
         * @throws IllegalStateException if size() &ne (1).
         */
        public boolean asBoolean ();

        /**
         * Data Conversion: byte[] to char.
         *
         * <p>
         * The bytes must be in big-endian byte-order.
         * </p>
         *
         * @return the converted value.
         * @throws IllegalStateException if size() &ne (2).
         */
        public char asChar ();

        /**
         * Data Conversion: byte[] to byte.
         *
         * @return the converted value.
         * @throws IllegalStateException if size() &ne (1).
         */
        public byte asByte ();

        /**
         * Data Conversion: byte[] to short.
         *
         * <p>
         * The bytes must be in big-endian byte-order.
         * </p>
         *
         * @return the converted value.
         * @throws IllegalStateException if size() &ne (2).
         */
        public short asShort ();

        /**
         * Data Conversion: byte[] to int.
         *
         * <p>
         * The bytes must be in big-endian byte-order.
         * </p>
         *
         * @return the converted value.
         * @throws IllegalStateException if size() &ne (4).
         */
        public int asInt ();

        /**
         * Data Conversion: byte[] to long.
         *
         * <p>
         * The bytes must be in big-endian byte-order.
         * </p>
         *
         * @return the converted value.
         * @throws IllegalStateException if size() &ne (8).
         */
        public long asLong ();

        /**
         * Data Conversion: byte[] to float.
         *
         * @return the converted value.
         * @throws IllegalStateException if size() &ne (4).
         */
        public float asFloat ();

        /**
         * Data Conversion: byte[] to double.
         *
         * @return the converted value.
         * @throws IllegalStateException if size() &ne (2).
         */
        public double asDouble ();

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
         * @throws IllegalStateException if size() &ne (2).
         */
        public String asString ();

        /**
         * Data Conversion: byte[] to boolean[].
         *
         * <p>
         * Each byte represents a single boolean value.
         * A byte equal to zero equates to false.
         * A byte not-equal to zero equates to true.
         * </p>
         *
         * @return the converted value.
         */
        public boolean[] asBooleanArray ();

        /**
         * Data Conversion: byte[] to char[].
         *
         * <p>
         * For each element, the bytes must be in big-endian byte-order.
         * </p>
         *
         * @return the converted value.
         * @throws IllegalStateException if size() &#37 (2) &ne (0).
         */
        public char[] asCharArray ();

        /**
         * Data Conversion: byte[] to byte[].
         *
         * @return the converted value.
         */
        public byte[] asByteArray ();

        /**
         * Data Conversion: byte[] to short[].
         *
         * <p>
         * For each element, the bytes must be in big-endian byte-order.
         * </p>
         *
         * @return the converted value.
         * @throws IllegalStateException if size() &#37 (2) &ne (0).
         */
        public short[] asShortArray ();

        /**
         * Data Conversion: byte[] to int[].
         *
         * <p>
         * For each element, the bytes must be in big-endian byte-order.
         * </p>
         *
         * @return the converted value.
         * @throws IllegalStateException if size() &#37 (4) &ne (0).
         */
        public int[] asIntArray ();

        /**
         * Data Conversion: byte[] to long[].
         *
         * <p>
         * For each element, the bytes must be in big-endian byte-order.
         * </p>
         *
         * @return the converted value.
         * @throws IllegalStateException if size() &#37 (8) &ne (0).
         */
        public long[] asLongArray ();

        /**
         * Data Conversion: byte[] to float[].
         *
         * @return the converted value.
         * @throws IllegalStateException if size() &#37 (4) &ne (0).
         */
        public float[] asFloatArray ();

        /**
         * Data Conversion: byte[] to double[].
         *
         * @return the converted value.
         * @throws IllegalStateException if size() &#37 (8) &ne (0).
         */
        public double[] asDoubleArray ();

        /**
         * Data Conversion: Encoded byte[] to String[].
         *
         * <p>
         * Each string must be UTF-8 encoded.
         * </p>
         *
         * <p>
         * The first four bytes is a big-endian integer,
         * which specifies the number of array elements.
         * </p>
         *
         * <p>
         * Each element in the array consists of a header and body.
         * The header is big-endian encoded four-byte integer that
         * specifies the number (N) of bytes in the body.
         * The body is (N) bytes that are the UTF-8 string itself.
         * </p>
         *
         * @return the converted value.
         * @throws IllegalStateException if size() &lt (4).
         * @throws IllegalStateException if the conversion is not
         * possible due to invalid headers in the data.
         */
        public String[] asStringArray ();

        /**
         * Copy operands from the top of one operand-stack
         * onto the top of this operand-stack.
         *
         * <p>
         * The default implementation of this method is recursive,
         * which avoids the need for unnecessary Java heap allocations,
         * but may not be suitable for all use-cases.
         * </p>
         *
         * @param values are the operand(s) to push.
         * @param count is the number of operands to push.
         * @return this.
         */
        public OperandStack push (OperandStack values,
                                  int count);

        /**
         * Push a operand onto the top of the stack.
         *
         * @param value is the operand to push.
         * @return this.
         */
        public OperandStack pushZ (boolean value);

        /**
         * Push a operand onto the top of the stack.
         *
         * @param value is the operand to push.
         * @return this.
         */
        public OperandStack pushC (char value);

        /**
         * Push a operand onto the top of the stack.
         *
         * @param value is the operand to push.
         * @return this.
         */
        public OperandStack pushB (byte value);

        /**
         * Push a operand onto the top of the stack.
         *
         * @param value is the operand to push.
         * @return this.
         */
        public OperandStack pushS (short value);

        /**
         * Push a operand onto the top of the stack.
         *
         * @param value is the operand to push.
         * @return this.
         */
        public OperandStack pushI (int value);

        /**
         * Push a operand onto the top of the stack.
         *
         * @param value is the operand to push.
         * @return this.
         */
        public OperandStack pushJ (long value);

        /**
         * Push a operand onto the top of the stack.
         *
         * @param value is the operand to push.
         * @return this.
         */
        public OperandStack pushF (float value);

        /**
         * Push a operand onto the top of the stack.
         *
         * @param value is the operand to push.
         * @return this.
         */
        public OperandStack pushD (double value);

        /**
         * Push a operand onto the top of the stack.
         *
         * @param value is the operand to push.
         * @return this.
         */
        public OperandStack pushStr (String value);

        /**
         * Push a operand onto the top of the stack.
         *
         * @param value is the operand to push.
         * @return this.
         */
        public OperandStack pushZA (boolean[] value);

        /**
         * Push a operand onto the top of the stack.
         *
         * @param value is the operand to push.
         * @return this.
         */
        public OperandStack pushCA (char[] value);

        /**
         * Push a operand onto the top of the stack.
         *
         * @param value is the operand to push.
         * @return this.
         */
        public OperandStack pushBA (byte[] value);

        /**
         * Push a operand onto the top of the stack.
         *
         * @param buffer is the operand to push.
         * @param offset is the start location of the data in the buffer.
         * @param length is the length of the data.
         * @return this.
         */
        public OperandStack pushBA (byte[] buffer,
                                    int offset,
                                    int length);

        /**
         * Push a operand onto the top of the stack.
         *
         * @param value is the operand to push.
         * @return this.
         */
        public OperandStack pushSA (short[] value);

        /**
         * Push a operand onto the top of the stack.
         *
         * @param value is the operand to push.
         * @return this.
         */
        public OperandStack pushIA (int[] value);

        /**
         * Push a operand onto the top of the stack.
         *
         * @param value is the operand to push.
         * @return this.
         */
        public OperandStack pushJA (long[] value);

        /**
         * Push a operand onto the top of the stack.
         *
         * @param value is the operand to push.
         * @return this.
         */
        public OperandStack pushFA (float[] value);

        /**
         * Push a operand onto the top of the stack.
         *
         * @param value is the operand to push.
         * @return this.
         */
        public OperandStack pushDA (double[] value);

        /**
         * Push a operand onto the top of the stack.
         *
         * @param value is the operand to push.
         * @return this.
         */
        public OperandStack pushStrA (String[] value);

        /**
         * Pop a single operand off the top of the operand-stack.
         *
         * @return this.
         */
        public OperandStack pop ();

        /**
         * Pop operands off the top of the operand-stack.
         *
         * @param count is the number of operands to pop.
         * @return this.
         */
        public OperandStack pop (int count);

        /**
         * Use this method to find an operand and assign it to a pointer.
         *
         * @param out will be assigned the operand.
         * @param depth is how far down the operand-stack the operand is located.
         * @return this.
         */
        public OperandStack get (OperandStack out,
                                 int depth);

        /**
         * Use this method to duplicate the operand on the top of the operand-stack.
         *
         * @return this.
         */
        public OperandStack dup ();

        /**
         * Use this method to duplicate the operand(s) on the top of the operand-stack.
         *
         * @param count is the number of operands to duplicate.
         * @return this.
         */
        public OperandStack dup (int count);

        /**
         * Use this method to reverse the order of the top two operands on the operand-stack.
         *
         * @return this.
         */
        public OperandStack swap ();

        /**
         * Arithmetic Operation: DIVIDE.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Divide the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type double.
         * </p>
         *
         * <p>
         * The result will be of primitive-type double.
         * </p>
         *
         * @return this.
         */
        public OperandStack divD ();

        /**
         * Arithmetic Operation: DIVIDE.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Divide the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type float.
         * </p>
         *
         * <p>
         * The result will be of primitive-type float.
         * </p>
         *
         * @return this.
         */
        public OperandStack divF ();

        /**
         * Arithmetic Operation: DIVIDE.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Divide the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type long.
         * </p>
         *
         * <p>
         * The result will be of primitive-type long.
         * </p>
         *
         * @return this.
         */
        public OperandStack divJ ();

        /**
         * Arithmetic Operation: DIVIDE.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Divide the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type int.
         * </p>
         *
         * <p>
         * The result will be of primitive-type int.
         * </p>
         *
         * @return this.
         */
        public OperandStack divI ();

        /**
         * Arithmetic Operation: DIVIDE.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Divide the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type short.
         * </p>
         *
         * <p>
         * The result will be of primitive-type short.
         * </p>
         *
         * @return this.
         */
        public OperandStack divS ();

        /**
         * Arithmetic Operation: DIVIDE.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Divide the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type byte.
         * </p>
         *
         * <p>
         * The result will be of primitive-type byte.
         * </p>
         *
         * @return this.
         */
        public OperandStack divB ();

        /**
         * Arithmetic Operation: DIVIDE.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Divide the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type char.
         * </p>
         *
         * <p>
         * The result will be of primitive-type char.
         * </p>
         *
         * @return this.
         */
        public OperandStack divC ();

        /**
         * Arithmetic Operation: MODULO.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Modulo the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type double.
         * </p>
         *
         * <p>
         * The result will be of primitive-type double.
         * </p>
         *
         * @return this.
         */
        public OperandStack modD ();

        /**
         * Arithmetic Operation: MODULO.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Modulo the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type float.
         * </p>
         *
         * <p>
         * The result will be of primitive-type float.
         * </p>
         *
         * @return this.
         */
        public OperandStack modF ();

        /**
         * Arithmetic Operation: MODULO.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Modulo the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type long.
         * </p>
         *
         * <p>
         * The result will be of primitive-type long.
         * </p>
         *
         * @return this.
         */
        public OperandStack modJ ();

        /**
         * Arithmetic Operation: MODULO.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Modulo the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type int.
         * </p>
         *
         * <p>
         * The result will be of primitive-type int.
         * </p>
         *
         * @return this.
         */
        public OperandStack modI ();

        /**
         * Arithmetic Operation: MODULO.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Modulo the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type short.
         * </p>
         *
         * <p>
         * The result will be of primitive-type short.
         * </p>
         *
         * @return this.
         */
        public OperandStack modS ();

        /**
         * Arithmetic Operation: MODULO.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Modulo the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type byte.
         * </p>
         *
         * <p>
         * The result will be of primitive-type byte.
         * </p>
         *
         * @return this.
         */
        public OperandStack modB ();

        /**
         * Arithmetic Operation: MODULO.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Modulo the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type char.
         * </p>
         *
         * <p>
         * The result will be of primitive-type char.
         * </p>
         *
         * @return this.
         */
        public OperandStack modC ();

        /**
         * Arithmetic Operation: MULTIPLY.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Multiply the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type double.
         * </p>
         *
         * <p>
         * The result will be of primitive-type double.
         * </p>
         *
         * @return this.
         */
        public OperandStack mulD ();

        /**
         * Arithmetic Operation: MULTIPLY.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Multiply the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type float.
         * </p>
         *
         * <p>
         * The result will be of primitive-type float.
         * </p>
         *
         * @return this.
         */
        public OperandStack mulF ();

        /**
         * Arithmetic Operation: MULTIPLY.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Multiply the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type long.
         * </p>
         *
         * <p>
         * The result will be of primitive-type long.
         * </p>
         *
         * @return this.
         */
        public OperandStack mulJ ();

        /**
         * Arithmetic Operation: MULTIPLY.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Multiply the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type int.
         * </p>
         *
         * <p>
         * The result will be of primitive-type int.
         * </p>
         *
         * @return this.
         */
        public OperandStack mulI ();

        /**
         * Arithmetic Operation: MULTIPLY.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Multiply the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type short.
         * </p>
         *
         * <p>
         * The result will be of primitive-type short.
         * </p>
         *
         * @return this.
         */
        public OperandStack mulS ();

        /**
         * Arithmetic Operation: MULTIPLY.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Multiply the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type byte.
         * </p>
         *
         * <p>
         * The result will be of primitive-type byte.
         * </p>
         *
         * @return this.
         */
        public OperandStack mulB ();

        /**
         * Arithmetic Operation: MULTIPLY.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Multiply the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type char.
         * </p>
         *
         * <p>
         * The result will be of primitive-type char.
         * </p>
         *
         * @return this.
         */
        public OperandStack mulC ();

        /**
         * Arithmetic Operation: ADD.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Add the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type double.
         * </p>
         *
         * <p>
         * The result will be of primitive-type double.
         * </p>
         *
         * @return this.
         */
        public OperandStack addD ();

        /**
         * Arithmetic Operation: ADD.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Add the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type float.
         * </p>
         *
         * <p>
         * The result will be of primitive-type float.
         * </p>
         *
         * @return this.
         */
        public OperandStack addF ();

        /**
         * Arithmetic Operation: ADD.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Add the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type long.
         * </p>
         *
         * <p>
         * The result will be of primitive-type long.
         * </p>
         *
         * @return this.
         */
        public OperandStack addJ ();

        /**
         * Arithmetic Operation: ADD.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Add the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type int.
         * </p>
         *
         * <p>
         * The result will be of primitive-type int.
         * </p>
         *
         * @return this.
         */
        public OperandStack addI ();

        /**
         * Arithmetic Operation: ADD.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Add the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type short.
         * </p>
         *
         * <p>
         * The result will be of primitive-type short.
         * </p>
         *
         * @return this.
         */
        public OperandStack addS ();

        /**
         * Arithmetic Operation: ADD.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Add the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type byte.
         * </p>
         *
         * <p>
         * The result will be of primitive-type byte.
         * </p>
         *
         * @return this.
         */
        public OperandStack addB ();

        /**
         * Arithmetic Operation: ADD.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Add the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type char.
         * </p>
         *
         * <p>
         * The result will be of primitive-type char.
         * </p>
         *
         * @return this.
         */
        public OperandStack addC ();

        /**
         * Arithmetic Operation: SUBTRACT.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Subtract the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type double.
         * </p>
         *
         * <p>
         * The result will be of primitive-type double.
         * </p>
         *
         * @return this.
         */
        public OperandStack subD ();

        /**
         * Arithmetic Operation: SUBTRACT.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Subtract the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type float.
         * </p>
         *
         * <p>
         * The result will be of primitive-type float.
         * </p>
         *
         * @return this.
         */
        public OperandStack subF ();

        /**
         * Arithmetic Operation: SUBTRACT.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Subtract the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type long.
         * </p>
         *
         * <p>
         * The result will be of primitive-type long.
         * </p>
         *
         * @return this.
         */
        public OperandStack subJ ();

        /**
         * Arithmetic Operation: SUBTRACT.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Subtract the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type int.
         * </p>
         *
         * <p>
         * The result will be of primitive-type int.
         * </p>
         *
         * @return this.
         */
        public OperandStack subI ();

        /**
         * Arithmetic Operation: SUBTRACT.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Subtract the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type short.
         * </p>
         *
         * <p>
         * The result will be of primitive-type short.
         * </p>
         *
         * @return this.
         */
        public OperandStack subS ();

        /**
         * Arithmetic Operation: SUBTRACT.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Subtract the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type byte.
         * </p>
         *
         * <p>
         * The result will be of primitive-type byte.
         * </p>
         *
         * @return this.
         */
        public OperandStack subB ();

        /**
         * Arithmetic Operation: SUBTRACT.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Subtract the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of primitive-type char.
         * </p>
         *
         * <p>
         * The result will be of primitive-type char.
         * </p>
         *
         * @return this.
         */
        public OperandStack subC ();

        /**
         * Concatenate Two Strings.
         *
         * <p>
         * Pop the right operand off the stack.
         * Pop the left operand off the stack.
         * Divide the operands.
         * Push the result onto the stack.
         * </p>
         *
         * <p>
         * Both operands must be of type String.
         * </p>
         *
         * <p>
         * The result will be of type String.
         * </p>
         *
         * @return this.
         */
        public OperandStack concat ();

        /**
         * Conversion: double TO double.
         *
         * <p>
         * Pop and operand off of the stack.
         * Perform the conversion.
         * Push the result onto the stack.
         * </p>
         *
         * @return this.
         */
        public OperandStack convertD2D ();

        /**
         * Conversion: double TO float.
         *
         * <p>
         * Pop and operand off of the stack.
         * Perform the conversion.
         * Push the result onto the stack.
         * </p>
         *
         * @return this.
         */
        public OperandStack convertD2F ();

        /**
         * Conversion: double TO long.
         *
         * <p>
         * Pop and operand off of the stack.
         * Perform the conversion.
         * Push the result onto the stack.
         * </p>
         *
         * @return this.
         */
        public OperandStack convertD2J ();

        /**
         * Conversion: double TO int.
         *
         * <p>
         * Pop and operand off of the stack.
         * Perform the conversion.
         * Push the result onto the stack.
         * </p>
         *
         * @return this.
         */
        public OperandStack convertD2I ();

        /**
         * Conversion: double TO short.
         *
         * <p>
         * Pop and operand off of the stack.
         * Perform the conversion.
         * Push the result onto the stack.
         * </p>
         *
         * @return this.
         */
        public OperandStack convertD2S ();

        /**
         * Conversion: double TO byte.
         *
         * <p>
         * Pop and operand off of the stack.
         * Perform the conversion.
         * Push the result onto the stack.
         * </p>
         *
         * @return this.
         */
        public OperandStack convertD2B ();

        /**
         * Conversion: double TO char.
         *
         * <p>
         * Pop and operand off of the stack.
         * Perform the conversion.
         * Push the result onto the stack.
         * </p>
         *
         * @return this.
         */
        public OperandStack convertD2C ();

        public OperandStack match (Predicate<OperandStack> functor);

    }

    /**
     * Use this method to create a new operand-stack.
     *
     * @return a new empty operand-stack.
     */
    public OperandStack newOperandStack ();

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
