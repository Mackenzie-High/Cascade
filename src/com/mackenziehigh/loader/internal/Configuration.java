package com.mackenziehigh.loader.internal;

import com.google.common.collect.ImmutableSortedMap;
import com.mackenziehigh.loader.ConfigObject;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Module Configuration.
 */
public final class Configuration
{
    public static interface ModuleConfiguration
    {
        public String moduleName ();

        public String moduleClass ();

        public ConfigObject moduleConfig ();
    }

    private int threadCount = 1;

    private int queueSize = 1024 * 1024;

    private final SortedMap<String, ModuleConfiguration> modules = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public void setThreadCount (final int count)
    {
        threadCount = count;
    }

    public int getThreadCount ()
    {
        return threadCount;
    }

    public int getQueueSize ()
    {
        return queueSize;
    }

    public void setQueueSize (final int queueSize)
    {
        this.queueSize = queueSize;
    }

    public void setModule (final String moduleName,
                           final String moduleClass,
                           final ConfigObject moduleConfig)
    {
        final ModuleConfiguration mc = new ModuleConfiguration()
        {
            @Override
            public String moduleClass ()
            {
                return moduleClass;
            }

            @Override
            public ConfigObject moduleConfig ()
            {
                return moduleConfig;
            }

            @Override
            public String moduleName ()
            {
                return moduleName;
            }
        };

        modules.put(moduleName, mc);
    }

    public ImmutableSortedMap<String, ModuleConfiguration> getModules ()
    {
        return ImmutableSortedMap.copyOfSorted(modules);
    }
}
