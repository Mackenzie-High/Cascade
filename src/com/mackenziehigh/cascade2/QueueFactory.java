package com.mackenziehigh.cascade2;

import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import java.util.Set;

/**
 * Needs to be transactional.
 */
public interface QueueFactory
{
    public interface OperandQueue
            extends AutoCloseable
    {
        public QueueFactory factory ();

        public int size ();

        public int capacity ();

        public boolean offer (Token key,
                              OperandStack operand);

        public Token poll (OperandStack out);

        @Override
        public void close ();
    }

    public int size ();

    public int capacity ();

    public OperandQueue createQueue (int capacity);

    public Set<OperandQueue> queues ();
}
