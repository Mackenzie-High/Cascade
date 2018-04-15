package com.mackenziehigh.cascade.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.math.LongMath;
import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeChannel;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 *
 */
public final class Dispatcher
{
    private final AtomicReferenceArray<Bucket> buckets;

    /**
     * Sole constructor.
     */
    private Dispatcher (final int bucketCount,
                        final int bucketCapacity,
                        final float bucketLoadFactor)
    {
        this.buckets = new AtomicReferenceArray<>(bucketCount);

        for (int i = 0; i < bucketCount; i++)
        {
            buckets.set(i, new Bucket(bucketCapacity, bucketLoadFactor));
        }
    }

    /**
     * Create a new dispatcher with default configuration settings.
     *
     * @return the new dispatcher.
     */
    public static Dispatcher newDispatcher ()
    {
        return new Dispatcher(256, 8, 0.5F);
    }

    private Bucket resolveBucketFor (final CascadeToken event)
    {
        final int idx = (int) LongMath.mod(event.hash1(), buckets.length());
        return buckets.get(idx);
    }

    /**
     * This method causes the given actor to begin receiving messages from the given event.
     *
     * <p>
     * If the actor is currently subscribed to the named event-channel,
     * then this method is simply a no-op.
     * </p>
     *
     * @param event identifies the event to listen for.
     * @param actor will receive the corresponding event-messages.
     * @return this.
     */
    public Dispatcher subscribe (final CascadeToken event,
                                 final CascadeActor actor)
    {
        final Bucket bucket = resolveBucketFor(event);

        synchronized (bucket)
        {
            if (bucket.channels.containsKey(event) == false)
            {
                bucket.channels.put(event, new Channel(bucket, event));
            }

            final Channel channel = bucket.channels.get(event);

            if (channel.subscribers.get().contains(actor))
            {
                return this;
            }

            final ImmutableList<CascadeActor> modified = ImmutableList
                    .<CascadeActor>builder()
                    .addAll(channel.subscribers.get())
                    .add(actor)
                    .build();

            channel.subscribers.set(modified);
        }

        return this;
    }

    /**
     * This method causes given the actor to stop receiving messages from the given event.
     *
     * <p>
     * If the actor is not currently subscribed to the given event,
     * then this method is simply a no-op.
     * </p>
     *
     * @param event identifies the event to no longer listen for.
     * @param actor will no longer receive the corresponding event-messages.
     * @return this.
     */
    public Dispatcher unsubscribe (final CascadeToken event,
                                   final CascadeActor actor)
    {
        final Bucket bucket = resolveBucketFor(event);

        synchronized (bucket)
        {
            final Channel channel = bucket.channels.get(event);

            if (channel == null)
            {
                return this;
            }

            if (channel.subscribers.get().contains(actor) == false)
            {
                return this;
            }

            final List<CascadeActor> modified = channel.subscribers
                    .get()
                    .stream()
                    .filter(x -> !x.equals(actor))
                    .collect(Collectors.toList());

            if (modified.isEmpty())
            {
                bucket.channels.remove(event);
            }
            else
            {
                channel.subscribers.set(ImmutableList.copyOf(modified));
            }
        }

        return this;
    }

    /**
     * This method retrieves the event-channel identified by the given token.
     *
     * @param event identifies the event-channel to find.
     * @return the channel, if it has at least one subscribed actor.
     */
    public Optional<CascadeChannel> lookup (final CascadeToken event)
    {
        final Bucket bucket = resolveBucketFor(event);
        final Channel channel = bucket.channels.get(event);

        if (channel != null)
        {
            return channel.self;
        }
        else
        {
            return Optional.empty();
        }
    }

    /**
     * Getter.
     *
     * @return an immutable map containing all the event-channels herein.
     */
    public SortedMap<CascadeToken, CascadeChannel> channels ()
    {
        final ImmutableSortedMap.Builder<CascadeToken, CascadeChannel> builder = ImmutableSortedMap.naturalOrder();

        for (int i = 0; i < buckets.length(); i++)
        {
            builder.putAll(buckets.get(i).channels);
        }

        return builder.build();
    }

    private final class Bucket
    {
        public final Map<CascadeToken, Channel> channels;

        public Bucket (final int initialCapacity,
                       final float loadFactor)
        {
            channels = new ConcurrentHashMap<>(initialCapacity, loadFactor);
        }
    }

    private final class Channel
            implements CascadeChannel
    {

        private final Bucket bucket;

        private final CascadeToken event;

        public final AtomicReference<ImmutableList<CascadeActor>> subscribers = new AtomicReference<>(ImmutableList.of());

        public final Optional<CascadeChannel> self = Optional.of(this);

        public Channel (final Bucket bucket,
                        final CascadeToken event)
        {
            this.bucket = Objects.requireNonNull(bucket, "bucket");
            this.event = Objects.requireNonNull(event, "event");
        }

        @Override
        public CascadeToken event ()
        {
            return event;
        }

        @Override
        public int subscriberCount ()
        {
            return subscribers.get().size();
        }

        @Override
        public Set<CascadeActor> subscribers ()
        {
            return ImmutableSet.copyOf(subscribers.get());
        }

        @Override
        public CascadeChannel forEachSubscriber (final Consumer<CascadeActor> functor)
        {
            final List<CascadeActor> actors = subscribers.get();

            /**
             * Do *not* use a for-each loop here in order to avoid an Iterator allocation.
             */
            for (int i = 0; i < actors.size(); i++)
            {
                functor.accept(actors.get(i));
            }

            return this;
        }

        @Override
        public CascadeChannel send (final CascadeStack stack)
        {
            final List<CascadeActor> actors = subscribers.get();

            /**
             * Do *not* use a for-each loop here in order to avoid an Iterator allocation.
             */
            for (int i = 0; i < actors.size(); i++)
            {
                actors.get(i).tell(event, stack);
            }

            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode ()
        {
            return 97 * event.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals (final Object other)
        {
            return other instanceof CascadeChannel && ((CascadeChannel) other).event().equals(event);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString ()
        {
            return event.name();
        }
    }
}
