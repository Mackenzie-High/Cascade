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

import com.mackenziehigh.internal.cascade.powerplants.DedicatedPowerplant;
import com.mackenziehigh.internal.cascade.powerplants.DirectPowerplant;
import com.mackenziehigh.internal.cascade.powerplants.ExecutorPowerplant;
import com.mackenziehigh.internal.cascade.powerplants.NopPowerplant;
import java.util.concurrent.ExecutorService;

/**
 * Factory.
 */
public final class Powerplants
{
    /**
     * Create a new <b>direct</b> powerplant.
     *
     * @return the new powerplant.
     */
    public static Powerplant newDirectPowerplant ()
    {
        return new DirectPowerplant();
    }

    /**
     * Create a new <b>executor</b> powerplant.
     *
     * @param service provides the threads that will be used by the powerplant.
     * @return the new powerplant.
     */
    public static Powerplant newExecutorPowerplant (final ExecutorService service)
    {
        return ExecutorPowerplant.from(service);
    }

    /**
     * Create a new <b>dedicated</b> powerplant.
     *
     * @return the new powerplant.
     */
    public static Powerplant newDedicatedPowerplant ()
    {
        return new DedicatedPowerplant();
    }

    /**
     * Create a new <b>nop</b> powerplant.
     *
     * @return the new powerplant.
     */
    public static Powerplant newNopPowerplant ()
    {
        return new NopPowerplant();
    }

}
