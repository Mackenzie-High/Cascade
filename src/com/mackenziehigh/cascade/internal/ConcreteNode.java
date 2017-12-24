package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeAllocator.AllocationPool;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.internal.engines.Connection;
import com.mackenziehigh.cascade.internal.routing.EventDispatcher;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import com.mackenziehigh.cascade.CascadePump;
import com.mackenziehigh.cascade.CascadeReactor;
import com.mackenziehigh.cascade.CascadeProcessor;

/**
 *
 */
public final class ConcreteNode
        implements CascadeReactor
{
    private final CascadeReactor node = this;

    private final CascadeToken name;

    private final SharedState shared;

    private final Core core;

    private final LazyRef<Connection> connection;

    private final LazyRef<CascadePump> engine;

    private final LazyRef<AllocationPool> pool;

    private final LazyRef<CascadeLogger> logger;

    private final EventDispatcher.ConcurrentEventSender transmitter;

    public ConcreteNode (final SharedState shared,
                         final CascadeToken name,
                         final Core core)
    {
        this.shared = shared;
        this.name = name;
        this.core = core;
        this.connection = LazyRef.create(null);
        this.transmitter = shared.dispatcher.lookup(name);
        this.pool = LazyRef.create(() -> shared.nodesToPools.get(name));
        this.logger = LazyRef.create(() -> shared.nodesToLoggers.get(name));
        this.engine = LazyRef.create(() -> shared.nodesToEngines.get(name));
    }

    @Override
    public Context protoContext ()
    {
        return protoContext;
    }

    @Override
    public Core core ()
    {
        return core;
    }

    private final CascadeReactor.Context protoContext = new Context()
    {
        @Override
        public Cascade cascade ()
        {
            return shared.cascade;
        }

        @Override
        public CascadeLogger logger ()
        {
            return logger.get();
        }

        @Override
        public CascadeAllocator allocator ()
        {
            return shared.allocator;
        }

        @Override
        public CascadeAllocator.AllocationPool pool ()
        {
            return pool.get();
        }

        @Override
        public CascadePump engine ()
        {
            return engine.get();
        }

        @Override
        public CascadeReactor node ()
        {
            return node;
        }

        @Override
        public CascadeToken name ()
        {
            return name;
        }

        @Override
        public CascadeProcessor input ()
        {
            return edge;
        }

        @Override
        public CascadeToken event ()
        {
            return null; // Because, this object is the proto-context.
        }

        @Override
        public CascadeAllocator.OperandStack message ()
        {
            return null; // Because, this object is the proto-context.
        }

        @Override
        public Throwable exception ()
        {
            return null; // Because, this object is the proto-context.
        }

        @Override
        public Set<CascadeToken> subscriptions ()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Context subscribe (CascadeToken event)
        {
            shared.dispatcher.register(name, event);
            return this;
        }

        @Override
        public Context unsubscribe (CascadeToken event)
        {
            shared.dispatcher.deregister(name, event);
            return this;
        }

        @Override
        public boolean async (final CascadeToken event,
                              final CascadeAllocator.OperandStack message)
        {
            return transmitter.sendAsync(event, message);
        }

        @Override
        public boolean sync (final CascadeToken event,
                             final CascadeAllocator.OperandStack message,
                             final long timeout,
                             final TimeUnit timeoutUnits)
        {
            try
            {
                return transmitter.sendSync(event, message, timeout, timeoutUnits);
            }
            catch (InterruptedException ex)
            {
                return false; // TODO: Propagate???
            }
        }

        @Override
        public void send (CascadeToken event,
                          CascadeAllocator.OperandStack message)
                throws SendFailureException
        {
            while (shared.stop.get() == false)
            {
                if (sync(event, message, 1, TimeUnit.SECONDS))
                {
                    return;
                }
            }

            throw new SendFailureException(node());
        }

        @Override
        public int broadcast (final CascadeToken event,
                              final CascadeAllocator.OperandStack message)
        {
            return transmitter.broadcast(event, message);
        }
    };

    private final CascadeProcessor edge = new CascadeProcessor()
    {
        @Override
        public Cascade cascade ()
        {
            return shared.cascade;
        }

        @Override
        public CascadeProcessor.EdgeStats stats ()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CascadeReactor node ()
        {
            return node;
        }

        @Override
        public int backlogSize ()
        {
            return connection.get().globalSize();
        }

        @Override
        public int backlogCapacity ()
        {
            return connection.get().globalCapacity();
        }

        @Override
        public int queueSize ()
        {
            return connection.get().localSize();
        }

        @Override
        public int queueCapacity ()
        {
            return connection.get().localCapacity();
        }
    };
}
