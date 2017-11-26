package com.mackenziehigh.cascade.internal.pumps3;

import com.google.common.base.Preconditions;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.internal.pumps3.LongTransactionalMultiQueue.TransactionQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class implements the Connector interface
 * on top of an underlying queue data-structure.
 *
 * <p>
 * The underlying queue is of fixed finite capacity.
 * The individual connections are still allowed to
 * have there own capacity, which must be within
 * the capacity of the underlying queue.
 * That being said, the sum of the capacities of
 * the individual connections is allowed to exceed
 * the capacity of the underlying queue; however,
 * the combined sizes of all the connections
 * can (obviously) never exceed the capacity
 * of the underlying queue.
 * </p>
 *
 * <p>
 * This class also provides the ability for consumer
 * threads to wait for messages to be added to any
 * of the connections. If more than one connection
 * has messages available for the consumer queues,
 * then the consumers will receive the messages
 * in a round-robin fashion in order to help
 * alleviate the risk of thread-starvation.
 * </p>
 */
public final class BufferedConnector
        implements Connector
{
    // TODO: trimToSize
    private final List<Connection> connections = Collections.synchronizedList(new ArrayList<>());

    private final List<Connection> unmodConnections = Collections.unmodifiableList(connections);

    /**
     * This is the underlying queue that this queue is based upon.
     */
    private final LongTransactionalMultiQueue globalQueue;

    /**
     * This is the allocator that owns the operand-stacks
     * that will be passing through the connections.
     */
    private final CascadeAllocator allocator;

    private final RoundRobinBarrier<BufferedConnection> robinBarrier;

    public BufferedConnector (final CascadeAllocator allocator,
                              final int globalCapacity,
                              final int[] localCapacity)
    {
        Preconditions.checkNotNull(allocator, "allocator");
        Preconditions.checkArgument(globalCapacity > 0, "globalCapacity <= 0");

        this.allocator = allocator;

        this.globalQueue = new LongTransactionalMultiQueue(globalCapacity, localCapacity);

        /**
         * Create the connections.
         */
        for (int i = 0; i < localCapacity.length; i++)
        {
            final TransactionQueue queue = globalQueue.members().get(i);
            final BufferedConnection connection = new BufferedConnection(i, queue);
            connections.add(connection);
        }

        this.robinBarrier = new RoundRobinBarrier(connections);
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
        return globalQueue.globalSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int globalCapacity ()
    {
        return globalQueue.globalCapacity();
    }

    /**
     * {@inheritDoc}
     */
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

        /**
         * This is simply used to help prevent bugs.
         */
        private final Object accessKey = new Object();

        /**
         * This queue stores numeric references to
         * the messages that are currently enqueued
         * inside of this buffered connection.
         * In other words, this buffered-connection
         * is simply a facade around this local-queue.
         */
        private final TransactionQueue localQueue;

        /**
         * This will be used to store the operand-stacks,
         * since the local-queue can only store values
         * of type primitive-long. This object will provide
         * us with a unique primitive-int key for each
         * operand-stack that we have stored herein.
         * We can then use the key in the local-queue
         * and simply retrieve the operand-stack from
         * this buffer whenever we remove the key from
         * the local-queue.
         */
        private final OperandStackStorage buffer;

        /**
         * This flag is true, if commit() was called,
         * but unlock() has not been called since then.
         * This allows us to detect whether a rollback
         * must be performed on the local-queue.
         */
        private final AtomicBoolean commitStatus = new AtomicBoolean();

        private BufferedConnection (final int id,
                                    final TransactionQueue queue)
        {
            this.id = id;
            this.buffer = new OperandStackStorage(allocator, queue.localCapacity());
            this.localQueue = queue;
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
            return localQueue.begin(timeout, timeoutUnits) ? accessKey : null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object lock ()
        {
            return localQueue.begin() ? accessKey : null;
        }

        /**
         * {@inheritDoc}
         */
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

        /**
         * {@inheritDoc}
         */
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

        /**
         * {@inheritDoc}
         */
        @Override
        public int localSize ()
        {
            return localQueue.localSize();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int localCapacity ()
        {
            return localQueue.localCapacity();
        }

        /**
         * {@inheritDoc}
         */
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
