package com.mackenziehigh.cascade.internal.pumps3;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class provides a thread-safe round-robin selection algorithm
 * for selecting elements from a list based on counters.
 *
 * @param <E> is the type of the selectable elements.
 */
public final class RoundRobinSelector<E>
{
    private final List<E> elements;

    private final long[] counts;

    public RoundRobinSelector (final List<E> elements)
    {
        this.elements = new CopyOnWriteArrayList<>(elements);
        this.counts = new long[elements.size()];
    }

    /**
     * Use this method to increment the count associated with an element.
     *
     * @param index identifies the element whose counter will increment.
     */
    public synchronized void increment (final int index)
    {
        ++counts[index];
    }

    /**
     * Use this method to decrement the count associated with an element.
     *
     * @param index identifies the element whose counter will decrement.
     */
    public synchronized void decrement (final int index)
    {
        if (counts[index] == 0)
        {
            ++counts[index];
        }
    }

    /**
     * Getter.
     *
     * @param index identifies the element whose counter will be returned.
     * @return the counter at the given index.
     */
    public synchronized long count (final int index)
    {
        return counts[index];
    }

    /**
     * Use this method to select the first element after the given
     * index whose counter is non-zero, wrapping around, if necessary.
     *
     * <p>
     * if an element is found, then its counter will be decremented.
     * </p>
     *
     * @param index identifies the starting position of the search.
     * @return the first element whose count is non-zero, or null,
     * if no such element was found during the search.
     */
    public synchronized E select (final int index)
    {
        int i = Math.max(Math.min(index, elements.size() - 1), 0);
        long p = 0;

        do
        {
            i = i + 1 < elements.size() ? i + 1 : 0;
            p = counts[i];
        }
        while (i != index && p == 0);

        if (p > 0)
        {
            decrement(i);
            return elements.get(i);
        }
        else
        {
            return null;
        }
    }

    public static void main (String[] args)
    {
        final List<String> girls = ImmutableList.of("Autumn", "Erin", "Emma", "Lana", "Rachel", "Jenna");
        final RoundRobinSelector rs = new RoundRobinSelector(girls);
        rs.increment(3);
        rs.increment(5);

        System.out.println(rs.select(5));
        System.out.println(rs.select(4));
    }
}
