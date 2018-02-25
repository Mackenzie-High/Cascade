package com.mackenziehigh.cascade.redo2.util.actors;

import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.redo2.CascadeContext;
import com.mackenziehigh.cascade.redo2.CascadeOperand;
import com.mackenziehigh.cascade.redo2.CascadeScript;

/**
 *
 */
public class SimpleCascadeScript
        implements CascadeScript
{

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onClose (CascadeContext ctx)
            throws Throwable
    {
        CascadeScript.super.onClose(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onMessage (CascadeContext ctx,
                                 CascadeToken event,
                                 CascadeOperand stack)
            throws Throwable
    {
        CascadeScript.super.onMessage(ctx, event, stack);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onSetup (CascadeContext ctx)
            throws Throwable
    {
        CascadeScript.super.onSetup(ctx);
    }

}
