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

import java.util.concurrent.atomic.AtomicReference;

/**
 * A <code>Powerplant</code> powers <code>Reactor</code>s, as needed.
 */
public interface Powerplant
        extends AutoCloseable
{
    /**
     * A reactor will invoke this event-handler in order
     * to notify this powerplant that the powerplant is
     * now responsible for powering the reactor.
     *
     * @param reactor needs to be powered by this powerplant.
     * @param meta can be freely used by the powerplant for
     * storing information related to the reactor.
     */
    public void onBind (Reactor reactor,
                         AtomicReference<Object> meta);

    /**
     * A reactor will invoke this event-handler in order
     * to notify this powerplant that the powerplant is
     * no longer responsible for powering the reactor.
     *
     * @param reactor no longer needs to be powered by this powerplant.
     * @param meta can freely used by the powerplant for
     * storing information related to the reactor.
     */
    public void onUnbind (Reactor reactor,
                        AtomicReference<Object> meta);

    /**
     * A reactor will invoke this event-handler in order
     * to notify this powerplant that the powerplant needs
     * to immediately provide power to the given reactor.
     *
     * <p>
     * Power will be applied by turning the reactor's <code>crank()</code>.
     * </p>
     *
     * @param reactor needs power applied immediately.
     * @param meta can freely used by the powerplant for
     * storing information related to the reactor.
     */
    public void onPing (Reactor reactor,
                        AtomicReference<Object> meta);

    /**
     * Invoke the method in order to cause the powerplant to shutdown.
     *
     * <p>
     * This method merely signals the powerplant to begin shutting down.
     * The powerplant may take an arbitrarily long amount of time
     * to fully shutdown after this method returns.
     * </p>
     *
     * @throws Exception if something goes wrong.
     */
    @Override
    public void close ()
            throws Exception;
}
