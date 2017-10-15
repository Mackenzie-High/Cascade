package com.mackenziehigh.cascade;

import java.io.Closeable;
import java.util.Collections;
import java.util.Set;

/**
 * Must have a nullary constructor.
 */
public interface Actor
{
    /**
     * This method is used during construction to set the enclosing Cascade object.
     *
     * <p>
     * This method can only be successfully invoked once.
     * </p>
     *
     * @param value is the new Cascade object.
     * @throws IllegalStateException on any subsequent invocations.
     */
    public void bindCascade (Cascade value);

    /**
     * This method is used during construction to set the logger.
     *
     * <p>
     * This method can only be successfully invoked once.
     * </p>
     *
     * @param value is the new logger.
     * @throws IllegalStateException on any subsequent invocations.
     */
    public void bindLogger (CascadeLogger value);

    /**
     * This method is used during construction to set the powerplant.
     *
     * <p>
     * This method can only be successfully invoked once.
     * </p>
     *
     * @param value is the new powerplant.
     * @throws IllegalStateException on any subsequent invocations.
     */
    public void bindPowerplant (Powerplant value);

    /**
     * This method is used during construction to set the inputs.
     *
     * <p>
     * This method can only be successfully invoked once.
     * </p>
     *
     * @param value are the new inputs.
     * @throws IllegalStateException on any subsequent invocations.
     */
    public void bindInputs (Set<Pipeline> value);

    /**
     * This method is used during construction to set the outputs.
     *
     * <p>
     * This method can only be successfully invoked once.
     * </p>
     *
     * @param value are the new outputs.
     * @throws IllegalStateException on any subsequent invocations.
     */
    public void bindOutputs (Set<Pipeline> value);

    /**
     * This method is invoked during construction in order to indicate
     * that no more bind methods will be invoked on this object.
     *
     * <p>
     * This method can be invoked multiple times.
     * </p>
     */
    public void bindingComplete ();

    /**
     * Getter.
     *
     * @return the enclosing Cascade instance.
     */
    public Cascade cascade ();

    /**
     * Getter.
     *
     * @return the logger for use by this actor.
     */
    public CascadeLogger logger ();

    /**
     * Getter.
     *
     * @return the powerplant that powers this actor.
     */
    public Powerplant powerplant ();

    /**
     * Getter.
     *
     * @return the inputs that supply message to this actor.
     */
    public Set<Pipeline> inputs ();

    /**
     * Getter.
     *
     * @return the pipelines that carry messages from this actor.
     */
    public Set<Pipeline> outputs ();

    /**
     * This method determines whether this actor supports parallel execution.
     *
     * @return
     */
    public default boolean isParallelizable ()
    {
        return false;
    }

    /**
     * Can a direct powerplant be used?
     *
     * @return
     */
    public default boolean isDirectAllowed ()
    {
        return true;
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
    public default void setup ()
            throws Throwable
    {
        // Pass
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
    public default void start ()
            throws Throwable
    {
        // Pass
    }

    /**
     * This method will be invoked, by the powerplant,
     * whenever a message is sent to this actor for processing.
     *
     * <p>
     * As a general assumption, if isParallelizable() is false,
     * then this method will only be invoked by one thread at a time.
     * </p>
     *
     * @param source is the pipeline that transported the message.
     * @param message is the message to process.
     * @throws java.lang.Throwable if something goes wrong.
     */
    public default void process (Pipeline source,
                                 MessageStack message)
            throws Throwable
    {
        // Pass
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
    public default void stop ()
            throws Throwable
    {
        // Pass
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
    public default void destroy ()
            throws Throwable
    {
        // Pass
    }

    /**
     * This method will be invoked, by the controller, during normal shutdowns.
     *
     * <p>
     * This method is not guaranteed to be invoked during abnormal shutdowns.
     * </p>
     *
     * <p>
     * When this method is invoked, destroy() has already been
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
     * @return any objects that need to be closed.
     */
    public default Set<Closeable> closeables ()
    {
        return Collections.emptySet();
    }
}
