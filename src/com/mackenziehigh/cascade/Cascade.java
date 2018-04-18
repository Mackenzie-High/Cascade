package com.mackenziehigh.cascade;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.internal.Dispatcher;
import com.mackenziehigh.cascade.internal.ServiceExecutor;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An instance of this interface contains a concurrent set of stages,
 * where each stage contains zero-or-more actors, which send and receive
 * event-messages for processing according to their scripts.
 */
public final class Cascade
{
    private static final int ACTIVE = 0;

    private static final int CLOSING = 1;

    private static final int CLOSED = 2;

    private final UUID uuid = UUID.randomUUID();

    private final AtomicReference<String> name = new AtomicReference<>(uuid.toString());

    private final Dispatcher dispatcher;

    private final Set<CascadeStage> stages = Sets.newConcurrentHashSet();

    private final AtomicBoolean close = new AtomicBoolean();

    private final AtomicInteger state = new AtomicInteger();

    private final CountDownLatch awaitCloseLatch = new CountDownLatch(1);

    private final Object lock = new Object();

    /**
     * Sole Constructor.
     *
     * @param dispatcher will be used to route event-messages.
     */
    private Cascade (final Dispatcher dispatcher)
    {
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
        Verify.verify(isActive());
        Verify.verify(!isClosing());
        Verify.verify(!isClosed());
    }

    /**
     * Factory method.
     *
     * @return a new instance of this class.
     */
    public static Cascade newCascade ()
    {
        final Dispatcher disp = Dispatcher.newDispatcher();
        final Cascade cas = new Cascade(disp);
        return cas;
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
     * Setter.
     *
     * @param name will henceforth be the name of this cascade.
     * @return this.
     */
    public Cascade named (final String name)
    {
        this.name.set(Objects.requireNonNull(name, "name"));
        return this;
    }

    /**
     * Getter.
     *
     * <p>
     * By default, the name of this cascade is the string representation of the uuid().
     * </p>
     *
     * @return the current name of this cascade.
     */
    public String name ()
    {
        return name.get();
    }

    /**
     * Adds a new stage powered by a single thread.
     *
     * @return the given stage.
     */
    public CascadeStage newStage ()
    {
        return newStage(Executors.newFixedThreadPool(1));
    }

    /**
     * Adds a new stage powered by a given ExecutorService.
     *
     * <p>
     * The service can safely be shared with other stages.
     * When the last stage closes, the service will be shutdown.
     * </p>
     *
     * @param service provides the power.
     * @return the new stage.
     */
    public CascadeStage newStage (final ExecutorService service)
    {
        return newStage(new ServiceExecutor(service));
    }

    /**
     * Adds a new stage.
     *
     * @param executor will be used to power the new stage.
     * @return the given stage.
     */
    public synchronized CascadeStage newStage (final CascadeExecutor executor)
    {
        /**
         * Prevent new stages from being created as we close the existing ones.
         */
        synchronized (lock)
        {
            final CascadeStage stage = new CascadeStage(this, dispatcher, executor, s -> removeStage(s));

            if (stage.isClosing())
            {
                throw new IllegalArgumentException("The stage is already closing!");
            }
            else if (stage.isClosed())
            {
                throw new IllegalArgumentException("The stage is already closed!");
            }
            else if (this.equals(stage.cascade()) == false)
            {
                throw new IllegalArgumentException("The stage has a different cascade!");
            }
            else if (stages.contains(stage))
            {
                throw new IllegalStateException("The cascade already contains the given stage!");
            }
            else if (close.get() == false)
            {
                stages.add(stage);
            }
            else
            {
                throw new IllegalStateException("The stage is already closing!");
            }

            return stage;
        }
    }

    /**
     * Getter.
     *
     * @return all of the stages currently active in this cascade.
     */
    public Set<CascadeStage> stages ()
    {
        return ImmutableSet.copyOf(stages);
    }

    /**
     * Getter.
     *
     * @return the event-channels that currently have at least one subscribed actor.
     */
    public Map<CascadeToken, CascadeChannel> channels ()
    {
        return dispatcher.channels();
    }

    /**
     * Getter.
     *
     * @return true, if and only if, this cascade is not closed or closing.
     */
    public boolean isActive ()
    {
        return state.get() == ACTIVE;
    }

    /**
     * Getter.
     *
     * @return true, if and only if, this cascade is closing.
     */
    public boolean isClosing ()
    {
        return state.get() == CLOSING;
    }

    /**
     * Getter.
     *
     * @return true, if and only, this cascade is now closed.
     */
    public boolean isClosed ()
    {
        return state.get() == CLOSED;
    }

    /**
     * This method closes all of the stages and permanently terminates this cascade.
     *
     * <p>
     * This method returns immediately; however, the cascade will
     * not be closed until all stages therein close.
     * </p>
     *
     * @return this.
     */
    public Cascade close ()
    {
        /**
         * Prevent new stages from being created as we close the existing ones.
         */
        synchronized (lock)
        {
            if (close.compareAndSet(false, true))
            {
                state.set(CLOSING);

                for (CascadeStage stage : stages)
                {
                    stage.close();
                }
            }
        }

        return this;
    }

    /**
     * This method blocks, until this cascade closes.
     *
     * <p>
     * This method does not cause the cascade to close.
     * Rather, this method merely causes the calling thread
     * to wait for the cascade to close.
     * </p>
     *
     * @param timeout is the maximum amount of time to wait.
     * @return this.
     * @throws java.lang.InterruptedException
     */
    public Cascade awaitClose (final Duration timeout)
            throws InterruptedException
    {
        awaitCloseLatch.await(timeout.toNanos(), TimeUnit.NANOSECONDS);
        return this;
    }

    /**
     * This method retrieves the event-channel identified by the given token.
     *
     * @param event identifies the event-channel to find.
     * @return the channel, if it has at least one subscribed actor.
     */
    public Optional<CascadeChannel> channelOf (final CascadeToken event)
    {
        return dispatcher.lookup(event);
    }

    /**
     * This method broadcasts an event-message.
     *
     * <p>
     * This method is a no-op, if no actors are subscribed
     * to receive event-messages from the given event-channel.
     * </p>
     *
     * @param event identifies the event being produced.
     * @param stack contains the content of the message.
     * @return this.
     */
    public Cascade send (final CascadeToken event,
                         final CascadeStack stack)
    {
        final Optional<CascadeChannel> channel = channelOf(event);

        if (channel.isPresent())
        {
            channel.get().send(stack);
        }

        return this;
    }

    private synchronized void removeStage (final CascadeStage stage)
    {
        stages.remove(stage);

        if (stages.isEmpty() && isClosing())
        {
            state.set(CLOSED);
            awaitCloseLatch.countDown();
        }
    }
}
