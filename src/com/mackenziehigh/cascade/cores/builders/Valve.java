package com.mackenziehigh.cascade.cores.builders;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mackenziehigh.cascade.CascadeReactor;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.commons.CascadeProperty;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This type of core conditionally forwards event-messages
 * from an inpurt event-channel to an output event-channel,
 * which effectively implements an on/off flow-control valve.
 */
public final class Valve
        implements CascadeReactor.CoreBuilder
{
    /**
     * (Required) This is the name of the event-channel use event-messages
     * will be conditionally forwarded to the output event-channel.
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
     *
     */
    public final CascadeProperty<CascadeToken> enable = CascadeProperty.<CascadeToken>newBuilder("enable")
            .makeFinal()
            .build();

    /**
     *
     */
    public final CascadeProperty<CascadeToken> disable = CascadeProperty.<CascadeToken>newBuilder("disable")
            .makeFinal()
            .build();

    public final CascadeProperty<CascadeToken> condition = CascadeProperty.<CascadeToken>newBuilder("condition")
            .makeFinal()
            .build();

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeReactor.Core build ()
    {
        final AtomicBoolean enabled = new AtomicBoolean();

        final List<CascadeToken> initial = Lists.newArrayList();

        final CascadeToken enableId = enable.getOrDefault(null);
        final CascadeToken disableId = disable.getOrDefault(null);
        final CascadeToken conditionId = condition.getOrDefault(null);
        final CascadeToken inputId = input.getOrDefault(null);
        final CascadeToken outputId = output.getOrDefault(null);

        if (input.isSet())
        {
            initial.add(inputId);
        }

        if (enable.isSet())
        {
            initial.add(enableId);
        }

        if (disable.isSet())
        {
            initial.add(disableId);
        }

        if (condition.isSet())
        {
            initial.add(conditionId);
        }

        return new CascadeReactor.Core()
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
                if (context.event().equals(enableId))
                {
                    enabled.set(true);
                }
                else if (context.event().equals(disableId))
                {
                    enabled.set(false);
                }
                else if (context.event().equals(conditionId))
                {
                    // TODO: True or False on Stack
                }
                else if (enabled.get())
                {
                    context.async(outputId, context.message());
                }
            }

        };
    }
}
