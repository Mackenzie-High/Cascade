package com.mackenziehigh.loader.modules.common;

import com.mackenziehigh.loader.ConfigObject;
import com.mackenziehigh.loader.ConfigSchema;
import com.mackenziehigh.loader.Controller;
import com.mackenziehigh.loader.Module;
import com.mackenziehigh.loader.TopicKey;
import java.util.ArrayList;

/**
 * An instance of this class receives a message from a single topic
 * and then forwards it to zero-or-more other topics.
 */
public final class Fanout
        implements Module
{
    private final ConfigSchema schema = new ConfigSchema();

    private Controller controller;

    private final ArrayList<TopicKey> outputs = new ArrayList<>();

    public Fanout ()
    {
        schema.requireMap();
        schema.entry("input").required().requireString().onPresent(x -> setInput(x));
        schema.entry("outputs").required().requireList().each().requireString().onPresent(x -> addOutput(x));
    }

    @Override
    public boolean setup (final Controller controller,
                          final String name,
                          final ConfigObject configuration)
    {
        this.controller = controller;
        this.schema.apply(configuration);
        this.outputs.trimToSize();
        return true;
    }

    private void setInput (final ConfigObject value)
    {
        final TopicKey topic = TopicKey.get(value.asString().get());
        controller.register(topic, message -> forward(message));
    }

    private void addOutput (final ConfigObject value)
    {
        final TopicKey topic = TopicKey.get(value.asString().get());
        outputs.add(topic);
    }

    private void forward (final Object message)
    {
        for (int i = 0; i < outputs.size(); i++)
        {
            final TopicKey topic = outputs.get(i);
            controller.send(topic, message);
        }
    }
}
