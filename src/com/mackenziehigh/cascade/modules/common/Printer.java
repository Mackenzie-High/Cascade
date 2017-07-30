package com.mackenziehigh.cascade.modules.common;

import com.mackenziehigh.cascade.AbstractModule;
import com.mackenziehigh.cascade.MessageHandler;
import com.mackenziehigh.cascade.MessageQueue;
import com.mackenziehigh.sexpr.SList;
import com.mackenziehigh.sexpr.SexprSchema;

/**
 * An instance of this class is a module that subscribes
 * to a set of queues and prints the string representations
 * of the messages that are received there from.
 */
public final class Printer
        extends AbstractModule
{
    @Override
    public void setup ()
            throws Throwable
    {
        SexprSchema.fromResource("/com/mackenziehigh/cascade/resources/Printer.txt")
                .pass("INIT")
                .after("INIT", "monitor", node -> addMonitor(node.toList()))
                .build()
                .match(configuration());
    }

    private void addMonitor (final SList node)
    {
        final SList format;
        final String queueName;
        final MessageHandler handler;

        if (node.size() == 3)
        {
            // TODO
            format = node.get(1).toList();
            queueName = node.get(2).toAtom().content();
            handler = null;
        }
        else
        {
            format = SList.of();
            queueName = node.get(1).toAtom().content();
            handler = x -> System.out.println(x);
        }

        final MessageQueue queue = controller().queues().get(queueName);
        queue.bind(handler);
    }

}
