package com.mackenziehigh.cascade.internal.pumps3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeAllocator.AllocationPool;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.internal.messages.ConcreteAllocator;
import com.mackenziehigh.cascade.internal.pumps3.BufferedConnector.BufferedConnection;
import com.mackenziehigh.cascade.internal.pumps3.Connector.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 *
 */
public final class ThreadedEngine
        implements Engine
{
    private final BufferedConnector connector;

    private final CascadeAllocator allocator;

    private final int minimumThreads;

    private final int maximumThreads;

    private final List<Consumer<OperandStack>> actions;

    private final Map<Connection, Consumer<OperandStack>> connections = new ConcurrentHashMap<>();

    private final Map<Connection, Consumer<OperandStack>> unmodConnections = Collections.unmodifiableMap(connections);

    private final Set<Thread> permanentThreads = Sets.newConcurrentHashSet();

    private final Set<Thread> threads = Sets.newConcurrentHashSet();

    private final Set<Thread> unmodThreads = Collections.unmodifiableSet(threads);

    private final AtomicBoolean started = new AtomicBoolean();

    private final AtomicInteger running = new AtomicInteger();

    private final AtomicBoolean stop = new AtomicBoolean();

    public ThreadedEngine (final CascadeAllocator allocator,
                           final int globalCapacity,
                           final int[] localCapacity,
                           final int minimumThreads,
                           final int maximumThreads,
                           final List<Consumer<OperandStack>> actions)
    {
        this.allocator = allocator;
        this.minimumThreads = minimumThreads;
        this.maximumThreads = maximumThreads;
        this.actions = new CopyOnWriteArrayList<>(actions);

        this.connector = new BufferedConnector(allocator, globalCapacity, localCapacity);

        for (int i = 0; i < actions.size(); i++)
        {
            final Connection connection = connector.connections().get(i);
            final Consumer<OperandStack> action = actions.get(i);
            connections.put(connection, action);
        }

        for (int i = 0; i < minimumThreads; i++)
        {
            final Thread thread = new Thread(() -> runTask());
            thread.setDaemon(true);
            permanentThreads.add(thread);
            threads.add(thread);
        }
    }

    public Set<Thread> threads ()
    {
        return unmodThreads;
    }

    @Override
    public Map<Connection, Consumer<OperandStack>> connections ()
    {
        return unmodConnections;
    }

    @Override
    public boolean isRunning ()
    {
        return running.get() != 0;
    }

    @Override
    public void start ()
    {
        if (started.getAndSet(true) == false)
        {
            permanentThreads.forEach(x -> x.start());
        }
    }

    @Override
    public void stop ()
    {
        stop.set(true);
    }

    private void runTask ()
    {
        running.incrementAndGet();

        int lastConnectionId = 0;

        try (OperandStack stack = allocator.newOperandStack())
        {
            while (stop.get() == false)
            {
                try
                {
                    final BufferedConnection connection = connector.roundRobinPoll(lastConnectionId, 1, TimeUnit.SECONDS);

                    if (connection == null)
                    {
                        continue;
                    }

                    lastConnectionId = connection.id();

                    final Consumer<OperandStack> action = actions.get(connection.id());

                    connection.poll(stack);

                    action.accept(stack);
                }
                catch (Throwable ex)
                {
                    ex.printStackTrace(System.out); // TODO. Also, what about InterruptedException.
                }
            }
        }
        finally
        {
            running.decrementAndGet();
        }
    }

    public static void main (String[] args)
            throws InterruptedException
    {
        final ConcreteAllocator alloc = new ConcreteAllocator();
        final AllocationPool pool = alloc.addFixedPool("default", 0, 128, 4);
        final OperandStack msg = alloc.newOperandStack();

        final int[] localCap = new int[1];
        localCap[0] = 128;

        final Consumer<OperandStack> action = x -> System.out.println("X = " + x.asString());

        final ThreadedEngine pump = new ThreadedEngine(alloc, 8, localCap, 1, 1, ImmutableList.of(action));
        pump.start();

        for (int i = 0; i < 16; i++)
        {
            msg.push("E #" + i);
            final Object key = pump.connector.connections().get(0).lockSync(1, TimeUnit.HOURS);
            pump.connector.connections().get(0).commit(key, msg);
            pump.connector.connections().get(0).unlock(key);
            msg.pop();
            Thread.sleep(1000);
        }
    }

}
