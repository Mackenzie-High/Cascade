package com.mackenziehigh.cascade.util.actors.faces;

import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Optional;

/**
 *
 * @param <T>
 */
public interface Sink<T extends CascadeActor.Builder>
{
    public T setDataInput (CascadeToken input);

    public Optional<CascadeToken> getDataInput ();
}
