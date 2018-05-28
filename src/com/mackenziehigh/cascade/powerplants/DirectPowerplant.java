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

import com.mackenziehigh.cascade.Powerplant;
import com.mackenziehigh.cascade.Reactor;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
public final class DirectPowerplant
        implements Powerplant
{

    @Override
    public void onStart (final Reactor reactor,
                         final AtomicReference<Object> meta)
    {
        // Pass
    }

    @Override
    public void onStop (final Reactor reactor,
                        final AtomicReference<Object> meta)
    {
        // Pass
    }

    @Override
    public void onPing (final Reactor reactor,
                        final AtomicReference<Object> meta)
    {
        reactor.crank();
    }

    @Override
    public void close ()
            throws Exception
    {
        // Pass
    }
}
