package com.mackenziehigh.cascade.dev2;

import com.mackenziehigh.cascade.Cascade.Stage;
import com.mackenziehigh.cascade.Cascade.Stage.Actor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class Reader
{
    public final Actor.Input<File> filesIn;

    public final Actor.Output<Data> dataOut;

    private final Actor<File, Data> actor;

    public Reader (final Stage stage)
    {
        actor = stage.newActor().withScript(this::read).create();
        filesIn = actor.input();
        dataOut = actor.output();
    }

    private Data read (final File file)
            throws IOException
    {
        final byte[] bytes = Files.readAllBytes(file.toPath());
        return new Data(file, bytes);
    }
}
