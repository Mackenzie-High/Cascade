package com.mackenziehigh.loader.modules.common;

import com.mackenziehigh.loader.ConfigObject;
import com.mackenziehigh.loader.ConfigSchema;
import com.mackenziehigh.loader.Controller;
import com.mackenziehigh.loader.Module;
import com.mackenziehigh.loader.TopicKey;

/**
 * An instance of this class receives messages from a set of topics
 * and forwards them to a single topic.
 *
 * In effect, this funnels the messages into a single topic.
 */
public final class Funnel
        implements Module
{
    private final ConfigSchema schema = new ConfigSchema();

    private Controller controller;

    private TopicKey output;

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
        final TopicKey topic = TopicKey.get(value.asString().get());
        controller.register(topic, message -> controller.send(output, message));
    }

    private void setOutput (final ConfigObject value)
    {
        output = TopicKey.get(value.asString().get());
    }
}
