//package com.mackenziehigh.cascade.internal.pumps3;
//
//import com.google.common.base.Preconditions;
//import com.google.common.collect.ImmutableList;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Sets;
//import com.mackenziehigh.cascade.CascadeAllocator;
//import com.mackenziehigh.cascade.CascadeAllocator.AllocationPool;
//import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
//import com.mackenziehigh.cascade.internal.messages.ConcreteAllocator;
//import com.mackenziehigh.cascade.internal.pumps3.BufferedConnector.BufferedConnection;
//import com.mackenziehigh.cascade.internal.pumps3.Connector.Connection;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.CopyOnWriteArrayList;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicInteger;
//
///**
// *
// */
//public final class ThreadedEngine
//        implements Engine
//{
//    private final BufferedConnector connector;
//
//    private final CascadeAllocator allocator;
//
//    private final int minimumThreads;
//
//    private final int maximumThreads;
//
//    private final List<MessageConsumer> actions;
//
//    private final Map<ConnectionSchema, Connection> connections = new ConcurrentHashMap<>();
//
//    private final Map<ConnectionSchema, Connection> unmodConnections = Collections.unmodifiableMap(connections);
//
//    private final Set<Thread> permanentThreads = Sets.newConcurrentHashSet();
//
//    private final Set<Thread> threads = Sets.newConcurrentHashSet();
//
//    private final Set<Thread> unmodThreads = Collections.unmodifiableSet(threads);
//
//    private final AtomicBoolean started = new AtomicBoolean();
//
//    private final AtomicInteger running = new AtomicInteger();
//
//    private final AtomicBoolean stop = new AtomicBoolean();
//
//    public ThreadedEngine (final CascadeAllocator allocator,
//                           final int globalCapacity,
//                           final int minimumThreads,
//                           final int maximumThreads,
//                           final List<ConnectionSchema> inputs)
//    {
//        this.allocator = allocator;
//        this.minimumThreads = minimumThreads;
//        this.maximumThreads = maximumThreads;
//
//        this.connector = new BufferedConnector(allocator, globalCapacity, localCapacity);
//
//        for (int i = 0; i < actions.size(); i++)
//        {
//            final Connection connection = connector.connections().get(i);
//            final MessageConsumer action = actions.get(i);
//            Preconditions.checkArgument(action.concurrentLimit() == 1);
//            connections.put(connection);
//        }
//
//        for (int i = 0; i < minimumThreads; i++)
//        {
//            final Thread thread = new Thread(() -> runTask());
//            thread.setDaemon(true);
//            permanentThreads.add(thread);
//            threads.add(thread);
//        }
//    }
//
//    @Override
//    public Set<Thread> threads ()
//    {
//        return unmodThreads;
//    }
//
//    @Override
//    public Map<ConnectionSchema, Connection> connections ()
//    {
//        return unmodConnections;
//    }
//
//    @Override
//    public boolean isRunning ()
//    {
//        return running.get() != 0;
//    }
//
//    @Override
//    public void start ()
//    {
//        if (started.getAndSet(true) == false)
//        {
//            permanentThreads.forEach(x -> x.start());
//        }
//    }
//
//    @Override
//    public void stop ()
//    {
//        stop.set(true);
//    }
//
//    private void runTask ()
//    {
//        running.incrementAndGet();
//
//        int lastConnectionId = 0;
//        MessageConsumer consumer = null;
//
//        try (OperandStack stack = allocator.newOperandStack())
//        {
//            while (stop.get() == false)
//            {
//                try
//                {
//                    final BufferedConnection connection = connector.roundRobinPoll(lastConnectionId, 1, TimeUnit.SECONDS);
//
//                    if (connection == null)
//                    {
//                        continue;
//                    }
//
//                    lastConnectionId = connection.id();
//
//                    consumer = actions.get(connection.id());
//
//                    connection.poll(stack);
//
//                    consumer.accept(stack);
//                }
//                catch (Throwable ex)
//                {
//                    if (consumer == null)
//                    {
//                        ex.printStackTrace(System.out); // TODO. Also, what about InterruptedException.
//                    }
//                    else
//                    {
//                        consumer.handle(ex);
//                    }
//                }
//                finally
//                {
//                    consumer = null;
//                }
//            }
//        }
//        finally
//        {
//            running.decrementAndGet();
//        }
//    }
//
//    public static void main (String[] args)
//            throws InterruptedException
//    {
//        final ConcreteAllocator alloc = new ConcreteAllocator();
//        final AllocationPool pool = alloc.addFixedPool("default", 0, 128, 100);
//        final OperandStack msg = alloc.newOperandStack();
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
//        final ThreadedEngine pump = new ThreadedEngine(alloc, 8, localCap, 1, 1, ImmutableList.of(action));
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
//
//    }
//
//}
