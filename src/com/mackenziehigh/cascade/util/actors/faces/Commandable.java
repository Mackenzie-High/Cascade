package com.mackenziehigh.cascade.util.actors.faces;

import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Optional;

/**
 *
 * @param <T>
 */
public interface Commandable<T extends CascadeActor.Builder>
{
    public T setCommandInput (CascadeToken input);

    public Optional<CascadeToken> getCommandInput ();
}
