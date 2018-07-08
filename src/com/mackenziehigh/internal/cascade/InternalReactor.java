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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.Input;
import com.mackenziehigh.cascade.Output;
import com.mackenziehigh.cascade.OverflowPolicy;
import com.mackenziehigh.cascade.Powerplant;
import com.mackenziehigh.cascade.Reaction;
import com.mackenziehigh.cascade.Reactor;
import com.mackenziehigh.cascade.powerplants.NopPowerplant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Combined Implementation of <code>ReactorBuilder</code> and <code>Reactor</code>.
 */
public final class InternalReactor
        implements Reactor
{
    private final UUID uuid = UUID.randomUUID();

    private volatile String name = uuid.toString();

    private volatile boolean reacting = false;

    private final AtomicReference<Object> meta = new AtomicReference<>();

    private volatile Powerplant powerplant = new NopPowerplant();

    private final Object lock = new Object();

    private final Semaphore runLock = new Semaphore(1);

    private final List<InternalReaction> reactionsList = Lists.newCopyOnWriteArrayList();

    private final Set<Input<?>> inputs = Sets.newCopyOnWriteArraySet();

    private final Set<Output<?>> outputs = Sets.newCopyOnWriteArraySet();

    private final List<Reaction> reactions = Lists.newCopyOnWriteArrayList();

    private final Set<Input<?>> unmodInputs = Collections.unmodifiableSet(inputs);

    private final Set<Output<?>> unmodOutputs = Collections.unmodifiableSet(outputs);

    private final List<Reaction> unmodReactions = Collections.unmodifiableList(reactions);

    /**
     * {@inheritDoc}
     */
    @Override
    public Reactor named (final String name)
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
    public Reactor poweredBy (final Powerplant executor)
    {
        Objects.requireNonNull(executor, "executor");

        synchronized (lock)
        {
            powerplant.onUnbind(this, meta);
            powerplant = executor;
            powerplant.onBind(this, meta);
            ping();
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reaction newReaction ()
    {
        synchronized (lock)
        {
            final InternalReaction builder = new InternalReaction(this);
            reactionsList.add(builder);
            return builder;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Input newArrayInput (final Class<T> type,
                                    final int capacity,
                                    final OverflowPolicy policy)
    {
        synchronized (lock)
        {
            final InternalInput<T> input = InternalInput.newArrayInput(this, type, capacity, policy);
            inputs.add(input);
            return input;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Input newLinkedInput (final Class<T> type,
                                     final int capacity,
                                     final OverflowPolicy policy)
    {
        synchronized (lock)
        {
            final InternalInput<T> input = InternalInput.newLinkedInput(this, type, capacity, policy);
            inputs.add(input);
            return input;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Output<T> newOutput (final Class<T> type)
    {
        synchronized (lock)
        {
            final Output<T> output = new InternalOutput<>(this, type);
            outputs.add(output);
            return output;
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
    public Set<Input<?>> inputs ()
    {
        return unmodInputs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Output<?>> outputs ()
    {
        return unmodOutputs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Reaction> reactions ()
    {
        return unmodReactions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reactor disconnect ()
    {
        synchronized (lock)
        {
            inputs().forEach(x -> x.disconnect());
            outputs().forEach(x -> x.disconnect());
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isReacting ()
    {
        return reacting;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Powerplant powerplant ()
    {
        return powerplant;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InternalReactor ping ()
    {
        powerplant.onPing(this, meta);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean crank ()
    {

        boolean result = false;

        /**
         * The tryAquire prevents multi-threaded power-plants from inadvertently blocking multiple worker threads.
         */
        if (runLock.tryAcquire())
        {
            try
            {
                reacting = true;

                synchronized (lock)
                {
                    for (int i = 0; i < reactionsList.size(); i++)
                    {
                        result |= reactionsList.get(i).crank();
                    }
                }
            }
            finally
            {
                reacting = false;
                runLock.release();
            }
        }

        return result;
    }

}
