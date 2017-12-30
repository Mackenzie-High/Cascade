package com.mackenziehigh.cascade.cores.builders;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mackenziehigh.cascade.CascadeReactor;
import com.mackenziehigh.cascade.CascadeReactor.Core;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.commons.CascadeProperty;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tally based rate limiting.
 */
public final class TallyLimiter
        implements CascadeReactor.CoreBuilder
{
    /**
     * (Required) This is the name of the event-channel use event-messages
     * will be forwarded to the output event-channel upto the given limit.
     */
    public final CascadeProperty<CascadeToken> input = CascadeProperty.<CascadeToken>newBuilder("input")
            .makeFinal()
            .build();

    /**
     * (Required) This is the name of the event-channel to that will
     * receive event-messages from the input event-channel.
     */
    public final CascadeProperty<CascadeToken> output = CascadeProperty.<CascadeToken>newBuilder("output")
            .makeFinal()
            .build();

    /**
     * (Optional) Whenever an event-message is received over this event-channel,
     * the tally will be reset to zero. Therefore, this event-channel can be used
     * to unblock the flow from the input to output event-channels.
     */
    public final CascadeProperty<CascadeToken> reset = CascadeProperty.<CascadeToken>newBuilder("reset")
            .makeFinal()
            .build();

    /**
     * (Required) This is the maximum number of messages to allow through
     * before blocking the flow from the input to output event-channels.
     */
    public final CascadeProperty<Long> limit = CascadeProperty.<Long>newBuilder("limit")
            .makeFinal()
            .build();

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeReactor.Core build ()
    {
        final AtomicLong tally = new AtomicLong();

        final List<CascadeToken> initial = Lists.newArrayList();

        final CascadeToken inputId = input.getOrDefault(null);
        final CascadeToken resetId = reset.getOrDefault(null);
        final CascadeToken outputId = output.getOrDefault(null);
        final long max = limit.getOrDefault(Long.MAX_VALUE);

        if (input.isSet())
        {
            initial.add(inputId);
        }

        if (reset.isSet())
        {
            initial.add(resetId);
        }

        return new Core()
        {
            @Override
            public Set<CascadeToken> initialSubscriptions ()
            {
                return ImmutableSet.copyOf(initial);
            }

            @Override
            public void onMessage (final CascadeReactor.Context context)
                    throws Throwable
            {
                if (context.event().equals(resetId))
                {
                    tally.set(0);
                }
                else if (tally.get() < max)
                {
                    tally.incrementAndGet();
                    context.broadcast(outputId, context.message()); // TODO: Change to send()
                }
            }

        };
    }

}
