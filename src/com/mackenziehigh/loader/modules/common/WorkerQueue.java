package com.mackenziehigh.loader.modules.common;

import com.mackenziehigh.loader.ConfigObject;
import com.mackenziehigh.loader.ConfigSchema;
import com.mackenziehigh.loader.Controller;
import com.mackenziehigh.loader.Module;
import com.mackenziehigh.loader.TopicKey;
import com.mackenziehigh.loader.util.Final;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

/**
 * An instance of this class receives work from a topic
 * and then performs the work on special worker threads.
 *
 * <p>
 * Instances of this class maintain an internal queue.
 * The queue stores messages that have been received,
 * but have not yet been handed off to the workers.
 * The queue has a finite user-specified maximum capacity.
 * </p>
 */
public final class WorkerQueue
        implements Module
{
    /**
     * This schema describes the module configuration.
     */
    private final ConfigSchema schema = new ConfigSchema();

    /**
     * This is the controller that created this module.
     */
    private final Final<Controller> controller = new Final<>();

    /**
     * This is the name of this module, per the configuration.
     */
    private final Final<String> name = new Final<>();

    /**
     * This flag will be set to true, if the controller says to stop.
     */
    private final AtomicBoolean stop = new AtomicBoolean();

    /**
     * This is the number of worker threads.
     */
    private int threadCount = 1;

    /**
     * This queue contains the tasks to perform on the worker threads.
     * The worker threads will wait to receive tasks from this queue.
     * When a task is received from this queue, a worker thread will execute it.
     */
    private final Final<ArrayBlockingQueue<Object>> queue = new Final<>();

    /**
     * Sole Constructor.
     */
    public WorkerQueue ()
    {
        schema.requireMap();
        schema.entry("threads").requireInteger().onPresent(x -> setThreadCount(x));
        schema.entry("input").requireString().onPresent(x -> setInput(x));
        schema.entry("capacity").requireInteger().onPresent(x -> setCapacity(x));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setup (final Controller controller,
                          final String name,
                          final ConfigObject configuration)
    {
        this.controller.set(controller);
        this.name.set(name);
        this.schema.apply(configuration);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean start ()
    {
        /**
         * Create and start the threads.
         */
        IntStream.rangeClosed(1, threadCount).forEach(i -> new Thread(() -> run(), "Worker" + i).start());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop ()
    {
        stop.set(true);
    }

    private void setThreadCount (final ConfigObject value)
    {
        this.threadCount = value.asInteger().get().intValue();
    }

    private void setCapacity (final ConfigObject value)
    {
        final int capacity = value.asInteger().get().intValue();
        this.queue.set(new ArrayBlockingQueue<>(capacity));
    }

    private void setInput (final ConfigObject value)
    {
        /**
         * Register a message-handler that will listen
         * for messages coming from the input topic.
         */
        final String key = value.asString().get();
        final TopicKey topic = TopicKey.get(key);
        controller.get().get().register(topic, message -> receive(message));
    }

    /**
     * This method is invoked to receive messages from the input topic.
     * The controller invokes this method when a message is available.
     *
     * @param task is the message received from the input topic.
     */
    private void receive (final Object task)
    {
        /**
         * Add the message to the task queue, if possible.
         * If the queue is full, etc, then issue an error-message.
         */
        if (task == null)
        {
//            logger.warning(String.format("%s received a null message.", name.get().get()));
        }
        else if (queue.get().get().offer(task) == false)
        {
//            logger.warning(String.format("%s dropped a message on receive.", name.get().get()));
        }
    }

    /**
     * Worker Thread - Main Loop.
     */
    private void run ()
    {
        while (stop.get() == false)
        {
            try
            {
                /**
                 * Wait for a new task to arrive.
                 * Do not wait forever; otherwise, we may miss the stop signal.
                 */
                final Object task = queue.get().get().poll(5, TimeUnit.SECONDS);

                if (task != null)
                {
                    ((Runnable) task).run();
                }
            }
            catch (Throwable ex)
            {
//                logger.throwing(ex);
            }
        }
    }
}
