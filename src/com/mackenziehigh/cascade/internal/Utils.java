package com.mackenziehigh.cascade.internal;

import com.google.common.base.Preconditions;

/**
 * Common Utility Methods.
 */
public class Utils
{
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
}
