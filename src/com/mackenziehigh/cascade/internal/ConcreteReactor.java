package com.mackenziehigh.cascade.internal;

import com.google.common.base.Verify;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeAllocator.AllocationPool;
import com.mackenziehigh.cascade.redo2.CascadeLogger;
import com.mackenziehigh.cascade.CascadePump;
import com.mackenziehigh.cascade.CascadeReactor;
import com.mackenziehigh.cascade.redo2.CascadeToken;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

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

    private final CascadeToken pump;

    private final CascadeLogger logger;

    private final InflowQueue input;

    private final EventDispatcher dispatcher;

    private final EventDispatcher.ConcurrentEventSender sender;

    private final AtomicLong eventCounter = new AtomicLong();

    /**
     * This flag is used as a sanity check to ensure that the core()
     * is not being executed by two pump threads simultaneously.
     */
    private final AtomicBoolean coreLock = new AtomicBoolean();

    public ConcreteReactor (final ConcreteCascade cascade,
                            final CascadeToken name,
                            final Core core,
                            final AllocationPool pool,
                            final CascadeToken pump,
                            final CascadeLogger logger,
                            final InflowQueue input,
                            final EventDispatcher dispatcher)
    {
        this.cascade = Objects.requireNonNull(cascade);
        this.name = Objects.requireNonNull(name);
        this.core = Objects.requireNonNull(core);
        this.dispatcher = Objects.requireNonNull(dispatcher);
        this.sender = Objects.requireNonNull(dispatcher.lookup(name));
        this.pool = Objects.requireNonNull(pool);
        this.pump = Objects.requireNonNull(pump);
        this.logger = Objects.requireNonNull(logger);
        this.input = Objects.requireNonNull(input);
    }

    /**
     * Invariant Checking.
     */
    public void selfTest ()
    {
        Verify.verifyNotNull(pump());
        Verify.verify(name().toString().equals(toString()));
        Verify.verify(cascade().allocator().equals(allocator()));
        Verify.verify(cascade().phase().equals(Cascade.ExecutionPhase.INITIAL));
        Verify.verify(cascade().pumps().get(pump().name()).equals(pump()));
        Verify.verify(cascade().reactors().get(name()).equals(this));
        Verify.verify(allocator().equals(cascade().allocator()));
        Verify.verify(allocator().pools().get(pool().name()).equals(pool()));
        Verify.verify(allocator().cascade().equals(cascade()));
        Verify.verify(pump().cascade().equals(cascade()));
        Verify.verify(pump().reactors().contains(this));
        Verify.verify(pool().allocator().equals(allocator()));
    }

    public InflowQueue input ()
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
        return cascade.pumps().get(pump);
    }

    @Override
    public int queueSize ()
    {
        return input.size();
    }

    @Override
    public int queueCapacity ()
    {
        return input.capacity();
    }

    @Override
    public long eventCount ()
    {
        return eventCounter.get();
    }

    @Override
    public Set<CascadeToken> subscriptions ()
    {
        return dispatcher.subscriptionsOf(name);
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
        // TODO: Implement This!!!!
//        return sender.sendSync(event, message, timeout, timeoutUnits);
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
    public void subscribe (final CascadeToken event)
    {
        dispatcher.register(name(), event);
    }

    @Override
    public void unsubscribe (final CascadeToken event)
    {
        dispatcher.deregister(name(), event);
    }

    @Override
    public String toString ()
    {
        return name.name();
    }

    public void enterCore ()
    {
        Verify.verify(coreLock.compareAndSet(false, true),
                      "core() is already being executed!");

        eventCounter.incrementAndGet();
    }

    public void exitCore ()
    {
        coreLock.set(false);
    }
}
