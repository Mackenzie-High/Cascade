package com.mackenziehigh.cascade;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 *
 */
public interface Input<T>
{
    /**
     * Retrieve a UUID that uniquely identifies this input in space-time.
     *
     * @return the unique identifier of this input.
     */
    public UUID uuid ();

    public Class<T> type ();

    public String name ();

    /**
     * Retrieve the reactor that this input is a part of.
     *
     * @return the enclosing reactor, or empty,
     * if the reactor is not fully constructed yet.
     */
    public Optional<Reactor> reactor ();

    /**
     * Connect this input to the given output.
     *
     * <p>
     * This method is a no-op, if the connection exists.
     * </p>
     *
     * @param output will subsequently be connected hereto.
     * @return this.
     */
    public Input<T> connect (Output<T> output);

    public Input<T> disconnect (Output<T> output);

    public Set<Output<T>> connections ();

    public int capacity ();

    public int size ();

    public boolean isEmpty ();

    public boolean isFull ();

    public T peekOrDefault (T defaultValue);

    public default Optional<T> peek ()
    {
        final T head = peekOrDefault(null);
        return Optional.ofNullable(head);
    }

    public Input<T> forEach (Consumer<T> functor);

    public Input<T> send (T value);

}
