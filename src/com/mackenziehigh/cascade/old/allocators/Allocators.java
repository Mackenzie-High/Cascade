package com.mackenziehigh.cascade.old.allocators;

/**
 *
 */
public final class Allocators
{
    public static DynamicAllocator newDynamicAllocator ()
    {
        return null;
    }

    public static DynamicAllocator newDynamicAllocator (final int minimumSize,
                                                        final int maximumSize)
    {
        return null;
    }

    public static FixedAllocator newFixedAllocator (final int bufferCount,
                                                    final int minimumSize,
                                                    final int maximumSize)
    {
        return null;
    }

    public static CompositeAllocator newCompositeAllocator (final Allocator fallback,
                                                            final Allocator... members)
    {
        return null;
    }

    public static CompositeAllocator newCompositeAllocator (final Allocator fallback,
                                                            final Iterable<Allocator> members)
    {
        return null;
    }
}
