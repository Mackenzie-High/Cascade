package com.mackenziehigh.cascade.internal.messages;

import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeAllocator.OperandArray;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import java.util.Objects;

/**
 * This is an operand-stack that detects thread-safety violations.
 *
 * <p>
 * There is a noticeable performance cost to this;
 * however, the cost is worth detecting violations,
 * because they would cause insidious bugs.
 * </p>
 */
public final class CheckedOperandStack
        implements CascadeAllocator.OperandStack
{
    private final Thread thread;

    private final OperandStack delegate;

    public CheckedOperandStack (final Thread thread,
                                final OperandStack delegate)
    {
        this.thread = Objects.requireNonNull(thread, "thread");
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public CascadeAllocator allocator ()
    {
        return delegate.allocator();
    }

    @Override
    public CascadeAllocator.AllocationPool pool ()
    {
        return delegate.pool();
    }

    @Override
    public OperandStack set (final OperandStack value)
    {
        detectMultipleThreads();
        delegate.set(value);
        return this;
    }

    @Override
    public OperandStack set (final OperandArray array,
                             final int index)
    {
        detectMultipleThreads();
        delegate.set(array, index);
        return this;
    }

    @Override
    public int operandSize ()
    {
        return delegate.operandSize();
    }

    @Override
    public int operandCapacity ()
    {
        return delegate.operandCapacity();
    }

    @Override
    public int stackSize ()
    {
        return delegate.stackSize();
    }

    @Override
    public OperandStack push (final OperandStack value)
    {
        detectMultipleThreads();
        return delegate.push(value);
    }

    @Override
    public byte byteAt (final int index)
    {
        return delegate.byteAt(index);
    }

    @Override
    public int copyTo (final byte[] buffer,
                       final int offset,
                       final int length)
    {
        return delegate.copyTo(buffer, offset, length);
    }

    @Override
    public OperandStack pop ()
    {
        detectMultipleThreads();
        return delegate.pop();
    }

    /**
     * Only invoke this method from mutator methods,
     * rather than regular getters, in order to mitigate
     * a little bit of the performance impact.
     */
    private void detectMultipleThreads ()
    {
        if (thread != Thread.currentThread())
        {
            throw new IllegalStateException("Thread Safety Violation: Multiple threads are using a thread-specific OperandStack!");
        }
    }

}
