package com.mackenziehigh.cascade;

import com.mackenziehigh.cascade.internal.InternalCascade;

/**
 * Cascade Utilities.
 */
public final class Cascades
{
    /**
     * Factory that produces Cascade instances.
     *
     * @return a new cascade.
     */
    public static Cascade newCascade ()
    {
        return new InternalCascade();
    }
}
