package com.mackenziehigh.cascade.internal;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.CascadePump;
import com.mackenziehigh.cascade.CascadeReactor;
import com.mackenziehigh.cascade.CascadeReactor.Core;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.internal.Scheduler.TaskStream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public final class ConcretePump
        implements CascadePump
{
    private final class ReactorInfo
    {
        public ConcreteReactor reactor;

        public ConcreteContext context;

        public InflowQueue pipeline;

        public TaskStream<ReactorInfo> stream;
    }

    private final ConcreteCascade cascade;

    private final CascadeToken name;

    private final ImmutableSet<Thread> threads;

    private final AtomicBoolean stop = new AtomicBoolean();

    private final ImmutableSet<CascadeReactor> reactors;

    private final Scheduler<ReactorInfo> scheduler;

    private final List<ReactorInfo> reactorInfos = Lists.newLinkedList();

    public ConcretePump (final ConcreteCascade cascade,
                         final CascadeToken name,
                         final Collection<ConcreteReactor> reactors,
                         final ThreadFactory threadFactory,
                         final int threadCount)
    {
        this.cascade = Objects.requireNonNull(cascade);
        this.name = Objects.requireNonNull(name);
        this.reactors = ImmutableSet.copyOf(reactors);

        /**
         * Create the consumer threads.
         */
        final Set<Thread> threadsSet = Sets.newHashSet();
        for (int i = 0; i < threadCount; i++)
        {
            threadsSet.add(threadFactory.newThread(() -> runTask()));
        }
        this.threads = ImmutableSet.copyOf(threadsSet);

        /**
         * Create the scheduler.
         */
        for (ConcreteReactor reactor : reactors)
        {
            final ReactorInfo info = new ReactorInfo();
            info.reactor = reactor;
            info.pipeline = reactor.input();
            info.context = new ConcreteContext(reactor);
            reactorInfos.add(info);
        }
        this.scheduler = new RoundRobinScheduler<>(reactorInfos);
        for (ReactorInfo info : scheduler.streams().keySet())
        {
            info.stream = scheduler.streams().get(info);
            info.pipeline.setCallback(x -> scheduler.addTask(info.stream));
            Verify.verifyNotNull(info.reactor);
            Verify.verifyNotNull(info.pipeline);
            Verify.verifyNotNull(info.context);
            Verify.verifyNotNull(info.stream);
        }
    }

    /**
     * Invariant Checking.
     */
    public void selfTest ()
    {
        Verify.verify(name().toString().equals(toString()));
        Verify.verify(cascade().pumps().get(name()).equals(this));

        for (CascadeReactor reactor : reactors())
        {
            Verify.verify(reactor.pump().equals(this));
            Verify.verify(reactor.cascade().equals(cascade()));
            Verify.verify(cascade().reactors().get(reactor.name()).equals(reactor));
        }
    }

    @Override
    public Cascade cascade ()
    {
        return cascade;
    }

    @Override
    public CascadeToken name ()
    {
        return name;
    }

    @Override
    public Set<Thread> threads ()
    {
        return threads;
    }

    @Override
    public Set<CascadeReactor> reactors ()
    {
        return reactors;
    }

    @Override
    public String toString ()
    {
        return name.name();
    }

    public void start ()
    {
        threads.forEach(x -> x.start());
    }

    public void stop ()
    {
        stop.set(true);
    }

    private void runTask ()
    {
        final Thread currentThread = Thread.currentThread();

        // TODO: try (OperandStack stack = new CheckedOperandStack(currentThread, cascade.allocator().newOperandStack()))
        try (OperandStack stack = cascade.allocator().newOperandStack())
        {
            TaskStream<ReactorInfo> taskStream = null;

            while (stop.get() == false)
            {
                try
                {
                    try
                    {
                        taskStream = scheduler.pollTask(1, TimeUnit.SECONDS);
                    }
                    catch (InterruptedException ex)
                    {
                        Thread.currentThread().interrupt();
                        continue;
                    }

                    if (taskStream == null)
                    {
                        continue;
                    }

                    final CascadeToken event = taskStream.source().pipeline.poll(stack);
                    final ConcreteContext context = taskStream.source().context;
                    final Core core = taskStream.source().reactor.core();

                    Verify.verifyNotNull(event);
                    Verify.verifyNotNull(context);
                    Verify.verifyNotNull(core);

                    try
                    {
                        taskStream.source().reactor.enterCore();
                        context.set(currentThread, event, stack, null);
                        core.onMessage(context);
                    }
                    catch (Throwable ex1)
                    {
                        try
                        {
                            context.set(currentThread, event, stack, ex1);
                            core.onException(context);
                        }
                        catch (Throwable ex2)
                        {
                            try
                            {
                                context.logger().warn(ex2);
                            }
                            catch (Throwable ex3)
                            {
                                // Pass
                            }
                        }
                    }
                    finally
                    {
                        context.set(currentThread, null, null, null);
                        taskStream.source().reactor.exitCore();
                    }
                }
                finally
                {
                    if (taskStream != null)
                    {
                        taskStream.release();
                    }
                }
            }
        }
    }

}
