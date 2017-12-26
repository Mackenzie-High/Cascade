package com.mackenziehigh.cascade.internal.schema;

import com.google.common.collect.ImmutableMap;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeAllocator.AllocationPool;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadePump;
import com.mackenziehigh.cascade.CascadeReactor;
import com.mackenziehigh.cascade.CascadeSubscription;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.internal.engines.Connection;
import com.mackenziehigh.cascade.internal.routing.EventDispatcher;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public final class ConcreteReactor
        implements CascadeReactor
{
    private final ConcreteCascade cascade;

    private final CascadeToken name;

    private final Core core;

    private final AllocationPool pool;

    private final CascadeLogger logger;

    private final Connection input;

    private final EventDispatcher.ConcurrentEventSender sender;

    private final ImmutableMap<CascadeToken, CascadeSubscription> subscriptions;

    public ConcreteReactor (final ConcreteCascade cascade,
                            final CascadeToken name,
                            final Core core,
                            final AllocationPool pool,
                            final CascadeLogger logger,
                            final Map<CascadeToken, CascadeSubscription> subscriptions,
                            final Connection input,
                            final EventDispatcher.ConcurrentEventSender sender)
    {
        this.cascade = cascade;
        this.name = name;
        this.core = core;
        this.sender = sender;
        this.pool = pool;
        this.logger = logger;
        this.input = input;
        this.subscriptions = ImmutableMap.copyOf(subscriptions);
    }

    public Connection input ()
    {
        return input;
    }

    @Override
    public Cascade cascade ()
    {
        return cascade;
    }

    @Override
    public CascadeToken name ()
    {
        return name;
    }

    @Override
    public Core core ()
    {
        return core;
    }

    @Override
    public CascadeLogger logger ()
    {
        return logger;
    }

    @Override
    public CascadeAllocator allocator ()
    {
        return pool().allocator();
    }

    @Override
    public CascadeAllocator.AllocationPool pool ()
    {
        return pool;
    }

    @Override
    public CascadePump pump ()
    {
        return cascade.pumps().get(name());
    }

    @Override
    public int backlogSize ()
    {
        return input.globalSize();
    }

    @Override
    public int backlogCapacity ()
    {
        return input.globalCapacity();
    }

    @Override
    public int queueSize ()
    {
        return input.localSize();
    }

    @Override
    public int queueCapacity ()
    {
        return input.localCapacity();
    }

    @Override
    public Map<CascadeToken, CascadeSubscription> subscriptions ()
    {
        return subscriptions;
    }

    @Override
    public boolean async (final CascadeToken event,
                          final CascadeAllocator.OperandStack message)
    {
        return sender.sendAsync(event, message);
    }

    @Override
    public boolean sync (final CascadeToken event,
                         final CascadeAllocator.OperandStack message,
                         final long timeout,
                         final TimeUnit timeoutUnits)
    {
        //return sender.sendSync(event, message, timeout, timeoutUnits);
        return false;
    }

    @Override
    public void send (final CascadeToken event,
                      final CascadeAllocator.OperandStack message)
            throws SendFailureException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int broadcast (final CascadeToken event,
                          final CascadeAllocator.OperandStack message)
    {
        return sender.broadcast(event, message);
    }

    @Override
    public String toString ()
    {
        return name.name();
    }

}
