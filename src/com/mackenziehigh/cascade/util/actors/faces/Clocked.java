package com.mackenziehigh.cascade.util.actors.faces;

import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Optional;

/**
 * Builds actors that listen for periodic event-messages from timing-sources (clocks).
 *
 * @param <T> is a builder-type.
 */
public interface Clocked<T extends CascadeActor.Builder>
{
    public T setClockInput (CascadeToken input);

    public Optional<CascadeToken> getClockInput ();
}
