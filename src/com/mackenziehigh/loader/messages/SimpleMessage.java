package com.mackenziehigh.loader.messages;

import com.mackenziehigh.loader.AbstractModule;
import com.mackenziehigh.loader.Message;
import com.mackenziehigh.loader.UniqueID;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author mackenzie
 */
public final class SimpleMessage
        implements Message
{
    private static final UniqueID prefix = UniqueID.random();

    private static final AtomicLong counter = new AtomicLong();

    private final UniqueID uniqueID = UniqueID.combine(prefix, UniqueID.fromBytes(counter.incrementAndGet()));

    private final AbstractModule module;

    private final Object content;

    private final UniqueID correlationID;

    private final long creationTime = System.currentTimeMillis();

    public SimpleMessage (final AbstractModule module,
                          final Object content)
    {
        this.module = module;
        this.content = content;
        this.correlationID = null;
    }

    public SimpleMessage (final AbstractModule module,
                          final Object content,
                          final UniqueID correlationID)
    {
        this.module = module;
        this.content = content;
        this.correlationID = correlationID;
    }

    @Override
    public String controllerName ()
    {
        return module.controller().name();
    }

    @Override
    public UniqueID controllerID ()
    {
        return module.controller().uniqueID();
    }

    @Override
    public String processorName ()
    {
        return null;
    }

    @Override
    public UniqueID processorID ()
    {
        return null;
    }

    @Override
    public String moduleName ()
    {
        return module.name();
    }

    @Override
    public UniqueID moduleID ()
    {
        return module.uniqueID();
    }

    @Override
    public UniqueID uniqueID ()
    {
        return correlationID;
    }

    @Override
    public UniqueID correlationID ()
    {
        return correlationID;
    }

    @Override
    public Instant creationTime ()
    {
        return Instant.ofEpochMilli(creationTime);
    }

    @Override
    public Object content ()
    {
        return content;
    }
}
