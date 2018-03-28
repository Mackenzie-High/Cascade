package com.mackenziehigh.cascade.util.flow;

import com.mackenziehigh.cascade.CascadeStack;
import java.time.Duration;

/**
 *
 * @author mackenzie
 */
public interface Poller
{
    public CascadeStack poll ();

    public CascadeStack poll (Duration timeout);
}
