package com.mackenziehigh.cascade.util.actors.faces;

import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Optional;

/**
 *
 * @param <T>
 */
public interface Source<T extends CascadeActor.Builder>
{
    public T setDataOutput (CascadeToken output);

    public Optional<CascadeToken> getDataOutput ();
}
