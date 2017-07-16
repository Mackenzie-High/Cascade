package com.mackenziehigh.loader;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Main Program.
 */
public interface Controller
{

    /**
     * These are the main threads.
     *
     * @return the main threads.
     */
    public ImmutableSet<Thread> threads ();

    /**
     * These are the modules controlled by this object.
     *
     * @return all of the modules herein.
     */
    public ImmutableMap<String, Module> modules ();

    /**
     * This method retrieves the number of milliseconds
     * since the creation of this object.
     *
     * @return the uptime of the controller.
     */
    public long uptime ();

    /**
     * This method retrieves the number of messages
     * that have been sent through this controller.
     *
     * @return the total number of sent messages.
     */
    public long messageCount ();

    /**
     * This method retrieves the number of messages that
     * are currently enqueued awaiting processing.
     *
     * @return the total number of back-logged messages.
     */
    public long queueSize ();

    /**
     * This method retrieves the maximum number of messages
     * that can be enqueued at one time awaiting processing.
     *
     * @return the maximum queue size.
     */
    public long maximumQueueSize ();

    /**
     * This method adds a message-handler.
     *
     * @param topic identifies the queue from which the handler will receive messages.
     * @param handler is the new message-handler.
     * @throws IllegalStateException if normal shutdown has begun.
     */
    public void register (TopicKey topic,
                          Consumer<Object> handler);

    /**
     * This method adds a message-handler.
     *
     * @param topic identifies the queue from which the handler will receive messages.
     * @param handler is the new message-handler.
     * @throws IllegalStateException if normal shutdown has begun.
     */
    public void register (TopicKey topic,
                          BiConsumer<TopicKey, Object> handler);

    /**
     * This method asynchronously sends a message to all message-handlers
     * that are listening on the given topic queue.
     *
     * <p>
     * This method simply enqueues the message and then returns immediately.
     * In other words, this method will never block.
     * </p>
     *
     * <p>
     * If normal shutdown has already begun,
     * then this method returns false.
     * </p>
     *
     * <p>
     * If the queueSize() has reached the maximumQueueSize(),
     * then this method will return false.
     * </p>
     *
     * @param topic identifies the queue to send the message to.
     * @param message is the message to add to the queue.
     * @return true, iff the message was successfully enqueued.
     */
    public boolean send (TopicKey topic,
                         Object message);

    /**
     * When this controller shuts down, the given object will be closed.
     *
     * <p>
     * The close() operation will be performed after every module is stopped.
     * </p>
     *
     * @param <T> is the type of the input.
     * @param closeable is the object to close, when this controller shuts down.
     * @return the input.
     */
    public <T extends AutoCloseable> T autoclose (T closeable);

    /**
     * Unloads all of the modules that are currently loaded
     * and then shuts down the program.
     */
    public void shutdown ();
}
