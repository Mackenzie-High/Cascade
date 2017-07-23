package com.mackenziehigh.loader.internal;

import com.mackenziehigh.loader.Controller;
import com.mackenziehigh.loader.MessageProcessor;
import com.mackenziehigh.loader.MessageQueue;
import com.mackenziehigh.loader.UniqueID;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author mackenzie
 */
abstract class AbstractProcessor
        implements MessageProcessor
{
    public abstract void declareLog (final String name);

    public abstract void declareQueue (final String name);

    public abstract void start ();

    public StandardController controller;

    public String name = "";

    public final UniqueID uniqueID = UniqueID.random();

    public final Map<String, MessageQueue> queues = new ConcurrentHashMap<>();

    public final Map<String, MessageQueue> unmodifiableQueues = Collections.unmodifiableMap(queues);

    @Override
    public Controller controller ()
    {
        return controller;
    }

    @Override
    public String name ()
    {
        return name;
    }

    @Override
    public UniqueID uniqueID ()
    {
        return uniqueID;
    }

    @Override
    public Map<String, MessageQueue> queues ()
    {
        return unmodifiableQueues;
    }
}
