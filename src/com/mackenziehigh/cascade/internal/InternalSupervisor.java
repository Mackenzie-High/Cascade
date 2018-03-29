package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.CascadeDirector;

/**
 * This is the actual implementation of the CascadeSupervisor interface.
 */
public final class InternalSupervisor
        implements CascadeDirector
{
    public InternalSupervisor (final CascadeDirector delegate)
    {

    }

    @Override
    public void onClosed (CascadeActor actor)
    {
        CascadeDirector.super.onClosed(actor);
    }

    @Override
    public void onClosing (CascadeActor actor)
    {
        CascadeDirector.super.onClosing(actor);
    }

    @Override
    public void onUndeliveredMessage (CascadeActor sender,
                                      CascadeToken event,
                                      CascadeStack stack)
    {
        CascadeDirector.super.onUndeliveredMessage(sender, event, stack);
    }

    @Override
    public void onProducedMessage (CascadeActor sender,
                                   CascadeToken event,
                                   CascadeStack stack)
    {
        CascadeDirector.super.onProducedMessage(sender, event, stack);
    }

    @Override
    public void onUnhandledException (CascadeActor actor,
                             Throwable cause)
    {
        CascadeDirector.super.onUnhandledException(actor, cause);
    }

    @Override
    public void onConsumedMessage (CascadeActor actor,
                                   CascadeToken event,
                                   CascadeStack stack)
    {
        CascadeDirector.super.onConsumedMessage(actor, event, stack);
    }

    @Override
    public void onConsumingMessage (CascadeActor actor,
                                    CascadeToken event,
                                    CascadeStack stack)
    {
        CascadeDirector.super.onConsumingMessage(actor, event, stack);
    }

    @Override
    public void onDroppedMessage (CascadeActor actor,
                                  CascadeToken event,
                                  CascadeStack stack)
    {
        CascadeDirector.super.onDroppedMessage(actor, event, stack);
    }

    @Override
    public void onReceivedMessage (CascadeActor actor,
                                   CascadeToken event,
                                   CascadeStack stack)
    {
        CascadeDirector.super.onReceivedMessage(actor, event, stack);
    }

    @Override
    public void onDeregistration (CascadeActor actor)
    {
        CascadeDirector.super.onDeregistration(actor);
    }

    @Override
    public void onRegistration (CascadeActor actor)
    {
        CascadeDirector.super.onRegistration(actor);
    }

}
