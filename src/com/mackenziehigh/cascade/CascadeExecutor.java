package com.mackenziehigh.cascade;

/**
 * An executor provides the power needed to power a stage.
 */
public interface CascadeExecutor
{
    /**
     * The stage that is powered by this executor will
     * invoke this method when the stage is first created.
     *
     * @param stage will be powered, going forward, by this executor.
     */
    public void onStageOpened (CascadeStage stage);

    /**
     * The stage that is powered by this executor will
     * invoke this method when the stage is closed.
     *
     * @param stage will no longer be powered by this executor.
     */
    public void onStageClosed (CascadeStage stage);

    /**
     * This method will be invoked whenever a stage needs power applied.
     * The executor is then responsible for providing power to the stage.
     *
     * @param stage needs powered by this executor.
     */
    public void onTask (CascadeStage stage);
}
