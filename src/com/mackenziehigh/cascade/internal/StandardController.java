package com.mackenziehigh.cascade.internal;

import com.google.common.util.concurrent.Uninterruptibles;
import com.mackenziehigh.cascade.AbstractModule;
import com.mackenziehigh.cascade.CommonLogger;
import com.mackenziehigh.cascade.Controller;
import com.mackenziehigh.cascade.MessageProcessor;
import com.mackenziehigh.cascade.MessageQueue;
import com.mackenziehigh.cascade.UniqueID;
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

    private final DirectProcessor globalLogProcessor = new DirectProcessor();

    private final MessageQueue globalLogQueue;

    public StandardController ()
    {
        this.globalLogProcessor.name = "global.log.processor";
        this.globalLogProcessor.controller = this;
        this.globalLogProcessor.declareQueue("global.log");
        this.globalLogQueue = globalLogProcessor.messageQueues.get("global.log");
        this.globalLogProcessor.logger = new CommonLogger(globalLogQueue, "global.log.processor", globalLogProcessor.uniqueID());
        this.processors.put("global.log.processor", globalLogProcessor);
        this.queues.put("global.log", globalLogQueue);
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
    public MessageQueue globalLogQueue ()
    {
        return globalLogQueue;
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

    public void run ()
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
            catch (Throwable ex)
            {
                ex.printStackTrace(System.out);
            }
        }

        /**
         * Step: Setup the modules.
         */
        for (AbstractModule m : modules.values())
        {
            try
            {
                m.setup();
            }
            catch (Throwable ex)
            {
                ex.printStackTrace(System.out);
            }
        }

        /**
         * Step: Start the modules.
         */
        for (AbstractModule m : modules.values())
        {
            try
            {
                m.start();
            }
            catch (Throwable ex)
            {
                ex.printStackTrace(System.out);
            }
        }

        /**
         * Wait for the shutdown signal.
         */
        while (stop.get() == false)
        {
            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        }

        /**
         * Step: Stop the message-processors.
         */
        for (MessageProcessor p : processors.values())
        {
            try
            {
                ((AbstractProcessor) p).stop();
            }
            catch (Throwable ex)
            {
                ex.printStackTrace(System.out);
            }
        }

        /**
         * Step: Stop the modules.
         */
        for (AbstractModule m : modules.values())
        {
            try
            {
                m.stop();
            }
            catch (Throwable ex)
            {
                ex.printStackTrace(System.out);
            }
        }

        /**
         * Step: Cleanup the modules.
         */
        for (AbstractModule m : modules.values())
        {
            try
            {
                m.destroy();
            }
            catch (Throwable ex)
            {
                ex.printStackTrace(System.out);
            }
        }
    }
}
