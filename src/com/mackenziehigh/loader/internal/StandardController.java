package com.mackenziehigh.loader.internal;

import com.google.common.util.concurrent.Uninterruptibles;
import com.mackenziehigh.loader.AbstractModule;
import com.mackenziehigh.loader.Controller;
import com.mackenziehigh.loader.MessageProcessor;
import com.mackenziehigh.loader.MessageQueue;
import com.mackenziehigh.loader.UniqueID;
import com.mackenziehigh.sexpr.Sexpr;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Standard implementation of Controller.
 */
final class StandardController
        implements Controller
{
    public String name = "";

    private final UniqueID uniqueID = UniqueID.random();

    public final Map<String, MessageProcessor> processors = new ConcurrentHashMap<>();

    public final Map<String, MessageQueue> queues = new ConcurrentHashMap<>();

    public final Map<String, AbstractModule> modules = new ConcurrentHashMap<>();

    public final Map<String, Sexpr> settings = new ConcurrentHashMap<>();

    private final Map<String, MessageProcessor> unmodifiableProcessors = Collections.unmodifiableMap(processors);

    private final Map<String, MessageQueue> unmodifiableQueues = Collections.unmodifiableMap(queues);

    private final Map<String, AbstractModule> unmodifiableModules = Collections.unmodifiableMap(modules);

    private final Map<String, Sexpr> unmodifiableSettings = Collections.unmodifiableMap(settings);

    private final AtomicBoolean stop = new AtomicBoolean();

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
    public Map<String, MessageProcessor> processors ()
    {
        return unmodifiableProcessors;
    }

    @Override
    public Map<String, MessageQueue> queues ()
    {
        return unmodifiableQueues;
    }

    @Override
    public Map<String, AbstractModule> modules ()
    {
        return unmodifiableModules;
    }

    @Override
    public Map<String, Sexpr> settings ()
    {
        return unmodifiableSettings;
    }

    @Override
    public void shutdown ()
    {
        stop.set(true);
    }

    public int run ()
    {
        /**
         * Step: Start the message-processors.
         */
        for (MessageProcessor p : processors.values())
        {
            try
            {
                ((AbstractProcessor) p).start();
            }
            catch (Exception ex)
            {
                // TODO
            }
        }

        /**
         * Step: Setup the modules.
         */
        for (AbstractModule m : modules.values())
        {
            try
            {
                ((AbstractModule) m).setup();
            }
            catch (Exception ex)
            {
                // TODO
            }
        }

        /**
         * Step: Start the modules.
         */
        for (AbstractModule m : modules.values())
        {
            try
            {
                ((AbstractModule) m).start();
            }
            catch (Exception ex)
            {
                // TODO
            }
        }

        queues.get("Clock").bind(x ->
        {
            System.out.println("X = " + x.content());
        });

        /**
         * Wait for the shutdown signal.
         */
        while (stop.get() == false)
        {
            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        }

        // TODO: Shutdown Logic
        return 0; // TODO
    }
}
