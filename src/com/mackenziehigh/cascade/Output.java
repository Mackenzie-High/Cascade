package com.mackenziehigh.cascade;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 *
 */
public interface Output<T>
{
    public UUID uuid ();

    public String name ();

    public Class<T> type ();

    public Optional<Reactor> reactor ();

    public Output<T> connect (Input<T> input);

    public Output<T> disconnect (Input<T> input);

    public Set<Input<T>> connections ();

    public boolean isFull ();

    public Output<T> send (T value);
}
