package com.mackenziehigh.cascade.reactor;

import java.util.Optional;
import java.util.UUID;

/**
 *
 * @author mackenzie
 */
public interface Reaction
{
    public UUID uuid ();

    public String name ();

    public Optional<Reactor> reactor ();

}
