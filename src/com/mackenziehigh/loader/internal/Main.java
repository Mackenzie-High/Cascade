package com.mackenziehigh.loader.internal;

import com.google.common.io.Files;
import com.mackenziehigh.loader.internal.parser.Parser;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;

/**
 * Program Entry Point.
 */
public final class Main
{
    public static void main (String[] args)
            throws MalformedURLException,
                   IOException
    {
        args = new String[1];
        args[0] = "/home/mackenzie/Config1.cfg";

        if (args.length != 1)
        {
            help();
        }

        final File configFile = new File(args[0]);

        if (configFile.exists() == false)
        {
            System.out.println("No Such File: " + configFile);
            return;
        }
        else if (configFile.isFile() == false)
        {
            System.out.println("Not a File: " + configFile);
            return;
        }
        else
        {
            final String content = Files.toString(configFile, Charset.forName("UTF-8"));
            final Parser parser = new Parser();
            final Configuration config = parser.parse(content);
            final MainController controller = new MainController(config);
            controller.start();
        }
    }

    private static void help ()
    {
        System.out.println("Help!");
    }
}
