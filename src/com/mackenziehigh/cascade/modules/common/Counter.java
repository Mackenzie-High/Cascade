package com.mackenziehigh.cascade.modules.common;

import com.mackenziehigh.cascade.AbstractModule;

/**
 * An instance of this class forwards a message from one
 * topic to another topic, sending the tally of messages
 * to another queue immediately.
 */
public final class Counter
        extends AbstractModule
{
    @Override
    public void setup ()
            throws Throwable
    {
        super.setup();
    }

}
