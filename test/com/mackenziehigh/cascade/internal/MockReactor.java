package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.internal.cascade.MockableReactor;
import com.mackenziehigh.cascade.Reactor;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author mackenzie
 */
public final class MockReactor
        implements MockableReactor
{
    public final AtomicLong pings = new AtomicLong();

    @Override
    public Optional<Reactor> reactor ()
    {
        return Optional.empty();
    }

    @Override
    public MockableReactor ping ()
    {
        pings.incrementAndGet();
        return this;
    }

}
