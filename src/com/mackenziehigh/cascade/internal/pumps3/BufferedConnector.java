package com.mackenziehigh.cascade.internal.pumps3;

import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.internal.pumps3.LongTransactionalMultiQueue.TransactionQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class BufferedConnector
        implements Connector
{
    // TODO: trimToSize
    private final List<Connection> connections = Collections.synchronizedList(new ArrayList<>());

    private final List<Connection> unmodConnections = Collections.unmodifiableList(connections);

    private final LongTransactionalMultiQueue globalQueue;

    private final CascadeAllocator allocator;

    private final RoundRobinBarrier<BufferedConnection> robinBarrier;

    public BufferedConnector (final CascadeAllocator allocator,
                              final int globalCapacity,
                              final int[] localCapacity)
    {
        this.allocator = allocator;

        this.globalQueue = new LongTransactionalMultiQueue(globalCapacity, localCapacity);

        for (int i = 0; i < localCapacity.length; i++)
        {
            final TransactionQueue queue = globalQueue.members().get(i);
            final BufferedConnection connection = new BufferedConnection(i, queue);
            connections.add(connection);
        }

        this.robinBarrier = new RoundRobinBarrier(connections);
    }

    @Override
    public List<Connection> connections ()
    {
        return unmodConnections;
    }

    @Override
    public int globalSize ()
    {
        return globalQueue.globalSize();
    }

    @Override
    public int globalCapacity ()
    {
        return globalQueue.globalCapacity();
    }

    @Override
    public void close ()
    {
        for (Connection x : connections)
        {
            try
            {
                x.close();
            }
            catch (Throwable ex)
            {
                // TODO?????
            }
        }
    }

    public BufferedConnection roundRobinPoll (final int id,
                                              final long timeout,
                                              final TimeUnit timeoutUnits)
            throws InterruptedException
    {
        return robinBarrier.select(id, timeout, timeoutUnits);
    }

    public final class BufferedConnection
            implements Connector.Connection
    {

        private final int id;

        private final Object accessKey = new Object();

        private final OperandStackStorage buffer;

        private final TransactionQueue localQueue;

        private final AtomicBoolean commitStatus = new AtomicBoolean();

        public BufferedConnection (final int id,
                                   final TransactionQueue queue)
        {
            this.id = id;
            this.buffer = new OperandStackStorage(allocator, queue.localCapacity());
            this.localQueue = queue;
        }

        @Override
        public int id ()
        {
            return id;
        }

        @Override
        public Object lockSync (final long timeout,
                                final TimeUnit timeoutUnits)
        {
            return localQueue.begin(timeout, timeoutUnits) ? accessKey : null;
        }

        @Override
        public Object lockAsync ()
        {
            return localQueue.begin() ? accessKey : null;
        }

        @Override
        public void commit (final Object key,
                            final OperandStack stack)
        {
            if (accessKey.equals(key) == false)
            {
                throw new IllegalArgumentException("Wrong Key");
            }
            else
            {
                final int elementKey = buffer.set(stack);
                localQueue.commit(elementKey);
                robinBarrier.increment(id);
                commitStatus.set(true);
            }
        }

        @Override
        public void unlock (final Object key)
        {
            final boolean commitPerformed = commitStatus.getAndSet(false);

            if (key == null)
            {
                // Pass, per method contract.
            }
            else if (accessKey.equals(key) == false)
            {
                throw new IllegalArgumentException("Wrong Key");
            }
            else if (commitPerformed)
            {
                // Nothing needs to be done.
            }
            else
            {
                localQueue.rollback();
            }
        }

        @Override
        public int localSize ()
        {
            return localQueue.localSize();
        }

        @Override
        public int localCapacity ()
        {
            return localQueue.localCapacity();
        }

        @Override
        public void close ()
        {
            buffer.close();
        }

        public void poll (final OperandStack out)
        {
            final int index = (int) localQueue.poll();
            buffer.get(index, out);
        }
    }
}
