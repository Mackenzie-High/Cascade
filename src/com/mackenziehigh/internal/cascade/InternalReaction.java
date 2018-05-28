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
import com.mackenziehigh.cascade.builder.ReactionBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/**
 * Combined Implementation of <code>ReactionBuilder</code> and <code>Reaction</code>.
 */
public final class InternalReaction
        implements ReactionBuilder,
                   Reaction
{
    private final Object lock = new Object();

    private final MockableReactor reactor;

    private final UUID uuid = UUID.randomUUID();

    private volatile String name = uuid.toString();

    private final List<BooleanSupplier> requirements = Lists.newCopyOnWriteArrayList();

    private volatile ReactionTask onTrue = () ->
    {
        // Pass.
    };

    // TODO: NOP instead????
    private volatile ReactionTask onError = () -> reactor().get().stop();

    private volatile boolean atStart = false;

    private volatile boolean atStop = false;

    private volatile boolean keepAliveRequired = false;

    private volatile boolean built = false;

    private volatile boolean starting = false;

    private volatile boolean stopping = false;

    public InternalReaction (final MockableReactor reactor)
    {
        this.reactor = Objects.requireNonNull(reactor, "reactor");
    }

    public void setStarting (final boolean flag)
    {
        synchronized (lock)
        {
            starting = flag;
        }
    }

    public void setStopping (final boolean flag)
    {
        synchronized (lock)
        {
            stopping = flag;
        }
    }

    private void requireEgg ()
    {
        Preconditions.checkState(!built, "Already Built!");
    }

    private ReactionBuilder doRequire (final BooleanSupplier condition)
    {
        requirements.add(condition);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReactionBuilder named (final String name)
    {
        synchronized (lock)
        {
            requireEgg();
            this.name = Objects.requireNonNull(name, "name");
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReactionBuilder atStart ()
    {
        synchronized (lock)
        {
            requireEgg();
            atStart = true;
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReactionBuilder atStop ()
    {
        synchronized (lock)
        {
            requireEgg();
            atStop = true;
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isKeepAliveRequired ()
    {
        return keepAliveRequired;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReactionBuilder require (final Input<?> input,
                                    final int count)
    {
        synchronized (lock)
        {
            requireEgg();
            Preconditions.checkNotNull(input, "input");
            Preconditions.checkArgument(count >= 0, "count < 0");
            return doRequire(() -> input.size() >= count);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReactionBuilder require (final Input<?> input)
    {
        return require(input, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> ReactionBuilder require (final Input<T> input,
                                        final Predicate<T> head)
    {
        synchronized (lock)
        {
            requireEgg();
            Preconditions.checkNotNull(input, "input");
            Preconditions.checkNotNull(head, "head");
            keepAliveRequired = true;
            return doRequire(() -> head.test(input.peekOrDefault(null)));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReactionBuilder require (final Output<?> output)
    {
        return require(output, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReactionBuilder require (final Output<?> output,
                                    final int count)
    {
        synchronized (lock)
        {
            requireEgg();
            Preconditions.checkNotNull(output, "output");
            Preconditions.checkArgument(count >= 0, "count < 0");
            return doRequire(() -> output.remainingCapacity() >= count);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReactionBuilder require (final BooleanSupplier condition)
    {
        synchronized (lock)
        {
            requireEgg();
            Preconditions.checkNotNull(condition, "condition");
            keepAliveRequired = true;
            return doRequire(condition);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReactionBuilder onMatch (final ReactionTask task)
    {
        synchronized (lock)
        {
            requireEgg();
            Preconditions.checkNotNull(task, "task");
            onTrue = onTrue.andThen(task);
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReactionBuilder onError (final ReactionTask handler)
    {
        synchronized (lock)
        {
            requireEgg();
            Preconditions.checkNotNull(handler, "handler");
            onError = onError.andThen(handler);
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reaction build ()
    {
        synchronized (lock)
        {
            requireEgg();
            built = true;
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
    public Optional<Reactor> reactor ()
    {
        return reactor.reactor();
    }

    public boolean crank ()
    {
        if (starting && !atStart)
        {
            return false;
        }
        else if (stopping && !atStop)
        {
            return false;
        }

        boolean condition = true;

        for (int i = 0; condition && i < requirements.size(); i++)
        {
            condition &= requirements.get(i).getAsBoolean();
        }

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
