package com.mackenziehigh.cascade;

import java.util.Optional;
import java.util.UUID;

/**
 *
 */
public interface PrivateOutput<T>
        extends Output<T>
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
    public String name ();

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> type ();

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Reactor> reactor ();

    /**
     * {@inheritDoc}
     */
    @Override
    public PrivateOutput<T> connect (Input<T> input);

    /**
     * {@inheritDoc}
     */
    @Override
    public PrivateOutput<T> disconnect ();

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Input<T>> connection ();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFull ();

    /**
     * Send a message via this output to the connected input, if any.
     *
     * <p>
     * This method is a no-op, if this output is not connected.
     * </p>
     *
     * @param value is the message to send.
     * @return this.
     */
    public PrivateOutput<T> send (T value);
}
