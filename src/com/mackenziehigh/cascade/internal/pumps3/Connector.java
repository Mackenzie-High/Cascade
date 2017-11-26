package com.mackenziehigh.cascade.internal.pumps3;

import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public interface Connector
{
    public interface Connection
    {
        public int id ();

        public Object lockSync (long timeout,
                                TimeUnit timeoutUnits);

        public Object lockAsync ();

        public void commit (Object key,
                            OperandStack message);

        public void unlock (Object key);

        public int localSize ();

        public int localCapacity ();

        public void close ();
    }

    public List<Connection> connections ();

    public int globalSize ();

    public int globalCapacity ();

    public void close ();
}
