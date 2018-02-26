package com.mackenziehigh.cascade.redo2.util.actors;

import com.mackenziehigh.cascade.redo2.CascadeActor;

/**
 *
 */
public final class CommonActors
{
    public static final CascadeActor.Builder.Factory<TickerBuilder> TICKER = stage -> new TickerBuilder(stage);
}
