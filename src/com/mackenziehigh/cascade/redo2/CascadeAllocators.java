package com.mackenziehigh.cascade.redo2;

/**
 *
 * @author mackenzie
 */
public final class CascadeAllocators
{
    public static CascadeAllocator newDynamicAllocator ()
    {
        return newDynamicAllocator(0, Integer.MAX_VALUE);
    }

    public static CascadeAllocator newDynamicAllocator (final int minimumSize,
                                                        final int maximumSize)
    {
        return null;
    }
}
