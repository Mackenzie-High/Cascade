package com.mackenziehigh.cascade.reactor.builder;

import com.mackenziehigh.cascade.reactor.Executor;
import com.mackenziehigh.cascade.reactor.Reactor;

/**
 *
 */
public interface ReactorBuilder
{
    public ReactorBuilder named (String name);

    public ReactorBuilder poweredBy (Executor executor);

    public <T> InputBuilder<T> newInput (Class<T> type);

    public <T> OutputBuilder<T> newOutput (Class<T> type);

    public ReactionBuilder newReaction ();

    public Reactor build ();

}
