package com.mackenziehigh.cascade.internal.messages;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * An instance of this class is a map that maps finite non-overlapping
 * inclusive positive integral ranges to user-specified values.
 *
 * <p>
 * This class considers zero to be a positive number.
 * </p>
 */
public final class PositiveIntRangeMap<E>
{
    /**
     * An instance of this class maps a finite inclusive
     * contiguous set of integers to a user-specified value.
     *
     * @param <T> is the type of user-specified value.
     */
    public static final class RangeEntry<T>
    {
        public final int minimum;

        public final int maximum;

        public final T value;

        public RangeEntry (final int minimum,
                           final int maximum,
                           final T value)
        {
            Preconditions.checkArgument(minimum >= 0, "minimum < 0");
            Preconditions.checkArgument(minimum <= maximum, "minimum > maximum");
            this.minimum = minimum;
            this.maximum = maximum;
            this.value = value;
        }
    }

    private final CopyOnWriteArrayList<RangeEntry<E>> entries;

    /**
     * Sole Constructor.
     *
     * @param ranges are the user-specified mappings to store herein.
     */
    public PositiveIntRangeMap (final List<RangeEntry<E>> ranges)
    {
        /**
         * Ensure that the list facilitates fast thread-safe random-access.
         * Strictly speaking, we could just use an ArrayList here,
         * since only this constructor will write to the array.
         */
        this.entries = new CopyOnWriteArrayList<>(ranges);

        /**
         * Sort the array based on the minimum value of each range therein.
         */
        Collections.sort(entries, (x, y) -> Integer.compare(x.minimum, y.minimum));

        /**
         * The user-provided set of ranges may contain gaps.
         * We are going to fill in those gaps with additional range objects,
         * which will themselves contain null values.
         * This will simplify the implementation to some degree.
         */
        final List<RangeEntry<E>> gaps = new LinkedList<>();

        /**
         * If the first user-specified range does not start at zero,
         * then add a null-valued range at the beginning.
         */
        if (entries.size() > 0 && entries.get(0).minimum != 0)
        {
            gaps.add(new RangeEntry<>(0, entries.get(0).minimum - 1, null));
        }

        /**
         * If the last user-specified range does not end at the max integer value,
         * then add a null-valued range at the end.
         */
        if (entries.size() > 0 && entries.get(entries.size() - 1).maximum < Integer.MAX_VALUE)
        {
            gaps.add(new RangeEntry<>(entries.get(entries.size() - 1).maximum + 1, Integer.MAX_VALUE, null));
        }

        /**
         * Iterate over the set of user-specified ranges,
         * which may *currently* be non-contiguous.
         */
        for (int i = 1; i < entries.size(); i++)
        {
            final RangeEntry x = entries.get(i - 1);
            final RangeEntry y = entries.get(i);

            /**
             * We do not allow two neighboring ranges to overlap.
             */
            if (x.maximum >= y.minimum)
            {
                final String msg = String.format("[%d, %d] overlaps [%d, %d]",
                                                 x.minimum,
                                                 x.maximum,
                                                 y.minimum,
                                                 y.maximum);
                throw new IllegalArgumentException(msg);
            }

            /**
             * If the two neighboring ranges are non-contiguous,
             * then add a null-valued range between them
             * in order to make the set of ranges contiguous.
             */
            if (y.minimum - x.maximum != 1)
            {
                gaps.add(new RangeEntry<>(x.maximum + 1, y.minimum - 1, null));
            }
        }

        /**
         * Insert the gap-fillers into the set of ranges
         * in order to make the set of ranges fully contiguous.
         */
        entries.addAll(gaps);

        /**
         * Sort the ranges again, since we just added gap-fillers.
         */
        Collections.sort(entries, (x, y) -> Integer.compare(x.minimum, y.minimum));
    }

    /**
     * Use this method to find a value given an integer key that is within
     * the contiguous user-defined range of integers that identify the value.
     *
     * @param key identifies the value to retrieve.
     * @return the sought after value, or null, if no such value exists.
     */
    public E search (final int key)
    {
        Preconditions.checkArgument(key >= 0, "key < 0");
        return entries.isEmpty() ? null : binarySearch(key, entries.size() / 2);
    }

    private E binarySearch (final int key,
                            final int pivot)
    {
        if (pivot >= entries.size())
        {
            return null;
        }
        else if (key < entries.get(pivot).minimum)
        {
            return binarySearch(key, pivot / 2);
        }
        else if (key > entries.get(pivot).maximum)
        {
            return binarySearch(key, pivot + (pivot / 2) + 1);
        }
        else
        {
            return entries.get(pivot).value;
        }
    }

    public static void main (String[] args)
    {
        final List<RangeEntry> xentries = new LinkedList<>();
        xentries.add(new RangeEntry(1, 2, "A"));
        xentries.add(new RangeEntry(3, 4, "B"));
        xentries.add(new RangeEntry(5, 6, "C"));
        xentries.add(new RangeEntry(7, 8, "D"));
        xentries.add(new RangeEntry(9, 10, "E"));

        final PositiveIntRangeMap b = new PositiveIntRangeMap(xentries);

        for (int i = 0; i <= 11; i++)
        {
            System.out.println("X[" + i + "] = " + b.search(i));
        }
    }
}
