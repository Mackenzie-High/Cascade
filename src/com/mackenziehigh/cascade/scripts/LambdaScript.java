package com.mackenziehigh.cascade.scripts;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
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
    @FunctionalInterface
    public interface SetupFunction
    {
        public void accept (CascadeContext ctx)
                throws Throwable;
    }

    @FunctionalInterface
    public interface MessageFunction
    {
        public void accept (CascadeContext ctx,
                            CascadeToken event,
                            CascadeStack stack)
                throws Throwable;
    }

    @FunctionalInterface
    public interface UnhandledExceptionFunction
    {
        public void accept (CascadeContext ctx,
                            Throwable cause)
                throws Throwable;
    }

    @FunctionalInterface
    public interface UndeliveredMessageFunction
    {
        public void accept (CascadeContext ctx,
                            CascadeToken event,
                            CascadeStack stack)
                throws Throwable;
    }

    @FunctionalInterface
    public interface CloseFunction
    {
        public void accept (CascadeContext ctx)
                throws Throwable;
    }

    private final Set<SetupFunction> setupFunctions = Sets.newConcurrentHashSet();

    private final SetMultimap<CascadeToken, MessageFunction> messageFunctions = Multimaps.synchronizedSetMultimap(HashMultimap.create());

    private final Set<UndeliveredMessageFunction> undeliveredMessageFunctions = Sets.newConcurrentHashSet();

    private final Set<UnhandledExceptionFunction> unhandledExceptionFunctions = Sets.newConcurrentHashSet();

    private final Set<CloseFunction> closeFunctions = Sets.newConcurrentHashSet();

    public LambdaScript bindOnSetup (final SetupFunction functor)
    {
        Preconditions.checkNotNull(functor, "functor");
        setupFunctions.add(functor);
        return this;
    }

    public LambdaScript bindOnMessage (final MessageFunction functor)
    {
        Preconditions.checkNotNull(functor, "functor");
        messageFunctions.put(null, functor);
        return this;
    }

    public LambdaScript bindOnUndeliveredMessage (final UndeliveredMessageFunction functor)
    {
        Preconditions.checkNotNull(functor, "functor");
        undeliveredMessageFunctions.add(functor);
        return this;
    }

    public LambdaScript bindOnUnhandledException (final UnhandledExceptionFunction functor)
    {
        Preconditions.checkNotNull(functor, "functor");
        unhandledExceptionFunctions.add(functor);
        return this;
    }

    public LambdaScript bindOnClose (final CloseFunction functor)
    {
        Preconditions.checkNotNull(functor, "functor");
        closeFunctions.add(functor);
        return this;
    }

    public LambdaScript subscribe (final String event,
                                   final MessageFunction functor)
    {
        return subscribe(CascadeToken.token(event), functor);
    }

    public LambdaScript subscribe (final CascadeToken event,
                                   final MessageFunction functor)
    {
        Preconditions.checkNotNull(functor, "functor");
        messageFunctions.put(event, functor);
        return this;
    }

    public LambdaScript unsubscribe (final MessageFunction functor,
                                     final String event)
    {
        return unsubscribe(CascadeToken.token(event), functor);
    }

    public LambdaScript unsubscribe (final CascadeToken event,
                                     final MessageFunction functor)
    {
        return this;
    }

    public Set<SetupFunction> getSetupFunctions ()
    {
        return ImmutableSet.copyOf(setupFunctions);
    }

    public Set<MessageFunction> getMessageFunctions ()
    {
        return ImmutableSet.copyOf(messageFunctions.values());
    }

    public Set<UndeliveredMessageFunction> getUndeliverdMessageFunctions ()
    {
        return ImmutableSet.copyOf(undeliveredMessageFunctions);
    }

    public Set<UnhandledExceptionFunction> getUnhandledExceptionFunctions ()
    {
        return ImmutableSet.copyOf(unhandledExceptionFunctions);
    }

    public Set<CloseFunction> getCloseFunctions ()
    {
        return ImmutableSet.copyOf(closeFunctions);
    }

    public Set<CascadeToken> subscriptionsOf (final MessageFunction functor)
    {
        return ImmutableSet.of();
    }

    public Set<CascadeToken> subscriptions ()
    {
        return ImmutableSet.of();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClose (final CascadeContext ctx)
            throws Throwable
    {
        for (CloseFunction function : closeFunctions)
        {
            function.accept(ctx);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUndeliveredMessage (final CascadeContext ctx,
                                      final CascadeToken event,
                                      final CascadeStack stack)
            throws Throwable
    {
        for (UndeliveredMessageFunction function : undeliveredMessageFunctions)
        {
            function.accept(ctx, event, stack);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUnhandledException (final CascadeContext ctx,
                                      final Throwable cause)
            throws Throwable
    {
        for (UnhandledExceptionFunction function : unhandledExceptionFunctions)
        {
            function.accept(ctx, cause);
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
        for (MessageFunction function : messageFunctions.get(null))
        {
            function.accept(ctx, event, stack);
        }

        for (MessageFunction function : messageFunctions.get(event))
        {
            function.accept(ctx, event, stack);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSetup (final CascadeContext ctx)
            throws Throwable
    {
        for (SetupFunction function : setupFunctions)
        {
            function.accept(ctx);
        }
    }

}
