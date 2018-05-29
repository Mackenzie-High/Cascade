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
package com.mackenziehigh.cascade.builder;

import com.mackenziehigh.cascade.Powerplant;
import com.mackenziehigh.cascade.Reactor;

/**
 *
 */
public interface ReactorBuilder
{
    /**
     * Specify the name of the reactor.
     *
     * @param name will be the name of the reactor.
     * @return this.
     */
    public ReactorBuilder named (String name);

    /**
     * Specify the powerplant that will power the reactor.
     *
     * @param plant will power the reactor whenever needed.
     * @return this.
     */
    public ReactorBuilder poweredBy (Powerplant plant);

    /**
     * Add an input that feeds incoming messages to the reactor.
     *
     * @param <T>
     * @param type will be the type of messages received via the input.
     * @return a builder that can be used to construct the input.
     */
    public <T> ArrayInputBuilder<T> newArrayInput (Class<T> type);

    /**
     * Add an output that will transmit outgoing messages from the reactor.
     *
     * @param <T>
     * @param type will be the type of messages transmitted via the output.
     * @return a builder that can be used to construct the output.
     */
    public <T> OutputBuilder<T> newOutput (Class<T> type);

    /**
     * Specify a reaction to execute whenever the reactor
     * is powered and any necessary conditions are met.
     *
     * @return a builder that can be used to construct the reaction.
     */
    public ReactionBuilder newReaction ();

    /**
     * Construct the reactor.
     *
     * @return the new reactor.
     * @throws IllegalStateException if any of the inputs, outputs,
     * or reactions are still under construction.
     */
    public Reactor build ();

}
