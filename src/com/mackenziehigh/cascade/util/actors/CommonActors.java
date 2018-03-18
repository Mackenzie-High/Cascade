package com.mackenziehigh.cascade.util.actors;

import com.mackenziehigh.cascade.CascadeActor;

/**
 *
 */
public final class CommonActors
{
    public static final CascadeActor.Builder.Factory<TickerBuilder> TICKER = stage -> new TickerBuilder(stage);

    public static final CascadeActor.Builder.Factory<StandardPrinterBuilder> STDOUT = stage -> new StandardPrinterBuilder(stage);
}
