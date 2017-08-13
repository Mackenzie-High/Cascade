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
     * @param size is the number of bytes to allocate.
     * @return a pointer to the allocated memory,
     * or a negative number, if an error occurs.
     */
    public int malloc (int size);

    /**
     * This method increments reference-counter associated
     * with the memory pointed to by the given pointer.
     *
     * @param ptr identifies a block of memory.
     */
    public void increment (int ptr);

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
    public void decrement (int ptr);

    /**
     * This method retrieves the length of a block of memory.
     *
     * @param ptr identifies a block of memory.
     * @return the number of bytes in the block of memory.
     */
    public int capacityOf (int ptr);

    /**
     * This method retrieves the length of data in a block of memory.
     *
     * @param ptr identifies a block of memory.
     * @return the length of the data stored in a block of memory.
     */
    public int sizeOf (int ptr);

    /**
     * This method sets the bytes contained inside of a memory block.
     *
     * <p>
     * Equivalent: <code>write(ptr, data, 0, data.length)</code>
     * </p>
     *
     * @param ptr identifies a block of memory.
     * @param data contains the bytes to assign to the memory block.
     * @return true, iff the assignment was successful.
     */
    public default boolean set (final int ptr,
                                final byte[] data)
    {
        return set(ptr, data, 0, data.length);
    }

    /**
     * This method sets the bytes contained inside of a memory block.
     *
     * <p>
     * This method will return false, if the given array contains
     * more bytes than can be fit within the block of memory.
     * </p>
     *
     * @param ptr identifies a block of memory.
     * @param data contains the bytes to assign to the memory block.
     * @param offset is the offset within the given array.
     * @param length is the number of bytes to read from the given array.
     * @return true, iff the assignment was successful.
     */
    public boolean set (int ptr,
                        byte[] data,
                        int offset,
                        int length);

    /**
     * This method retrieves the data contained within a block of memory.
     *
     * @param ptr identifies a block of memory.
     * @param data is a buffer that will receive the data from the block of memory.
     * @return the number of bytes read from the block of memory,
     * or a negative number, if something went wrong.
     */
    public int get (int ptr,
                    byte[] data);

}
