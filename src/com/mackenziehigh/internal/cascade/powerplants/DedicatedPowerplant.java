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
package com.mackenziehigh.internal.cascade.powerplants;

import com.mackenziehigh.cascade.Powerplant;
import com.mackenziehigh.cascade.Reactor;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
public final class DedicatedPowerplant
        implements Powerplant
{
    private final List<Reactor> reactors = new CopyOnWriteArrayList<>();

    private final Semaphore semaphore = new Semaphore(1);

    @Override
    public void onBind (final Reactor reactor,
                        final AtomicReference<Object> meta)
    {

    }

    @Override
    public void onUnbind (final Reactor reactor,
                          final AtomicReference<Object> meta)
    {

    }

    @Override
    public void onSignal (final Reactor reactor,
                          final AtomicReference<Object> meta)
    {

    }

    @Override
    public void close ()
            throws Exception
    {

    }

    private void run ()
    {
        while (true)
        {
            try
            {
                if (semaphore.tryAcquire(1, TimeUnit.SECONDS))
                {
                    boolean workPerformed = false;

                    for (int i = 0; i < reactors.size(); i++)
                    {
                        try
                        {
                            final Reactor reactor = reactors.get(i);
                            workPerformed |= reactor.crank();
                        }
                        catch (Throwable ex)
                        {
                            // Pass
                        }
                    }
                }
            }
            catch (Throwable ex)
            {
                // Pass
            }
        }
    }
}
