package com.mackenziehigh.cascade.internal.schema;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadePump;
import com.mackenziehigh.cascade.CascadeReactor;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.internal.engines.Connection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public final class ConcretePump
        implements CascadePump
{
    private final CascadeToken name;

    private final ImmutableSet<Thread> threads;

    private final AtomicBoolean stop = new AtomicBoolean();

    public ConcretePump (final CascadeToken name,
                         final ThreadFactory threadFactory,
                         final int threadCount)
    {
        this.name = Objects.requireNonNull(name);

        final Set<Thread> threadsSet = Sets.newHashSet();
        for (int i = 0; i < threadCount; i++)
        {
            threadsSet.add(threadFactory.newThread(null));
        }
        this.threads = ImmutableSet.copyOf(threadsSet);
    }

    @Override
    public Cascade cascade ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CascadeToken name ()
    {
        return name;
    }

    @Override
    public int minimumThreads ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int maximumThreads ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Thread> threads ()
    {
        return threads;
    }

    @Override
    public Set<CascadeReactor> reactors ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addReactor (final CascadeReactor reactor,
                            final Connection queue)
    {

    }

    private void mainLoop ()
    {
        while (stop.get() == false)
        {

        }
    }
}
