package com.mackenziehigh.loader.internal;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.mackenziehigh.loader.Controller;
import com.mackenziehigh.loader.Module;
import com.mackenziehigh.loader.TopicKey;
import com.mackenziehigh.loader.internal.Configuration.ModuleConfiguration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Implementation of Controller.
 */
final class MainController
        implements Controller
{
    private final Configuration configuration;

    private final ImmutableSet<Thread> threads;

    private final ImmutableSortedMap<String, Module> modules;

    private final ConcurrentHashMap<TopicKey, ArrayList<BiConsumer<TopicKey, Object>>> handlers = new ConcurrentHashMap<>();

    private final MessageQueue messages = new MessageQueue();

    private final long startTime = System.currentTimeMillis();

    private final AtomicLong messageCount = new AtomicLong();

    private final AtomicBoolean stop = new AtomicBoolean();

    private final Set<AutoCloseable> closeOnExit = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * This is the number of message-passing threads that have already exited.
     * This allows the last thread to exit to know that it is the last thread.
     * The last thread is responsible for performing the cleanup operations.
     */
    private final AtomicInteger exitedThreads = new AtomicInteger();

    public MainController (final Configuration config)
    {
        this.configuration = Objects.requireNonNull(config);

        /**
         * Create the message-passing threads.
         */
        final ImmutableSet.Builder threadsBuilder = ImmutableSet.builder();
        for (int i = 1; i <= config.getThreadCount(); i++)
        {
            final Thread thread = new Thread(() -> run(), "GlobalMessageHandlerThread" + i);
            threadsBuilder.add(thread);
        }
        this.threads = threadsBuilder.build();

        /**
         * Instantiate the module objects, per the configuration.
         */
        final ImmutableSortedMap.Builder modulesBuilder = ImmutableSortedMap.orderedBy(String.CASE_INSENSITIVE_ORDER);
        for (ModuleConfiguration moduleConfig : configuration.getModules().values())
        {
            modulesBuilder.put(moduleConfig.moduleName(), loadModule(moduleConfig));
        }
        this.modules = modulesBuilder.build();

        /**
         * Add shutdown hook that handles external terminations.
         */
        final Thread thread = new Thread(() -> shutdown(), "ShutDownHook");
        Runtime.getRuntime().addShutdownHook(thread);
    }

    @Override
    public <T extends AutoCloseable> T autoclose (final T closeable)
    {
        closeOnExit.add(closeable);
        return closeable;
    }

    @Override
    public long maximumQueueSize ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long messageCount ()
    {
        return messageCount.get();
    }

    @Override
    public ImmutableMap<String, Module> modules ()
    {
        return modules;
    }

    @Override
    public long queueSize ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void register (final TopicKey topic,
                          final Consumer<Object> handler)
    {
        register(topic, (x, y) -> handler.accept(y));
    }

    @Override
    public synchronized void register (final TopicKey topic,
                                       final BiConsumer<TopicKey, Object> handler)
    {
        if (stop.get())
        {
            shutdownHasBegun();
        }

        if (handlers.containsKey(topic) == false)
        {
            handlers.put(topic, new ArrayList<>());
        }

        handlers.get(topic).add(handler);

        /**
         * This degrades the insertion performance.
         * However, we want to avoid wasting memory, for now.
         */
        handlers.get(topic).trimToSize();
    }

    @Override
    public boolean send (final TopicKey topic,
                         final Object message)
    {
        if (stop.get())
        {
            return false;
        }

        messages.add(topic, message);

        return true;
    }

    @Override
    public synchronized void shutdown ()
    {
        if (stop.get())
        {
            return;
        }

        modules().values().forEach(x -> x.stop());
        stop.set(true);
    }

    @Override
    public ImmutableSet<Thread> threads ()
    {
        return threads;
    }

    @Override
    public long uptime ()
    {
        return System.currentTimeMillis() - startTime;
    }

    public void start ()
    {
        /**
         * Configure each module.
         */
        modules.forEach((name, module) -> module.setup(this, name, configuration.getModules().get(name).moduleConfig()));

        /**
         * Start the message-passing threads.
         */
        threads.forEach(x -> x.start());

        /**
         * Start each module.
         */
        modules.values().forEach(module -> module.start());
    }

    private Module loadModule (final ModuleConfiguration module)
    {
        try
        {
            final Class moduleClass = Class.forName(module.moduleClass());
            final Module moduleObject = (Module) moduleClass.newInstance();
            return moduleObject;
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    private void run ()
    {
        while (stop.get() == false)
        {
            try
            {
                messages.poll(5000, (topic, message) -> handleMessage(topic, message));
            }
            catch (Throwable ex)
            {
                ex.printStackTrace();
                // TODO
            }
        }

        /**
         * If this is the last thread to exit,
         * then perform cleanup before exiting.
         */
        if (exitedThreads.incrementAndGet() == threads.size())
        {
            cleanup();
        }
    }

    private void handleMessage (final TopicKey topic,
                                final Object message)
    {
        final ArrayList<BiConsumer<TopicKey, Object>> list = handlers.get(topic);

        for (int i = 0; list != null && i < list.size(); i++)
        {
            list.get(i).accept(topic, message);
        }
    }

    private void cleanup ()
    {
        for (AutoCloseable x : closeOnExit)
        {
            try
            {
                x.close();
                closeOnExit.remove(x);
            }
            catch (Throwable ex)
            {
                ex.printStackTrace();
                // TODO
            }
        }
    }

    private void shutdownHasBegun ()
    {
        throw new IllegalStateException("Shutdown has begun.");
    }

}
