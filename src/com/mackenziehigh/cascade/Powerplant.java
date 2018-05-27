package com.mackenziehigh.cascade;

import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
public interface Powerplant
        extends AutoCloseable
{
    public void onStart (Reactor reactor,
                         AtomicReference<?> meta);

    public void onStop (Reactor reactor,
                        AtomicReference<?> meta);

    public void onPing (Reactor reactor,
                         AtomicReference<?> meta);

    public default void onKeepalive ()
    {
        // TODO: Make non-default
    }
}
