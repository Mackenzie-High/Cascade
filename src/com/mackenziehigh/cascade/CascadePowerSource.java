package com.mackenziehigh.cascade;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides the power needed to power actor(s).
 */
public interface CascadePowerSource
{
    /**
     * The actor will invoke this method when this power-source
     * becomes responsible for providing power to the actor.
     *
     * @param actor will be powered, going forward, by this executor.
     * @param pocket can be used freely by this executor for storage.
     */
    public void addActor (CascadeActor actor,
                          AtomicReference<?> pocket);

    /**
     * The actor will invoke this method when this power-source
     * becomes no longer responsible for providing power to the actor.
     *
     * @param actor will be powered, going forward, by this executor.
     * @param pocket can be used freely by this executor for storage.
     */
    public void removeActor (CascadeActor actor,
                             AtomicReference<?> pocket);

    /**
     * This method will be invoked whenever a actor needs power applied.
     * The executor is then responsible for providing power to the actor.
     *
     * @param actor needs powered by this executor.
     * @param pocket can be used freely by this executor for storage.
     */
    public void submit (CascadeActor actor,
                        AtomicReference<?> pocket);
}
