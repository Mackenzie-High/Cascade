package com.mackenziehigh.cascade;

/**
 * An executor provides the power needed to power a stage.
 */
public interface CascadeExecutor
{
    /**
     * The actor that is powered by this executor will
     * invoke this method when the actor is first created.
     *
     * @param actor will be powered, going forward, by this executor.
     */
    public void onActorOpened (CascadeActor actor);

    /**
     * The actor that is powered by this executor will
     * invoke this method when the actor is closed.
     *
     * @param actor will no longer be powered by this executor.
     */
    public void onActorClosed (CascadeActor actor);

    /**
     * This method will be invoked whenever a actor needs power applied.
     * The executor is then responsible for providing power to the actor.
     *
     * @param actor needs powered by this executor.
     */
    public void onTask (CascadeActor actor);
}
