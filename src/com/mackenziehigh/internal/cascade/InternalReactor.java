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

import com.mackenziehigh.cascade.Powerplant;
import com.mackenziehigh.cascade.Reactor;
import com.mackenziehigh.internal.cascade.powerplants.NopPowerplant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementation of <code>Reactor</code>.
 */
public final class InternalReactor
        implements Reactor
{
    private final UUID uuid = UUID.randomUUID();

    private volatile String name = uuid.toString();

    private volatile boolean reacting = false;

    private final AtomicReference<Object> meta = new AtomicReference<>();

    private volatile Powerplant powerplant = new NopPowerplant();

    private final Semaphore runLock = new Semaphore(1);

    private final CopyOnWriteArrayList<InternalReaction> reactions = new CopyOnWriteArrayList<>();

    private final Set<Input<?>> inputs = new CopyOnWriteArraySet<>();

    private final Set<Output<?>> outputs = new CopyOnWriteArraySet<>();

    private final Set<Input<?>> unmodInputs = Collections.unmodifiableSet(inputs);

    private final Set<Output<?>> unmodOutputs = Collections.unmodifiableSet(outputs);

    private final List<Reaction> unmodReactions = Collections.unmodifiableList(reactions);

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Reactor named (final String name)
    {
        this.name = Objects.requireNonNull(name, "name");
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Reactor poweredBy (final Powerplant executor)
    {
        Objects.requireNonNull(executor, "executor");
        powerplant.onUnbind(this, meta);
        powerplant = executor;
        powerplant.onBind(this, meta);
        signal();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized <T> Input<T> newInput (final Class<T> type)
    {
        Objects.requireNonNull(type, "type");
        final Input<T> input = new InternalInput<>(this, type);
        inputs.add(input);
        return input;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized <T> Output<T> newOutput (final Class<T> type)
    {
        Objects.requireNonNull(type, "type");
        final Output<T> output = new InternalOutput<>(this, type);
        outputs.add(output);
        return output;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Reaction newReaction ()
    {
        final InternalReaction builder = new InternalReaction(this);
        reactions.add(builder);
        return builder;

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
    public synchronized Set<Input<?>> inputs ()
    {
        return unmodInputs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Set<Output<?>> outputs ()
    {
        return unmodOutputs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized List<Reaction> reactions ()
    {
        return unmodReactions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Reactor disconnect ()
    {
        inputs().forEach(x -> x.disconnect());
        outputs().forEach(x -> x.disconnect());
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean isReacting ()
    {
        return reacting;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Powerplant powerplant ()
    {
        return powerplant;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized String toString ()
    {
        return name;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Do *not* make this method synchronized.
     * </p>
     */
    @Override
    public InternalReactor signal ()
    {
        powerplant.onSignal(this, meta);
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Do *not* make this method synchronized.
     * </p>
     */
    @Override
    public boolean crank ()
    {

        boolean result = false;

        try
        {
            /**
             * The try-aquire prevents multi-threaded power-plants
             * from inadvertently blocking multiple worker threads.
             */
            if (runLock.tryAcquire())
            {
                try
                {
                    reacting = true;

                    for (int i = 0; i < reactions.size(); i++)
                    {
                        result |= reactions.get(i).crank();
                    }
                }
                finally
                {
                    reacting = false;
                    runLock.release();
                }
            }
        }
        catch (Throwable ex)
        {
            // Pass, but never throw exceptions.
        }

        return result;
    }
}
