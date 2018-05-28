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

/**
 * What to do when overflow occurs.
 */
public enum OverflowPolicy
{
    /**
     * The overflow-policy depends on the queue implementation.
     */
    UNSPECIFIED,

    /**
     * Drop the oldest message that is already in the queue.
     */
    DROP_OLDEST,

    /**
     * Drop the newest message that is already in the queue.
     */
    DROP_NEWEST,

    /**
     * Drop the message that is being inserted into the queue,
     * rather than removing an element that is already in the queue.
     */
    DROP_INCOMING,

    /**
     * Drop everything already in the queue, but accept the incoming message.
     */
    DROP_PENDING,

    /**
     * Drop everything already in the queue and the incoming message.
     */
    DROP_ALL,
    /**
     * Throw an <code>IllegalStateException</code>.
     */
    THROW,
}
