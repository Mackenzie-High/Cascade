package com.mackenziehigh.cascade.internal.powerplants;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mackenziehigh.cascade.Actor;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.MessageStack;
import com.mackenziehigh.cascade.Pipeline;
import com.mackenziehigh.cascade.Powerplant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Powerplant based on Dedicated Threads.
 */
public final class DedicatedPowerplant
        implements Powerplant
{
    private final CascadeLogger logger = null;

    private final String name;

    private final Set<Actor> actors;

    private final ImmutableSet<Thread> threads;

    private final ImmutableMap<Actor, Set<Pipeline>> inflow = null;

    private final ImmutableMap<Actor, Semaphore> blockers = null;

    private final AtomicBoolean started = new AtomicBoolean();

    private final AtomicBoolean stop = new AtomicBoolean();

    private final SyncCombinedQueue<MessageStack> sharedQueue;

    public DedicatedPowerplant (final String name,
                                final Map<Actor, Set<Actor>> inflow,
                                final ThreadFactory factory)
    {
        this.name = name;
        this.actors = ImmutableSet.copyOf(inflow.keySet());
        this.sharedQueue = new SyncCombinedQueue<>(1024);

        /**
         * Create the pipelines.
         */
        for (Actor consumer : inflow.keySet())
        {
            for (Actor supplier : inflow.get(consumer))
            {
                final PipelineImp pipeline = new PipelineImp(supplier,
                                                             consumer,
                                                             sharedQueue.addMemberQueue(1024));
            }
        }

        /**
         * Create the threads.
         */
        final ImmutableSet.Builder<Thread> threadsBuilder = ImmutableSet.builder();
        for (Actor actor : inflow.keySet())
        {
//            threadsBuilder.add(factory.newThread(() -> run(actor)));
        }
        this.threads = threadsBuilder.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name ()
    {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int minimumThreads ()
    {
        return threads().size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int maximumThreads ()
    {
        return threads().size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Thread> threads ()
    {
        return threads;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Actor> actors ()
    {
        return actors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Actor, Set<Pipeline>> inflow ()
    {
        return inflow;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean send (final Actor actor,
                         final MessageStack message)
    {
        boolean result = true;
        final List<PipelineImp> pipelines = null;
        Preconditions.checkArgument(pipelines != null, "Unknown Actor");
        for (int i = 0; i < pipelines.size(); i++)
        {
            final PipelineImp pipeline = pipelines.get(i);
            if (pipeline.queue.add(message))
            {
                pipeline.blocker.release();
            }
            else
            {
                result = false;
            }
        }
        return result;
    }

    public void start ()
    {
        if (started.getAndSet(true) == false)
        {
            threads().forEach(x -> x.start());
        }
    }

    public void stop ()
    {
        stop.set(true);
    }

    private void run (final Actor actor,
                      final List<PipelineImp> inputs,
                      final Semaphore blocker)
    {
        int offset = -1; // Prevent Starvation

        while (stop.get() == false && inputs.isEmpty() == false)
        {
            try
            {
                try
                {
                    offset = (offset + 1) % inputs.size();

                    final boolean hasPermit = blocker.tryAcquire(1, TimeUnit.SECONDS);

                    if (hasPermit == false)
                    {
                        continue;
                    }

                    for (int i = 0; i < inputs.size(); i++)
                    {
                        final PipelineImp pipeline = inputs.get((i + offset) % inputs.size());
                        final MessageStack message = pipeline.queue.remove();

                        if (message == null)
                        {
                            continue;
                        }

                        try
                        {
                            actor.process(pipeline, message);
                        }
                        catch (Throwable ex)
                        {
                            actor.logger().error(ex);
                        }
                        break;
                    }
                }
                catch (Throwable ex)
                {
                    logger.error(ex);
                }
            }
            catch (Throwable ex)
            {
                // Even if logging fails, continue running.
            }
        }
    }

    private final class PipelineImp
            implements Pipeline
    {
        private final Actor supplier;

        private final Actor consumer;

        private final SyncCombinedQueue<MessageStack>.SyncMemberQueue queue;

        private final Semaphore blocker = null;

        public PipelineImp (final Actor supplier,
                            final Actor consumer,
                            final SyncCombinedQueue<MessageStack>.SyncMemberQueue queue)
        {
            this.supplier = supplier;
            this.consumer = consumer;
            this.queue = queue;
        }

        @Override
        public Actor supplier ()
        {
            return supplier;
        }

        @Override
        public Actor consumer ()
        {
            return consumer;
        }

        @Override
        public int backlogSize ()
        {
            return sharedQueue.size();
        }

        @Override
        public int backlogCapacity ()
        {
            return sharedQueue.capacity();
        }

        @Override
        public int queueSize ()
        {
            return queue.size();
        }

        @Override
        public int queueCapacity ()
        {
            return sharedQueue.capacity();
        }
    }

}
