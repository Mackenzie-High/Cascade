package com.mackenziehigh.cascade.internal.pumps;

import com.google.common.base.Preconditions;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * For use by Direct Pumps
 */
public final class DirectConnector
        implements Connector
{
    private final Connector SELF = this;

    private final List<Connection> connections = Collections.synchronizedList(new ArrayList<>());

    private final List<Connection> unmodConnections = Collections.unmodifiableList(connections);

    private final AtomicInteger globalCapacity = new AtomicInteger();

    private final AtomicInteger globalSize = new AtomicInteger();

    public Connection add (final Consumer<OperandStack> action,
                           final int permits)
    {
        globalCapacity.addAndGet(permits);
        final int id = connections.size();
        final Connection result = new DirectConnection(id, permits, action);
        connections.add(result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Connection> connections ()
    {
        return unmodConnections;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int globalSize ()
    {
        return globalSize.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int globalCapacity ()
    {
        return globalCapacity.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close ()
    {
        connections().forEach(x -> x.close());
    }

    private final class DirectConnection
            implements Connection
    {

        private final int id;

        private final Semaphore permits;

        private final int localCapacity;

        private final AtomicInteger localSize = new AtomicInteger();

        private final Consumer<OperandStack> action;

        private final Object accessKey = new Object();

        private final AtomicBoolean open = new AtomicBoolean(true);

        public DirectConnection (final int id,
                                 final int capacity,
                                 final Consumer<OperandStack> action)
        {
            this.id = id;
            this.localCapacity = capacity;
            this.permits = new Semaphore(capacity);
            this.action = action;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Connector parent ()
        {
            return SELF;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int id ()
        {
            return id;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object lock (final long timeout,
                            final TimeUnit timeoutUnits)
        {
            Preconditions.checkState(open.get(), "closed");

            boolean acquired;

            try
            {
                acquired = permits.tryAcquire(timeout, timeoutUnits);
            }
            catch (InterruptedException ex)
            {
                acquired = false;
            }

            if (acquired)
            {
                return accessKey;
            }
            else
            {
                return null;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object lock ()
        {
            Preconditions.checkState(open.get(), "closed");

            final boolean acquired = permits.tryAcquire();

            if (acquired)
            {
                return accessKey;
            }
            else
            {
                return null;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void commit (final Object key,
                            final OperandStack message)
        {
            Preconditions.checkState(open.get(), "closed");

            if (accessKey.equals(key) == false)
            {
                throw new IllegalArgumentException("Wrong Key");
            }
            else
            {
                globalSize.incrementAndGet();
                localSize.incrementAndGet();
                {
                    try
                    {
                        action.accept(message);
                    }
                    catch (Throwable ex)
                    {
                        ex.printStackTrace(System.out);
                    }
                }
                globalSize.decrementAndGet();
                localSize.decrementAndGet();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void unlock (Object key)
        {
            Preconditions.checkState(open.get(), "closed");

            if (key == null)
            {
                // Pass, per method contract.
            }
            else if (accessKey.equals(key) == false)
            {
                throw new IllegalArgumentException("Wrong Key");
            }
            else
            {
                permits.release();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int localSize ()
        {
            return localSize.get();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int localCapacity ()
        {
            return localCapacity;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close ()
        {
            open.set(false);
        }
    }
}
