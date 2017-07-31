package com.mackenziehigh.cascade.modules.common;

import com.mackenziehigh.cascade.AbstractModule;
import com.mackenziehigh.cascade.Message;
import com.mackenziehigh.cascade.MessageQueue;
import com.mackenziehigh.cascade.UniqueID;
import com.mackenziehigh.sexpr.SAtom;
import com.mackenziehigh.sexpr.SList;
import com.mackenziehigh.sexpr.SexprSchema;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An instance of this class forwards a message from one
 * topic to another topic, sending the tally of messages
 * to another queue immediately.
 */
public final class Counter
        extends AbstractModule
{
    private final UniqueID sourceID = UniqueID.random();

    private final AtomicLong sequenceNumber = new AtomicLong(-1);

    @Override
    public void setup ()
            throws Throwable
    {
        SexprSchema.fromResource("/com/mackenziehigh/cascade/resources/Counter.txt")
                .pass("INIT")
                .after("INIT", "configuration", node -> configure(node.toList()))
                .setFailureHandler(SexprSchema.PRINT_FAILURE)
                .build()
                .match(configuration());
    }

    private void configure (final SList node)
    {
        // configuration = [ "send" , "count" , "of" , source , "to" , destination ];
        final String sourceName = node.get(3).toAtom().content();
        final String destinationName = node.get(5).toAtom().content();
        final MessageQueue sourceQueue = controller().queues().get(sourceName);
        final MessageQueue destinationQueue = controller().queues().get(destinationName);
        sourceQueue.bind(x -> onTick(destinationQueue));
    }

    private void onTick (final MessageQueue destinationQueue)
    {
        final long seqnum = sequenceNumber.incrementAndGet();
        final SAtom count = new SAtom(seqnum + 1);
        final Message message = Message.newMessage(name(), sourceID, seqnum, count);
        destinationQueue.send(message);
    }

}
