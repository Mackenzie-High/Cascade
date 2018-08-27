package com.mackenziehigh.cascade.dev2;

import com.mackenziehigh.cascade.Cascade.Stage;
import com.mackenziehigh.cascade.Cascade.Stage.Actor;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Instant;

public final class Watcher
{
    public final Actor.Input<Instant> clockIn;

    public final Actor.Output<File> filesOut;

    private final Actor<File, File> recv;

    private final long now = System.currentTimeMillis();

    private final WatchService watchService;

    public Watcher (final Stage stage,
                    final File directory)
            throws IOException
    {
        watchService = FileSystems.getDefault().newWatchService();
        directory.toPath().register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);

        clockIn = stage.newActor().withScript((Instant x) -> load(directory)).create().input();
        recv = stage.newActor().withScript((File file) -> file).create();
        filesOut = recv.output();
    }

    private void load (final File directory)
    {
        final WatchKey key = watchService.poll();

        if (key != null)
        {
            for (WatchEvent evt : key.pollEvents())
            {
                final Path path = (Path) evt.context();
                final File file = new File(directory, path.toFile().getName());
                System.out.println(file + " = " + file.isFile());
                if (file.isFile())
                {
                    recv.accept(file);
                }

                key.reset();
            }
        }
    }
}
