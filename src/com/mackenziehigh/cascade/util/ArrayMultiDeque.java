package com.mackenziehigh.cascade.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import java.util.AbstractCollection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * This class provides the ability to create multiple dynamically-sized
 * queues that are based upon a shared preallocated array.
 *
 * @param <E>
 */
public final class ArrayMultiDeque<E>
{

    private final int capacity;

    private final int[] nextPointers;

    private final int[] lastPointers;

    private final Object[] data;

    private int freePtr;

    private int freeCount;

    private final Set<Deque<E>> members = new HashSet<>();

    private final Set<Deque<E>> unmodMembers = Collections.unmodifiableSet(members);

    public ArrayMultiDeque (final int capacity)
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

    public Deque<E> addDeque ()
    {
        return new MemberQueue();
    }

    public Set<Deque<E>> members ()
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

    private final class MemberQueue
            extends AbstractCollection<E>
            implements Deque<E>
    {
        private int size = 0;

        private int tailPtr = -1;

        private int headPtr = -1;

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean add (final E value)
        {
            addLast(value);
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings ("unchecked")
        @Override
        public E remove ()
        {
            if (isEmpty())
            {
                throw new NoSuchElementException("Empty Deque");
            }
            else
            {
                return removeFirst();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int size ()
        {
            return size;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void addFirst (final E value)
        {
            Preconditions.checkState(freeCount > 0, "Capacity Exceeded");
            offerFirst(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void addLast (final E value)
        {
            Preconditions.checkState(freeCount > 0, "Capacity Exceeded");
            offerLast(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean offerFirst (final E value)
        {
            // TODO
            throw new UnsupportedOperationException("Not supported yet.");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean offerLast (final E value)
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
        @Override
        public E removeFirst ()
        {
            if (isEmpty())
            {
                throw new NoSuchElementException("Empty Deque");
            }
            else
            {
                return pollFirst();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public E removeLast ()
        {
            if (isEmpty())
            {
                throw new NoSuchElementException("Empty Deque");
            }
            else
            {
                return pollLast();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public E pollFirst ()
        {
            if (isEmpty())
            {
                return null;
            }

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

        /**
         * {@inheritDoc}
         */
        @Override
        public E pollLast ()
        {
            if (isEmpty())
            {
                return null;
            }

            /**
             * Get the value out of the tail node.
             * Then, remove the data from the tail node.
             */
            final E value = (E) data[tailPtr];
            data[tailPtr] = null;
            --size;

            /**
             * Update the pointers to remove the tail node.
             */
            final int oldTailPtr = tailPtr;
            final int lastPtr = nextPointers[tailPtr];
            tailPtr = lastPtr;
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
            nextPointers[oldTailPtr] = freePtr;
            lastPointers[freePtr] = oldTailPtr;
            lastPointers[oldTailPtr] = (-1);
            freePtr = oldTailPtr;
            ++freeCount;

            return value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public E getFirst ()
        {
            if (isEmpty())
            {
                throw new NoSuchElementException("Empty Deque");
            }
            else
            {
                return peekFirst();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public E getLast ()
        {
            if (isEmpty())
            {
                throw new NoSuchElementException("Empty Deque");
            }
            else
            {
                return peekLast();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public E peekFirst ()
        {
            if (isEmpty())
            {
                return null;
            }
            else
            {
                return (E) data[headPtr];
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public E peekLast ()
        {
            if (isEmpty())
            {
                return null;
            }
            else
            {
                return (E) data[tailPtr];
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean removeFirstOccurrence (final Object value)
        {
            final Iterator<E> iter = iterator();

            while (iter.hasNext())
            {
                final E element = iter.next();
                if (value == null ? element == null : value.equals(element))
                {
                    iter.remove();
                    return true;
                }
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean removeLastOccurrence (final Object value)
        {
            final Iterator<E> iter = descendingIterator();

            while (iter.hasNext())
            {
                final E element = iter.next();
                if (value == null ? element == null : value.equals(element))
                {
                    iter.remove();
                    return true;
                }
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean offer (final E value)
        {
            return offerLast(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public E poll ()
        {
            return pollFirst();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public E element ()
        {
            if (isEmpty())
            {
                throw new NoSuchElementException("Empty Deque");
            }
            else
            {
                return peek();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public E peek ()
        {
            return peekFirst();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void push (final E value)
        {
            addFirst(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public E pop ()
        {
            return removeFirst();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<E> iterator ()
        {
            return new Iterator<E>()

            {
                private int ptr = headPtr;

                @Override
                public boolean hasNext ()
                {
                    return ptr != (-1);
                }

                @Override
                public E next ()
                {
                    if (ptr == -1)
                    {
                        throw new NoSuchElementException("No next() element exists in the deque.");
                    }

                    final E result = (E) data[ptr];
                    ptr = nextPointers[ptr];
                    return result;
                }

                @Override
                public void remove ()
                {
                    // TODO

                    if (ptr == -1)
                    {

                    }
                    else if (ptr == headPtr)
                    {
                        removeFirst();
                    }
                    else if (ptr == tailPtr)
                    {
                        removeLast();
                    }
                    else
                    {
                        // TODO
//                        final int nextPtr = nextPointers[ptr];
//                        nextPointers[lastPointers[ptr]] = nextPointers[ptr];
//                        lastPointers[nextPointers[ptr]] = lastPointers[ptr];
//                        nextPointers[ptr] = -1;
//                        lastPointers[ptr] = -1;
//                        data[ptr] = null;
//                        ptr = nextPtr;

                    }
                }
            };
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<E> descendingIterator ()
        {
            return new Iterator<E>()

            {
                private int ptr = tailPtr;

                @Override
                public boolean hasNext ()
                {
                    return ptr != (-1);
                }

                @Override
                public E next ()
                {
                    if (ptr == -1)
                    {
                        throw new NoSuchElementException("No next() element exists in the deque.");
                    }

                    final E result = (E) data[ptr];
                    ptr = lastPointers[ptr];
                    return result;
                }

                @Override
                public void remove ()
                {
                    // TODO
                    Iterator.super.remove();
                }
            };
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

    public static void main (String[] args)
    {
        final ArrayMultiDeque<String> f = new ArrayMultiDeque<>(16);
        final Deque<String> p = f.addDeque();
        p.add("A");
        p.add("B");
        p.add("C");
        ((ArrayMultiDeque<String>.MemberQueue) p).print();
        final Iterator<String> i = p.iterator();
        i.next();
        i.remove();
        p.descendingIterator().forEachRemaining(x -> System.out.println(x));
    }
}
