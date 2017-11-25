package com.mackenziehigh.cascade.internal.pumps2;

import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import java.util.Set;

/**
 *
 */
public interface Connector
{
    public interface Pipe
    {
        public Object lock ();

        public boolean addAsync (Object key,
                                 OperandStack message);

        public boolean addSync (Object key,
                                OperandStack message);

        public void rollback (Object key);

        public void commit (Object key);

        public void unlock (Object key);

        public int size ();

        public int capacity ();

        public boolean poll (OperandStack out);
    }

    public Set<Pipe> pipes ();

    public int size ();

    public int capacity ();

    public Pipe block (final long timeoutMillis);

    public void close ();
}
