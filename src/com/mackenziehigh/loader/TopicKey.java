package com.mackenziehigh.loader;

import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An instance of this class identifies a message queue.
 */
public final class TopicKey
        implements Comparable<TopicKey>
{
    /**
     * This map contains all of the instances of this class.
     */
    private static final Map<String, TopicKey> instances = new TreeMap<>();

    /**
     * this counter is used to allocate numeric instance identifiers.
     */
    private static final AtomicInteger counter = new AtomicInteger();

    /**
     * This is the user specified name of this instance.
     */
    private final String name;

    /**
     * This is the numeric identifier of this instance.
     */
    private final int id = counter.incrementAndGet();

    /**
     * Sole Constructor.
     *
     * @param name is the user-defined name of this instance.
     */
    private TopicKey (final String name)
    {
        this.name = Objects.requireNonNull(name);
    }

    public String name ()
    {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo (final TopicKey other)
    {
        return Integer.compare(id, other.id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode ()
    {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals (final Object other)
    {
        if (this == other)
        {
            return true;
        }
        else if (other instanceof TopicKey)
        {
            return compareTo((TopicKey) other) == 0;
        }
        else
        {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString ()
    {
        return name;
    }

    /**
     * This method obtains the sole instance of this class that has the given name.
     *
     * <p>
     * A name is a case-sensitive series of one-or-more letters,
     * numbers, underscores, periods, or dollar-signs.
     * </p>
     *
     * @param name is the name of the instance to create and/or retrieve.
     * @return an instance of this class.
     * @throws NullPointerException if name is null.
     * @throws IllegalArgumentException if name is empty.
     * @throws IllegalArgumentException if the names contains illegal characters.
     */
    public synchronized static TopicKey get (final String name)
    {

        if (instances.containsKey(name) == false)
        {
            Preconditions.checkArgument(name.matches("[A-Za-z0-9_\\.$]+"), "Invalid Key: " + name);
            instances.put(name, new TopicKey(name));
        }

        return instances.get(name);
    }
}
