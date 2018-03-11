package com.mackenziehigh.cascade;

import com.mackenziehigh.cascade.CascadeStage;
import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.Cascade;

/**
 *
 */
@FunctionalInterface
public interface CascadeUncaughtExceptionHandler
{
    public void onUncaughtException (Cascade cascade,
                                     Throwable cause);

    public default void onUncaughtException (Cascade cascade,
                                             CascadeStage stage,
                                             Throwable cause)
    {
        onUncaughtException(cascade, cause);
    }

    public default void onUncaughtException (Cascade cascade,
                                             CascadeStage stage,
                                             CascadeActor actor,
                                             Throwable cause)
    {
        onUncaughtException(cascade, stage, cause);
    }
}
