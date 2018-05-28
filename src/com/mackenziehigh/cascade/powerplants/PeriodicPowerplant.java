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
package com.mackenziehigh.cascade.powerplants;

import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.Powerplant;
import com.mackenziehigh.cascade.Reactor;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TODO: Remove later. For dev only. Replaced by Executor Powerplant.
 */
public class PeriodicPowerplant
        implements Powerplant
{
    private final Set<Reactor> reactors = Sets.newConcurrentHashSet();

    private final Thread thread = new Thread(this::run);

    private final Semaphore wait = new Semaphore(0);

    public void add (final Reactor reactor)
    {
        reactors.add(reactor);
    }

    public void start ()
    {
        reactors.forEach(x -> x.start());
        thread.start();
    }

    private void run ()
    {
        try
        {
            while (true)
            {
                reactors.stream().forEach(x -> x.crank());
                wait.tryAcquire(250, TimeUnit.MILLISECONDS);
            }
        }
        catch (Throwable ex)
        {
            ex.printStackTrace(System.err);
        }
    }

    @Override
    public void onStart (final Reactor reactor,
                         final AtomicReference<Object> meta)
    {
        reactors.add(reactor);
    }

    @Override
    public void onStop (final Reactor reactor,
                        final AtomicReference<Object> meta)
    {
        // Pass.
    }

    @Override
    public void onPing (final Reactor reactor,
                        final AtomicReference<Object> meta)
    {
        // Pass.
    }

    @Override
    public void close ()
            throws Exception
    {
        // Pass.
    }

}
