/*
 * Copyright 2018 Michael Mackenzie High
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mackenziehigh.internal.cascade;

import com.google.common.base.Verify;
import com.mackenziehigh.cascade.builder.OverflowPolicy;
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
        else if (policy == OverflowPolicy.THROW)
        {
            throw new IllegalStateException("Overflow");
        }
        else
        {
            Verify.verify(policy == OverflowPolicy.UNSPECIFIED);
            return queue.offer(value);
        }
    }
}
