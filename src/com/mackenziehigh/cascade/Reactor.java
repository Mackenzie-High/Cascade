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

import java.util.SortedMap;
import java.util.UUID;

/**
 * A reaction responds to inputs by producing outputs and/or side-effects.
 */
public interface Reactor
{
    /**
     * Retrieve a UUID that uniquely identifies this reactor in space-time.
     *
     * @return the unique identifier of this reactor.
     */
    public UUID uuid ();

    /**
     * Retrieve the name of this reactor.
     *
     * @return the name of this reactor.
     */
    public String name ();

    /**
     * Retrieve all of the inputs that feed messages into this reactor.
     *
     * @return an immutable map that maps input-names to inputs.
     */
    public SortedMap<String, Input<?>> inputs ();

    /**
     * Retrieve all of the outputs that transmit messages from this reactor.
     *
     * @return an immutable map that maps output-names to outputs.
     */
    public SortedMap<String, Output<?>> outputs ();

    /**
     * Retrieve all of the reactions that define the behavior of this reactor.
     *
     * @return an immutable map that maps reaction-names to reactions.
     */
    public SortedMap<String, Reaction> reactions ();

    /**
     * Cause this reactor to begin accepting and reacting to inputs.
     *
     * <p>
     * This method is a no-op, if this method was invoked previously.
     * </p>
     *
     * @return this.
     */
    public Reactor start ();

    /**
     * Cause this reactor to cease accepting and reacting to inputs.
     *
     * <p>
     * This method is a no-op, if this method was invoked previously.
     * </p>
     *
     * @return this.
     */
    public Reactor stop ();

    /**
     * Determine whether this reactor is in the unstarted phase of its life-cycle.
     *
     * @return true, if <code>start()</code> was not invoked yet.
     */
    public boolean isUnstarted ();

    /**
     * Determine whether this reactor is in the startup phase of its life-cycle.
     *
     * @return true, if <code>start()</code> was invoked, but startup is not yet complete.
     */
    public boolean isStarting ();

    /**
     * Determine whether this reactor has already started and has not begun to stop.
     *
     * @return true, if startup is complete, but <code>stop()</code> was not invoked yet.
     */
    public boolean isStarted ();

    /**
     * Determine whether this reactor is in the stopping phase of its life-cycle.
     *
     * @return true, if <code>stop</code> was invoked, but stopping has not completed yet.
     */
    public boolean isStopping ();

    /**
     * Determine whether this reactor is now in the stopped phase of its life-cycle.
     *
     * @return true, if this reactor has fully stopped.
     */
    public boolean isStopped ();

    /**
     * Determine whether this reactor is neither unstarted nor stopped.
     *
     * @return true, if this reactor is still capable of reacting to inputs.
     */
    public boolean isAlive ();

    /**
     * Determine whether this reactor is currently reacting to an input.
     *
     * @return true, if a reaction is in-progress.
     */
    public boolean isReacting ();

    /**
     * Retrieve the powerplant that provides power to this reactor whenever necessary.
     *
     * @return the associated powerplant.
     */
    public Powerplant powerplant ();

    /**
     * Determine whether this reactor requires periodic <i>keep-alive</i> <code>crank()</code> invocations.
     *
     * @return true, if any of the underlying reactions require keep-alive execution.
     */
    public boolean isKeepAliveRequired ();

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
