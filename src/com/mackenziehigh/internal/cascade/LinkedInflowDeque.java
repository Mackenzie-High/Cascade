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

import com.mackenziehigh.cascade.Reactor;
import com.mackenziehigh.cascade.Reactor.Input.InflowDeque;
import com.mackenziehigh.cascade.Reactor.Input.OverflowPolicy;
import java.util.ArrayDeque;
import java.util.Objects;

/**
 * Linked-based <code>InfowQueue</code>.
 *
 * @param <E>
 */
public final class LinkedInflowDeque<E>
        extends ArrayDeque<E>
        implements InflowDeque<E>
{
    private final int capacity;

    private final Reactor.Input.OverflowPolicy policy;

    /**
     * Sole constructor.
     *
     * @param capacity will be the maximum capacity of the queue.
     * @param policy defines what <i>should</i> happen when the queue overflows.
     */
    public LinkedInflowDeque (final int capacity,
                              final OverflowPolicy policy)
    {
        super();
        this.capacity = capacity;
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int capacity ()
    {
        return capacity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reactor.Input.OverflowPolicy overflowPolicy ()
    {
        return policy;
    }
}
