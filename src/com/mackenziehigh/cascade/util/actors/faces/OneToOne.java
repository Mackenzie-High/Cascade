package com.mackenziehigh.cascade.util.actors.faces;

import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Optional;

/**
 * Builds actors that take a single data-input and produce a single data-output.
 *
 * @param <T> is a builder-type.
 */
public interface OneToOne<T extends CascadeActor.Builder>
        extends Source<T>,
                Sink<T>
{
    @Override
    public T setDataInput (CascadeToken input);

    @Override
    public T setDataOutput (CascadeToken output);

    @Override
    public Optional<CascadeToken> getDataInput ();

    @Override
    public Optional<CascadeToken> getDataOutput ();

}
