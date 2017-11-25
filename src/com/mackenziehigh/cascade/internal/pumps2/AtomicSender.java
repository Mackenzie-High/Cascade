package com.mackenziehigh.cascade.internal.pumps2;

import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.internal.pumps2.Connector.Pipe;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides the algorithm needed to send
 * a message atomically to multiple independent pipes.
 */
public final class AtomicSender
{
    private final ArrayList<Pipe> outputs;

    private final ArrayList<Object> keys;

    public AtomicSender (final List<Pipe> outputs)
    {
        this.outputs = new ArrayList<>(outputs);
        this.keys = new ArrayList<>(outputs.size());
    }

    public void send (final boolean block,
                      final OperandStack message)
    {
        /**
         * Acquire the locks.
         */
        for (int i = 0; i < keys.size(); i++)
        {
            keys.set(i, outputs.get(i).lock()); // TODO: Async????
        }

        /**
         * Pass the message to the pipes.
         */
        boolean failed = false;
        for (int i = 0; i < keys.size(); i++)
        {
            if (block)
            {
                failed |= outputs.get(i).addSync(keys.get(i), message);
            }
            else
            {
                failed |= outputs.get(i).addAsync(keys.get(i), message);
            }
        }

        /**
         * If any failures occurred, then the send is not atomic,
         * so rollback the transaction; otherwise, the send is atomic;
         * therefore, go ahead and commit the transaction.
         */
        if (failed)
        {
            for (int i = 0; i < keys.size(); i++)
            {
                outputs.get(i).rollback(keys.get(i));
            }
        }
        else
        {
            for (int i = 0; i < keys.size(); i++)
            {
                outputs.get(i).commit(keys.get(i));
            }
        }

        /**
         * Release the locks.
         */
        for (int i = 0; i < keys.size(); i++)
        {
            outputs.get(i).unlock(keys.get(i));
        }
        keys.clear();
    }

}
