package com.mackenziehigh.cascade;

import com.mackenziehigh.cascade.Cascade.AbstractStage.ActorTask;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Micro Actor Framework.
 */
public interface Cascade
{
    /**
     * A group of <code>Actor</code>s with a common power supply.
     */
    public interface Stage
    {
        /**
         * Actor.
         *
         * @param <I> is the type of objects the actor will consume.
         * @param <O> is the type of objects the actor will produce.
         */
        public interface Actor<I, O>
                extends Consumer<I>
        {
            /**
             * Actor Builder.
             *
             * @param <I> is the type of objects the actor will consume.
             * @param <O> is the type of objects the actor will produce.
             */
            public interface Builder<I, O>
            {
                /**
                 * Define the normal behavior of the actor.
                 *
                 * @param <X> is the type of objects the actor will consume.
                 * @param <Y> is the type of objects the actor will produce.
                 * @param script defines the message-handling behavior of the actor.
                 * @return a modified copy of this builder.
                 */
                public <X, Y> Builder<X, Y> withScript (Script<X, Y> script);

                /**
                 * Define the normal behavior of the actor.
                 *
                 * @param <X> is the type of objects the actor will consume.
                 * @param script defines the message-handling behavior of the actor.
                 * @return a modified copy of this builder.
                 */
                public default <X> Builder<X, X> withScript (ConsumerScript<X> script)
                {
                    return withScript(x ->
                    {
                        script.execute(x);
                        return null;
                    });
                }

                /**
                 * Define how the actor responds to unhandled exceptions.
                 *
                 * @param script defines the error-handling behavior of the actor.
                 * @return a modified copy of this builder.
                 */
                public Builder<I, O> withErrorHandler (Consumer<Throwable> script);

                /**
                 * Cause the actor to use the given queue to store incoming messages.
                 *
                 * <p>
                 * <b>Warning:</b> The queue must ensure thread-safety.
                 * </p>
                 *
                 * @param queue will store incoming messages as they await processing.
                 * @return a modified copy of this builder.
                 */
                public Builder<I, O> withInflowQueue (Queue<I> queue);

                /**
                 * Cause the actor to use a <code>ConcurrentLinkedQueue</code> to store incoming messages.
                 *
                 * @return this.
                 */
                public default Builder<I, O> withConcurrentInflowQueue ()
                {
                    return withInflowQueue(new ConcurrentLinkedQueue<>());
                }

                /**
                 * Cause the actor to use a <code>LinkedBlockingQueue</code> to store incoming messages.
                 *
                 * @return a modified copy of this builder.
                 */
                public default Builder<I, O> withLinkedInflowQueue ()
                {
                    return withInflowQueue(new LinkedBlockingQueue<>());
                }

                /**
                 * Cause the actor to use a <code>LinkedBlockingQueue</code> to store incoming messages.
                 *
                 * @param capacity is the maximum number of simultaneously pending messages.
                 * @return a modified copy of this builder.
                 */
                public default Builder<I, O> withLinkedInflowQueue (int capacity)
                {
                    return withInflowQueue(new LinkedBlockingQueue<>(capacity));
                }

                /**
                 * Cause the actor to use a <code>ArrayBlockingQueue</code> to store incoming messages.
                 *
                 * @param capacity is the maximum number of simultaneously pending messages.
                 * @return a modified copy of this builder.
                 */
                public default Builder<I, O> withArrayInflowQueue (int capacity)
                {
                    return withInflowQueue(new ArrayBlockingQueue<>(capacity));
                }

                /**
                 * Construct the actor and add it to the stage.
                 *
                 * @return the newly created actor.
                 */
                public Actor<I, O> create ();
            }

            /**
             * Input to an Actor.
             *
             * @param <T> is the type of objects that the actor will consume.
             */
            public interface Input<T>
            {

                /**
                 * Get the actor that this input pertains to.
                 *
                 * @return the enclosing actor.
                 */
                public Actor<T, ?> actor ();

                /**
                 * Connect this input to the given output of another actor.
                 *
                 * <p>
                 * This method is a no-op, if the connection already exists.
                 * </p>
                 *
                 * @param output will send messages to this input.
                 * @return this.
                 */
                public Input<T> connect (Output<T> output);

                /**
                 * Disconnect this input from the given output.
                 *
                 * <p>
                 * This method is a no-op, if the connection does not exist.
                 * </p>
                 *
                 * @param output will no longer be connected.
                 * @return this.
                 */
                public Input<T> disconnect (Output<T> output);

                /**
                 * Get the current incoming connections.
                 *
                 * @return the current connections as an immutable <code>Set</code>.
                 */
                public Set<Output<T>> connections ();

                /**
                 * Send a message to the actor via this input.
                 *
                 * @param message will be processed by the actor, eventually,
                 * if the message is not dropped due to capacity restrictions.
                 * @return this.
                 */
                public Input<T> send (T message);

            }

            /**
             * Output to an Actor.
             *
             * @param <T> is the type of objects that the actor will produce.
             */
            public interface Output<T>
            {
                /**
                 * Get the actor that this input pertains to.
                 *
                 * @return the enclosing actor.
                 */
                public Actor<?, T> actor ();

                /**
                 * Connect this output to the given input of another actor.
                 *
                 * <p>
                 * This method is a no-op, if the connection already exists.
                 * </p>
                 *
                 * @param input will be sent messages from this output.
                 * @return this.
                 */
                public Output<T> connect (Input<T> input);

                /**
                 * Disconnect this output from the given input.
                 *
                 * <p>
                 * This method is a no-op, if the connection does not exist.
                 * </p>
                 *
                 * @param input will no longer be connected.
                 * @return this.
                 */
                public Output<T> disconnect (Input<T> input);

                /**
                 * Get the current outgoing connections.
                 *
                 * @return the current connections as an immutable <code>Set</code>.
                 */
                public Set<Input<T>> connections ();

            }

            /**
             * Actor Behavior.
             *
             * @param <I> is the type of objects that the actor will consume.
             * @param <O> is the type of objects that the actor will produce.
             */
            @FunctionalInterface
            public interface Script<I, O>
            {
                public O execute (I input)
                        throws Throwable;
            }

            /**
             * Actor Behavior.
             *
             * @param <I> is the type of objects that the actor will consume.
             */
            @FunctionalInterface
            public interface ConsumerScript<I>
            {
                public void execute (I input)
                        throws Throwable;
            }

            /**
             * Get the <code>Stage</code> that contains this actor.
             *
             * @return the enclosing stage.
             */
            public Stage stage ();

            /**
             * Get the <code>Input</code> that supplies messages to this actor.
             *
             * @return the input to the actor.
             */
            public Input<I> input ();

            /**
             * Get the <code>Output</code> that receives messages from this actor.
             *
             * @return the output from the actor.
             */
            public Output<O> output ();

        }

