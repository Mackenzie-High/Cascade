package com.mackenziehigh.cascade.allocators;

/**
 * An allocator that delegates to other allocators.
 *
 * <p>
 * A composite-allocator consists of a series of member-allocators and an optional fallback-allocator.
 * </p>
 *
 * <p>
 * When an allocation request is made for (X) bytes, the composite-allocator will attempt
 * to locate a member-allocator whose minimum allocation-size is less-or-equal-to (X)
 * and whose maximum allocation-size is greater-or-equal-to (X). If such a member-allocator
 * is found, then the allocation request will be forwarded to that member-allocator.
 * If no such member-allocator is found and a fallback-pool was specified,
 * then the allocation request will be forwarded to the fallback-allocator.
 * </p>
 *
 * <p>
 * As usual, each member-allocator has its own minimum and maximum allocation-sizes.
 * None of the member-allocators can have overlapping allocation-sizes.
 * No such restriction is placed onto the fallback-allocator.
 * The member-pools are not required to be contiguous.
 * </p>
 *
 * <p>
 * An logarithmic-time, O(log N), search algorithm is used to locate the matching member-allocator.
 * Thus, using a composite-allocator should not add a meaningful amount of allocation overhead.
 * </p>
 */
public final class CompositeAllocator
{

}
