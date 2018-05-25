package com.mackenziehigh.cascade.reactor.internal;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.reactor.Executor;
import com.mackenziehigh.cascade.reactor.Input;
import com.mackenziehigh.cascade.reactor.Output;
import com.mackenziehigh.cascade.reactor.Reaction;
import com.mackenziehigh.cascade.reactor.Reactor;
import com.mackenziehigh.cascade.reactor.builder.InputBuilder;
import com.mackenziehigh.cascade.reactor.builder.OutputBuilder;
import com.mackenziehigh.cascade.reactor.builder.ReactionBuilder;
import com.mackenziehigh.cascade.reactor.builder.ReactorBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 *
 */
public final class InternalReactor
        implements ReactorBuilder,
                   Reactor
{
    private final UUID uuid = UUID.randomUUID();

    private volatile String name = uuid.toString();

    private volatile boolean built = false;

    private volatile boolean reacting = false;

    private final AtomicReference<?> meta = new AtomicReference<>();

    private volatile Executor executor;

    private final Semaphore lock = new Semaphore(1);

    private volatile Set<Input<?>> inputsBuilder = Sets.newConcurrentHashSet();

    private volatile Set<Output<?>> outputsBuilder = Sets.newConcurrentHashSet();

    private volatile Set<Reaction> reactionsBuilder = Sets.newConcurrentHashSet();

    private volatile SortedMap<String, Input<?>> inputs = ImmutableSortedMap.of();

    private volatile SortedMap<String, Output<?>> outputs = ImmutableSortedMap.of();

    private volatile SortedMap<String, Reaction> reactions = ImmutableSortedMap.of();

    private final List<InternalReaction> reactionsList = Lists.newCopyOnWriteArrayList();

    public Optional<Reactor> reactor ()
    {
        return built ? Optional.of(this) : Optional.empty();
    }

    @Override
    public ReactorBuilder named (final String name)
    {
        this.name = Objects.requireNonNull(name, "name");
        return this;
    }

    @Override
    public ReactorBuilder poweredBy (final Executor executor)
    {
        this.executor = Objects.requireNonNull(executor, "executor");
        return this;
    }

    @Override
    public <T> InputBuilder<T> newInput (final Class<T> type)
    {
        final InternalInput<T> input = new InternalInput<>(this, type);
        inputsBuilder.add(input);
        return input;
    }

    @Override
    public <T> OutputBuilder<T> newOutput (final Class<T> type)
    {
        final InternalOutput<T> output = new InternalOutput<>(this, type);
        outputsBuilder.add(output);
        return output;
    }

    @Override
    public ReactionBuilder newReaction ()
    {
        final InternalReaction builder = new InternalReaction(this);
        reactionsList.add(builder);
        return builder;
    }

    @Override
    public Reactor build ()
    {
        inputs = ImmutableSortedMap.copyOf(inputsBuilder.stream().collect(Collectors.toMap(x -> x.name(), x -> x)));
        outputs = ImmutableSortedMap.copyOf(outputsBuilder.stream().collect(Collectors.toMap(x -> x.name(), x -> x)));
        reactions = ImmutableSortedMap.copyOf(reactionsBuilder.stream().collect(Collectors.toMap(x -> x.name(), x -> x)));

        built = true;
        return this;
    }

    @Override
    public UUID uuid ()
    {
        return uuid;
    }

    @Override
    public String name ()
    {
        return name;
    }

    @Override
    public SortedMap<String, Input<?>> inputs ()
    {
        return inputs;
    }

    @Override
    public SortedMap<String, Output<?>> outputs ()
    {
        return outputs;
    }

    @Override
    public SortedMap<String, Reaction> reactions ()
    {
        return reactions;
    }

    @Override
    public Reactor start ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Reactor stop ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isUnstarted ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isStarting ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isStarted ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isStopping ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isStopped ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isAlive ()
    {
        final boolean result = isStarting() || isStarted() || isStopping();
        return result;
    }

    @Override
    public boolean isReacting ()
    {
        return reacting;
    }

    @Override
    public Executor executor ()
    {
        return executor;
    }

    @Override
    public Reactor ping ()
    {
        executor.onReady(this, meta);
        return this;
    }

    @Override
    public boolean crank ()
    {
        boolean result = false;

        if (lock.tryAcquire())
        {
            try
            {
                reacting = true;

                for (int i = 0; i < reactionsList.size(); i++)
                {
                    result |= reactionsList.get(i).crank();
                }
            }
            finally
            {
                reacting = false;
                lock.release();
            }
        }

        return result;
    }

}
