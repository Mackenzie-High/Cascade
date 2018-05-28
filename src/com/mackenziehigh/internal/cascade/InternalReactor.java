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
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.Input;
import com.mackenziehigh.cascade.Output;
import com.mackenziehigh.cascade.Powerplant;
import com.mackenziehigh.cascade.Reaction;
import com.mackenziehigh.cascade.Reactor;
import com.mackenziehigh.cascade.builder.OutputBuilder;
import com.mackenziehigh.cascade.builder.ReactionBuilder;
import com.mackenziehigh.cascade.builder.ReactorBuilder;
import com.mackenziehigh.cascade.powerplants.NopPowerplant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 *
 */
public final class InternalReactor
        implements ReactorBuilder,
                   Reactor,
                   MockableReactor
{

    private static enum LifeCycle
    {
        UNSTARTED,
        STARTING,
        STARTED,
        STOPPING,
        STOPPED,
    }

    private final UUID uuid = UUID.randomUUID();

    private volatile String name = uuid.toString();

    private volatile boolean built = false;

    private volatile boolean reacting = false;

    private volatile LifeCycle phase = LifeCycle.UNSTARTED;

    private final AtomicReference<Object> meta = new AtomicReference<>();

    private volatile Powerplant powerplant = new NopPowerplant();

    private final Object lock = new Object();

    private final Semaphore runLock = new Semaphore(1);

    private final Set<Input<?>> inputsBuilder = Sets.newConcurrentHashSet();

    private final Set<Output<?>> outputsBuilder = Sets.newConcurrentHashSet();

    private final List<InternalReaction> reactionsList = Lists.newCopyOnWriteArrayList();

    private volatile SortedMap<String, Input<?>> inputs = ImmutableSortedMap.of();

    private volatile SortedMap<String, Output<?>> outputs = ImmutableSortedMap.of();

    private volatile SortedMap<String, Reaction> reactions = ImmutableSortedMap.of();

    private void requireEgg ()
    {
        Preconditions.checkState(!built, "Already Built!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Reactor> reactor ()
    {
        return built ? Optional.of(this) : Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReactorBuilder named (final String name)
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
    public ReactorBuilder poweredBy (final Powerplant executor)
    {
        synchronized (lock)
        {
            requireEgg();
            this.powerplant = Objects.requireNonNull(executor, "executor");
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> ArrayInput<T> newArrayInput (final Class<T> type)
    {
        synchronized (lock)
        {
            requireEgg();
            Preconditions.checkNotNull(type, "type");
            final ArrayInput<T> input = new ArrayInput<>(this, type);
            inputsBuilder.add(input);
            return input;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> OutputBuilder<T> newOutput (final Class<T> type)
    {
        synchronized (lock)
        {
            requireEgg();
            Preconditions.checkNotNull(type, "type");
            final InternalOutput<T> output = new InternalOutput<>(this, type);
            outputsBuilder.add(output);
            return output;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReactionBuilder newReaction ()
    {
        synchronized (lock)
        {
            requireEgg();
            final InternalReaction builder = new InternalReaction(this);
            reactionsList.add(builder);
            return builder;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reactor build ()
    {
        synchronized (lock)
        {
            requireEgg();
            inputs = ImmutableSortedMap.copyOf(inputsBuilder.stream().collect(Collectors.toMap(x -> x.name(), x -> x)));
            outputs = ImmutableSortedMap.copyOf(outputsBuilder.stream().collect(Collectors.toMap(x -> x.name(), x -> x)));
            reactions = ImmutableSortedMap.copyOf(reactionsList.stream().collect(Collectors.toMap(x -> x.name(), x -> x)));
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
    public SortedMap<String, Input<?>> inputs ()
    {
        return inputs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedMap<String, Output<?>> outputs ()
    {
        return outputs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedMap<String, Reaction> reactions ()
    {
        return reactions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reactor start ()
    {
        synchronized (lock)
        {
            if (isUnstarted())
            {
                phase = LifeCycle.STARTING;
                ping();
            }
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reactor stop ()
    {
        synchronized (lock)
        {
            // TODO: What if Unstarted or Starting??????

            if (isStarted())
            {
                phase = LifeCycle.STOPPING;
                ping();
            }
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUnstarted ()
    {
        return phase == LifeCycle.UNSTARTED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStarting ()
    {
        return phase == LifeCycle.STARTING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStarted ()
    {
        return phase == LifeCycle.STARTED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStopping ()
    {
        return phase == LifeCycle.STOPPING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStopped ()
    {
        return phase == LifeCycle.STOPPED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAlive ()
    {
        final boolean result = isStarting() || isStarted() || isStopping();
        return result;
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
    public boolean isKeepAliveRequired ()
    {
        return reactionsList.stream().anyMatch(x -> x.isKeepAliveRequired());
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
            /**
             * This lock prevents start() and stop() being called while in the critical section,
             * where the start() and stop() state will be queried.
             */
            synchronized (lock)
            {
                if (phase == LifeCycle.UNSTARTED)
                {
                    return false;
                }
                else if (phase == LifeCycle.STOPPED)
                {
                    return false;
                }

                try
                {
                    reacting = true;

                    if (isStarting())
                    {
                        powerplant.onStart(this, meta);
                        reactionsList.forEach(x -> x.setStarting(true));
                    }
                    else if (isStopping())
                    {
                        reactionsList.forEach(x -> x.setStopping(true));
                    }

                    for (int i = 0; i < reactionsList.size(); i++)
                    {
                        result |= reactionsList.get(i).crank();
                    }

                    if (isStarting())
                    {
                        reactionsList.forEach(x -> x.setStarting(false));
                        phase = LifeCycle.STARTED;
                    }
                    else if (isStopping())
                    {
                        reactionsList.forEach(x -> x.setStopping(false));
                        phase = LifeCycle.STOPPED;
                        powerplant.onStop(this, meta);
                    }
                }
                finally
                {
                    reacting = false;
                    runLock.release();
                }
            }
        }

        return result;
    }

}
