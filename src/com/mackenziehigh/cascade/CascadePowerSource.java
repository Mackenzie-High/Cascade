package com.mackenziehigh.cascade;

import java.util.concurrent.atomic.AtomicReference;

/**
 * An executor provides the power needed to power a stage.
 */
public interface CascadePowerSource
{
    /**
     * The actor that is powered by this executor will
     * invoke this method when the actor is first created.
     *
     * @param actor will be powered, going forward, by this executor.
     * @param pocket can be used freely by this executor for storage.
     */
    public void addActor (CascadeActor actor,
                          AtomicReference<?> pocket);

    /**
     * The actor that is powered by this executor will
     * invoke this method when the actor is closed.
     *
     * @param actor will no longer be powered by this executor.
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
