package com.mackenziehigh.cascade.reactor;

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

    public void onReady (Reactor reactor,
                         AtomicReference<?> meta);
}
