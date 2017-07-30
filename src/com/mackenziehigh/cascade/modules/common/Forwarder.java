package com.mackenziehigh.cascade.modules.common;

import com.mackenziehigh.cascade.AbstractModule;
import com.mackenziehigh.cascade.MessageQueue;
import com.mackenziehigh.sexpr.SList;
import com.mackenziehigh.sexpr.SexprSchema;

/**
 * An instance of this class unconditionally forwards
 * messages from one queue to another.
 *
 * <p>
 * More than one (input, output) pair can be specified,
 * per instance of this class in order to make usage easier.
 * </p>
 *
 * <p>
 * An instance of this module can be used to implement funnels.
 * A funnel is an (N -> 1) mapping of queues to a single queue.
 * </p>
 *
 * <p>
 * An instance of this module can be used to implement fanouts.
 * A fanout is an (1 -> N) mapping from a single queue to multiple queues.
 * </p>
 */
public final class Forwarder
        extends AbstractModule
{
    @Override
    public void setup ()
            throws Throwable
    {
        SexprSchema.fromResource("/com/mackenziehigh/cascade/resources/Forwarder.txt")
                .pass("INIT")
                .after("INIT", "mapping", node -> addRedirect(node.toList()))
                .build()
                .match(configuration());
    }

    private void addRedirect (final SList node)
    {
        final String sourceQueueName = node.get(1).toAtom().content();
        final String destinationQueueName = node.get(3).toAtom().content();
        final MessageQueue sourceQueue = controller().queues().get(sourceQueueName);
        final MessageQueue destinationQueue = controller().queues().get(destinationQueueName);
        sourceQueue.bind(x -> destinationQueue.send(x));
    }

}
