package com.mackenziehigh.cascade.redo2;

import com.mackenziehigh.cascade.allocators.Allocator;
import java.util.Set;
import java.util.UUID;

/**
 *
 */
public interface CascadeStage
{
    public UUID uniqueId ();

    public Cascade cascade ();

    public CascadeStage useLoggerFactory (final CascadeLogger.Factory logger);

    public CascadeLogger.Factory getLoggerFactory ();

    public CascadeStage useAllocator (final Allocator allocator);

    public Allocator getAllocator (final Allocator allocator);

    public Set<CascadeActor> actors ();

    public CascadeActor newActor (CascadeScript script);

    /**
     *
     * Actor must be on this same stage!
     *
     * @param builder
     * @return
     */
    public CascadeActor newActor (CascadeActor.Builder builder);

    public <T extends CascadeActor.Builder> T newActor (CascadeActor.Builder.Factory<T> builder);

    public boolean isAlive ();

    public CascadeStage close ();
}
