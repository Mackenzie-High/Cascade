package com.mackenziehigh.cascade.util.actors;

import com.mackenziehigh.cascade.CascadeActor;

/**
 * Provides static factories that produce builders that build actors.
 */
public final class CommonActors
{
    public static final CascadeActor.Builder.Factory<ClockBuilder> CLOCK = stage -> new ClockBuilder(stage);

    public static final CascadeActor.Builder.Factory<StandardPrinterBuilder> STDOUT = stage -> new StandardPrinterBuilder(stage);
}
