package com.mackenziehigh.loader.modules.common;

import com.mackenziehigh.loader.ConfigObject;
import com.mackenziehigh.loader.Controller;
import com.mackenziehigh.loader.AbstractModule;

/**
 * An instance of this class forwards a message from one
 * topic to another topic, while tallying the messages.
 * The tally will be stored internally, until a message
 * is received from a specially designated topic.
 * Thereafter, the tally will be sent via yet another topic.
 */
public final class Counter
        implements AbstractModule
{
    public Counter ()
    {
    }

    @Override
    public void stop ()
    {
        AbstractModule.super.stop();
    }

    @Override
    public boolean start ()
    {
        return AbstractModule.super.start();
    }

    @Override
    public boolean setup (Controller controller,
                          String name,
                          ConfigObject configuration)
    {
        return AbstractModule.super.setup(controller, name, configuration);
    }

}
