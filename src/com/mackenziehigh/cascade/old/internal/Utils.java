package com.mackenziehigh.cascade.old.internal;

import com.google.common.base.Preconditions;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.old.CascadeReactor.Core;

/**
 * Common Utility Methods.
 */
public class Utils
{
    public static Core nop ()
    {
        return new Core()
        {
            // Pass
        };
    }

    public static String checkSimpleName (final String name)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(name.matches("[A-Za-z_$][A-Za-z0-9_$]*"), "Invalid Simple Name: " + name);
        return name;
    }

    public static String getSimpleName (final String name)
    {
        return name.replaceAll("([^.]*\\.)*", name);
    }

    public static void safeWarn (final Throwable ex,
                                 final CascadeLogger logger)
    {
        try
        {
            logger.warn(ex);
        }
        catch (Throwable ex2)
        {
            // Pass.
        }
    }

}
