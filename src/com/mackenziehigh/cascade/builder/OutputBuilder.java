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

import com.mackenziehigh.cascade.PrivateOutput;
import java.util.function.Predicate;

/**
 *
 */
public interface OutputBuilder<T>
{
    /**
     * Specify the name of the output.
     *
     * @param name will be the name of the output.
     * @return this.
     */
    public OutputBuilder<T> named (String name);

    /**
     * Specify a verification-check that will be performed
     * whenever a message is via this output.
     *
     * <p>
     * The verification-check will be performed by the sender
     * on whatever thread the sender is executing on.
     * Moreover, the verification-check may be executed concurrently
     * by different senders given different messages.
     * </p>
     *
     * <p>
     * The verification-check will never receive null as a message.
     * </p>
     *
     * @param condition must be true given the outgoing message.
     * @return this.
     */
    public OutputBuilder<T> verify (Predicate<T> condition);

    /**
     * Construct the new output and add it to the reactor.
     *
     * @return the new output.
     */
    public PrivateOutput<T> build ();
}
