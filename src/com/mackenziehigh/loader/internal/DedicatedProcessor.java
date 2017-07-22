package com.mackenziehigh.loader.internal;

import com.google.common.collect.ImmutableMap;
import com.mackenziehigh.loader.Controller;
import com.mackenziehigh.loader.MessageProcessor;
import com.mackenziehigh.loader.MessageQueue;
import com.mackenziehigh.loader.UniqueID;

/**
 *
 * @author mackenzie
 */
final class DedicatedProcessor
        implements MessageProcessor
{
    @Override
    public Controller controller ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

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
    public ImmutableMap<String, MessageQueue> queues ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
