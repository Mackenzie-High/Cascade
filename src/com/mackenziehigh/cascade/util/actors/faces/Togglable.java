package com.mackenziehigh.cascade.util.actors.faces;

import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Optional;

/**
 * Builds actors that can be turned on/off on-demand via event-messages.
 *
 * @param <T> is a builder-type.
 */
public interface Togglable<T extends CascadeActor.Builder>
{
    public T defaultToggleOn ();

    public T defaultToggleOff ();

    public T setToggleInput (CascadeToken input);

    public Optional<CascadeToken> getToggleInput ();
}
