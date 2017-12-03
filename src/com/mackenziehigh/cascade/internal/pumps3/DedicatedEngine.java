package com.mackenziehigh.cascade.internal.pumps3;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.internal.pumps3.Connector.Connection;
import com.mackenziehigh.cascade.internal.pumps3.IndependentConnector.IndependentConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class implements the Engine interface using
 * a single dedicated thread for message processing.
 */
public final class DedicatedEngine
        implements Engine
{
    private final IndependentConnector connector;

    private final CascadeAllocator allocator;

    private final List<MessageConsumer> actions;

    private final Map<ConnectionSchema, Connection> connections = new ConcurrentHashMap<>();

    private final Map<ConnectionSchema, Connection> unmodConnections = Collections.unmodifiableMap(connections);

    private final Thread thread;

    private final AtomicBoolean started = new AtomicBoolean();

    private final AtomicBoolean running = new AtomicBoolean();

    private final AtomicBoolean stop = new AtomicBoolean();

    public DedicatedEngine (final ThreadFactory threadFactory,
                            final CascadeAllocator allocator,
                            final List<ConnectionSchema> inputs)
    {
        this.allocator = allocator;
        this.thread = threadFactory.newThread(() -> runTask());

        final List<Integer> localCaps = new ArrayList<>();
        final List<MessageConsumer> consumers = new ArrayList<>();

        for (ConnectionSchema input : inputs)
        {
            Preconditions.checkArgument(input.consumer.concurrentLimit() >= 1);
            consumers.add(input.consumer);
            localCaps.add(input.capacity);
        }

        this.actions = ImmutableList.copyOf(consumers);
        this.connector = new IndependentConnector(allocator, localCaps);

        for (int i = 0; i < actions.size(); i++)
        {
            final ConnectionSchema schema = inputs.get(i);
            final Connection connection = connector.connections().get(i);
            connections.put(schema, connection);
        }
    }

    @Override
    public Set<Thread> threads ()
    {
        return ImmutableSet.of(thread);
    }

    @Override
    public Map<ConnectionSchema, Connection> connections ()
    {
        return unmodConnections;
    }

    @Override
    public boolean isRunning ()
    {
        return running.get();
    }

    @Override
    public void start ()
    {
        if (started.getAndSet(true) == false)
        {
            thread.start();
        }
    }

    @Override
    public void stop ()
    {
        stop.set(true);
    }

    private void runTask ()
    {
        running.set(true);

        MessageConsumer consumer = null;

        try (OperandStack stack = allocator.newOperandStack())
        {
            while (stop.get() == false)
            {
                try
                {
                    final IndependentConnection connection = connector.poll(1, TimeUnit.SECONDS);

                    if (connection == null)
                    {
                        continue;
                    }

                    consumer = actions.get(connection.id());

                    connection.poll(stack);

                    consumer.accept(stack);
                }
                catch (Throwable ex)
                {
                    if (consumer == null)
                    {
                        ex.printStackTrace(System.out); // TODO. Also, what about InterruptedException.
                    }
                    else
                    {
                        consumer.handle(ex);
                    }
                }
                finally
                {
                    consumer = null;
                }
            }
        }
        finally
        {
            running.set(false);
        }
    }

    public static void main (String[] args)
            throws InterruptedException
    {
//        final ConcreteAllocator alloc = new ConcreteAllocator();
//        final CascadeAllocator.AllocationPool pool = alloc.addFixedPool("default", 0, 128, 100);
//        final CascadeAllocator.OperandStack msg = alloc.newOperandStack();
//
//        final int[] localCap = new int[1];
//        localCap[0] = 128;
//
//        final MessageConsumer action = new MessageConsumer()
//        {
//            @Override
//            public void accept (OperandStack message)
//            {
////                throw new Error();
//                System.out.println("X = #" + message.asString() + ", Thread = " + Thread.currentThread().getId());
//            }
//
//            @Override
//            public void handle (Throwable exception)
//            {
//                System.out.println("Error: " + exception.getClass());
//            }
//
//            @Override
//            public int concurrentLimit ()
//            {
//                return 1;
//            }
//        };
//
//        final ThreadFactory threadFactory = new ThreadFactoryBuilder().build();
//        final DedicatedEngine pump = new DedicatedEngine(threadFactory, alloc, localCap, ImmutableList.of(action));
//        pump.start();
//
//        final OrderlyAtomicSender sender = new OrderlyAtomicSender(Lists.newArrayList(pump.connections.keySet()));
//
//        for (int i = 0; i < 16; i++)
//        {
//            msg.push("E #" + i);
//            sender.sendAsync(msg);
//            msg.pop();
//            Thread.sleep(1000);
//        }

    }
}
