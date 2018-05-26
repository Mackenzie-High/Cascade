package com.mackenziehigh.cascade.builder;

import com.mackenziehigh.cascade.Powerplant;
import com.mackenziehigh.cascade.Reactor;

/**
 *
 */
public interface ReactorBuilder
{
    public ReactorBuilder named (String name);

    public ReactorBuilder poweredBy (Powerplant executor);

    public <T> ArrayInputBuilder<T> newArrayInput (Class<T> type);

    public <T> OutputBuilder<T> newOutput (Class<T> type);

    public ReactionBuilder newReaction ();

    public Reactor build ();

}
