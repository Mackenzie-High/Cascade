package com.mackenziehigh.cascade.reactor.builder;

import com.mackenziehigh.cascade.reactor.Reactor;
import com.mackenziehigh.cascade.reactor.Powerplant;

/**
 *
 */
public interface ReactorBuilder
{
    public ReactorBuilder named (String name);

    public ReactorBuilder poweredBy (Powerplant executor);

    public <T> InputBuilder<T> newInput (Class<T> type);

    public <T> OutputBuilder<T> newOutput (Class<T> type);

    public ReactionBuilder newReaction ();

    public Reactor build ();

}
