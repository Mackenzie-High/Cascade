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
 * An instance of this class sums integers
 * from one queue and sends them to another.
 */
public final class Summation
        extends AbstractModule
{
    private final UniqueID sourceID = UniqueID.random();

    private final AtomicLong sequenceNumber = new AtomicLong(-1);

    private final AtomicLong sum = new AtomicLong();

    @Override
    public void setup ()
            throws Throwable
    {
        SexprSchema.fromResource("/com/mackenziehigh/cascade/resources/Summation.txt")
                .pass("INIT")
                .after("INIT", "configuration", node -> configure(node.toList()))
                .build()
                .match(configuration());
    }

    private void configure (final SList node)
    {
        // configuration = [ "send" , "sum" , "of" , source , "to" , destination ];
        final String sourceName = node.get(3).toAtom().content();
        final String destinationName = node.get(5).toAtom().content();
        final MessageQueue sourceQueue = controller().queues().get(sourceName);
        final MessageQueue destinationQueue = controller().queues().get(destinationName);
        sourceQueue.bind(x -> onTick(destinationQueue, x.content().toAtom().asLong().get()));
    }

    private void onTick (final MessageQueue destinationQueue,
                         final long value)
    {
        final long seqnum = sequenceNumber.incrementAndGet();
        final SAtom content = new SAtom(sum.addAndGet(value));
        final Message message = Message.newMessage(name(), sourceID, seqnum, content);
        destinationQueue.send(message);
    }
}
