package com.mackenziehigh.loader.internal;

import com.google.common.collect.ImmutableMap;
import com.mackenziehigh.loader.AbstractModule;
import com.mackenziehigh.loader.Controller;
import com.mackenziehigh.loader.MessageProcessor;
import com.mackenziehigh.loader.MessageQueue;
import com.mackenziehigh.loader.UniqueID;
import com.mackenziehigh.sexpr.Sexpr;

/**
 *
 * @author mackenzie
 */
final class MainController
        implements Controller
{
    @Override
    public String name ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public UniqueID uniqueID ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ImmutableMap<String, MessageProcessor> processors ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ImmutableMap<String, MessageQueue> queues ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ImmutableMap<String, AbstractModule> modules ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ImmutableMap<String, Sexpr> settings ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void shutdown ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
