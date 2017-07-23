package com.mackenziehigh.loader.modules.common;

import com.mackenziehigh.loader.AbstractModule;
import com.mackenziehigh.loader.Message;
import com.mackenziehigh.loader.messages.SimpleMessage;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An instance of this class sends a date-time object
 * or formated string to a user-defined topic on a
 * periodic basis, per the user-specified configuration.
 */
public final class Clock
        extends AbstractModule
{

    @Override
    public void setup ()
    {
        System.out.println("Setup");
    }

    @Override
    public void start ()
    {
        System.out.println("Start");

        final AtomicInteger i = new AtomicInteger();

        final Runnable sender = () ->
        {
            final Message message = new SimpleMessage(this, i.incrementAndGet());
            controller().queues().get("Clock").send(message);
        };

        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> sender.run(), 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void stop ()
    {
        super.stop();
    }
}
