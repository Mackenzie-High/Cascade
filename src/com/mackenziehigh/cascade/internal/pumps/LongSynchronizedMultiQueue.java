package com.mackenziehigh.cascade.internal.pumps;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Skip this class for now.
 *
 * TODO: Finish. Also, synchronize!
 */
public final class LongSynchronizedMultiQueue
{
    private final int capacity;

    private final int[] nextPointers;

    private final int[] lastPointers;

    private final long[] data;

    private int freePtr;

    private int freeCount;

    private final Set<MemberQueue> members = new HashSet<>();

    private final Set<MemberQueue> unmodMembers = Collections.unmodifiableSet(members);

    public LongSynchronizedMultiQueue (final int capacity)
    {
        Preconditions.checkArgument(capacity > 1);
        this.capacity = capacity;
        this.nextPointers = new int[capacity];
        this.lastPointers = new int[capacity];
        this.data = new long[capacity];

        for (int i = 0; i < capacity; i++)
        {
            nextPointers[i] = i + 1;
            lastPointers[capacity - i - 1] = capacity - i - 2;
        }
        nextPointers[capacity - 1] = -1;
        lastPointers[0] = -1;
        freePtr = 0;
        freeCount = capacity;
    }

    public MemberQueue addDeque ()
    {
        return new MemberQueue();
    }

    public Set<MemberQueue> members ()
    {
        return unmodMembers;
    }

    public boolean isEmpty ()
    {
        return size() == 0;
    }

    public int size ()
    {
        return capacity - freeCount;
    }

    public int capacity ()
    {
        return capacity;
    }

    public final class MemberQueue
    {
        private int size = 0;

        private int tailPtr = -1;

        private int headPtr = -1;

        public int size ()
        {
            return size;
        }

        /**
         * {@inheritDoc}
         */
        public boolean offerLast (final long value)
        {
            Preconditions.checkNotNull(value);

            if (freeCount == 0)
            {
                return false;
            }

            final int oldTailPtr = tailPtr;
            final int newFreePtr = nextPointers[freePtr];

            /**
             * Obtain a node from the free-list.
             * The node will be the new tail node.
             */
            tailPtr = freePtr;
            nextPointers[tailPtr] = -1;
            lastPointers[tailPtr] = oldTailPtr;

            /**
             * The old tail needs to point forwards to the new tail,
             * unless this is the very first node.
             */
            if (size > 0)
            {
                nextPointers[oldTailPtr] = tailPtr;
            }

            /**
             * If this is the first node, then we need to set
             * the head pointer to point to the new node;
             * otherwise, do not change the head pointer,
             * because we are adding a node to the tail
             * rather than the head.
             */
            headPtr = size == 0 ? tailPtr : headPtr;

            /**
             * We removed a node from the free-list.
             * Make updates as needed to reflect this.
             */
            freePtr = newFreePtr;
            lastPointers[freePtr] = -1;
            --freeCount;

            /**
             * Add the actual data to the tail node.
             */
            data[tailPtr] = value;
            ++size;

            return true;
        }

        /**
         * {@inheritDoc}
         */
        public long pollFirst ()
        {
            if (isEmpty())
            {
                return 0;
            }

            /**
             * Get the value out of the head node.
             * Then, remove the data from the head node.
             */
            final long value = data[headPtr];
            data[headPtr] = 0;
            --size;

            /**
             * Update the pointers to remove the head node.
             */
            final int oldHeadPtr = headPtr;
            final int nextPtr = nextPointers[headPtr];
            headPtr = nextPtr;
            if (size == 0)
            {
                headPtr = -1;
                tailPtr = -1;
            }
            else
            {
                lastPointers[headPtr] = -1;
                nextPointers[tailPtr] = -1;
            }

            /**
             * Return the removed node to the free-list.
             */
            nextPointers[oldHeadPtr] = freePtr;
            lastPointers[freePtr] = oldHeadPtr;
            lastPointers[oldHeadPtr] = (-1);
            freePtr = oldHeadPtr;
            ++freeCount;

            return value;
        }

        public void print ()
        {
            System.out.println("FreePtr = " + freePtr);
            System.out.println("FreeCount = " + freeCount);
            System.out.println("HeadPtr = " + tailPtr);
            System.out.println("TailPtr = " + headPtr);
            System.out.println("Size = " + size);
            for (int i = 0; i < capacity; i++)
            {
                System.out.printf("[%d] = (%d, %d) (%d)\n", i, nextPointers[i], lastPointers[i], data[i]);
            }
        }

        public void verifyPointers ()
        {
            /**
             * Sanity check.
             */
            if (size == 0)
            {
                Verify.verify(headPtr == -1);
                Verify.verify(tailPtr == -1);
            }
            else
            {
                Verify.verify(lastPointers[headPtr] == -1);
                Verify.verify(nextPointers[tailPtr] == -1);
            }

            /**
             * Sanity Check.
             */
            if (freeCount == 0)
            {
                Verify.verify(freePtr == -1);
            }
            else
            {
                Verify.verify(lastPointers[freePtr] == -1);
            }

            Verify.verify(nextPointers.length == capacity);
            Verify.verify(nextPointers.length == lastPointers.length);
            Verify.verify(data.length == capacity);

            for (int i = 0; i < capacity; i++)
            {
                if (nextPointers[i] >= 0)
                {
                    Verify.verify(i == lastPointers[nextPointers[i]]);
                }

                if (lastPointers[i] >= 0)
                {
                    Verify.verify(i == nextPointers[lastPointers[i]]);
                }
            }
        }
    }

    public static void main (String[] args)
    {
        final LongSynchronizedMultiQueue f = new LongSynchronizedMultiQueue(16);
        final MemberQueue p = f.addDeque();
        p.offerLast(100);
        p.offerLast(200);
        p.offerLast(300);
        p.print();

    }
}
