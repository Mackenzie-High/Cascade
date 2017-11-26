package com.mackenziehigh.cascade.internal.messages;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.internal.messages.PositiveIntRangeMap.RangeEntry;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Concrete Implementation Of: CascadeAllocator.
 */
public final class ConcreteAllocator
        implements CascadeAllocator
{

    private final CascadeAllocator ALLOCATOR = this;

    private static final class StandardOperand
    {
        public final byte[] data;

        public final AllocationPool pool;

        public volatile int dataSize;

        public volatile StandardOperand below;

        public volatile int stackSize = 1;

        private volatile long refCount = 0;

        private final Consumer<StandardOperand> onFree;

        public StandardOperand (final AllocationPool pool,
                                final Consumer<StandardOperand> onFree,
                                final int capacity)
        {
            this.pool = pool;
            this.onFree = onFree;
            this.data = new byte[capacity];
        }

        public synchronized void increment ()
        {
            ++refCount;
        }

        public synchronized void decrement ()
        {
            StandardOperand p = this;
            StandardOperand next;

            /**
             * Walk down the spaghetti-stack, freeing operands,
             * until either no more frees are needed or the bottom
             * of the operand-stack is reached.
             */
            while (p != null)
            {
                Verify.verify(p != p.below);

                if (--p.refCount == 0)
                {
                    next = p.below;

                    // Free
                    p.below = null;
                    p.dataSize = 0;
                    p.stackSize = 0;
                    if (p.onFree != null)
                    {
                        p.onFree.accept(p);
                    }

                    p = next;
                }
                else
                {
                    Verify.verify(p.below == null || p.below.refCount > 0);
                    p = null;
                }
            }
        }

        public synchronized void init (final byte[] buffer,
                                       final int offset,
                                       final int length,
                                       final StandardOperand below)
        {
            this.refCount = 0;
            this.dataSize = length;
            this.stackSize = (below == null ? 1 : below.stackSize + 1);
            Verify.verify(this != below);
            Verify.verify(0 != (below == null ? 1 : below.refCount));
            this.below = below;
            System.arraycopy(buffer, offset, this.data, 0, length);

            if (below != null)
            {
                below.increment();
            }
        }
    }

    /**
     * Concrete Implementation Of: OperandStack.
     *
     * <p>
     * A unit-test is used to ensure that all of the methods herein are either
     * (final + synchronized) or (default), because OperandStack must be thread-safe.
     * </p>
     */
    private final class StandardOperandStack
            implements OperandStack
    {

        private volatile StandardOperand top = null;

        public final synchronized void performSet (final StandardOperand operand)
        {
            if (operand == null && top == null)
            {
                return;
            }
            else if (operand == null && top != null)
            {
                top.decrement();
                top = null;
                return;
            }
            else if (operand != null && top == null)
            {
                top = operand;
                top.increment();
                return;
            }
            else // (operand != null && top != null)
            {
                assert operand != null;
                final StandardOperand oldTop = top;
                top = operand;
                top.increment();
                oldTop.decrement();
                return;
            }
        }

        @Override
        public final synchronized CascadeAllocator allocator ()
        {
            return ALLOCATOR;
        }

        @Override
        public final synchronized AllocationPool pool ()
        {
            return isStackEmpty() ? defaultPool() : top.pool;
        }

        @Override
        public final synchronized OperandStack set (final OperandStack value)
        {

            if (value == null || value.isStackEmpty())
            {
                performSet(null);
                return this;
            }
            else if (allocator().equals(value.allocator()) == false)
            {
                throw new AllocatorMismatchException(allocator(), value.allocator());
            }
            else
            {
                final StandardOperandStack other = (StandardOperandStack) value;
                performSet(other.top);
                return this;
            }
        }

        @Override
        public final synchronized int operandSize ()
        {
            return top == null ? 0 : top.dataSize;
        }

        @Override
        public final synchronized int operandCapacity ()
        {
            return top == null ? 0 : top.data.length;
        }

        @Override
        public final synchronized int stackSize ()
        {
            return top == null ? 0 : top.stackSize;
        }

        @Override
        public final synchronized byte byteAt (final int index)
        {
            if (isStackEmpty())
            {
                throw new IllegalStateException("Empty Stack");
            }
            else if (index < 0 || index >= top.dataSize)
            {
                throw new IndexOutOfBoundsException(String.format("index = %d, size = %d", index, top.dataSize));
            }
            else
            {
                return top.data[index];
            }
        }

        @Override
        public final synchronized OperandStack set (final OperandArray array,
                                                    final int index)
        {
            if (array == null)
            {
                throw new NullPointerException("array");
            }
            else if (allocator().equals(array.allocator()) == false)
            {
                throw new AllocatorMismatchException(allocator(), array.allocator());
            }
            else if (index < 0)
            {
                throw new IndexOutOfBoundsException("index < 0");
            }
            else if (index >= array.size())
            {
                throw new IndexOutOfBoundsException("index >= array.length");
            }
            else
            {
                final StandardOperand operand = ((StandardOperandArray) array).array[index];
                performSet(operand);
                return this;
            }
        }

        @Override
        public final synchronized OperandStack push (final OperandStack value)
        {
            if (value == null)
            {
                throw new NullPointerException("value");
            }
            else if (allocator().equals(value.allocator()) == false)
            {
                throw new AllocatorMismatchException(allocator(), value.allocator());
            }
            else if (value.isStackEmpty())
            {
                throw new IllegalArgumentException("Empty Stack");
            }
            else
            {
                final StandardOperandStack other = ((StandardOperandStack) value);
                return push(other.top.data, 0, value.operandSize());
            }
        }

        @Override
        public final synchronized int copyTo (final byte[] buffer,
                                              final int offset,
                                              final int length)
        {
            if (isStackEmpty())
            {
                throw new IllegalStateException("Empty Stack");
            }
            else if (buffer == null)
            {
                throw new NullPointerException("buffer");
            }
            else if (offset < 0)
            {
                throw new IndexOutOfBoundsException("offset < 0");
            }
            else if (offset >= buffer.length)
            {
                throw new IndexOutOfBoundsException("offset >= buffer.length");
            }
            else if (offset + length > buffer.length)
            {
                throw new IllegalArgumentException("offset + length >= buffer.length");
            }
            else
            {
                System.arraycopy(top.data, 0, buffer, offset, length);
                final int diff = buffer.length - offset;
                final int copied = diff < length ? diff : length;
                return copied;
            }
        }

        @Override
        public final synchronized OperandStack pop ()
        {
            if (isStackEmpty())
            {
                throw new IllegalStateException("Empty Stack");
            }
            else if (stackSize() > 1)
            {
                final StandardOperand oldTop = top;
                top = oldTop.below;
                top.increment();
                oldTop.decrement();
                return this;
            }
            else // stackSize() == 1
            {
                final StandardOperand oldTop = top;
                top = null;
                oldTop.decrement();
                return this;
            }
        }

    }

    private final class StandardOperandArray
            implements OperandArray
    {
        private final StandardOperand[] array;

        public StandardOperandArray (final int size)
        {
            Preconditions.checkArgument(size >= 0, "size < 0");
            this.array = new StandardOperand[size];
        }

        @Override
        public CascadeAllocator allocator ()
        {
            return ALLOCATOR;
        }

        @Override
        public int size ()
        {
            return array.length;
        }

        @Override
        public OperandArray set (final int index,
                                 final OperandStack value)
        {
            if (index < 0)
            {
                throw new IndexOutOfBoundsException("index < 0");
            }
            else if (index >= array.length)
            {
                throw new IndexOutOfBoundsException("index >= array.length");
            }
            else if (value == null && array[index] == null)
            {
                return this;
            }
            else if (value == null && array[index] != null)
            {
                array[index].decrement();
                array[index] = null;
            }
            else if (allocator().equals(value.allocator()) == false)
            {
                throw new AllocatorMismatchException(allocator(), value.allocator());
            }
            else if (value.isStackEmpty())
            {
                return set(index, null);
            }
            else // value.isStackEmpty() == false
            {
                final StandardOperand operand = ((StandardOperandStack) value).top;
                operand.increment();
                array[index] = operand;
            }

            return this;
        }

        @Override
        public void close ()
        {
            for (int i = 0; i < array.length; i++)
            {
                set(i, null);
            }
        }

    }

    /**
     * This class provides a concrete implementation of AllocationPool,
     * which allocates operands on-demand, rather than pre-allocating them.
     */
    public final class DynamicAllocationPool
            implements AllocationPool
    {
        private final String name;

        private final int minimumSize;

        private final int maximumSize;

        public DynamicAllocationPool (final String name,
                                      final int minimumSize,
                                      final int maximumSize)
        {
            this.name = name;
            this.minimumSize = minimumSize;
            this.maximumSize = maximumSize;
        }

        @Override
        public String name ()
        {
            return name;
        }

        @Override
        public boolean isFixed ()
        {
            return false;
        }

        @Override
        public int minimumAllocationSize ()
        {
            return minimumSize;
        }

        @Override
        public int maximumAllocationSize ()
        {
            return maximumSize;
        }

        @Override
        public CascadeAllocator allocator ()
        {
            return ALLOCATOR;
        }

        @Override
        public boolean tryAlloc (final OperandStack stack,
                                 final byte[] buffer,
                                 final int offset,
                                 final int length)
        {
            try
            {
                checkAlloc(this, stack, buffer, offset, length);
                final StandardOperandStack operands = (StandardOperandStack) stack;
                final StandardOperand operand = new StandardOperand(this, null, length);
                operand.init(buffer, offset, length, operands.top);
                operands.performSet(operand);
                return true;
            }
            catch (OutOfMemoryError ex)
            {
                return false;
            }
        }
    }

    /**
     * This class provides a concrete implementation of AllocationPool,
     * which allocates operands using pre-allocated buffers.
     */
    public final class FixedAllocationPool
            implements AllocationPool
    {
        private final String name;

        private final int minimumSize;

        private final int maximumSize;

        private final int capacity;

        private final ArrayBlockingQueue<StandardOperand> freePool;

        private final Consumer<StandardOperand> onFree;

        public FixedAllocationPool (final String name,
                                    final int minimumSize,
                                    final int maximumSize,
                                    final int capacity)
        {
            this.name = name;
            this.minimumSize = minimumSize;
            this.maximumSize = maximumSize;
            this.capacity = capacity;
            this.freePool = new ArrayBlockingQueue<>(capacity);
            this.onFree = x -> freePool.offer(x);

            for (int i = 0; i < capacity; i++)
            {
                freePool.offer(new StandardOperand(this, onFree, maximumSize));
            }
        }

        @Override
        public OptionalLong size ()
        {
            return OptionalLong.of(capacity - freePool.size());
        }

        @Override
        public OptionalLong capacity ()
        {
            return OptionalLong.of(capacity);
        }

        @Override
        public String name ()
        {
            return name;
        }

        @Override
        public boolean isFixed ()
        {
            return true;
        }

        @Override
        public int minimumAllocationSize ()
        {
            return minimumSize;
        }

        @Override
        public int maximumAllocationSize ()
        {
            return maximumSize;
        }

        @Override
        public CascadeAllocator allocator ()
        {
            return ALLOCATOR;
        }

        @Override
        public boolean tryAlloc (final OperandStack stack,
                                 final byte[] buffer,
                                 final int offset,
                                 final int length)
        {
            checkAlloc(this, stack, buffer, offset, length);
            final StandardOperandStack operands = (StandardOperandStack) stack;
            final StandardOperand operand = freePool.poll();
            if (operand == null)
            {
                return false;
            }
            Verify.verify(operands.top == null || operands.top.refCount >= 1);
            operand.init(buffer, offset, length, operands.top);
            operands.performSet(operand);
            return true;
        }

    }

    /**
     * This class provides a concrete implementation of AllocationPool,
     * which delegates to other pools based on a best-match policy.
     */
    public final class CompositeAllocationPool
            implements AllocationPool
    {
        private final String name;

        private final int minimumSize;

        private final int maximumSize;

        private final AllocationPool fallback;

        /**
         * This object is used to select the best-matching delegate pool
         * based on the size of the requested allocation.
         */
        private final PositiveIntRangeMap<AllocationPool> lookup;

        public CompositeAllocationPool (final String name,
                                        final AllocationPool fallback,
                                        final List<AllocationPool> pools)
        {
            this.name = name;
            this.fallback = fallback;

            final int min1 = fallback == null ? Integer.MAX_VALUE : fallback.minimumAllocationSize();
            final int min2 = pools.stream().mapToInt(x -> x.minimumAllocationSize()).min().getAsInt();
            this.minimumSize = Math.min(min1, min2);

            final int max1 = fallback == null ? Integer.MIN_VALUE : fallback.maximumAllocationSize();
            final int max2 = pools.stream().mapToInt(x -> x.maximumAllocationSize()).max().getAsInt();
            this.maximumSize = Math.max(max1, max2);

            this.lookup = new PositiveIntRangeMap<>(pools.stream().map(x -> new RangeEntry<>(x.minimumAllocationSize(), x.maximumAllocationSize(), x)).collect(Collectors.toList()));
        }

        @Override
        public String name ()
        {
            return name;
        }

        @Override
        public boolean isFixed ()
        {
            return false;
        }

        @Override
        public int minimumAllocationSize ()
        {
            return minimumSize;
        }

        @Override
        public int maximumAllocationSize ()
        {
            return maximumSize;
        }

        @Override
        public CascadeAllocator allocator ()
        {
            return ALLOCATOR;
        }

        @Override
        public boolean tryAlloc (final OperandStack stack,
                                 final byte[] buffer,
                                 final int offset,
                                 final int length)
        {
            checkAlloc(this, stack, buffer, offset, length);
            final AllocationPool match = lookup.search(length);
            final AllocationPool pool = match == null ? fallback : match;
            if (pool == null)
            {
                throw new IllegalArgumentException("No Applicable Delegate Pool");
            }

            final boolean firstTry = pool.tryAlloc(stack, buffer, offset, length);

            if (firstTry == false && fallback != null)
            {
                return fallback.tryAlloc(stack, buffer, offset, length);
            }
            else
            {
                return firstTry;
            }
        }
    }

    private Cascade cascade;

    private final Map<String, AllocationPool> pools = new ConcurrentHashMap<>();

    private final Map<String, AllocationPool> unmodPools = Collections.unmodifiableMap(pools);

    /**
     * Constructor, for testing purposes.
     */
    public ConcreteAllocator ()
    {
        this(null);
    }

    /**
     * Constructor, for production.
     *
     * @param owner owns this allocator.
     */
    public ConcreteAllocator (final Cascade owner)
    {
        this.cascade = owner;
    }

    /**
     * Use this method to add a new dynamic allocation-pool to this allocator.
     *
     * <p>
     * This method should only be called during initial configuration.
     * </p>
     *
     * @param name is the name of the new pool.
     * @param minimumSize is the minimum size of allocations in the pool.
     * @param maximumSize is the maximum size of allocations in the pool.
     * @return the new pool.
     */
    public AllocationPool addDynamicPool (final String name,
                                          final int minimumSize,
                                          final int maximumSize)
    {
        final AllocationPool pool = new DynamicAllocationPool(name, minimumSize, maximumSize);
        pools.put(name, pool);
        return pool;
    }

    /**
     * Use this method to add a new fixed-size allocation-pool to this allocator.
     *
     * <p>
     * This method should only be called during initial configuration.
     * </p>
     *
     * @param name is the name of the new pool.
     * @param minimumSize is the minimum size of allocations in the pool.
     * @param maximumSize is the maximum size of allocations in the pool.
     * @param capacity is the maximum number of allocations in the pool at one time.
     * @return the new pool.
     */
    public AllocationPool addFixedPool (final String name,
                                        final int minimumSize,
                                        final int maximumSize,
                                        final int capacity)
    {
        final AllocationPool pool = new FixedAllocationPool(name, minimumSize, maximumSize, capacity);
        pools.put(name, pool);
        return pool;
    }

    /**
     * Use this method to add a new composite allocation-pool to this allocator.
     *
     * <p>
     * This method should only be called during initial configuration.
     * </p>
     *
     * @param name is the name of the new pool.
     * @param fallback is the allocator to use, if none of the delegates are applicable.
     * @param delegates are the pools that the new pool will be composed of.
     * @return the new pool.
     */
    public AllocationPool addCompositePool (final String name,
                                            final AllocationPool fallback,
                                            final List<AllocationPool> delegates)
    {
        final AllocationPool pool = new CompositeAllocationPool(name, fallback, delegates);
        pools.put(name, pool);
        return pool;
    }

    @Override
    public Cascade cascade ()
    {
        return cascade;
    }

    @Override
    public OperandStack newOperandStack ()
    {
        return new StandardOperandStack();
    }

    @Override
    public OperandArray newOperandArray (final int size)
    {
        return new StandardOperandArray(size);
    }

    @Override
    public Map<String, AllocationPool> pools ()
    {
        return unmodPools;
    }

    @Override
    public AllocationPool defaultPool ()
    {
        final AllocationPool pool = unmodPools.get("default");

        if (pool == null)
        {
            throw new IllegalStateException("No Default Pool Exists");
        }
        else
        {
            return pool;
        }
    }

    private void checkAlloc (final AllocationPool pool,
                             final OperandStack stack,
                             final byte[] buffer,
                             final int offset,
                             final int length)
    {

        if (stack == null)
        {
            throw new NullPointerException("stack");
        }
        else if (pool.allocator().equals(stack.allocator()) == false)
        {
            throw new AllocatorMismatchException(pool.allocator(), stack.allocator());
        }
        else if (buffer == null)
        {
            throw new NullPointerException("buffer");
        }
        else if (offset < 0)
        {
            throw new IndexOutOfBoundsException("offset < 0");
        }
        else if (offset > 0 && offset >= buffer.length)
        {
            throw new IndexOutOfBoundsException("offset > 0 && offset >= buffer.length");
        }
        else if (length < pool.minimumAllocationSize())
        {
            throw new IllegalArgumentException("length < minimumAllocationSize()");
        }
        else if (length > pool.maximumAllocationSize())
        {
            throw new IllegalArgumentException("length > maximumAllocationSize()");
        }
        else if (offset > 0 && offset + length >= buffer.length)
        {
            throw new IllegalArgumentException("offset > 0 && offset + length >= buffer.length");
        }
        else if (offset + length > buffer.length)
        {
            throw new IllegalArgumentException("offset + length > buffer.length");
        }
    }
}
