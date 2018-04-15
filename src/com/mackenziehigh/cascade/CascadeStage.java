package com.mackenziehigh.cascade;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.internal.Dispatcher;
import com.mackenziehigh.cascade.internal.Scheduler;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * A stage contains actor(s) that are powered by an underlying executor.
 */
public final class CascadeStage
{
    private static final int ACTIVE = 0;

    private static final int CLOSING = 1;

    private static final int CLOSED = 2;

    private final Cascade cascade;

    private final UUID uuid = UUID.randomUUID();

    private volatile String name = uuid.toString();

    private final Set<CascadeActor> actors = Sets.newConcurrentHashSet();

    private final CascadeExecutor executor;

    private final AtomicInteger state = new AtomicInteger();

    private final CountDownLatch stageAwaitCloseLatch = new CountDownLatch(1);

    public final Scheduler<CascadeActor> scheduler = new Scheduler<>();

    private final Dispatcher dispatcher;

    CascadeStage (final Cascade cascade,
                  final Dispatcher dispatcher,
                  final CascadeExecutor executor,
                  final Consumer<CascadeStage> remover)
    {
        this.cascade = Objects.requireNonNull(cascade, "cascade");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
        executor.onStageOpened(this);
    }

    public final AtomicInteger cranks = new AtomicInteger();

    /**
     * Repeatedly invoke this method in order to supply power to the stage,
     * which in-turn supplies power to the actors contained herein.
     *
     * <p>
     * This method will only perform one unit-of-work at a time.
     * For example, a single unit-of-work would be when an actor
     * processes a single incoming message. Or, when an actor is
     * setup or closed.
     * </p>
     *
     * @param timeout
     */
    public void crank (final Duration timeout)
    {
        /**
         * Try to obtain a task from the round-robin scheduler.
         */
        try
        {
            final Scheduler.Process<CascadeActor> task = scheduler.poll(timeout);

            if (task == null)
            {
                return; // No Work Performed
            }

            /**
             * Execute the actor.
             */
            try (Scheduler.Process<CascadeActor> proc = task)
            {
                final CascadeActor actor = proc.getUserObject().get();
                actor.perform();
            }
            catch (Throwable ex)
            {
                /**
                 * This should never happen due to the error-handling philosophy of the actor itself.
                 */
                ex.printStackTrace(System.err);
            }
        }
        catch (InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
        catch (Throwable ex)
        {
            // Pass
        }
    }

    /**
     * Getter.
     *
     * @return the executor that powers this stage.
     */
    public CascadeExecutor executor ()
    {
        return executor;
    }

    /**
     * Setter.
     *
     * @param name will henceforth be the name of this stage.
     * @return this.
     */
    public CascadeStage named (final String name)
    {
        this.name = Objects.requireNonNull(name, "name");
        return this;
    }

    /**
     * Getter.
     *
     * <p>
     * By default, the name of this stage is the string representation of the stage's UUID.
     * </p>
     *
     * @return the current name of this stage.
     */
    public String name ()
    {
        return name;
    }

    /**
     * Getter.
     *
     * @return a universally-unique-identifier of this object.
     */
    public UUID uuid ()
    {
        return uuid;
    }

    /**
     * Getter.
     *
     * @return the cascade that contains this stage.
     */
    public Cascade cascade ()
    {
        return cascade;
    }

    /**
     * Getter.
     *
     * @return the actors that are currently alive on this stage.
     */
    public Set<CascadeActor> actors ()
    {
        return ImmutableSet.copyOf(actors);
    }

    /**
     * This method creates a new actor with no behavior defined.
     *
     * @return the new actor.
     */
    public CascadeActor newActor ()
    {
        final Scheduler.Process<CascadeActor> task = scheduler.newProcess();

        /**
         * The actor will invoke this method whenever it needs to be executed.
         * This callback will be invoked when the actor is started,
         * enqueues an event-message, or begins being closed.
         */
        final Runnable callback = () ->
        {
            /**
             * Schedule the actor for execution using the round-robin scheduler.
             * In effect, this just adds the actor to a queue or pending tasks for the executor.
             */
            task.schedule();

            /**
             * Inform the executor that it needs to provide power to this stage,
             * which in-turn will cause the actor to be executed when the power is applied.
             */
            executor.onTask(this);
        };

        /**
         * This is the new actor itself, which will invoke the callback.
         */
        final CascadeActor actor = new CascadeActor(this,
                                                    dispatcher,
                                                    callback,
                                                    x -> actors.remove(x));
        actors.add(actor);
        task.getUserObject().set(actor);
        return actor;
    }

    /**
     * This method creates a new actor using the given builder.
     *
     * <p>
     * The builder must create an actor that is on this stage.
     * Thus, directly or indirectly, the builder actually calls
     * the <code>newActor(CascadeScript)</code> overload
     * of this method and returns the resultant actor.
     * In short, the builder just provides a nice way
     * of being able to configure the new actor,
     * usually via the use of a fluent API.
     * </p>
     *
     * @param builder will create the new actor.
     * @return the new actor.
     */
    public CascadeActor newActor (CascadeActor.Builder builder)
    {
        final CascadeActor actor = builder.build();
        Preconditions.checkState(this.equals(actor.stage()), "Wrong Stage");
        return actor;
    }

    /**
     * This method creates a new builder that will later
     * create a new actor on this stage.
     *
     * <p>
     * This method is just syntactic sugar to aid fluent APIs.
     * </p>
     *
     * @param <T> is the type of builder that will be returned.
     * @param factory will produce the new builder.
     * @return the new builder.
     */
    public <T extends CascadeActor.Builder> T newActor (CascadeActor.Builder.Factory<T> factory)
    {
        return factory.newBuilder(this);
    }

    /**
     * Getter.
     *
     * @return true, if and only if, this stage is not closing or closed.
     */
    public boolean isActive ()
    {
        return state.get() == ACTIVE;
    }

    /**
     * Getter.
     *
     * @return true, if and only if, this stage is closing.
     */
    public boolean isClosing ()
    {
        return state.get() == CLOSING;
    }

    /**
     * Getter.
     *
     * @return true, if and only if, this stage is closed.
     */
    public boolean isClosed ()
    {
        return state.get() == CLOSED;
    }

    /**
     * This method kills all the actors on the stage and removes
     * this stage from the enclosing cascade().
     *
     * @return this.
     */
    public CascadeStage close ()
    {
        executor.onStageClosed(this);
        stageAwaitCloseLatch.countDown();
        return this;
    }

    /**
     * This method blocks, until this stage closes.
     *
     * @param timeout is the maximum amount of time to wait.
     * @return this.
     * @throws java.lang.InterruptedException
     */
    public CascadeStage awaitClose (final Duration timeout)
            throws InterruptedException
    {
        stageAwaitCloseLatch.await(timeout.toNanos(), TimeUnit.NANOSECONDS);
        return this;
    }
}
