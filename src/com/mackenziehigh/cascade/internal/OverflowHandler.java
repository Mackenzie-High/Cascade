package com.mackenziehigh.cascade.internal;

import com.google.common.base.Verify;
import java.util.Deque;
import java.util.Objects;

/**
 *
 */
public final class OverflowHandler<T>
{
    private final Deque<T> queue;

    private final int capacity;

    private final OverflowPolicy policy;

    public OverflowHandler (final Deque<T> queue,
                            final int capacity,
                            final OverflowPolicy policy)
    {
        this.queue = Objects.requireNonNull(queue);
        this.policy = Objects.requireNonNull(policy);
        this.capacity = capacity;
    }

    public boolean offer (final T value)
    {
        if (capacity == 0)
        {
            return false;
        }
        else if (queue.size() < capacity)
        {
            return queue.offer(value);
        }
        else if (policy == OverflowPolicy.DROP_ALL)
        {
            queue.clear();
            return false;
        }
        else if (policy == OverflowPolicy.DROP_INCOMING)
        {
            return false;
        }
        else if (policy == OverflowPolicy.DROP_NEWEST)
        {
            queue.pollLast();
            return queue.offer(value);
        }
        else if (policy == OverflowPolicy.DROP_OLDEST)
        {
            queue.pollFirst();
            return queue.offer(value);
        }
        else if (policy == OverflowPolicy.DROP_PENDING)
        {
            queue.clear();
            return queue.offer(value);
        }
        else
        {
            Verify.verify(policy == OverflowPolicy.UNSPECIFIED);
            return queue.offer(value);
        }
    }
}
