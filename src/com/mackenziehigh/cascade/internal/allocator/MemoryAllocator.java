package com.mackenziehigh.cascade.internal.allocator;

/**
 * An instance of this interface can be used to efficiently allocate blocks of bytes.
 *
 * <p>
 * Instances of this interface are usually used to relieve pressure on the GC.
 * </p>
 */
public interface MemoryAllocator
{
    /**
     * Use this method to allocate a block of memory.
     *
     * <p>
     * The reference-count associated with the block
     * of memory will be one upon return of this method.
     * </p>
     *
     * @param data contains the bytes to assign to the memory block.
     * @param offset is the offset within the given array.
     * @param length is the number of bytes to read from the given array.
     * @return a pointer to the allocated memory,
     * or a negative number, if an error occurs.
     */
    public long malloc (byte[] data,
                        int offset,
                        int length);

    /**
     * This method increments reference-counter associated
     * with the memory pointed to by the given pointer.
     *
     * @param ptr identifies a block of memory.
     */
    public void increment (long ptr);

    /**
     * This method decrements reference-counter associated
     * with the memory pointed to by the given pointer.
     *
     * <p>
     * If the reference-count reaches zero,
     * then the memory will be freed.
     * </p>
     *
     * @param ptr identifies a block of memory.
     */
    public void decrement (long ptr);

    /**
     * This method retrieves the length of data in a block of memory.
     *
     * @param ptr identifies a block of memory.
     * @return the length of the data stored in a block of memory.
     */
    public int sizeOf (long ptr);

    /**
     * This method retrieves the data contained within a block of memory.
     *
     * @param ptr identifies a block of memory.
     * @param data is a buffer that will receive the data from the block of memory.
     * @return the number of bytes read from the block of memory,
     * or a negative number, if something went wrong.
     */
    public int get (long ptr,
                    byte[] data);

}
