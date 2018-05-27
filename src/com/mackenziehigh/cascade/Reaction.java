package com.mackenziehigh.cascade;

import java.util.Optional;
import java.util.UUID;

/**
 *
 */
public interface Reaction
{
    public UUID uuid ();

    public String name ();

    public Optional<Reactor> reactor ();

}
