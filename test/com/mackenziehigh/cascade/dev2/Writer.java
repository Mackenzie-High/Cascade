package com.mackenziehigh.cascade.dev2;

import com.mackenziehigh.cascade.Cascade.Stage;
import com.mackenziehigh.cascade.Cascade.Stage.Actor;

public final class Writer
{
    public final Actor.Input<Data> dataIn;

    private final Actor<Data, Data> actor;

    public Writer (final Stage stage)
    {
        actor = stage.newActor().withScript(this::consume).create();
        dataIn = actor.input();
    }

    private void consume (final Data data)
    {
        System.out.println(data.file + " = " + data.data.length);
    }
}
