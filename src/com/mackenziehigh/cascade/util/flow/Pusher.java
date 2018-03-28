package com.mackenziehigh.cascade.util.flow;

import com.mackenziehigh.cascade.CascadeStack;
import java.time.Duration;

/**
 *
 * @author mackenzie
 */
public interface Pusher
{
    public Pusher push (CascadeStack message);

    public Pusher push (CascadeStack message,
                        Duration timeout);
}
