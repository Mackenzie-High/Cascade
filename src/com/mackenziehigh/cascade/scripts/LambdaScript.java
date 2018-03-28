package com.mackenziehigh.cascade.scripts;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.CascadeContext;
import com.mackenziehigh.cascade.CascadeScript;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Set;

/**
 *
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
                            CascadeToken event,
                            CascadeStack stack)
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

    private final Set<MessageFunction> messageFunctions = Sets.newConcurrentHashSet();

    private final Set<CloseFunction> closeFunctions = Sets.newConcurrentHashSet();

    public LambdaScript bindOnSetup (final SetupFunction functor)
    {
        Preconditions.checkNotNull(functor, "functor");
        setupFunctions.add(functor);
        return this;
    }

    public LambdaScript bindOnClose (final CloseFunction functor)
    {
        Preconditions.checkNotNull(functor, "functor");
        closeFunctions.add(functor);
        return this;
    }

    public LambdaScript subscribe (final MessageFunction functor,
                                   final String event)
    {
        return subscribe(functor, CascadeToken.token(event));
    }

    public LambdaScript subscribe (final MessageFunction functor,
                                   final CascadeToken event)
    {
        Preconditions.checkNotNull(functor, "functor");
        messageFunctions.add(functor);
        return this;
    }

    public LambdaScript unsubscribe (final MessageFunction functor,
                                     final String event)
    {
        return unsubscribe(functor, CascadeToken.token(event));
    }

    public LambdaScript unsubscribe (final MessageFunction functor,
                                     final CascadeToken event)
    {
        return this;
    }

    public Set<SetupFunction> getSetupFunctions ()
    {
        return ImmutableSet.of();
    }

    public Set<MessageFunction> getMessageFunctions ()
    {
        return ImmutableSet.of();
    }

    public Set<CloseFunction> getCloseFunctions ()
    {
        return ImmutableSet.of();
    }

    public Set<CascadeToken> subscriptionsOf (final MessageFunction functor)
    {
        return ImmutableSet.of();
    }

    public Set<CascadeToken> subscriptions ()
    {
        return ImmutableSet.of();
    }
}
