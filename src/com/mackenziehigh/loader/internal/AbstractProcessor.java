package com.mackenziehigh.loader.internal;

import com.mackenziehigh.loader.CommonLogger;
import com.mackenziehigh.loader.Controller;
import com.mackenziehigh.loader.MessageProcessor;
import com.mackenziehigh.loader.MessageQueue;
import com.mackenziehigh.loader.UniqueID;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author mackenzie
 */
abstract class AbstractProcessor
        implements MessageProcessor
{
    public abstract void declareQueue (final String name);

    public abstract void start ();

    public abstract void stop ();

    public StandardController controller;

    public CommonLogger logger;

    public String name = "";

    public final UniqueID uniqueID = UniqueID.random();

    public final Map<String, MessageQueue> messageQueues = new ConcurrentHashMap<>();

    public final Map<String, MessageQueue> unmodifiableMessageQueues = Collections.unmodifiableMap(messageQueues);

    public final List<LazyQueueRef> logQueues = new CopyOnWriteArrayList<>();

    public final List<LazyQueueRef> unmodifiableLogQueues = Collections.unmodifiableList(logQueues);

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
        return unmodifiableMessageQueues;
    }
}
