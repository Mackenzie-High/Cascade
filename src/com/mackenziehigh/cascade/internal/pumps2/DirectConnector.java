package com.mackenziehigh.cascade.internal.pumps2;

import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import java.util.Set;

/**
 * For use by Direct Pumps
 */
public final class DirectConnector
        implements Connector
{
    private final class DirectPipe
            implements Pipe
    {
        @Override
        public Object lock ()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean addAsync (Object lock,
                                 OperandStack message)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean addSync (Object lock,
                                OperandStack message)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void rollback (Object lock)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void commit (Object lock)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void unlock (Object key)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int size ()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int capacity ()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean poll (OperandStack out)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    @Override
    public Set<Pipe> pipes ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int size ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int capacity ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Pipe block (long timeoutMillis)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
