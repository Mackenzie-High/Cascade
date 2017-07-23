package com.mackenziehigh.loader.modules.common;

import com.mackenziehigh.loader.ConfigObject;
import com.mackenziehigh.loader.ConfigSchema;
import com.mackenziehigh.loader.QueueKey;
import com.mackenziehigh.loader.Controller;
import com.mackenziehigh.loader.AbstractModule;

/**
 * An instance of this class receives messages from a set of topics
 * and forwards them to a single topic.
 *
 * In effect, this funnels the messages into a single topic.
 */
public final class Funnel
        implements AbstractModule
{
    private final ConfigSchema schema = new ConfigSchema();

    private Controller controller;

    private QueueKey output;

    public Funnel ()
    {
        schema.requireMap();
        schema.entry("inputs").required().requireList().each().requireString().onPresent(x -> addInput(x));
        schema.entry("output").required().requireString().onPresent(x -> setOutput(x));
    }

    @Override
    public boolean setup (final Controller controller,
                          final String name,
                          final ConfigObject configuration)
    {
        this.controller = controller;
        this.schema.apply(configuration);
        return true;
    }

    private void addInput (final ConfigObject value)
    {
        final QueueKey topic = QueueKey.get(value.asString().get());
        controller.register(topic, message -> controller.send(output, message));
    }

    private void setOutput (final ConfigObject value)
    {
        output = QueueKey.get(value.asString().get());
    }
}
