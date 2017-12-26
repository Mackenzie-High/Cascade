package com.mackenziehigh.cascade.internal.schema;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.CascadePump;
import com.mackenziehigh.cascade.CascadeReactor;
import com.mackenziehigh.cascade.CascadeReactor.Core;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public final class ConcretePump
        implements CascadePump
{
    private final ConcreteCascade cascade;

    private final CascadeToken name;

    private final ImmutableSet<Thread> threads;

    private final AtomicBoolean stop = new AtomicBoolean();

    private final Set<ConcreteReactor> reactors = Sets.newConcurrentHashSet();

    private final Set<CascadeReactor> unmodReactors = Collections.unmodifiableSet(reactors);

    private final Semaphore consumerPermits;

    public ConcretePump (final ConcreteCascade cascade,
                         final CascadeToken name,
                         final ThreadFactory threadFactory,
                         final int threadCount)
    {
        this.cascade = Objects.requireNonNull(cascade);
        this.name = Objects.requireNonNull(name);

        final Set<Thread> threadsSet = Sets.newHashSet();
        for (int i = 0; i < threadCount; i++)
        {
            threadsSet.add(threadFactory.newThread(() -> runTask()));
        }
        this.threads = ImmutableSet.copyOf(threadsSet);

        this.consumerPermits = new Semaphore(Integer.MAX_VALUE);
        this.consumerPermits.drainPermits();
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
    public int minimumThreads ()
    {
        return maximumThreads();
    }

    @Override
    public int maximumThreads ()
    {
        return threads.size();
    }

    @Override
    public Set<Thread> threads ()
    {
        return threads;
    }

    @Override
    public Set<CascadeReactor> reactors ()
    {
        return unmodReactors;
    }

    @Override
    public String toString ()
    {
        return name.name();
    }

    public void addReactor (final ConcreteReactor reactor)
    {
        reactors.add(reactor);
        reactor.input().setCallback(x -> consumerPermits.release());
    }

    public void start ()
    {
        threads.forEach(x -> x.start());
    }

    private void runTask ()
    {
        final OperandStack stack = cascade.allocator().newOperandStack();

        final Iterable<ConcreteReactor> cycle = Iterables.cycle(reactors);

        while (stop.get() == false)
        {
            consumerPermits.acquireUninterruptibly();

            CascadeToken event = null;
            ConcreteContext context = null;

            for (ConcreteReactor x : cycle)
            {
                event = x.input().poll(stack);

                if (event != null)
                {
                    context = new ConcreteContext(x);
                    break;
                }
            }

            if (context == null)
            {
                continue;
            }

//            final ConcreteReactor reactor = (ConcreteReactor) context.reactor();
//
//            final Connection queue = reactor.input();
//
//            final CascadeToken event = queue.poll(stack);
            if (event == null)
            {
                continue;
            }

            final Core core = context.reactor().core();

            try
            {
                context.set(event, stack, null);
                core.onMessage(context);
            }
            catch (Throwable ex1)
            {
                try
                {
                    context.set(event, stack, ex1);
                    core.onException(context);
                }
                catch (Throwable ex2)
                {
                    try
                    {
                        cascade.defaultLogger().warn(ex2);
                    }
                    catch (Throwable ex3)
                    {
                        ex1.printStackTrace(System.err);
                        ex2.printStackTrace(System.err);
                        ex3.printStackTrace(System.err);
                    }
                }
            }
        }
    }

}
