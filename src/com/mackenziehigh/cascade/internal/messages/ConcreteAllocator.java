package com.mackenziehigh.cascade.internal.messages;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mackenziehigh.cascade.CascadeAllocator;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Concrete Implementation Of: CascadeAllocator.
 */
public final class ConcreteAllocator
        implements CascadeAllocator
{
    private final CascadeAllocator ALLOCATOR = this;

    private static final class Operand
    {
        public final byte[] data;

        private final AllocationPool pool;

        public volatile int dataSize;

        public volatile Operand below;

        public volatile int stackSize = 1;

        private volatile long refCount = 0;

        private final Consumer<Operand> onFree;

        public Operand (final AllocationPool pool,
                        final Consumer<Operand> onFree,
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
            if (--refCount == 0)
            {
                if (onFree != null)
                {
                    onFree.accept(this);
                }
            }
        }
    }

    /**
     * Concrete Implementation Of: OperandStack.
     *
     * <p>
     * A unit-test is used to ensure that all of the methods herein are either
     * (final + synchronized), because OperandStack needs to be thread-safe.
     * </p>
     */
    private final class StandardOperandStack
            implements OperandStack
    {

        private Operand top = null;

        public final synchronized void performPush (final Operand operand)
        {
            Preconditions.checkNotNull(operand, "operand");
            operand.stackSize = top == null ? 1 : top.stackSize + 1;
            operand.below = top;
            top = operand;
            top.increment();
        }

        @Override
        public final synchronized CascadeAllocator allocator ()
        {
            return ALLOCATOR;
        }

        @Override
        public final synchronized AllocationPool pool ()
        {
            return top == null ? allocator().anon() : top.pool;
        }

        @Override
        public final synchronized OperandStack set (final OperandStack value)
        {
            // TODO: Optimize
            Preconditions.checkArgument(ALLOCATOR.equals(value.allocator()), "Wrong Allocator");
            final StandardOperandStack stack = (StandardOperandStack) value;
            clear();
            if (stack.top != null)
            {
                performPush(stack.top);
            }
            return this;
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
            Preconditions.checkState(top != null, "Empty Stack");
            if (index < 0 || index >= top.dataSize)
            {
                throw new IndexOutOfBoundsException(String.format("index=%d, size=%d", index, top.dataSize));
            }
            else
            {
                return top.data[index];
            }
        }

        @Override
        public final synchronized OperandStack set (final OperandStackArray array,
                                                    final int index)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public final synchronized OperandStack push (final OperandStack value)
        {
            Preconditions.checkArgument(ALLOCATOR.equals(value.allocator()), "Wrong Allocator");
            final StandardOperandStack other = ((StandardOperandStack) value);
            Preconditions.checkState(other.isStackEmpty() == false, "Empty Stack");
            return push(((StandardOperandStack) value).top.data, 0, 0);
        }

        @Override
        public final synchronized OperandStack push (final byte[] buffer,
                                                     final int offset,
                                                     final int length)
        {
            allocator().anon().alloc(this, buffer, offset, length);
            return this;
        }

        @Override
        public final synchronized int copyTo (final byte[] buffer,
                                              final int offset,
                                              final int length)
        {
            Preconditions.checkArgument(length >= 0, "length < 0");
            System.arraycopy(top.data, 0, buffer, offset, length);
            final int diff = buffer.length - offset;
            final int copied = diff < length ? diff : length;
            return copied;
        }

        @Override
        public final synchronized OperandStack pop ()
        {
            Preconditions.checkState(isStackEmpty() == false, "Empty Stack");
            final Operand oldTop = top;
            top = top.below;
            oldTop.decrement();
            return this;
        }

    }

    /**
     * This class provides a concrete implementation of AllocationPool,
     * which allocates operands on-demand, rather than pre-allocating them.
     */
    private final class DynamicAllocationPool
            implements AllocationPool
    {
        private final String name;

        private final boolean defaultFlag;

        private final int minimumSize;

        private final int maximumSize;

        private final AtomicInteger size = new AtomicInteger();

        private final int capacity;

        public DynamicAllocationPool (final String name,
                                      final boolean defaultFlag,
                                      final int minimumSize,
                                      final int maximumSize,
                                      final int capacity)
        {
            this.name = name;
            this.defaultFlag = defaultFlag;
            this.minimumSize = minimumSize;
            this.maximumSize = maximumSize;
            this.capacity = capacity;
        }

        @Override
        public String name ()
        {
            return name;
        }

        @Override
        public boolean isDefault ()
        {
            return defaultFlag;
        }

        @Override
        public boolean isPreallocated ()
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
        public int size ()
        {
            return size.get();
        }

        @Override
        public int capacity ()
        {
            return capacity;
        }

        @Override
        public AllocationCodes tryAlloc (final OperandStack stack,
                                         final byte[] buffer,
                                         final int offset,
                                         final int length)
        {
            // TODO Capacity check
            Preconditions.checkArgument(ALLOCATOR.equals(stack.allocator()), "Different Allocator");
            Preconditions.checkArgument(offset + length <= buffer.length, "(offset + length) > buffer.length");
            final StandardOperandStack operands = (StandardOperandStack) stack;
            final Operand operand = new Operand(this, null, length);
            operand.dataSize = length;
            System.arraycopy(buffer, 0, operand.data, offset, length);
            operands.performPush(operand);
            return AllocationCodes.OK;
        }

    }

    /**
     * This class provides a concrete implementation of AllocationPool,
     * which allocates operands using pre-allocated buffers.
     */
    private final class FixedAllocationPool
            implements AllocationPool
    {
        private final String name;

        private final boolean defaultFlag;

        private final int minimumSize;

        private final int maximumSize;

        private final int capacity;

        private final ArrayBlockingQueue<Operand> freePool;

        private final Consumer<Operand> onFree;

        public FixedAllocationPool (final String name,
                                    final boolean defaultFlag,
                                    final int minimumSize,
                                    final int maximumSize,
                                    final int capacity)
        {
            this.name = name;
            this.defaultFlag = defaultFlag;
            this.minimumSize = minimumSize;
            this.maximumSize = maximumSize;
            this.capacity = capacity;
            this.freePool = new ArrayBlockingQueue<>(capacity);
            this.onFree = x -> freePool.offer(x);

            for (int i = 0; i < capacity; i++)
            {
                freePool.offer(new Operand(this, onFree, maximumSize));
            }
        }

        @Override
        public String name ()
        {
            return name;
        }

        @Override
        public boolean isDefault ()
        {
            return defaultFlag;
        }

        @Override
        public boolean isPreallocated ()
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
        public int size ()
        {
            return capacity() - freePool.size();
        }

        @Override
        public int capacity ()
        {
            return capacity;
        }

        @Override
        public AllocationCodes tryAlloc (final OperandStack stack,
                                         final byte[] buffer,
                                         final int offset,
                                         final int length)
        {
            Preconditions.checkArgument(ALLOCATOR.equals(stack.allocator()), "Different Allocator");
            Preconditions.checkArgument(offset + length <= buffer.length, "(offset + length) > buffer.length");
            final StandardOperandStack operands = (StandardOperandStack) stack;
            final Operand operand = freePool.poll();
            if (operand == null)
            {
                return AllocationCodes.OUT_OF_MEMORY;
            }
            operand.dataSize = length;
            System.arraycopy(buffer, 0, operand.data, offset, length);
            operands.performPush(operand);
            return AllocationPool.AllocationCodes.OK;
        }
    }

    /**
     * This class provides a concrete implementation of AllocationPool,
     * which delegates to other pools based on a best-match policy.
     */
    private final class BestMatchAllocationPool
            implements AllocationPool
    {
        private final String name;

        private final boolean defaultFlag;

        private final int minimumSize;

        private final int maximumSize;

        private final int capacity;

        private final List<AllocationPool> pools;

        public BestMatchAllocationPool (final String name,
                                        final boolean defaultFlag,
                                        final AllocationPool dynamicFallback,
                                        final List<AllocationPool> pools)
        {
            this.name = name;
            this.defaultFlag = defaultFlag;
            this.pools = ImmutableList.copyOf(pools);
            this.minimumSize = pools.stream().mapToInt(x -> x.minimumAllocationSize()).min().getAsInt();
            this.maximumSize = pools.stream().mapToInt(x -> x.maximumAllocationSize()).max().getAsInt();
            this.capacity = pools.stream().mapToInt(x -> x.capacity()).sum();
        }

        @Override
        public String name ()
        {
            return name;
        }

        @Override
        public boolean isDefault ()
        {
            return defaultFlag;
        }

        @Override
        public boolean isPreallocated ()
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
        public int size ()
        {
            int sum = 0;

            for (int i = 0; i < pools.size(); i++)
            {
                sum += pools.get(i).size();
            }

            return sum;
        }

        @Override
        public int capacity ()
        {
            return capacity;
        }

        @Override
        public AllocationCodes tryAlloc (final OperandStack stack,
                                         final byte[] buffer,
                                         final int offset,
                                         final int length)
        {
            return null;
        }
    }

    private final Map<String, AllocationPool> pools = new ConcurrentHashMap<>();

    private final Map<String, AllocationPool> unmodPools = Collections.unmodifiableMap(pools);

    private final FixedAllocationPool anon = new FixedAllocationPool("anon", true, 0, 1024, 128);

    public void addDynamic (final String name,
                            final int minimumSize,
                            final int maximumSize,
                            final int capacity)
    {
        pools.put(name, new DynamicAllocationPool(name, false, minimumSize, maximumSize, capacity));
    }

    public void addFixed (final String name,
                          final int minimumSize,
                          final int maximumSize,
                          final int capacity)
    {
        pools.put(name, new FixedAllocationPool(name, false, minimumSize, maximumSize, capacity));
    }

    public void addAnonFixed (final int minimumSize,
                              final int maximumSize,
                              final int capacity)
    {

    }

    @Override
    public OperandStack newOperandStack ()
    {
        return new StandardOperandStack();
    }

    @Override
    public OperandStackArray newOperandStackArray (int size)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, AllocationPool> pools ()
    {
        return unmodPools;
    }

    @Override
    public AllocationPool anon ()
    {
        return anon;
    }

    public static void main (String[] args)
    {
        final ConcreteAllocator ca = new ConcreteAllocator();
        final StandardOperandStack s = (StandardOperandStack) ca.newOperandStack();

        System.out.println(ca.anon.size());
        s.push("Andoria");
        s.push("Mars");
        System.out.println(ca.anon.size());
        s.pop();
        System.out.println(s.asString());
        System.out.println(ca.anon.size());
    }
}
