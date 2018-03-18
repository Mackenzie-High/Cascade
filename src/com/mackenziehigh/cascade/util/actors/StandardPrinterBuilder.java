package com.mackenziehigh.cascade.util.actors;

import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeContext;
import com.mackenziehigh.cascade.CascadeScript;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeStage;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Objects;

/**
 *
 */
public final class StandardPrinterBuilder
        implements CascadeActor.Builder
{
    private final CascadeStage stage;

    private volatile CascadeToken input;

    public StandardPrinterBuilder (final CascadeStage stage)
    {
        this.stage = Objects.requireNonNull(stage, "stage");
    }

    @Override
    public CascadeStage stage ()
    {
        return stage;
    }

    public StandardPrinterBuilder setInput (final CascadeToken event)
    {
        this.input = event;
        return this;
    }

    public StandardPrinterBuilder setInput (final String event)
    {
        return setInput(CascadeToken.token(event));
    }

    @Override
    public CascadeActor build ()
    {
        final CascadeScript script = new CascadeScript()
        {
            @Override
            public void onMessage (final CascadeContext ctx,
                                   final CascadeToken event,
                                   final CascadeStack stack)
                    throws Throwable
            {
                System.out.println(stack.peekAsObject());
            }
        };

        final CascadeActor actor = stage().newActor(script);
        actor.subscribe(input);
        return actor;
    }

}
