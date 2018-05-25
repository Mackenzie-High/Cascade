package com.mackenziehigh.cascade.reactor.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mackenziehigh.cascade.reactor.CheckedRunnable;
import com.mackenziehigh.cascade.reactor.Reaction;
import com.mackenziehigh.cascade.reactor.Reactor;
import com.mackenziehigh.cascade.reactor.builder.ReactionBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;

/**
 *
 * @author mackenzie
 */
public final class InternalReaction
        implements ReactionBuilder,
                   Reaction
{
    private final InternalReactor reactor;

    private final UUID uuid = UUID.randomUUID();

    private volatile String name = uuid.toString();

    private final List<BooleanSupplier> conditions = Lists.newCopyOnWriteArrayList();

    private volatile CheckedRunnable onTrue = null;

    private volatile CheckedRunnable onFalse = null;

    // TODO: NOP instead????
    private volatile CheckedRunnable onError = () -> reactor().get().stop();

    public InternalReaction (final InternalReactor reactor)
    {
        this.reactor = Objects.requireNonNull(reactor, "reactor");
    }

    @Override
    public ReactionBuilder named (final String name)
    {
        this.name = Objects.requireNonNull(name, "name");
        return this;
    }

    @Override
    public ReactionBuilder require (final BooleanSupplier condition)
    {
        Preconditions.checkNotNull(condition, "condition");
        conditions.add(condition);
        return this;
    }

    @Override
    public ReactionBuilder onMatch (final CheckedRunnable task)
    {
        Preconditions.checkNotNull(task, "task");
        onTrue = (onTrue == null) ? task : onTrue.andThen(task);
        return this;
    }

    @Override
    public ReactionBuilder orElse (CheckedRunnable task)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ReactionBuilder onError (final CheckedRunnable handler)
    {
        Preconditions.checkNotNull(handler, "handler");
        onError = (onError == null) ? handler : onError.andThen(handler);
        return this;
    }

    @Override
    public Reaction build ()
    {
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
    public Optional<Reactor> reactor ()
    {
        return reactor.reactor();
    }

    public boolean crank ()
    {
        boolean condition = true;

        for (int i = 0; i < conditions.size(); i++)
        {
            if (conditions.get(i).getAsBoolean() == false)
            {
                condition = false;
                break;
            }
        }

        if (condition && onTrue != null)
        {
            crankOnTrue();
            return true;
        }
        else
        {
            return false;
        }
    }

    private void crankOnTrue ()
    {
        try
        {
            onTrue.run();
        }
        catch (Throwable ex1)
        {
            try
            {
                onError.run();
            }
            catch (Throwable ex2)
            {
                // TODO
                ex1.printStackTrace(System.err);
            }
        }
    }
}
