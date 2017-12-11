package com.mackenziehigh.cascade.internal.pumps;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public final class DirectEngine
        implements Engine
{
    private final DirectConnector connector;

    private final Map<ConnectionSchema, Connector.Connection> connections = new ConcurrentHashMap<>();

    private final Map<ConnectionSchema, Connector.Connection> unmodConnections = Collections.unmodifiableMap(connections);

    private final AtomicBoolean started = new AtomicBoolean();

    private final AtomicBoolean running = new AtomicBoolean();

    private final AtomicBoolean stop = new AtomicBoolean();

    private final MessageConsumer consumer;

    public DirectEngine (final List<ConnectionSchema> inputs,
                         final MessageConsumer action)
    {
        for (ConnectionSchema input : inputs)
        {
            Preconditions.checkArgument(input.consumer.concurrentLimit() >= 1);
            Preconditions.checkArgument(action.equals(input.consumer));
        }

        this.consumer = action;
        this.connector = new DirectConnector(x -> execute(x), action.concurrentLimit());

        for (int i = 0; i < inputs.size(); i++)
        {
            final ConnectionSchema schema = inputs.get(i);
            final Connector.Connection connection = connector.addConnection(schema.capacity);
            connections.put(schema, connection);
        }
    }

    @Override
    public Set<Thread> threads ()
    {
        return ImmutableSet.of();
    }

    @Override
    public Map<ConnectionSchema, Connector.Connection> connections ()
    {
        return unmodConnections;
    }

    @Override
    public boolean isRunning ()
    {
        return running.get();
    }

    @Override
    public void start ()
    {
        started.getAndSet(true);
    }

    @Override
    public void stop ()
    {
        stop.set(true);
    }

    private void execute (final OperandStack message)
    {
        running.set(true);

        try
        {
            consumer.accept(message);
        }
        catch (Throwable ex1)
        {
            try
            {
                consumer.handle(ex1);
            }
            catch (Throwable ex2)
            {
                // TODO: Pass??
            }
        }

        running.set(false);
    }
}
