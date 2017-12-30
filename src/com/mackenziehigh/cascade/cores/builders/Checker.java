package com.mackenziehigh.cascade.cores.builders;

import com.mackenziehigh.cascade.CascadeReactor;

/**
 * Verifies a series of event-messages using a series of predicates.
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

//    public Checker expect (final Predicate<Context> condition)
//    {
//
//    }
    @Override
    public CascadeReactor.Core build ()
    {
        return null;
    }

}
