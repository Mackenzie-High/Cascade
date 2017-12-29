package com.mackenziehigh.cascade.cores.builders;

import com.mackenziehigh.cascade.CascadeReactor;

/**
 * This type of core expects to receive a fixed finite number
 * of event-messages from a single event-channel in well-defined
 * user-defined sequence, such that each of the event-messages
 * matches a specific predicate.
 *
 * <p>
 * You may find this type of core particularly useful during
 * unit-testing of other types of cores. The core under test
 * should send messages to this core. This core will verify
 * the content of the received messages.
 * </p>
 */
public final class Checker
        implements CascadeReactor.CoreBuilder
{

    @Override
    public CascadeReactor.Core build ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
