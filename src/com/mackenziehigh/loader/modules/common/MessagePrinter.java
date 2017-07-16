package com.mackenziehigh.loader.modules.common;

import com.mackenziehigh.loader.ConfigObject;
import com.mackenziehigh.loader.Controller;
import com.mackenziehigh.loader.Module;
import com.mackenziehigh.loader.TopicKey;
import java.util.List;

/**
 * An instance of this class is a module that subscribes
 * to a set of topics and prints the string representations
 * of the messages that are received there from.
 */
public final class MessagePrinter
        implements Module
{
    private Controller controller;

    @Override
    public boolean setup (Controller controller,
                          String name,
                          ConfigObject configuration)
    {
        this.controller = controller;
        final List<ConfigObject> topics = configuration.asMap().get().get("topics").asList().get();
        topics.forEach(x -> controller.register(TopicKey.get(x.asString().get()), MessagePrinter::print));
        return true;
    }

    private static void print (Object value)
    {
        System.out.println(value);
    }
}
