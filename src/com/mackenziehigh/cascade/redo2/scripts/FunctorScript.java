package com.mackenziehigh.cascade.redo2.scripts;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.redo2.CascadeContext;
import com.mackenziehigh.cascade.redo2.CascadeOperand;
import com.mackenziehigh.cascade.redo2.CascadeScript;
import com.mackenziehigh.cascade.redo2.CascadeToken;
import java.util.Set;

/**
 *
 */
public final class FunctorScript
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
                            CascadeOperand stack)
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

    public FunctorScript bindOnSetup (final SetupFunction functor)
    {
        Preconditions.checkNotNull(functor, "functor");
        setupFunctions.add(functor);
        return this;
    }

    public FunctorScript bindOnClose (final CloseFunction functor)
    {
        Preconditions.checkNotNull(functor, "functor");
        closeFunctions.add(functor);
        return this;
    }

    public FunctorScript subscribe (final MessageFunction functor,
                                    final String event)
    {
        return subscribe(functor, CascadeToken.create(event));
    }

    public FunctorScript subscribe (final MessageFunction functor,
                                    final CascadeToken event)
    {
        Preconditions.checkNotNull(functor, "functor");
        messageFunctions.add(functor);
        return this;
    }

    public FunctorScript unsubscribe (final MessageFunction functor,
                                      final String event)
    {
        return unsubscribe(functor, CascadeToken.create(event));
    }

    public FunctorScript unsubscribe (final MessageFunction functor,
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
