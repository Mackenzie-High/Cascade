package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeDirector;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A supervisor composed of zero-or-more supervisors.
 */
public final class InternalSupervisors
        implements CascadeDirector
{
    private final AtomicReference<List<CascadeDirector>> delegates = new AtomicReference<>(Collections.emptyList());

    public synchronized void register (final CascadeDirector delegate)
    {
        final List<CascadeDirector> original = delegates.get();
        final List<CascadeDirector> modified = new CopyOnWriteArrayList<>(original);
        modified.add(delegate);
        final List<CascadeDirector> unmod = Collections.unmodifiableList(modified);
        delegates.set(unmod);
    }

    public synchronized void deregister (final CascadeDirector delegate)
    {
        final List<CascadeDirector> original = delegates.get();
        final List<CascadeDirector> modified = new CopyOnWriteArrayList<>(original);
        modified.remove(delegate);
        final List<CascadeDirector> unmod = Collections.unmodifiableList(modified);
        delegates.set(unmod);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClosed (final CascadeActor actor)
    {
        delegates.get().forEach(x -> x.onClosed(actor));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClosing (final CascadeActor actor)
    {
        delegates.get().forEach(x -> x.onClosing(actor));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onException (final CascadeActor actor,
                             final Throwable cause)
    {
        delegates.get().forEach(x -> x.onException(actor, cause));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConsumedMessage (final CascadeActor actor,
                                   final CascadeToken event,
                                   final CascadeStack stack)
    {
        delegates.get().forEach(x -> x.onConsumedMessage(actor, event, stack));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConsumingMessage (final CascadeActor actor,
                                    final CascadeToken event,
                                    final CascadeStack stack)
    {
        delegates.get().forEach(x -> x.onConsumingMessage(actor, event, stack));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDroppedMessage (final CascadeActor actor,
                                  final CascadeToken event,
                                  final CascadeStack stack)
    {
        delegates.get().forEach(x -> x.onDroppedMessage(actor, event, stack));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAcceptedMessage (final CascadeActor actor,
                                   final CascadeToken event,
                                   final CascadeStack stack)
    {
        delegates.get().forEach(x -> x.onAcceptedMessage(actor, event, stack));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDeregistration (final CascadeActor actor)
    {
        delegates.get().forEach(x -> x.onDeregistration(actor));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRegistration (final CascadeActor actor)
    {
        delegates.get().forEach(x -> x.onRegistration(actor));
    }

}
