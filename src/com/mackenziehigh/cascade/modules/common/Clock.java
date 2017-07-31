package com.mackenziehigh.cascade.modules.common;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.mackenziehigh.cascade.AbstractModule;
import com.mackenziehigh.cascade.Message;
import com.mackenziehigh.cascade.MessageQueue;
import com.mackenziehigh.cascade.UniqueID;
import com.mackenziehigh.sexpr.SList;
import com.mackenziehigh.sexpr.Sexpr;
import com.mackenziehigh.sexpr.SexprSchema;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An instance of this class is a module that periodically
 * sends tick-events based on a user-specified configuration.
 */
public final class Clock
        extends AbstractModule
{

    private final ScheduledExecutorService clock = Executors.newScheduledThreadPool(1);

    private final UniqueID sourceID = UniqueID.random();

    private final AtomicLong sequenceNumber = new AtomicLong(-1);

    private final Table<Long, Long, Runnable> tasks = HashBasedTable.create();

    @Override
    public void setup ()
            throws Throwable
    {
        SexprSchema.fromResource("/com/mackenziehigh/cascade/resources/Clock.txt")
                .pass("INIT")
                .after("INIT", "after", node -> addAfterTask(node.toList()))
                .after("INIT", "every", node -> addEveryTask(node.toList()))
                .build()
                .match(configuration());
    }

    private void addAfterTask (final SList node)
    {
        final long delay = node.get(1).toAtom().asInt().get();
        final TimeUnit delayUnits = TimeUnit.valueOf(node.get(2).toAtom().content().toUpperCase());
        final Sexpr value = node.get(4);
        final String queueName = node.get(6).toAtom().content();
        final long period = node.get(8).toAtom().asInt().get();
        final TimeUnit periodUnits = TimeUnit.valueOf(node.get(9).toAtom().content().toUpperCase());
        addTask(delayUnits.toNanos(delay), periodUnits.toNanos(period), value, queueName);
    }

    private void addEveryTask (final SList node)
    {
        final Sexpr value = node.get(1);
        final String queueName = node.get(3).toAtom().content();
        final long period = node.get(5).toAtom().asInt().get();
        final TimeUnit periodUnits = TimeUnit.valueOf(node.get(6).toAtom().content().toUpperCase());
        addTask(0, periodUnits.toNanos(period), value, queueName);
    }

    private void addTask (final long delay,
                          final long period,
                          final Sexpr value,
                          final String queueName)
    {
        final MessageQueue queue = controller().queues().get(queueName);

        final Runnable task = () ->
        {
            final Message message = Message.newMessage(name(), sourceID, sequenceNumber.incrementAndGet(), value);
            queue.send(message);
        };

        tasks.put(delay, period, task);
    }

    @Override
    public void start ()
    {
        for (Cell<Long, Long, Runnable> cell : tasks.cellSet())
        {
            final long delay = cell.getRowKey();
            final long period = cell.getColumnKey();
            final Runnable task = cell.getValue();
            clock.scheduleAtFixedRate(task, delay, period, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public void stop ()
    {
        clock.shutdownNow();
    }
}
