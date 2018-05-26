package com.mackenziehigh.cascade.internal;

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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MockableReactor ping ()
    {
        pings.incrementAndGet();
        return this;
    }

}
