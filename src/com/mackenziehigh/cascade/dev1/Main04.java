package com.mackenziehigh.cascade.dev1;

import com.google.common.hash.Hashing;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.Cascade.Stage;
import com.mackenziehigh.cascade.Cascade.Stage.Actor;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 *
 */
public class Main04
{
    private final ExecutorService clock = Executors.newSingleThreadScheduledExecutor();

    private final Stage stage = Cascade.newStage(4);

    private final Consumer<File> loader = stage.newActor().withScript(this::load).create();

    private final Actor<File, File> gateway = stage.newActor().withScript(this::gate).create();

    private final Actor<File, String> hasher = stage.newActor().withScript(this::hash).create();

    private final Actor<String, String> printer = stage.newActor().withScript(this::print).create();

    private Main04 ()
    {
        gateway.output().connect(hasher.input());
        hasher.output().connect(printer.input());
    }

    private void load (final File dir)
    {
        Arrays.asList(dir.listFiles()).forEach(gateway);
    }

    private File gate (final File dir)
    {
        return dir;
    }

    private String hash (final File dir)
    {
        return Hashing.md5().hashString(dir.getAbsolutePath(), Charset.defaultCharset()).toString();
    }

    private void print (final String dir)
    {
        System.out.println("Dir = " + dir);
    }

    public static void main (String[] args)
    {
        final Main04 main = new Main04();
        main.loader.accept(new File("/tmp"));
    }
}
