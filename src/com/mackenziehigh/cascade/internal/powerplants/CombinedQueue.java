package com.mackenziehigh.cascade.internal.powerplants;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;

/**
 * This class provides the ability to create multiple dynamically-sized
 * queues that are based upon a shared preallocated array.
 */
public final class CombinedQueue<E>
{

    private final int capacity;

    private final int[] nextPointers;

    private final int[] lastPointers;

    private final Object[] data;

    private int freePtr;

    private int freeCount;

    public CombinedQueue (final int capacity)
    {
        Preconditions.checkArgument(capacity > 1);
        this.capacity = capacity;
        this.nextPointers = new int[capacity];
        this.lastPointers = new int[capacity];
        this.data = new Object[capacity];

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

    public MemberQueue addMemberQueue ()
    {
        return new MemberQueue();
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

        public void add (final E value)
        {
            Preconditions.checkNotNull(value);
            Preconditions.checkState(freeCount > 0, "Capacity Exceeded");

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
        }

        @SuppressWarnings ("unchecked")
        public E remove ()
        {
            Preconditions.checkState(size > 0, "Empty Queue");

            /**
             * Get the value out of the head node.
             * Then, remove the data from the head node.
             */
            final E value = (E) data[headPtr];
            data[headPtr] = null;
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

        public int size ()
        {
            return size;
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
                System.out.printf("[%d] = (%d, %d) (%s)\n", i, nextPointers[i], lastPointers[i], data[i] == null ? "?" : data[i]);
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
}
