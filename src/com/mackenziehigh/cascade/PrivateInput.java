package com.mackenziehigh.cascade;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 *
 */
public interface PrivateInput<T>
        extends Input<T>
{
    /**
     * {@inheritDoc}
     */
    @Override
    public UUID uuid ();

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> type ();

    /**
     * {@inheritDoc}
     */
    @Override
    public String name ();

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Reactor> reactor ();

    /**
     * {@inheritDoc}
     */
    @Override
    public PrivateInput<T> connect (Output<T> output);

    /**
     * {@inheritDoc}
     */
    @Override
    public PrivateInput<T> disconnect ();

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Output<T>> connection ();

    /**
     * {@inheritDoc}
     */
    @Override
    public int capacity ();

    /**
     * {@inheritDoc}
     */
    @Override
    public int size ();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty ();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFull ();

    /**
     * {@inheritDoc}
     */
    @Override
    public T peekOrDefault (T defaultValue);

    /**
     * {@inheritDoc}
     */
    @Override
    public default Optional<T> peek ()
    {
        final T head = peekOrDefault(null);
        return Optional.ofNullable(head);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrivateInput<T> forEach (Consumer<T> functor);

    /**
     * {@inheritDoc}
     */
    @Override
    public PrivateInput<T> send (T value);

    public PrivateInput<T> clear ();

    public T pollOrDefault (T defaultValue);

    public default Optional<T> poll ()
    {
        final T head = pollOrDefault(null);
        return Optional.ofNullable(head);
    }

}
