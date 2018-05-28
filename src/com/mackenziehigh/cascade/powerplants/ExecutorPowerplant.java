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
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
public final class ExecutorPowerplant
        implements Powerplant
{
    private final ExecutorService service;

    private final Set<Reactor> keepAlives = Sets.newConcurrentHashSet();

    private ExecutorPowerplant (final ExecutorService service)
    {
        this.service = service;
    }

    @Override
    public void onStart (final Reactor reactor,
                         final AtomicReference<Object> meta)
    {
        meta.set(new AtomicBoolean());

        if (reactor.isKeepAliveRequired())
        {
            keepAlives.add(reactor);
        }
    }

    @Override
    public void onStop (final Reactor reactor,
                        final AtomicReference<Object> meta)
    {
        keepAlives.remove(reactor);
    }

    @Override
    public void onPing (final Reactor reactor,
                        final AtomicReference<Object> meta)
    {
        final AtomicBoolean flag = (AtomicBoolean) meta.get();

        if (flag.compareAndSet(false, true))
        {
            // TODO: Catch excpetions???
            service.submit(() -> run(reactor));
        }
    }

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

    private void keepAlive ()
    {
        keepAlives.forEach(x -> x.ping());
    }

    public static ExecutorPowerplant create (final int threadCount,
                                             final Duration keepalive)
    {
        final ScheduledExecutorService service = Executors.newScheduledThreadPool(threadCount);
        return create(service, keepalive);
    }

    public static ExecutorPowerplant create (final ScheduledExecutorService service,
                                             final Duration keepalive)
    {
        final ExecutorPowerplant plant = new ExecutorPowerplant(service);

        // TODO: Is this prone to some sort of internal queue backlogs???
        service.scheduleWithFixedDelay(plant::keepAlive, 0, keepalive.toNanos(), TimeUnit.NANOSECONDS);

        plant.keepAlive();

        return plant;
    }

}
