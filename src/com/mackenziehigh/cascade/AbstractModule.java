package com.mackenziehigh.cascade;

import com.google.common.base.Preconditions;
import com.mackenziehigh.sexpr.Sexpr;

/**
 * An instance of this interface defines a dynamically-loaded sub-program.
 */
public abstract class AbstractModule
{
    private Controller controller;

    private String name;

    private Sexpr configuration;

    private CommonLogger logger;

    private final UniqueID uniqueID = UniqueID.random();

    /**
     * This method is invoked by the controller on-load.
     *
     * @param configuration is the value for configuration().
     */
    public final void assignConfiguration (final Sexpr configuration)
    {
        Preconditions.checkNotNull(configuration, "configuration");
        Preconditions.checkState(this.configuration == null, "Already Initialized");
        this.configuration = configuration;
    }

    /**
     * This method is invoked by the controller on-load.
     *
     * <p>
     * This method will only be invoked once.
     * </p>
     *
     * @param controller is the value for controller().
     */
    public final void assignController (final Controller controller)
    {
        Preconditions.checkNotNull(controller, "controller");
        Preconditions.checkState(this.controller == null, "Already Initialized");
        this.controller = controller;
    }

    /**
     * This method is invoked by the controller on-load.
     *
     * <p>
     * This method will only be invoked once.
     * </p>
     *
     * @param name is the value for name().
     */
    public final void assignName (final String name)
    {
        Preconditions.checkNotNull(name, "name");
        Preconditions.checkState(this.name == null, "Already Initialized");
        this.name = name;
    }

    /**
     * This method is invoked by the controller on-load.
     *
     * <p>
     * This method will only be invoked once.
     * </p>
     *
     * @param logger is the value for logger().
     */
    public final void assignLogger (final CommonLogger logger)
    {
        Preconditions.checkNotNull(logger, "logger");
        Preconditions.checkState(this.logger == null, "Already Initialized");
        this.logger = logger;
    }

    /**
     * This is the controller that is managing this module.
     *
     * @return the controlling controller.
     */
    public final Controller controller ()
    {
        return controller;
    }

    /**
     * This is the user-specified name of this module.
     *
     * @return the name of this module.
     */
    public final String name ()
    {
        return name;
    }

    /**
     * This is the user-specified configuration of this module.
     *
     * @return the configuration.
     */
    public final Sexpr configuration ()
    {
        return configuration;
    }

    /**
     * This is logger for this particular module object.
     *
     * @return the local logger.
     */
    public final CommonLogger logger ()
    {
        return logger;
    }

    /**
     * This value uniquely identifies this module in time and space.
     *
     * @return the unique-ID of this module.
     */
    public final UniqueID uniqueID ()
    {
        return uniqueID;
    }

    /**
     * This is the first non-assignment method to be invoked,
     * by the controller, during initialization of this module.
     *
     * <p>
     * Only the controller shall invoke this method.
     * </p>
     *
     * <p>
     * This method will only be invoked once.
     * </p>
     *
     * @throws java.lang.Throwable if something goes wrong.
     */
    public void setup ()
            throws Throwable
    {
        // Pass.
    }

    /**
     * This is the second method to be invoked, by the controller,
     * during initialization of this module.
     *
     * <p>
     * When this method is invoked, setup(*) has already been
     * invoked on all of the modules that are being loaded,
     * including this module itself.
     * </p>
     *
     * <p>
     * Only the controller shall invoke this method.
     * </p>
     *
     * <p>
     * This method will only be invoked once.
     * </p>
     *
     * @throws java.lang.Throwable if something goes wrong.
     */
    public void start ()
            throws Throwable
    {
        // Pass.
    }

    /**
     * This method will be invoked, by the controller, during normal shutdowns.
     *
     * <p>
     * This method is not guaranteed to be invoked during abnormal shutdowns.
     * </p>
     *
     * <p>
     * Only the controller shall invoke this method.
     * </p>
     *
     * <p>
     * This method will only be invoked once.
     * </p>
     *
     * @throws java.lang.Throwable if something goes wrong.
     */
    public void stop ()
            throws Throwable
    {
        // Pass.
    }

    /**
     * This method will be invoked, by the controller, during normal shutdowns.
     *
     * <p>
     * This method is not guaranteed to be invoked during abnormal shutdowns.
     * </p>
     *
     * <p>
     * When this method is invoked, stop() has already been
     * invoked on all of the modules that are loaded,
     * including this module itself.
     * </p>
     *
     * <p>
     * Only the controller shall invoke this method.
     * </p>
     *
     * <p>
     * This method will only be invoked once.
     * </p>
     *
     * @throws java.lang.Throwable if something goes wrong.
     */
    public void destroy ()
            throws Throwable
    {
        // Pass.
    }
}
