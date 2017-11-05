package com.mackenziehigh.cascade.exceptions;

import com.google.common.base.Preconditions;
import com.mackenziehigh.cascade.CascadeAllocator;

/**
 * This type of exception indicates that a MemoryAllocator
 * cannot allocate memory due to insufficient resources.
 */
public class InsufficientMemoryException
        extends RuntimeException
{
    private final CascadeAllocator allocator;

    private final long demand;

    /**
     * Sole Constructor.
     *
     * @param allocator is throwing this exception.
     * @param demand is the number of requested bytes.
     */
    public InsufficientMemoryException (final CascadeAllocator allocator,
                                        final long demand)
    {
        Preconditions.checkNotNull(allocator, "allocator");
        this.allocator = allocator;
        this.demand = demand;
    }

    public CascadeAllocator getAllocator ()
    {
        return allocator;
    }

    public long getDemand ()
    {
        return demand;
    }
}
