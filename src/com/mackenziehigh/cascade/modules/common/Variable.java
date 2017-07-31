package com.mackenziehigh.cascade.modules.common;

import com.mackenziehigh.cascade.AbstractModule;
import com.mackenziehigh.cascade.Message;
import com.mackenziehigh.cascade.MessageHandler;
import com.mackenziehigh.cascade.MessageQueue;
import com.mackenziehigh.cascade.UniqueID;
import com.mackenziehigh.sexpr.SAtom;
import com.mackenziehigh.sexpr.SList;
import com.mackenziehigh.sexpr.Sexpr;
import com.mackenziehigh.sexpr.SexprSchema;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An instance of this class stores the latest message (M)
 * received from one topic (X) and then forwards (M)
 * to another topic (Y) upon receiving a message
 * from a topic (Z).
 */
public final class Variable
        extends AbstractModule
{
    private final UniqueID sourceID = UniqueID.random();

    private final AtomicLong sequenceNumber = new AtomicLong(-1);

    private volatile Sexpr value = null;

    @Override
    public void setup ()
            throws Throwable
    {
        SexprSchema.fromResource("/com/mackenziehigh/cascade/resources/Variable.txt")
                .pass("INIT")
                .after("INIT", "initial", node -> addInitial(node.toList()))
                .after("INIT", "set", node -> addSet(node.toList()))
                .after("INIT", "get", node -> addGet(node.toList()))
                .after("INIT", "query", node -> addQuery(node.toList()))
                .after("INIT", "clear", node -> addClear(node.toList()))
                .setFailureHandler(SexprSchema.PRINT_FAILURE)
                .build()
                .match(configuration());
    }

    private void addInitial (final SList node)
    {
        value = node.get(2);
    }

    private void addSet (final SList node)
    {
        final String queueName = node.get(2).toAtom().content();
        final MessageQueue queue = controller().queues().get(queueName);
        queue.bind(x -> value = x.content());
    }

    private void addGet (final SList node)
    {
        final String requestQueueName = node.get(2).toAtom().content();
        final MessageQueue requestQueue = controller().queues().get(requestQueueName);
        final String responseQueueName = node.get(4).toAtom().content();
        final MessageQueue responseQueue = controller().queues().get(responseQueueName);

        final MessageHandler handler = x ->
        {
            final Sexpr content = value;
            if (content != null)
            {
                responseQueue.send(Message.newMessage(name(),
                                                      sourceID,
                                                      sequenceNumber.incrementAndGet(),
                                                      content));
            }
        };

        requestQueue.bind(handler);
    }

    private void addClear (final SList node)
    {
        final String queueName = node.get(2).toAtom().content();
        final MessageQueue queue = controller().queues().get(queueName);
        queue.bind(x -> value = null);
    }

    private void addQuery (final SList node)
    {
        final String requestQueueName = node.get(2).toAtom().content();
        final MessageQueue requestQueue = controller().queues().get(requestQueueName);
        final String responseQueueName = node.get(4).toAtom().content();
        final MessageQueue responseQueue = controller().queues().get(responseQueueName);
        requestQueue.bind(x -> responseQueue.send(Message.newMessage(name(),
                                                                     sourceID,
                                                                     sequenceNumber.incrementAndGet(),
                                                                     new SAtom(value != null))));
    }

}
