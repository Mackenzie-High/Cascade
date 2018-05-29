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

import java.util.Optional;
import java.util.UUID;

/**
 * A reaction defines how a reactor behaves in response to inputs.
 */
public interface Reaction
{

    /**
     * Retrieve a UUID that uniquely identifies this reaction in space-time.
     *
     * @return the unique identifier of this reaction.
     */
    public UUID uuid ();

    /**
     * Retrieve the name of this reaction.
     *
     * @return the name of this reaction.
     */
    public String name ();

    /**
     * Retrieve the reactor that this reaction is a part of.
     *
     * @return the enclosing reactor, or empty,
     * if the reactor is not fully constructed yet.
     */
    public Optional<Reactor> reactor ();

    /**
     * Determine whether this reaction requires periodic <i>keep-alive</i> execution.
     *
     * @return true, if this reaction requires keep-alive execution.
     */
    public boolean isKeepAliveRequired ();
}
