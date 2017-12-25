package com.mackenziehigh.cascade.internal.engines;

import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.internal.routing.LongSynchronizedQueue;
import com.mackenziehigh.cascade.internal.routing.OperandStackStorage;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Single Queue, Single Node, Single Thread.
 */
public final class DedicatedEngineWithArrayQueue
        implements Connection,
                   Engine
{
    private final int capacity;

    private final Semaphore producerPermits;

    private final Semaphore consumerPermits;

    private final Object transactionKey = new Object();

    private final ArrayBlockingQueue<CascadeToken> eventQueue;

    private final LongSynchronizedQueue messageQueue;

    private final OperandStackStorage messageStorage;

    /**
     * This lock must be held by whatever thread
     * that invokes the commit(*) method.
     */
    private final Lock transactionLock = new ReentrantLock();

    private final AtomicBoolean started = new AtomicBoolean();

    private final AtomicBoolean stop = new AtomicBoolean();

    private final AtomicBoolean running = new AtomicBoolean();

    private final Thread thread = new Thread(() -> runTask());

    private final CascadeAllocator allocator;

    private final EventConsumer consumer;

    public DedicatedEngineWithArrayQueue (final CascadeAllocator allocator,
                                          final int capacity,
                                          final EventConsumer consumer)
    {
        this.capacity = capacity;
        this.producerPermits = new Semaphore(capacity);
        this.consumerPermits = new Semaphore(capacity);
        this.consumerPermits.drainPermits();
        this.messageStorage = new OperandStackStorage(allocator, capacity);
        this.eventQueue = new ArrayBlockingQueue<>(capacity);
        this.messageQueue = new LongSynchronizedQueue(capacity);
        this.allocator = allocator;
        this.consumer = consumer;
    }

    @Override
    public void start ()
    {
        if (started.compareAndSet(false, true))
        {
            thread.start();
        }
    }

    @Override
    public void stop ()
    {
        stop.set(true);
    }

    @Override
    public Object lock (final long timeout,
                        final TimeUnit timeoutUnits)
    {
        try
        {
            if (producerPermits.tryAcquire(timeout, timeoutUnits))
            {
                transactionLock.lock();
                return transactionKey;
            }
            else
            {
                return null;
            }
        }
        catch (InterruptedException ex)
        {
            return null; // TODO: Propagate?????
        }
    }

    @Override
    public Object lock ()
    {
        if (producerPermits.tryAcquire())
        {
            transactionLock.lock();
            return transactionKey;
        }
        else
        {
            return null;
        }
    }

    @Override
    public void commit (final Object key,
                        final CascadeToken event,
                        final CascadeAllocator.OperandStack message)
    {
        if (key == transactionKey)
        {
            eventQueue.offer(event);
            final int pos = messageStorage.set(message);
            messageQueue.offer(pos);
            consumerPermits.release();
        }
    }

    @Override
    public void unlock (Object key)
    {
        if (key == transactionKey)
        {
            transactionLock.unlock();
        }
    }

    @Override
    public int localSize ()
    {
        return eventQueue.size();
    }

    @Override
    public int localCapacity ()
    {
        return capacity;
    }

    @Override
    public int globalSize ()
    {
        return localSize();
    }

    @Override
    public int globalCapacity ()
    {
        return localCapacity();
    }

    @Override
    public void close ()
    {
        messageStorage.close();
    }

    private void runTask ()
    {
        running.set(true);

        try (OperandStack stack = allocator.newOperandStack())
        {
            while (stop.get() == false)
            {
                try
                {
                    if (consumerPermits.tryAcquire(1, TimeUnit.SECONDS) == false)
                    {
                        continue;
                    }

                    /**
                     * Get the event and message from the queue.
                     */
                    final CascadeToken eventId = eventQueue.poll();
                    final int messageIdx = (int) messageQueue.poll();
                    messageStorage.get(messageIdx, stack);

                    /**
                     * Since we removed an event/message from the queue,
                     * there is now more room for producer thread(s)
                     * to insert events/messages into the queue.
                     */
                    producerPermits.release();

                    /**
                     * Execute the user-defined response to the event.
                     */
                    consumer.onMessage(eventId, stack);
                }
                catch (Throwable ex1)
                {
                    try
                    {
                        assert consumer != null;
                        consumer.onException(ex1);
                    }
                    catch (Throwable ex2)
                    {
                        // TODO: Pass??
                    }
                }
            }
        }
        finally
        {
            running.set(false);
        }
    }
}
