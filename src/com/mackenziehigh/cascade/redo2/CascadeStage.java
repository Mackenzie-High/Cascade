package com.mackenziehigh.cascade.redo2;

import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.allocators.Allocator;
import java.util.Set;

/**
 *
 */
public interface CascadeStage
{

    public Cascade cascade ();

    public CascadeStage useLoggerFactory (final CascadeLogger.Factory logger);

    public CascadeLogger.Factory getLoggerFactory ();

    public CascadeStage useAllocator (final Allocator allocator);

    public Allocator getAllocator (final Allocator allocator);

    public Set<CascadeActor> actors ();

    public CascadeActor newActor (CascadeScript script);

    public CascadeActor newActor (CascadeActor.Builder builder);

    public <T extends CascadeActor.Builder> T newActor (CascadeActor.Builder.Factory<T> builder);

    public boolean isAlive ();

    public CascadeStage close ();
}
