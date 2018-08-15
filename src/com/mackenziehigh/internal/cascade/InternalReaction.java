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
import com.mackenziehigh.cascade.Reactor.Reaction;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;

/**
 * Implementation of <code>Reaction</code>.
 */
final class InternalReaction
        implements Reaction
{
    private final Reactor reactor;

    private final UUID uuid = UUID.randomUUID();

    private volatile String name = uuid.toString();

    private final List<BooleanSupplier> requirements = new CopyOnWriteArrayList<>();

    private volatile ReactionTask onTrue = () ->
    {
        // Pass.
    };

    // TODO: NOP instead????
    private volatile ErrorHandlerTask onError = ex ->
    {
        // Pass.
    };

    public InternalReaction (final Reactor reactor)
    {
        this.reactor = Objects.requireNonNull(reactor, "reactor");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Reaction named (final String name)
    {
        this.name = Objects.requireNonNull(name, "name");
        reactor.signal();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Reaction require (final BooleanSupplier condition)
    {
        Objects.requireNonNull(condition, "condition");
        requirements.add(condition);
        reactor.signal();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Reaction onMatch (final ReactionTask task)
    {
        Objects.requireNonNull(task, "task");
        onTrue = onTrue.andThen(task);
        reactor.signal();
        return this;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Reaction onError (final ErrorHandlerTask handler)
    {
        Objects.requireNonNull(handler, "handler");
        onError = onError.andThen(handler);
        reactor.signal();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized UUID uuid ()
    {
        return uuid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized String name ()
    {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Reactor reactor ()
    {
        return reactor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized String toString ()
    {
        return name;
    }

    public synchronized boolean crank ()
    {
        final boolean condition = isReady();

        if (condition)
        {
            crankOnTrue();
        }

        return condition;
    }

    public synchronized boolean isReady ()
    {
        boolean condition = true;

        for (int i = 0; condition && i < requirements.size(); i++)
        {
            try
            {
                condition &= requirements.get(i).getAsBoolean();
            }
            catch (Throwable ex1)
            {
                /**
                 * The condition threw an exception; therefore, we do not know for
                 * sure that all of the *necessary* preconditions have been met.
                 */
                condition = false;

                try
                {
                    onError.accept(ex1);
                }
                catch (Throwable ex2)
                {
                    // Pass.
                }
            }
        }

        return condition;
    }

    private void crankOnTrue ()
    {
        try
        {
            onTrue.execute();
        }
        catch (Throwable ex1)
        {
            try
            {
                onError.accept(ex1);
            }
            catch (Throwable ex2)
            {
                // Pass.
            }
        }
    }

}
