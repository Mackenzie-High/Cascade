package com.mackenziehigh.cascade.internal.pumps;

import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.internal.pumps.Connector.Connection;
import java.util.Map;
import java.util.Set;

/**
 * Instances of this class receive messages from connections
 * and process them using appropriate message-consumers.
 */
public interface Engine
{
    /**
     * The engine uses instances of this interface
     * in order to process incoming messages.
     */
    public interface MessageConsumer
    {
        /**
         * Use this method to consume a message.
         *
         * @param message will be consumed.
         * @throws java.lang.Throwable at will.
         */
        public void accept (OperandStack message)
                throws Throwable;

        /**
         * If accept(*) throws an exception,
         * then handle the exception by invoking
         * this method with the given exception.
         *
         * @param exception was thrown by accept(*).
         */
        public void handle (Throwable exception);

        /**
         * Getter.
         *
         * @return the maximum number of threads that
         * can be concurrently executing this consumer.
         */
        public int concurrentLimit ();

    }

    /**
     * Getter.
     *
     * @return the current threads of this engine.
     */
    public Set<Thread> threads ();

    /**
     * Getter.
     *
     * @return a map that maps configurations to related connections.
     */
    public Map<ConnectionSchema, Connection> connections ();

    /**
     * Getter.
     *
     * @return true, if messages *can* currently be processed
     * or *are* still being processed.
     */
    public boolean isRunning ();

    /**
     * Start processing incoming messages.
     */
    public void start ();

    /**
     * Permanently stop processing incoming messages.
     */
    public void stop ();

}
