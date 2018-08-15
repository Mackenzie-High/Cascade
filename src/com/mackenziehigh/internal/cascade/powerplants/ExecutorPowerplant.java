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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A powerplant this is based on a <code>ScheduledExecutorService</code>.
 */
public final class ExecutorPowerplant
        implements Powerplant
{
    private final ExecutorService service;

    private ExecutorPowerplant (final ExecutorService service)
    {
        this.service = service;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBind (final Reactor reactor,
                        final AtomicReference<Object> meta)
    {
        meta.set(new AtomicBoolean());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUnbind (final Reactor reactor,
                          final AtomicReference<Object> meta)
    {
        // Pass
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSignal (final Reactor reactor,
                          final AtomicReference<Object> meta)
    {
        final AtomicBoolean flag = (AtomicBoolean) meta.get();

        if (flag.compareAndSet(false, true))
        {
            try
            {
                // TODO: Catch excpetions???
                service.submit(() -> run(reactor));
            }
            finally
            {
                flag.set(false);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close ()
            throws Exception
    {
        service.shutdown();
    }

    private void run (final Reactor reactor)
    {
        if (reactor.crank())
        {
            service.submit(() -> run(reactor));
        }
    }

    public static ExecutorPowerplant from (final ExecutorService service)
    {
        return new ExecutorPowerplant(service);
    }
}
