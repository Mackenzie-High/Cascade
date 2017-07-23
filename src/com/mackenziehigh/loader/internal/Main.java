package com.mackenziehigh.loader.internal;

import com.mackenziehigh.sexpr.SList;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

/**
 * Main Program.
 */
public final class Main
{
    public static void main (String[] args)
            throws IOException
    {
        args = new String[1];
        args[0] = "file:///home/mackenzie/Input.cfg";

        if (args.length != 1)
        {
            help();
        }

        /**
         * Step: Load the configuration file.
         */
        final ConfigLoader loader = new ConfigLoader();
        final String path = args[0];
        loader.load(new URL(path));
        final Optional<SList> config = loader.result();

        if (config.isPresent())
        {
            System.out.println(config.get());
        }
        else
        {
            System.out.println(loader.errorMessage());
            System.exit(1);
        }

        /**
         * Step: Create the controller, message-processor(s),
         * message-queue(s), and module(s).
         */
        final Configurator builder = new Configurator();
        final StandardController controller = builder.load(config.get());

        /**
         * Step: Hand off control to the controller.
         */
        final int exitCode = controller.run();
        System.exit(exitCode);
    }

    private static void help ()
    {
        System.out.println("Help!");
    }
}
