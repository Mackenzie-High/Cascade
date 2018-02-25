package com.mackenziehigh.cascade.redo;

import com.google.common.base.Preconditions;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.allocators.Allocator;
import com.mackenziehigh.cascade.allocators.OperandStack;
import com.mackenziehigh.cascade.internal.StandardLogger;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 *
 */
public final class Cascade
{
    private final Cascade SELF = this;

    private volatile CascadeToken name;

    private final UUID uuid = UUID.randomUUID();

    private final AtomicInteger phaseOrdinal = new AtomicInteger();

    /**
     * This latch becomes permanently unblocked once this system comes to a complete stop.
     */
    private final CountDownLatch awaitLatch = new CountDownLatch(1);

    /**
     * Sole Constructor.
     */
    private Cascade ()
    {
        // Pass
    }

    /**
     * Getter.
     *
     * @return the name of this object.
     */
    public CascadeToken name ()
    {
        return name;
    }

    /**
     * Getter.
     *
     * @return a UUID that uniquely identifies this object.
     */
    public UUID uuid ()
    {
        return uuid;
    }

    /**
     * Getter.
     *
     * @return the current phase of execution.
     */
    public ExecutionPhase phase ()
    {
        final ExecutionPhase phase = ExecutionPhase.values()[phaseOrdinal.get()];
        return phase;
    }

    /**
     * Use this method to start the execution of the system.
     *
     * <p>
     * This method does *not* block (returns immediately).
     * </p>
     *
     * <p>
     * Subsequent invocations of this method are no-ops.
     * </p>
     *
     * @return this.
     */
    public Cascade start ()
    {
        return this;
    }

    /**
     * Use this method to stop the execution of the system.
     *
     * <p>
     * This method does *not* block (returns immediately).
     * </p>
     *
     * <p>
     * Subsequent invocations of this method are no-ops.
     * </p>
     *
     * <p>
     * An arbitrary amount of time may be needed in order to stop.
     * </p>
     *
     * <p>
     * If any reactor has a non well-behaved implementation,
     * such as creating threads that do not get notified
     * of stop requests, then it may not be possible to
     * entirely clean up the system. Thus, this method
     * should generally *not* be considered a *guaranteed*
     * way to stop the system.
     * </p>
     *
     * @return this.
     */
    public Cascade stop ()
    {
        return this;
    }

    /**
     * This method blocks until the system completely stops.
     *
     * <p>
     * This method is useful, for example, in unit-tests.
     * </p>
     *
     * @param timeout is the maximum amount of time to wait.
     * @param timeoutUnit describes the timeout.
     * @return true, iff the system has stopped.
     * @throws java.lang.InterruptedException
     */
    public boolean await (final long timeout,
                          final TimeUnit timeoutUnit)
            throws InterruptedException
    {
        awaitLatch.await(timeout, timeoutUnit);
        return phase() == ExecutionPhase.TERMINATED;
    }

    /**
     * This method blocks until the system completely stops.
     *
     * <p>
     * This method is useful, for example, in unit-tests.
     * </p>
     *
     * @throws java.lang.InterruptedException
     */
    public void await ()
            throws InterruptedException
    {
        awaitLatch.await();
    }

    /**
     * Use this method to send an event-message.
     *
     * @param event identifies the event-channel to send the message to.
     * @param message is the message to send.
     * @return this.
     */
    public Cascade send (final String event,
                         final OperandStack message)
    {
        final CascadeToken token = CascadeToken.create(event);
        return send(token, message);
    }

    /**
     * Use this method to send an event-message.
     *
     * @param event identifies the event-channel to send the message to.
     * @param message is the message to send.
     * @return this.
     */
    public Cascade send (final CascadeToken event,
                         final OperandStack message)
    {
        return this;
    }

    /**
     * Use this method to add/load a new reactor to this system.
     *
     * @param builder will provide the core() of the new reactor.
     * @return an object to-use for completing the registration process.
     */
    public PendingRegistration register (final ReactorCore.Builder builder)
    {
        return new PendingRegistration();
    }

    /**
     * Use this method to add/load a new reactor to this system,
     * using reflection to define the various event-handlers.
     *
     * @param builder will provide the core() of the new reactor.
     * @return an object to-use for completing the registration process.
     */
    public PendingRegistration registerObject (final Supplier<Object> builder)
    {
        return new PendingRegistration();
    }

    /**
     * Use this method to add/load a new reactor to this system,
     * using reflection to define the various event-handlers.
     *
     * @param klass will provide the core() of the new reactor.
     * @return an object to-use for completing the registration process.
     */
    public PendingRegistration registerClass (final Class<?> klass)
    {
        return new PendingRegistration();
    }

    /**
     * Use this method in order to remove/unload a reactor from this system.
     *
     * @param name identifies the reactor to remove.
     * @return this.
     */
    public Cascade deregister (final String name)
    {
        final CascadeToken token = CascadeToken.create(name);
        return deregister(token);
    }

