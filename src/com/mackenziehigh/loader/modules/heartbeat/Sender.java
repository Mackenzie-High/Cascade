package com.mackenziehigh.loader.modules.heartbeat;

import com.mackenziehigh.loader.ConfigObject;
import com.mackenziehigh.loader.Controller;
import com.mackenziehigh.loader.Module;
import com.mackenziehigh.loader.TopicKey;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * An instance of this class periodically sends
 * a message to a user-defined topic.
 */
public final class Sender
        implements Module
{
    private Controller controller;

    private final Timer heart = new Timer(true);

    private TimerTask task;

    private long period;

    @Override
    public boolean setup (Controller controller,
                          String name,
                          ConfigObject configuration)
    {
        this.controller = controller;
        period = configuration.asMap().get().get("period").asInteger().get();
        final String topic = configuration.asMap().get().get("topic").asString().get();

        final String uuid = UUID.randomUUID().toString();

        task = new TimerTask()
        {
            @Override
            public void run ()
            {
                controller.send(TopicKey.get(topic), name + ":" + System.currentTimeMillis());
            }
        };

        return true;
    }

    @Override
    public boolean start ()
    {
        heart.scheduleAtFixedRate(task, 1000, period);
        return true;
    }

    @Override
    public void stop ()
    {

    }
}
