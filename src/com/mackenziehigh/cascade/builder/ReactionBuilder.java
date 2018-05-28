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

import com.google.common.base.Preconditions;
import com.mackenziehigh.cascade.Input;
import com.mackenziehigh.cascade.Output;
import com.mackenziehigh.cascade.Reaction;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/**
 *
 */
public interface ReactionBuilder
{
    /**
     *
     */
    @FunctionalInterface
    public interface ReactionTask
    {
        public void run ()
                throws Throwable;

        public default ReactionTask andThen (final ReactionTask next)
        {
            Preconditions.checkNotNull(next, "next");
            return () ->
            {
                this.run();
                next.run();
            };
        }
    }

    public ReactionBuilder named (String name);

    public ReactionBuilder require (BooleanSupplier condition);

    public ReactionBuilder require (Input<?> input,
                                    int count);

    public ReactionBuilder require (Input<?> input);

    public <T> ReactionBuilder require (Input<T> input,
                                        Predicate<T> head);

    public ReactionBuilder require (Output<?> output);

    public ReactionBuilder require (Output<?> output,
                                    int count);

    public ReactionBuilder atStart ();

    public ReactionBuilder atStop ();

    public ReactionBuilder onMatch (ReactionTask task);

    public ReactionBuilder onError (ReactionTask handler);

    public Reaction build ();
}
