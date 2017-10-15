package com.mackenziehigh.cascade.actors;

import com.mackenziehigh.cascade.AbstractActor;
import com.mackenziehigh.cascade.MessageStack;
import com.mackenziehigh.cascade.Pipeline;
import java.util.function.Function;

/**
 *
 */
public class TransformActor
        extends AbstractActor
{
    private Function<MessageStack, MessageStack> transform;

    public Function<MessageStack, MessageStack> getTransform ()
    {
        return transform;
    }

    public void setTransform (final Function<MessageStack, MessageStack> transform)
    {
        this.transform = transform;
    }

    @Override
    public void process (final Pipeline source,
                         final MessageStack message)
            throws Throwable
    {
        send(transform.apply(message));
    }

}
