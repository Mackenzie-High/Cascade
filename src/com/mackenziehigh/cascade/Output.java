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

import java.util.Optional;
import java.util.UUID;

/**
 *
 */
public interface Output<E>
{
    public UUID uuid ();

    public String name ();

    public Class<E> type ();

    public Optional<Reactor> reactor ();

    public Output<E> connect (Input<E> input);

    public Output<E> disconnect ();

    public Optional<Input<E>> connection ();

    public boolean isFull ();

    public boolean isEmpty ();

    public int capacity ();

    public int size ();

    public int remainingCapacity ();

}
