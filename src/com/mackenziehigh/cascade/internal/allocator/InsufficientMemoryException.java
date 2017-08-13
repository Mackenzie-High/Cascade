package com.mackenziehigh.cascade.internal.allocator;

import com.google.common.base.Preconditions;

/**
 * This type of exception indicates that a MemoryAllocator
 * cannot allocate memory due to insufficient resources.
 */
public class InsufficientMemoryException
        extends RuntimeException
{
    private final MemoryAllocator allocator;

    private final long demand;

    /**
     * Sole Constructor.
     *
     * @param allocator is throwing this exception.
     * @param demand is the number of requested bytes.
     */
    public InsufficientMemoryException (final MemoryAllocator allocator,
                                        final long demand)
    {
        Preconditions.checkNotNull(allocator, "allocator");
        this.allocator = allocator;
        this.demand = demand;
    }

    public MemoryAllocator getAllocator ()
    {
        return allocator;
    }

    public long getDemand ()
    {
        return demand;
    }
}
