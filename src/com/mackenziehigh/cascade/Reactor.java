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
package com.mackenziehigh.cascade;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A reactor responds to inputs by producing outputs and/or side-effects via reactions.
 */
public interface Reactor
{
    /**
     * Create a reaction to execute whenever this reactor receives an input.
     *
     * @return the new reaction.
     * @throws IllegalStateException if the reactor has already started.
     */
    public Reaction newReaction ();

    /**
     *
     *
     * @param <T>
     * @param type
     * @param capacity
     * @return
     * @throws IllegalStateException if the reactor has already started.
     */
    public default <T> Input newArrayInput (final Class<T> type,
                                            final int capacity)
    {
        return newArrayInput(type, capacity, OverflowPolicy.DROP_INCOMING);
    }

    /**
     *
     * @param <T>
     * @param type
     * @param capacity
     * @param policy
     * @return
     * @throws IllegalStateException if the reactor has already started.
     */
    public <T> Input newArrayInput (Class<T> type,
                                    int capacity,
                                    OverflowPolicy policy);

    /**
     *
     * @param <T>
     * @param type
     * @return
     * @throws IllegalStateException if the reactor has already started.
     */
    public default <T> Input newLinkedInput (final Class<T> type)
    {
        return newLinkedInput(type, Integer.MAX_VALUE, OverflowPolicy.DROP_INCOMING);
    }

    /**
     *
     * @param <T>
     * @param type
     * @param capacity
     * @return
     * @throws IllegalStateException if the reactor has already started.
     */
    public default <T> Input newLinkedInput (final Class<T> type,
                                             final int capacity)
    {
        return newLinkedInput(type, capacity, OverflowPolicy.DROP_INCOMING);
    }

    /**
     *
     * @param <T>
     * @param type
     * @param capacity
     * @param policy
     * @return
     * @throws IllegalStateException if the reactor has already started.
     */
    public <T> Input newLinkedInput (Class<T> type,
                                     int capacity,
                                     OverflowPolicy policy);

    /**
     *
     * @param <T>
     * @param type
     * @return
     * @throws IllegalStateException if the reactor has already started.
     */
    public <T> Output<T> newOutput (Class<T> type);

    /**
     * Get the UUID that uniquely identifies this reactor in space-time.
     *
     * @return the unique identifier of this reactor.
     */
    public UUID uuid ();

    /**
     * Get the name of this reactor.
     *
     * <p>
     * By default, the name is the string representation of the <code>uuid()</code>.
     * </p>
     *
     * @return the name of this reactor.
     */
    public String name ();

    /**
     * Set the name of the reactor.
     *
     * <p>
     * This method can be called after the reactor has started.
     * </p>
     *
     * @param name will be the name of the reactor.
     * @return this.
     */
    public Reactor named (String name);

    /**
     * Get all of the inputs that feed messages into this reactor.
     *
     * @return the inputs.
     */
    public Set<Input<?>> inputs ();

    /**
     * Get all of the outputs that transmit messages from this reactor.
     *
     * @return the outputs.
     */
    public Set<Output<?>> outputs ();

    /**
     * Get all of the reactions that define the behavior of this reactor.
     *
     * @return the reactions.
     */
    public List<Reaction> reactions ();

    /**
     * Disconnect the reactor from all of the inputs and outputs.
     *
     * @return this.
     */
    public Reactor disconnect ();

    /**
     * Determine whether this reactor is currently reacting to an input.
     *
     * @return true, if a reaction is in-progress.
     */
    public boolean isReacting ();

    /**
     * Get the powerplant that provides power to this reactor whenever necessary.
     *
     * <p>
     * The powerplant may change during the life of the reactor.
     * </p>
     *
     * @return the associated powerplant.
     */
    public Powerplant powerplant ();

    /**
     * Set the powerplant that will power the reactor.
     *
     * <p>
     * This method can be called after the reactor has started.
     * </p>
     *
     * @param plant will power the reactor whenever needed.
     * @return this.
     */
    public Reactor poweredBy (Powerplant plant);

    /**
     * Cause this reactor to be scheduled for execution by the powerplant.
     *
     * @return this.
     */
    public Reactor ping ();

    /**
     * Cause this reactor to perform a single unit-of-work, if work is available.
     *
     * <p>
     * A single unit-of-work entails executing each reaction once.
     * </p>
     *
     * @return true, if meaningful work was performed.
     */
    public boolean crank ();
}
