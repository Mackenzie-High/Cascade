package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.Reactor;
import java.util.Optional;

/**
 *
 * @author mackenzie
 */
public interface MockableReactor
{
    public Optional<Reactor> reactor ();

    public MockableReactor ping ();
}
