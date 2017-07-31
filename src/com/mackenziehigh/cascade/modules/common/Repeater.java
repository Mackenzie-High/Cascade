package com.mackenziehigh.cascade.modules.common;

import com.mackenziehigh.cascade.AbstractModule;
import com.mackenziehigh.cascade.MessageQueue;
import com.mackenziehigh.sexpr.SList;
import com.mackenziehigh.sexpr.SexprSchema;
import java.util.stream.IntStream;

/**
 * An instance of this class create (N) duplicates
 * of the reference to a message object received from
 * one queue and then forwards the (N) references
 * to another queue.
 */
public final class Repeater
        extends AbstractModule
{
    @Override
    public void setup ()
            throws Throwable
    {
        SexprSchema.fromResource("/com/mackenziehigh/cascade/resources/Repeater.txt")
                .pass("INIT")
                .after("INIT", "configuration", node -> configure(node.toList()))
                .setFailureHandler(SexprSchema.PRINT_FAILURE)
                .build()
                .match(configuration());
    }

    private void configure (final SList node)
    {
        // configuration = [ "send" , count , "copies" , "of" , source , "to" , destination ];
        final int count = node.get(1).toAtom().asInt().get();
        final String sourceName = node.get(4).toAtom().content();
        final String destinationName = node.get(6).toAtom().content();
        final MessageQueue sourceQueue = controller().queues().get(sourceName);
        final MessageQueue destinationQueue = controller().queues().get(destinationName);
        sourceQueue.bind(x -> IntStream.range(0, count).forEach(i -> destinationQueue.send(x)));
    }
}
