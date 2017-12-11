package com.mackenziehigh.cascade.internal.pumps;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
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

    private final int globalCapacity;

    private final AtomicInteger globalSize = new AtomicInteger();

    private final Semaphore globalPermits;

    private final Consumer<OperandStack> action;

    public DirectConnector (final Consumer<OperandStack> action,
                            final int concurrentLimit)
    {
        this.globalCapacity = concurrentLimit;
        this.globalPermits = new Semaphore(concurrentLimit);
        this.action = action;
    }

    public Connection addConnection (final int capacity)
    {
        final int id = connections.size();
        final Connection result = new DirectConnection(id, capacity);
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
        return globalCapacity;
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

        private final Semaphore localPermits;

        private final SemaphoreSeries transactionLock;

        private final int localCapacity;

        private final AtomicInteger localSize = new AtomicInteger();

        private final Object accessKey = new Object();

        private final AtomicBoolean open = new AtomicBoolean(true);

        public DirectConnection (final int id,
                                 final int capacity)
        {
            this.id = id;
            this.localCapacity = capacity;
            this.localPermits = new Semaphore(capacity);
            this.transactionLock = new SemaphoreSeries(ImmutableList.of(localPermits, globalPermits));
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

            try
            {
                final boolean acquired = transactionLock.tryAcquire(timeout, timeoutUnits);
                return acquired ? accessKey : null;
            }
            catch (InterruptedException ex)
            {
                return null; // TODO: Propagate instead????
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object lock ()
        {
            Preconditions.checkState(open.get(), "closed");

            final boolean acquired = transactionLock.tryAcquire();
            return acquired ? accessKey : null;
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

                try
                {
                    action.accept(message);
                }
                finally
                {
                    globalSize.decrementAndGet();
                    localSize.decrementAndGet();
                }
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
                transactionLock.release();
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
