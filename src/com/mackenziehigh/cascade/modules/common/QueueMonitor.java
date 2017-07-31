package com.mackenziehigh.cascade.modules.common;

import com.mackenziehigh.cascade.AbstractModule;
import com.mackenziehigh.cascade.Message;
import com.mackenziehigh.cascade.MessageQueue;
import com.mackenziehigh.cascade.UniqueID;
import com.mackenziehigh.sexpr.SList;
import com.mackenziehigh.sexpr.SexprSchema;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Computes statistics regarding the messages that
 * are passing through queue(s) and then sends those
 * statistics elsewhere in tabular form.
 */
public final class QueueMonitor
        extends AbstractModule
{

    @Override
    public void setup ()
            throws Throwable
    {
        SexprSchema.fromResource("/com/mackenziehigh/cascade/resources/QueueMonitor.txt")
                .pass("INIT")
                .after("INIT", "mapping", node -> addMapping(node.toList()))
                .build()
                .match(configuration());
    }

    private void addMapping (final SList node)
    {
        final String sourceName = node.get(3).toAtom().content();
        final String destinationName = node.get(5).toAtom().content();
        final String clockName = node.get(7).toAtom().content();
        final MessageQueue sourceQueue = controller().queues().get(sourceName);
        final MessageQueue destinationQueue = controller().queues().get(destinationName);
        final MessageQueue clockQueue = controller().queues().get(clockName);
        final QueueStatistics stats = new QueueStatistics();
        sourceQueue.bind(x -> onReceive(stats, x));
        clockQueue.bind(x -> onTick(stats, destinationQueue));
    }

    private void onReceive (final QueueStatistics stats,
                            final Message message)
    {

    }

    private void onTick (final QueueStatistics stats,
                         final MessageQueue destinationQueue)
    {

    }

    private static final class QueueStatistics
    {
        private final Map<UniqueID, SourceStatistics> sources = new ConcurrentHashMap<>();

        public void record (final Message message)
        {
            sources.putIfAbsent(message.sourceID(),
                                new SourceStatistics(message.sourceName(),
                                                     message.sourceID()));
            sources.get(message.sourceID()).record(message);
        }
    }

    private static final class SourceStatistics
    {
        private final String sourceName;

        private final UniqueID sourceID;

        private final Instant startTime = Instant.now();

        private final AtomicLong messageCount = new AtomicLong();

        private final AtomicLong byteCount = new AtomicLong();

        public SourceStatistics (final String sourceName,
                                 final UniqueID sourceID)
        {
            this.sourceName = sourceName;
            this.sourceID = sourceID;
        }

        public void record (final Message message)
        {

        }
    }
}
