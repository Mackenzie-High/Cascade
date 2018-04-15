package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.CascadeExecutor;
import com.mackenziehigh.cascade.CascadeStage;

/**
 *
 */
public final class DirectExecutor
        implements CascadeExecutor
{
    /**
     * {@inheritDoc}
     */
    @Override
    public void onStageOpened (CascadeStage stage)
    {
        // Pass
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStageClosed (CascadeStage stage)
    {
        // Pass
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTask (CascadeStage stage)
    {
        stage.executor();
    }
}
