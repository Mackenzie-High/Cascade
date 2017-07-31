package com.mackenziehigh.cascade.modules.common;

import com.google.common.io.Resources;
import com.mackenziehigh.cascade.AbstractModule;
import com.mackenziehigh.cascade.Message;
import com.mackenziehigh.cascade.MessageQueue;
import com.mackenziehigh.cascade.UniqueID;
import com.mackenziehigh.sexpr.SAtom;
import com.mackenziehigh.sexpr.SList;
import com.mackenziehigh.sexpr.Sexpr;
import com.mackenziehigh.sexpr.SexprSchema;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An instance of this class is a poller that periodically
 * polls a file or URL, whenever a tick-event is received
 * from a specially designated queue, and then sends
 * the content of the file/URL to another queue.
 */
public final class ResourcePoller
        extends AbstractModule
{
    private final UniqueID sourceID = UniqueID.random();

    private final AtomicLong sequenceNumber = new AtomicLong(-1);

    @Override
    public void setup ()
            throws Throwable
    {
        SexprSchema.fromResource("/com/mackenziehigh/cascade/resources/ResourcePoller.txt")
                .pass("INIT")
                .after("INIT", "mapping", node -> addMapping(node.toList()))
                .build()
                .match(configuration());
    }

    private void addMapping (final SList node)
    {
        final URL source;

        try
        {
            source = new URL(node.get(1).toAtom().content());
        }
        catch (MalformedURLException ex)
        {
            logger().error(ex);
            return;
        }

        final String type = node.get(3).toAtom().content();
        final String destinationName = node.get(5).toAtom().content();
        final String clockName = node.get(7).toAtom().content();
        final MessageQueue destinationQueue = controller().queues().get(destinationName);
        final MessageQueue clockQueue = controller().queues().get(clockName);
        clockQueue.bind(x -> onTick(source, type, destinationQueue));
    }

    private void onTick (final URL source,
                         final String type,
                         final MessageQueue destinationQueue)
            throws IOException
    {
        final Sexpr content;

        /**
         * Read (and maybe parse) the resource file.
         */
        if (type.equals("bytes"))
        {
            final byte[] array = Resources.asByteSource(source).read();
            content = new SAtom(array);
        }
        else // sexpr
        {
            final String text = Resources.asCharSource(source, Charset.forName("UTF-8")).read();
            content = SList.parse(source.toString(), text);
        }

        /**
         * Send the resource to any interested receivers.
         */
        final Message message = Message.newMessage(name(), sourceID, sequenceNumber.incrementAndGet(), content);
        destinationQueue.send(message);
    }

}