        /**
         * Set the default error-handler that will handle unhandled exceptions,
         * if no other more specific error-handler is available.
         *
         * @param handler will be used to handle unhandled exceptions.
         * @return this.
         */
        public Stage setErrorHandler (Consumer<Throwable> handler);

        /**
         * Create a builder that can be used to add a new actor to this stage.
         *
         * @param <I> is the type of objects that the actor will consume.
         * @param <O> is the type of objects that the actor will produce.
         * @return the new builder.
         */
        public <I, O> Actor.Builder<I, O> newActor ();

        /**
         * Asynchronously shutdown this stage, as soon as reasonably possible.
         */
        public void close ();
    }

    /////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////
    /**
     * Partial Implementation of <code>Stage</code>.
     */
    public abstract class AbstractStage
            implements Cascade.Stage
    {
        private final Stage STAGE = this;

        private final Object stageLock = new Object();

        private final AtomicBoolean stageClosed = new AtomicBoolean(false);

        private volatile Consumer<Throwable> stageErrorHandler = ex ->
        {
            ex.printStackTrace(System.err);
        };

        /**
         * This method will be invoked whenever an actor needs executed.
         *
         * <p>
         * This method will not be re-invoked, until the actor finishes
         * being executed, even if the actor determines that it needs
         * to be executed again. Rather, the actor will invoke this
         * method again, if needed, at the end of its execution.
         * This strategy helps ensure that no two threads will
         * ever power the actor concurrently. Moreover,
         * this strategy lessons the amount of memory used
         * by some implementations in order to schedule actors.
         * </p>
         *
         * @param state provides the methods needed to execute an actor.
         */
        protected abstract void onActorSubmit (ActorTask state);

        /**
         * This method will be invoked when this stage closes.
         */
        protected abstract void onStageClose ();

        /**
         * {@inheritDoc}
         */
        @Override
        public final <I, O> Stage.Actor.Builder<I, O> newActor ()
        {
            synchronized (stageLock)
            {
                requireOpenStage();
                return new ActorBuilder<>();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final Stage setErrorHandler (final Consumer<Throwable> handler)
        {
            Objects.requireNonNull(handler, "handler");

            final Consumer<Throwable> safeConsumer = ex ->
            {
                try
                {
                    handler.accept(ex);
                }
                catch (Throwable ignored)
                {
                    // Pass.
                }
            };

            synchronized (stageLock)
            {
                requireOpenStage();
                stageErrorHandler = safeConsumer;
                return this;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final void close ()
        {
            synchronized (stageLock)
            {
                if (stageClosed.compareAndSet(false, true))
                {
                    onStageClose();
                }
            }
        }

        private void requireOpenStage ()
        {
            checkState(stageClosed.get() == false, "This stage was already closed!");
        }

        /**
         * An instance of this class facilitates executing an actor.
         *
         * <p>
         * A (meta) object is stored herein, which is intended
         * for use by implementing sub-classes, so that they
         * can store actor specific information.
         * </p>
         */
        protected final class ActorTask
        {
            private final InternalActor<?, ?> actor;

            private volatile Object meta;

            private ActorTask (final InternalActor<?, ?> actor)
            {
                this.actor = actor;
            }

            public Stage.Actor<?, ?> actor ()
            {
                return actor;
            }

            public Object meta ()
            {
                return meta;
            }

            public void meta (final Object value)
            {
                meta = value;
            }

            public void crank ()
            {
                actor.run();
            }
        }

        private final class ActorBuilder<I, O>
                implements Cascade.Stage.Actor.Builder<I, O>
        {
            private final Queue<I> inputQueue;

            private final Stage.Actor.Script<I, O> script;

            private final Consumer<Throwable> errorHandler;

            private ActorBuilder ()
            {
                this.inputQueue = new ConcurrentLinkedQueue<>();
                this.script = (I x) -> null;
                this.errorHandler = ex -> stageErrorHandler.accept(ex);
            }

            private ActorBuilder (final Queue<I> inputQueue,
                                  final Stage.Actor.Script<I, O> script,
                                  final Consumer<Throwable> errorHandler)
            {
                this.inputQueue = inputQueue;
                this.script = script;
                this.errorHandler = errorHandler;
            }

            @Override
            public <X, Y> Stage.Actor.Builder<X, Y> withScript (final Stage.Actor.Script<X, Y> script)
            {
                Objects.requireNonNull(script, "script");
                return new ActorBuilder(inputQueue, script, errorHandler);
            }

            @Override
            public Stage.Actor.Builder<I, O> withErrorHandler (final Consumer<Throwable> handler)
            {
                Objects.requireNonNull(handler, "handler");

                final Consumer<Throwable> safeConsumer = ex ->
                {
                    try
                    {
                        handler.accept(ex);
                    }
                    catch (Throwable ignored)
                    {
                        // Pass.
                    }
                };

                return new ActorBuilder(inputQueue, script, safeConsumer);
            }

            @Override
            public Stage.Actor.Builder<I, O> withInflowQueue (final Queue<I> queue)
            {
                Objects.requireNonNull(queue, "queue");
                return new ActorBuilder(queue, script, errorHandler);
            }

            @Override
            public Stage.Actor<I, O> create ()
            {
                synchronized (stageLock)
                {
                    requireOpenStage();
                    final InternalActor<I, O> actor = new InternalActor<>(this);
                    return actor;
                }
            }

        }

        private final class InternalActor<I, O>
                implements Cascade.Stage.Actor<I, O>
        {
            private final Stage.Actor<I, O> ACTOR = this;

            private final ActorBuilder<I, O> builder;

            private final Queue<I> mailbox;

            private final Script<I, O> script;

            private final InternalInput input = new InternalInput();

            private final InternalOutput output = new InternalOutput();

            private final ActorTask state = new ActorTask(this);

            private final AtomicLong pendingCranks = new AtomicLong();

            private final AtomicBoolean inProgress = new AtomicBoolean(false);

            private InternalActor (final ActorBuilder<I, O> builder)
            {
                this.builder = builder;
                this.script = builder.script;
                this.mailbox = builder.inputQueue;
            }

            private void run ()
            {
                if (inProgress.compareAndSet(false, true) == false)
                {
                    throw new IllegalStateException("concurrent run()");
                }

                try
                {
                    processMessage();
                }
                catch (Throwable ex1)
                {
                    builder.errorHandler.accept(ex1);
                }
                finally
                {
                    inProgress.set(false);

                    if (pendingCranks.decrementAndGet() != 0)
                    {
                        onActorSubmit(state);
                    }
                }
            }

            private void processMessage ()
                    throws Throwable
            {
                final I msgIn = mailbox.poll();
                final O msgOut = script.execute(msgIn);

                if (msgOut != null)
                {
                    final List<Stage.Actor.Input<O>> outputs = output.connectionList;
                    final int length = outputs.size();

                    for (int i = 0; i < length; i++)
                    {
                        outputs.get(i).send(msgOut);
                    }
                }
            }

            private void submit ()
            {
                if (pendingCranks.incrementAndGet() == 1)
                {
                    onActorSubmit(state);
                }
            }

            @Override
            public Cascade.Stage stage ()
            {
                return STAGE;
            }

            @Override
            public Stage.Actor.Input<I> input ()
            {
                return input;
            }

            @Override
            public Stage.Actor.Output<O> output ()
            {
                return output;
            }

            @Override
            public void accept (final I message)
            {
                input().send(message);
            }

            private final class InternalInput
                    implements Cascade.Stage.Actor.Input<I>
            {
                private final Object inputLock = new Object();

                private volatile Set<Stage.Actor.Output<I>> connections = newImmutableSet(Collections.EMPTY_LIST);

                @Override
                public Stage.Actor<I, ?> actor ()
                {
                    return ACTOR;
                }

                @Override
                public Stage.Actor.Input<I> connect (final Stage.Actor.Output<I> output)
                {
                    /**
                     * Only state within this input is accessed in the critical-section;
                     * otherwise, dead-lock could theoretically occur.
                     */
                    synchronized (inputLock)
                    {
                        if (connections.contains(output))
                        {
                            return this;
                        }
                        else
                        {
                            final Set<Stage.Actor.Output<I>> modified = new CopyOnWriteArraySet<>(connections);
                            modified.add(output);
                            connections = newImmutableSet(modified);
                        }
                    }

                    /**
                     * Do not perform this in the critical-section.
                     */
                    output.connect(this);
                    return this;
                }

                @Override
                public Stage.Actor.Input<I> disconnect (final Stage.Actor.Output<I> output)
                {
                    /**
                     * Only state within this input is accessed in the critical-section;
                     * otherwise, dead-lock could theoretically occur.
                     */
                    synchronized (inputLock)
                    {
                        if (connections.contains(output))
                        {
                            final Set<Stage.Actor.Output<I>> modified = new CopyOnWriteArraySet<>(connections);
                            modified.remove(output);
                            connections = newImmutableSet(modified);
                        }
                        else
                        {
                            return this;
                        }
                    }

                    /**
                     * Do not perform this in the critical-section.
                     */
                    output.disconnect(this);
                    return this;
                }

                @Override
                public Set<Stage.Actor.Output<I>> connections ()
                {
                    return connections;
                }

                @Override
                public Stage.Actor.Input<I> send (final I message)
                {
                    Objects.requireNonNull(message, "message");
                    builder.inputQueue.offer(message);
                    submit();
                    return this;
                }
            }

            private final class InternalOutput
                    implements Cascade.Stage.Actor.Output<O>
            {
                private final Object outputLock = new Object();

                private volatile List<Stage.Actor.Input<O>> connectionList = newImmutableList(Collections.EMPTY_LIST);

                private volatile Set<Stage.Actor.Input<O>> connectionSet = newImmutableSet(Collections.EMPTY_LIST);

                @Override
                public Stage.Actor<?, O> actor ()
                {
                    return ACTOR;
                }

                @Override
                public Stage.Actor.Output<O> connect (final Stage.Actor.Input<O> input)
                {
                    /**
                     * Only state within this output is accessed in the critical-section;
                     * otherwise, dead-lock could theoretically occur.
                     */
                    synchronized (outputLock)
                    {
                        if (connectionSet.contains(input))
                        {
                            return this;
                        }
                        else
                        {
                            final List<Stage.Actor.Input<O>> modified = new ArrayList<>(connectionSet);
                            modified.add(input);
                            connectionList = newImmutableList(modified);
                            connectionSet = newImmutableSet(modified);
                        }
                    }

                    /**
                     * Do not perform this in the critical-section.
                     */
                    input.connect(this);
                    return this;
                }

                @Override
                public Stage.Actor.Output<O> disconnect (final Stage.Actor.Input<O> input)
                {
                    /**
                     * Only state within this output is accessed in the critical-section;
                     * otherwise, dead-lock could theoretically occur.
                     */
                    synchronized (outputLock)
                    {
                        if (connectionSet.contains(input))
                        {
                            final List<Stage.Actor.Input<O>> modified = new ArrayList<>(connectionSet);
                            modified.remove(input);
                            connectionList = newImmutableList(modified);
                            connectionSet = newImmutableSet(modified);
                        }
                        else
                        {
                            return this;
                        }
                    }

                    /**
                     * Do not perform this in the critical-section.
                     */
                    input.disconnect(this);
                    return this;
                }

                @Override
                public Set<Stage.Actor.Input<O>> connections ()
                {
                    return connectionSet;
                }
            }
        }

        private static <T> List<T> newImmutableList (final Collection<T> collection)
        {
            return Collections.unmodifiableList(new CopyOnWriteArrayList<>(collection));
        }

        private static <T> Set<T> newImmutableSet (final Collection<T> collection)
        {
            return Collections.unmodifiableSet(new CopyOnWriteArraySet<>(collection));
        }

        private static void checkState (final boolean condition,
                                        final String message,
                                        final Object... args)
        {
            if (condition == false)
            {
                final String text = String.format(message, args);
                throw new IllegalStateException(text);
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////
    /**
     * Create a new single-threaded stage.
     *
     * @return the new stage.
     */
    public static Stage newStage ()
    {
        final ExecutorService service = Executors.newSingleThreadExecutor();
        return newExecutorStage(service);
    }

    /**
     * Create a new multi-threaded stage.
     *
     * @param threadCount is the number of worker threads that the stage will use.
     * @return the new stage.
     */
    public static Stage newStage (final int threadCount)
    {
        final ExecutorService service = Executors.newFixedThreadPool(threadCount);
        return newExecutorStage(service);
    }

    /**
     * Create a new stage based on a given <code>ExecutorService</code>.
     *
     * @param service will power the new stage.
     * @return the new stage.
     */
    public static Stage newExecutorStage (final ExecutorService service)
    {
        return new AbstractStage()
        {
            @Override
            protected void onActorSubmit (final AbstractStage.ActorTask actor)
            {
                service.submit(() -> actor.crank());
            }

            @Override
            protected void onStageClose ()
            {
                service.shutdown();
            }
        };
    }

    /**
     * Create a new stage based on a fixed-size pool of threads.
     *
     * @param threadCount is the number of threads in the pool.
     * @return the new stage.
     */
    public static Stage newPooledStage (final int threadCount)
    {
        final ThreadFactory factory = (final Runnable task) ->
        {
            final String name = String.format("newPooledStage-%d-%s", threadCount, UUID.randomUUID().toString());
            final Thread thread = new Thread(task, name);
            thread.setDaemon(true);
            return thread;
        };

        final LinkedBlockingQueue<ActorTask> queue = new LinkedBlockingQueue<>();

        return newPooledStage(factory, threadCount, queue);
    }

    /**
     * Create a new stage based on a fixed-size pool of threads.
     *
     * @param factory will be used to create the threads in the pool.
     * @param threadCount is the number of threads in the pool.
     * @param taskQueue will be used to feed tasks to the threads in the pool.
     * @return the new stage.
     */
    public static Stage newPooledStage (final ThreadFactory factory,
                                        final int threadCount,
                                        final BlockingQueue<ActorTask> taskQueue)
    {
        final AtomicBoolean stop = new AtomicBoolean(false);

        final Runnable mainLoop = () ->
        {
            while (stop.get() == false)
            {
                try
                {
                    final ActorTask task = taskQueue.poll(1, TimeUnit.SECONDS);

                    if (task != null)
                    {
                        task.crank();
                    }
                }
                catch (Throwable ex)
                {
                    // Pass.
                }
            }
        };

        /**
         * Create the threads.
         */
        for (int i = 0; i < threadCount; i++)
        {
            final Thread thread = factory.newThread(mainLoop);
            thread.start();
        }

        return new AbstractStage()
        {
            @Override
            protected void onActorSubmit (final AbstractStage.ActorTask state)
            {
                taskQueue.add(state);
            }

            @Override
            protected void onStageClose ()
            {
                stop.set(true);
            }
        };
    }
}
