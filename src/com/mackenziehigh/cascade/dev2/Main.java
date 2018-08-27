package com.mackenziehigh.cascade.dev2;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.Cascade.Stage;
import java.io.File;
import java.io.IOException;

public final class Main
{
    public static void main (String[] args)
            throws IOException
    {
        final Stage stage = Cascade.newStage(4);

        final Clock clock = new Clock();
        final Watcher watcher1 = new Watcher(stage, new File("/home/mhigh"));
        final Reader reader = new Reader(stage);
        final Writer writer = new Writer(stage);

        clock.clockOut.connect(watcher1.clockIn);
        watcher1.filesOut.connect(reader.filesIn);
        reader.dataOut.connect(writer.dataIn);
    }
}
