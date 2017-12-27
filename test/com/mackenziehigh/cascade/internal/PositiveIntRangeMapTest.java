package com.mackenziehigh.cascade.internal;

import java.util.LinkedList;
import java.util.List;
import static junit.framework.Assert.assertEquals;
import org.junit.Test;

/**
 * Unit Test.
 */
public final class PositiveIntRangeMapTest
{
    /**
     * Test: 20171121220357226060
     *
     * <p>
     * Case: Normal Cases
     * </p>
     */
    @Test
    public void test20171121220357226060 ()
    {
        System.out.println("Test: 20171121220357226060");

        final List<PositiveIntRangeMap.RangeEntry> entries = new LinkedList<>();
        PositiveIntRangeMap map;

        entries.clear();
        entries.add(new PositiveIntRangeMap.RangeEntry(1, 2, "A"));
        entries.add(new PositiveIntRangeMap.RangeEntry(3, 4, "B"));
        entries.add(new PositiveIntRangeMap.RangeEntry(5, 6, "C"));
        entries.add(new PositiveIntRangeMap.RangeEntry(7, 8, "D"));
        entries.add(new PositiveIntRangeMap.RangeEntry(9, 10, "E"));
        map = new PositiveIntRangeMap(entries);
        assertEquals(null, map.search(0));
        assertEquals("A", map.search(1));
        assertEquals("A", map.search(2));
        assertEquals("B", map.search(3));
        assertEquals("B", map.search(4));
        assertEquals("C", map.search(5));
        assertEquals("C", map.search(6));
        assertEquals("D", map.search(7));
        assertEquals("D", map.search(8));
        assertEquals("E", map.search(9));
        assertEquals("E", map.search(10));
        assertEquals(null, map.search(11));
    }
}
