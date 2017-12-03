package com.mackenziehigh.cascade.internal.pumps;

import com.mackenziehigh.cascade.internal.pumps.Engine.MessageConsumer;

/**
 *
 */
public final class ConnectionSchema
{
    public final Object correlationId;

    public final int capacity;

    public final MessageConsumer consumer;

    public ConnectionSchema (final Object correlationId,
                             final int capacity,
                             final MessageConsumer consumer)
    {
        this.correlationId = correlationId;
        this.capacity = capacity;
        this.consumer = consumer;
    }

}
