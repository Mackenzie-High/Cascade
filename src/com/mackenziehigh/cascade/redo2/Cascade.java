package com.mackenziehigh.cascade.redo2;

import com.mackenziehigh.cascade.allocators.Allocator;
import java.util.Set;
import java.util.UUID;

/**
 *
 */
public final class Cascade
{
    private Cascade ()
    {
        // Pass.
    }

    public static Cascade create ()
    {
        return new Cascade();
    }

    public Cascade setLoggerFactory (final CascadeLogger.Factory logger)
    {
        return this;
    }

    public CascadeLogger.Factory getLoggerFactory ()
    {
        return null;
    }

    public Cascade setAllocator (final Allocator allocator)
    {
        return this;
    }

    public Allocator getAllocator (final Allocator allocator)
    {
        return null;
    }

    public CascadeStage newStage ()
    {
        return newPooledStage(1);
    }

    public CascadeStage newPooledStage (final int threadPoolSize)
    {
        return null;
    }

    public UUID uniqueId ()
    {
        return null;
    }

    public boolean isAlive ()
    {
        return true;
    }

    public void close ()
    {

    }

    public Set<CascadeStage> reactors ()
    {
        return null;
    }

    public Cascade send (final String event,
                         final CascadeOperand stack)
    {
        return null;
    }

    public Cascade send (final CascadeToken event,
                         final CascadeOperand stack)
    {
        return null;
    }
}
