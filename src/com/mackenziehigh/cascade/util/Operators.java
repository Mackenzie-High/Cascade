package com.mackenziehigh.cascade.util;

import com.mackenziehigh.cascade.CascadeStack;

/**
 *
 */
public final class Operators
{
    public interface BinaryOperator
    {
        public void acceptIntInt (CascadeStack out,
                                  CascadeStack left,
                                  CascadeStack right);
    }

    public void widenAndExecute (CascadeStack stack,
                                 BinaryOperator operator)
    {

    }
}
