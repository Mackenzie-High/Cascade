package com.mackenziehigh.loader;

/**
 * An instance of this interface defines a dynamically-loaded sub-program.
 */
public interface Module
{
    /**
     * This is the first method to be invoked, by the controller,
     * during initialization of this module.
     *
     * <p>
     * Only the controller shall invoke this method.
     * </p>
     *
     * @param controller is the object invoking this method.
     * @param name is the name assigned to this module object.
     * @param configuration is the user-supplied configuration.
     * @return true, iff the invocation succeeded without error.
     */
    public default boolean setup (Controller controller,
                                  String name,
                                  ConfigObject configuration)
    {
        return true;
    }

    /**
     * This is the third method to be invoked, by the controller,
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
     * @return true, iff the invocation succeeded.
     */
    public default boolean start ()
    {
        return true;
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
     */
    public default void stop ()
    {
        // Pass.
    }
}
