package com.mackenziehigh.loader;

import com.mackenziehigh.sexpr.Sexpr;
import java.util.Map;

/**
 * An instance of this interface manages the modules, message queues,
 * and message processors throughout the lifetime of the application.
 */
public interface Controller
{
    /**
     * This is the user-defined name of this controller.
     *
     * @return the name.
     */
    public String name ();

    /**
     * This value uniquely identifies this controller in time and space.
     *
     * @return the unique-ID of this controller.
     */
    public UniqueID uniqueID ();

    /**
     * These are all of the processors controlled by this object.
     *
     * @return a map that maps the name of a processor to the processor.
     */
    public Map<String, MessageProcessor> processors ();

    /**
     * These are all of the message-queues controlled by this object.
     *
     * @return a map that maps the name of a queue to the queue.
     */
    public Map<String, MessageQueue> queues ();

    /**
     * These are the modules controlled by this object.
     *
     * @return a map that maps the name of a module to the module.
     */
    public Map<String, AbstractModule> modules ();

    /**
     * These are the user-defined settings in the configuration file(s).
     *
     * @return the user-defined settings.
     */
    public Map<String, Sexpr> settings ();

    /**
     * Unloads all of the modules that are currently loaded
     * and then forcibly shuts down the program.
     */
    public void shutdown ();
}
