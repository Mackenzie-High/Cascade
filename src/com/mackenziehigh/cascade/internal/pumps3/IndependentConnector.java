package com.mackenziehigh.cascade.internal.pumps3;

import com.google.common.base.Preconditions;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.internal.pumps3.Connector.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class implements the Connector interface,
 * such that each connection is based on an its
 * own fixed capacity queue that is independent
 * of the queues of other connections.
 */
public final class IndependentConnector
        implements Connector
{
    private final Connector SELF = this;

    // TODO: trimToSize
    private final List<Connection> connections = Collections.synchronizedList(new ArrayList<>());

    private final List<Connection> unmodConnections = Collections.unmodifiableList(connections);

    /**
     * This is the allocator that owns the operand-stacks
     * that will be passing through the connections.
     */
    private final CascadeAllocator allocator;

    private final int globalCapacity;

    private final BlockingQueue<IndependentConnection> globalQueue;

    public IndependentConnector (final CascadeAllocator allocator,
                                 final int[] localCapacity)
    {
        Preconditions.checkNotNull(allocator, "allocator");

        this.allocator = allocator;

        /**
         * Create the connections.
         */
        int globalCap = 0;
        for (int i = 0; i < localCapacity.length; i++)
        {
            globalCap += localCapacity[i];
            Preconditions.checkArgument(localCapacity[i] >= 0);
            final IndependentConnection connection = new IndependentConnection(i, localCapacity[i]);
            connections.add(connection);
        }

        this.globalCapacity = globalCap;
        this.globalQueue = new ArrayBlockingQueue<>(globalCapacity);
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
        return globalQueue.size();
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

    /**
     * Use this method in order to wait for incoming messages.
     *
     * <p>
     * This method will return an object exactly one time per message.
     * </p>
     *
     * @param timeout is the maximum amount of time to wait.
     * @param timeoutUnits describes the timeout.
     * @return a connection that has a message available.
     * @throws InterruptedException
     */
    public IndependentConnection poll (final long timeout,
                                       final TimeUnit timeoutUnits)
            throws InterruptedException
    {
        return globalQueue.poll(timeout, timeoutUnits);
    }

    public final class IndependentConnection
            implements Connection
    {

        private final int id;

        /**
         * This is simply used to help prevent bugs.
         */
        private final Object accessKey = new Object();

        private final LongSynchronizedQueue localQueue;

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

        private final Semaphore permits;

        /**
         * This lock must be held by whatever thread
         * that invokes the commit(*) method.
         */
        private final Lock transactionLock = new ReentrantLock();

        private IndependentConnection (final int id,
                                       final int capacity)
        {
            this.id = id;
            this.buffer = new OperandStackStorage(allocator, capacity);
            this.localQueue = new LongSynchronizedQueue(capacity);
            this.permits = new Semaphore(capacity);
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
            try
            {
                if (permits.tryAcquire(timeout, timeoutUnits))
                {
                    transactionLock.lock();
                    return accessKey;
                }
                else
                {
                    return null;
                }
            }
            catch (InterruptedException ex)
            {
                return null; // TODO: InterruptedException throw instead???
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object lock ()
        {
            if (permits.tryAcquire())
            {
                transactionLock.lock();
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
            if (accessKey.equals(key) == false)
            {
                throw new IllegalArgumentException("Wrong Key");
            }
            else
            {
                final int elementKey = buffer.set(message);
                localQueue.offer(elementKey);
                globalQueue.add(this);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void unlock (final Object key)
        {
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
                transactionLock.unlock();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int localSize ()
        {
            return localQueue.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int localCapacity ()
        {
            return localQueue.capacity();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close ()
        {
            buffer.close();
        }

        /**
         * Use this method in order to retrieve and remove
         * a message from this connection queue.
         *
         * <p>
         * You are responsible for ensuring that you
         * only invoke this method when a message
         * is actually available for you.
         * </p>
         *
         * @param out will receive the message.
         */
        public void poll (final OperandStack out)
        {
            Preconditions.checkNotNull(out);
            final int index = (int) localQueue.poll();
            buffer.get(index, out);
        }

    }
}
