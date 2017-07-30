package com.mackenziehigh.cascade.modules.common;

import com.mackenziehigh.cascade.AbstractModule;

/**
 * An instance of this class forwards a message from one
 * topic to another topic, while tallying the messages.
 * The tally will be stored internally, until a message
 * is received from a specially designated topic.
 * Thereafter, the tally will be sent via yet another topic.
 */
public final class Counter
        extends AbstractModule
{

}