    /**
     * Use this method in order to remove/unload a reactor from this system.
     *
     * @param name identifies the reactor to remove.
     * @return this.
     */
    public Cascade deregister (final CascadeToken name)
    {
        return this;
    }

    /**
     * Getter.
     *
     * <p>
     * The returned map maps the name of a reactor to the reactor itself.
     * </p>
     *
     * @return all of the reactors that are currently registered herein.
     */
    public Map<CascadeToken, Reactor> reactors ()
    {
        return null;
    }

    /**
     * Getter.
     *
     * @return a new Builder object.
     */
    public static Builder newBuilder ()
    {
        return new Cascade().new Builder();
    }

    /**
     * This is the current phase of execution.
     * Execution starts in the INITIAL phase.
     * Execution will progress to the TERMINATED phase.
     * Once the TERMINATED phase is reached,
     * no further phase transitions will occur.
     *
     * <p>
     * More phases may be added in the future.
     * </p>
     */
    public static enum ExecutionPhase
    {
        INITIAL,
        SETUP,
        START,
        RUN,
        STOP,
        DESTROY,
        TERMINATED,
    }

    /**
     * Describes the pending registration of a reactor.
     */
    public final class PendingRegistration
    {
        private volatile boolean allowed = true;

        /**
         * Sole Constructor.
         */
        private PendingRegistration ()
        {
            // Pass
        }

        /**
         * Setter.
         *
         * @param name will be the name of the reactor being registered.
         * @return this.
         */
        public PendingRegistration named (final String name)
        {
            Preconditions.checkState(allowed, "Registration Already Complete!");
            final CascadeToken token = CascadeToken.create(name);
            return named(token);
        }

        /**
         * Setter.
         *
         * @param name will be the name of the reactor being registered.
         * @return this.
         */
        public PendingRegistration named (final CascadeToken name)
        {
            Preconditions.checkState(allowed, "Registration Already Complete!");
            return this;
        }

        /**
         * Setter.
         *
         * @param name is the name of the parameter to add a value to.
         * @param value will be added to the named parameter.
         * @return this.
         */
        public PendingRegistration param (final String name,
                                          final Object value)
        {
            Preconditions.checkState(allowed, "Registration Already Complete!");
            return this;
        }

        /**
         * Use this method to finalize the registration of the reactor.
         *
         * @return this.
         */
        public PendingRegistration activate ()
        {
            Preconditions.checkState(allowed, "Registration Already Complete!");
            allowed = false;

            return this;
        }
    }

    /**
     * A builder that builds Cascade objects.
     */
    public final class Builder
    {
        private volatile boolean allowed = true;

        /**
         * Sole Constructor.
         */
        private Builder ()
        {
            // Pass
        }

        /**
         * Setter.
         *
         * @param name will be the name of the new Cascade object.
         * @return this.
         */
        public synchronized Builder named (final String name)
        {
            Preconditions.checkNotNull(name, "name");
            Preconditions.checkState(allowed, "Already Built!");
            SELF.name = CascadeToken.create(name);
            return this;
        }

        /**
         * Setter.
         *
         * @param count will be the number of messaging-threads.
         * @return this.
         */
        public synchronized Builder withThreadCount (final int count)
        {
            Preconditions.checkState(allowed, "Already Built!");
            Preconditions.checkArgument(count >= 0, "count < 0");
            return this;
        }

        /**
         * Setter.
         *
         * @param factory will be used to create all threads.
         * @return this.
         */
        public synchronized Builder usingThreadFactory (final ThreadFactory factory)
        {
            Preconditions.checkNotNull(factory, "factory");
            Preconditions.checkState(allowed, "Already Built!");
            return this;
        }

        /**
         * Setter.
         *
         * @param factory will be used to create all loggers.
         * @return this.
         */
        public synchronized Builder usingLoggerFactory (final CascadeLogger.Factory factory)
        {
            Preconditions.checkNotNull(factory, "factory");
            Preconditions.checkState(allowed, "Already Built!");
            return this;
        }

        /**
         * Setter.
         *
         * @param allocator will be the default allocator of the reactors.
         * @return this.
         */
        public synchronized Builder usingAllocator (final Allocator allocator)
        {
            Preconditions.checkNotNull(allocator, "allocator");
            Preconditions.checkState(allowed, "Already Built!");
            return this;
        }

        /**
         * Getter.
         *
         * <p>
         * After this method returns, this builder can no longer be used.
         * </p>
         *
         * @return the new Cascade object.
         */
        public synchronized Cascade build ()
        {
            Preconditions.checkState(allowed, "Already Built!");
            allowed = false;
            return SELF;
        }
    }

    public static void main (String[] args)
            throws InterruptedException
    {
        // Should register(), etc, block until start() is finished???

        final Cascade cas = Cascade.newBuilder()
                .named("MyCascade")
                .withThreadCount(2)
                .usingLoggerFactory(x -> new StandardLogger(x))
                .build()
                .start();
        cas.registerObject(() -> new Object())
                .named("Anna")
                .param("input", "com.mhigh.events.newXX")
                .param("priority", 1)
                .activate();
        cas.stop();
        cas.await();
    }

}
