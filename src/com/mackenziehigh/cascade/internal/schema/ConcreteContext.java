package com.mackenziehigh.cascade.internal.schema;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadePump;
import com.mackenziehigh.cascade.CascadeReactor;
import com.mackenziehigh.cascade.CascadeReactor.Context;
import com.mackenziehigh.cascade.CascadeSubscription;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public final class ConcreteContext
        implements Context
{
    private final CascadeReactor reactor;

    private volatile Thread user;

    private volatile CascadeToken event;

    private volatile OperandStack stack;

    private volatile Throwable exception;

    public ConcreteContext (final CascadeReactor reactor)
    {
        this.reactor = Objects.requireNonNull(reactor);
    }

    public void set (final Thread user,
                     final CascadeToken event,
                     final OperandStack stack,
                     final Throwable exception)
    {
        this.user = user;
        this.event = event;
        this.stack = stack;
        this.exception = exception;
    }

    @Override
    public CascadeReactor reactor ()
    {
        return reactor;
    }

    @Override
    public CascadeToken event ()
    {
        detectMultipleThreads();
        return event;
    }

    @Override
    public CascadeAllocator.OperandStack message ()
    {
        detectMultipleThreads();
        return stack;
    }

    @Override
    public Throwable exception ()
    {
        detectMultipleThreads();
        return exception;
    }

    @Override
    public Cascade cascade ()
    {
        return reactor.cascade();
    }

    @Override
    public CascadeToken name ()
    {
        return reactor.name();
    }

    @Override
    public Core core ()
    {
        return reactor.core();
    }

    @Override
    public CascadeLogger logger ()
    {
        return reactor.logger();
    }

    @Override
    public CascadeAllocator allocator ()
    {
        return reactor.allocator();
    }

    @Override
    public CascadeAllocator.AllocationPool pool ()
    {
        return reactor.pool();
    }

    @Override
    public CascadePump pump ()
    {
        return reactor.pump();
    }

    @Override
    public int backlogSize ()
    {
        return reactor.backlogSize();
    }

    @Override
    public int backlogCapacity ()
    {
        return reactor.backlogCapacity();
    }

    @Override
    public int queueSize ()
    {
        return reactor.queueSize();
    }

    @Override
    public int queueCapacity ()
    {
        return reactor.queueCapacity();
    }

    @Override
    public double backpressure ()
    {
        return reactor.backpressure();
    }

    @Override
    public Map<CascadeToken, CascadeSubscription> subscriptions ()
    {
        return reactor.subscriptions();
    }

    @Override
    public boolean async (final CascadeToken event,
                          final CascadeAllocator.OperandStack message)
    {
        return reactor.async(event, message);
    }

    @Override
    public boolean sync (final CascadeToken event,
                         final CascadeAllocator.OperandStack message,
                         final long timeout,
                         final TimeUnit timeoutUnits)
    {
        return reactor.sync(event, message, timeout, timeoutUnits);
    }

    @Override
    public void send (final CascadeToken event,
                      final CascadeAllocator.OperandStack message)
            throws SendFailureException
    {
        reactor.send(event, message);
    }

    @Override
    public int broadcast (final CascadeToken event,
                          final CascadeAllocator.OperandStack message)
    {
        return reactor.broadcast(event, message);
    }

    @Override
    public String toString ()
    {
        return reactor.toString();
    }

    private void detectMultipleThreads ()
    {
        if (user != Thread.currentThread())
        {
            throw new IllegalStateException("Thread Safety Violation: Context objects are *NOT* thread-safe.");
        }
    }
}
