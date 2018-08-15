package com.mackenziehigh.internal.cascade;

/**
 * Shared Utilities.
 */
public final class Utils
{
    public static void verify (final boolean condition)
    {
        if (condition == false)
        {
            throw new IllegalStateException();
        }
    }
}
