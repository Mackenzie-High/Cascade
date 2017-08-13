package com.mackenziehigh.cascade.internal.allocator;

/**
 * This type of exception indicates that a MemoryAllocator
 * does not recognize a given integer as a valid memory pointer.
 */
public class InvalidPointerException
        extends RuntimeException
{
    private final MemoryAllocator allocator;

    private final int pointer;

    /**
     * Sole Constructor.
     *
     * @param allocator is throwing this exception.
     * @param ptr is not recognized by the given allocator.
     */
    public InvalidPointerException (final MemoryAllocator allocator,
                                    final int ptr)
    {
        this.allocator = allocator;
        this.pointer = ptr;
    }

    public MemoryAllocator getAllocator ()
    {
        return allocator;
    }

    public int getPointer ()
    {
        return pointer;
    }
}
