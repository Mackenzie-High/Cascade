package com.mackenziehigh.cascade.scripts;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.CascadeContext;
import com.mackenziehigh.cascade.CascadeScript;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Set;

/**
 * A lambda-script is a script defined using lambdas.
 */
public final class LambdaScript
        implements CascadeScript
{

    private final Set<SetupFunction> immutableSetupFunctions;

    private final ListMultimap<CascadeToken, MessageFunction> immutableMessageFunctions;

    private final Set<ExceptionFunction> immutableExceptionFunctions;

    private final Set<CloseFunction> immutableCloseFunctions;

    private LambdaScript (final Builder builder)
    {
        immutableSetupFunctions = ImmutableSet.copyOf(builder.setupFunctions);
        immutableMessageFunctions = ImmutableListMultimap.copyOf(builder.messageFunctions);
        immutableExceptionFunctions = ImmutableSet.copyOf(builder.unhandledExceptionFunctions);
        immutableCloseFunctions = ImmutableSet.copyOf(builder.closeFunctions);
    }

    /**
     * Creates a new builder that builds lambda-scripts.
     *
     * @return the new builder.
     */
    public static LambdaScript.Builder newBuilder ()
    {
        return new Builder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSetup (final CascadeContext ctx)
            throws Throwable
    {
        for (CascadeToken input : immutableMessageFunctions.keySet())
        {
            ctx.actor().subscribe(input);
        }

        for (SetupFunction function : immutableSetupFunctions)
        {
            function.accept(ctx);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage (final CascadeContext ctx,
                           final CascadeToken event,
                           final CascadeStack stack)
            throws Throwable
    {
        for (MessageFunction function : immutableMessageFunctions.get(event))
        {
            function.accept(ctx, event, stack);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onException (final CascadeContext ctx,
                             final Throwable cause)
            throws Throwable
    {
        for (ExceptionFunction function : immutableExceptionFunctions)
        {
            function.accept(ctx, cause);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClose (final CascadeContext ctx)
            throws Throwable
    {
        for (CloseFunction function : immutableCloseFunctions)
        {
            function.accept(ctx);
        }
    }

    /**
     * Lambda function whose signature is the same as the onSetup() event-handler.
     */
    @FunctionalInterface
    public interface SetupFunction
    {
        public void accept (CascadeContext ctx)
                throws Throwable;
    }

    /**
     * Lambda function whose signature is the same as the onMessage() event-handler.
     */
    @FunctionalInterface
    public interface MessageFunction
    {
        public void accept (CascadeContext ctx,
                            CascadeToken event,
                            CascadeStack stack)
                throws Throwable;
    }

    /**
     * Lambda function whose signature is the same as the onUnhandledException() event-handler.
     */
    @FunctionalInterface
    public interface ExceptionFunction
    {
        public void accept (CascadeContext ctx,
                            Throwable cause)
                throws Throwable;
    }

    /**
     * Lambda function whose signature is the same as the onUndeliveredMessage() event-handler.
     */
    @FunctionalInterface
    public interface UndeliveredMessageFunction
    {
        public void accept (CascadeContext ctx,
                            CascadeToken event,
                            CascadeStack stack)
                throws Throwable;
    }

    /**
     * Lambda function whose signature is the same as the onClose() event-handler.
     */
    @FunctionalInterface
    public interface CloseFunction
    {
        public void accept (CascadeContext ctx)
                throws Throwable;
    }

    /**
     * A builder that builds lambda-scripts.
     */
    public static final class Builder
    {
        private final Set<SetupFunction> setupFunctions = Sets.newConcurrentHashSet();

        private final ListMultimap<CascadeToken, MessageFunction> messageFunctions = LinkedListMultimap.create();

        private final Set<ExceptionFunction> unhandledExceptionFunctions = Sets.newConcurrentHashSet();

        private final Set<CloseFunction> closeFunctions = Sets.newConcurrentHashSet();

        /**
         * Specify a function that will be invoked by the actor's onSetup() event-handler.
         *
         * <p>
         * This method may be invoked multiple times in order to
         * specify a sequence of behaviors for the event-handler.
         * </p>
         *
         * @param functor will define the behavior of the event-handler.
         * @return this.
         */
        public Builder bindOnSetup (final SetupFunction functor)
        {
            Preconditions.checkNotNull(functor, "functor");
            setupFunctions.add(functor);
            return this;
        }

        /**
         * Specify a function that will be invoked by the actor's onMessage() event-handler.
         *
         * <p>
         * The given function will be invoked whenever an event-message
         * is received that was produced by the by the given event.
         * </p>
         *
         * <p>
         * This method, or its overload, may be invoked multiple times in
         * order to specify a sequence of behaviors for the event-handler.
         * </p>
         *
         * @param event identifies the even that the function will handle.
         * @param functor will define the behavior of the event-handler.
         * @return this.
         */
        public Builder bindOnMessage (final CascadeToken event,
                                      final MessageFunction functor)
        {
            Preconditions.checkNotNull(functor, "functor");
            messageFunctions.put(event, functor);
            return this;
        }

        /**
         * Specify a function that will be invoked by the actor's onException() event-handler.
         *
         * <p>
         * The given function will be invoked whenever an unhandled exception
         * occurs and causes the actor's onException() event-handler to execute.
         * </p>
         *
         * <p>
         * This method, or its overload, may be invoked multiple times in
         * order to specify a sequence of behaviors for the event-handler.
         * </p>
         *
         * @param functor will define the behavior of the event-handler.
         * @return this.
         */
        public Builder bindOnException (final ExceptionFunction functor)
        {
            Preconditions.checkNotNull(functor, "functor");
            unhandledExceptionFunctions.add(functor);
            return this;
        }

        /**
         * Specify a function that will be invoked by the actor's onClose() event-handler.
         *
         * <p>
         * This method, or its overload, may be invoked multiple times in
         * order to specify a sequence of behaviors for the event-handler.
         * </p>
         *
         * @param functor will define the behavior of the event-handler.
         * @return this.
         */
        public Builder bindOnClose (final CloseFunction functor)
        {
            Preconditions.checkNotNull(functor, "functor");
            closeFunctions.add(functor);
            return this;
        }

        /**
         * Create the lambda-script based on the settings in this builder.
         *
         * @return the new script.
         */
        public LambdaScript build ()
        {
            return new LambdaScript(this);
        }
    }
}
