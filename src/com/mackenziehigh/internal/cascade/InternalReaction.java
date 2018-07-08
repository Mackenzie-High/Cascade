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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mackenziehigh.cascade.Input;
import com.mackenziehigh.cascade.Output;
import com.mackenziehigh.cascade.Reaction;
import com.mackenziehigh.cascade.Reactor;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/**
 * Combined Implementation of <code>ReactionBuilder</code> and <code>Reaction</code>.
 */
final class InternalReaction
        implements Reaction
{
    private final Object lock = new Object();

    private final Reactor reactor;

    private final UUID uuid = UUID.randomUUID();

    private volatile String name = uuid.toString();

    private final List<BooleanSupplier> requirements = Lists.newCopyOnWriteArrayList();

    private volatile ReactionTask onTrue = () ->
    {
        // Pass.
    };

    // TODO: NOP instead????
    private volatile ReactionTask onError = () ->
    {
    };

    public InternalReaction (final Reactor reactor)
    {
        this.reactor = Objects.requireNonNull(reactor, "reactor");
    }

    private Reaction doRequire (final BooleanSupplier condition)
    {
        requirements.add(condition);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reaction named (final String name)
    {
        synchronized (lock)
        {
            this.name = Objects.requireNonNull(name, "name");
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reaction require (final Input<?> input,
                             final int count)
    {
        Preconditions.checkNotNull(input, "input");
        Preconditions.checkArgument(count >= 0, "count < 0");
        synchronized (lock)
        {
            return doRequire(() -> input.size() >= count);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Reaction require (final Input<T> input,
                                 final Predicate<T> head)
    {
        Preconditions.checkNotNull(input, "input");
        Preconditions.checkNotNull(head, "head");
        synchronized (lock)
        {
            return doRequire(() -> head.test(input.peekOrDefault(null)));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reaction require (final Output<?> output,
                             final int count)
    {
        Preconditions.checkNotNull(output, "output");
        Preconditions.checkArgument(count >= 0, "count < 0");
        synchronized (lock)
        {
            return doRequire(() -> !output.connection().isPresent() || output.remainingCapacity() >= count);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reaction require (final BooleanSupplier condition)
    {
        Preconditions.checkNotNull(condition, "condition");
        synchronized (lock)
        {
            return doRequire(condition);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reaction onMatch (final ReactionTask task)
    {
        Preconditions.checkNotNull(task, "task");
        synchronized (lock)
        {
            onTrue = onTrue.andThen(task);
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reaction onError (final ReactionTask handler)
    {
        Preconditions.checkNotNull(handler, "handler");
        synchronized (lock)
        {
            onError = onError.andThen(handler);
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID uuid ()
    {
        return uuid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name ()
    {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reactor reactor ()
    {
        return reactor;
    }

    public boolean isReady ()
    {
        boolean condition = true;

        for (int i = 0; condition && i < requirements.size(); i++)
        {
            condition &= requirements.get(i).getAsBoolean();
        }

        return condition;
    }

    public boolean crank ()
    {
        final boolean condition = isReady();

        if (condition)
        {
            crankOnTrue();
        }

        return condition;
    }

    private void crankOnTrue ()
    {
        try
        {
            onTrue.run();
        }
        catch (Throwable ex1)
        {
            try
            {
                onError.run();
            }
            catch (Throwable ex2)
            {
                // TODO
                ex1.printStackTrace(System.err);
            }
        }
    }

}
