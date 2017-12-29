package com.mackenziehigh.cascade.commons;

import com.mackenziehigh.cascade.CascadeSchema;
import com.mackenziehigh.cascade.internal.ConcreteSchema;

/**
 *
 */
public final class CascadeSchemas
{
    public static CascadeSchema create ()
    {
        return new ConcreteSchema();
    }

    public CascadeSchema createDefault ()
    {
        final CascadeSchema schema = create();
        schema.addDynamicPool().named("default").withMinimumSize(0).withMaximumSize(Integer.MAX_VALUE);
        schema.addPump().named("pump").withThreadCount(Runtime.getRuntime().availableProcessors() + 1);
        schema.usingPool("default");
        schema.usingPump("pump");
        return schema;
    }
}
