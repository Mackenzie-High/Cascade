package com.mackenziehigh.cascade.internal.schema;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadePump;
import com.mackenziehigh.cascade.CascadeReactor;
import com.mackenziehigh.cascade.CascadeSubscription;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.internal.routing.EventDispatcher;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public final class ConcreteReactor
        implements CascadeReactor
{
    private final EventDispatcher.ConcurrentEventSender sender;

    public ConcreteReactor (final CascadeToken name,
                            final EventDispatcher.ConcurrentEventSender sender)
    {
        this.sender = sender;
    }

    @Override
    public Cascade cascade ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CascadeToken name ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Core core ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CascadeLogger logger ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CascadeAllocator allocator ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CascadeAllocator.AllocationPool pool ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CascadePump pump ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int backlogSize ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int backlogCapacity ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int queueSize ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int queueCapacity ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<CascadeToken, CascadeSubscription> subscriptions ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
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

}
