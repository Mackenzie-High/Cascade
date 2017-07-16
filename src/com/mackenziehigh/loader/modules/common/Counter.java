package com.mackenziehigh.loader.modules.common;

import com.mackenziehigh.loader.ConfigObject;
import com.mackenziehigh.loader.Controller;
import com.mackenziehigh.loader.Module;

/**
 * An instance of this class forwards a message from one
 * topic to another topic, while tallying the messages.
 * The tally will be stored internally, until a message
 * is received from a specially designated topic.
 * Thereafter, the tally will be sent via yet another topic.
 */
public final class Counter
        implements Module
{
    public Counter ()
    {
    }

    @Override
    public void stop ()
    {
        Module.super.stop();
    }

    @Override
    public boolean start ()
    {
        return Module.super.start();
    }

    @Override
    public boolean setup (Controller controller,
                          String name,
                          ConfigObject configuration)
    {
        return Module.super.setup(controller, name, configuration);
    }

}
