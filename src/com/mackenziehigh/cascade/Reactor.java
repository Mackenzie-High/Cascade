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
 *
 */
public interface Reactor
{
    public UUID uuid ();

    public String name ();

    public SortedMap<String, Input<?>> inputs ();

    public SortedMap<String, Output<?>> outputs ();

    public SortedMap<String, Reaction> reactions ();

    public Reactor start ();

    public Reactor stop ();

    public boolean isUnstarted ();

    public boolean isStarting ();

    public boolean isStarted ();

    public boolean isStopping ();

    public boolean isStopped ();

    public boolean isAlive ();

    public boolean isReacting ();

    public Powerplant powerplant ();

    public boolean isKeepAliveRequired ();

    public Reactor ping ();

    public boolean crank ();

}
