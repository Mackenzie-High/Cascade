package com.mackenziehigh.cascade.internal.pumps2;

import com.google.common.base.Verify;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public final class SimpleConnector
        implements Connector
{
    private final Set<Pipe> pipes = new HashSet<>();

    private final Set<Pipe> unmodPipes = Collections.unmodifiableSet(pipes);

    private final BlockingQueue<Pipe> blockQueue = new ArrayBlockingQueue<>(100 * 16);

    @Override
    public Set<Pipe> pipes ()
    {
        return unmodPipes;
    }

    @Override
    public int size ()
    {
        return unmodPipes.stream().mapToInt(x -> x.size()).sum();
    }

    @Override
    public int capacity ()
    {
        return unmodPipes.stream().mapToInt(x -> x.capacity()).sum();
    }

    @Override
    public Pipe block (final long timeoutMillis)
    {
        try
        {
            return blockQueue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException ex)
        {
            return null;
        }
    }

    @Override
    public void close ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private final class SimplePipe
            implements Connector.Pipe
    {

        private final Semaphore lock = new Semaphore(1);

        private final Object keyObject = new Object();

        private volatile Object state = keyObject;

        private final int capacity = 16;

        private final Queue<OperandStack> dataQueue = new ArrayBlockingQueue<>(capacity);

        private volatile OperandStack uncommitted = null;

        @Override
        public Object lock ()
        {
            lock.acquireUninterruptibly();
            Verify.verify(state != null, "Someone has the KEY!");
            return keyObject;
        }

        @Override
        public boolean addAsync (final Object key,
                                 final OperandStack message)
        {
            Verify.verify(uncommitted == null);

            if (Objects.equals(key, keyObject) == false)
            {
                throw new IllegalArgumentException("Wrong Key");
            }
            else
            {
                uncommitted = message.copy();
            }

            return false;
        }

        @Override
        public boolean addSync (final Object key,
                                final OperandStack message)
        {
            Verify.verify(uncommitted == null);

            if (Objects.equals(key, keyObject) == false)
            {
                throw new IllegalArgumentException("Wrong Key");
            }
            else
            {
                uncommitted = message.copy();
            }

            return true;
        }

        @Override
        public void rollback (Object key)
        {
            if (Objects.equals(key, keyObject) == false)
            {
                throw new IllegalArgumentException("Wrong Key");
            }
            else if (uncommitted != null)
            {
                uncommitted.close();
                uncommitted = null;
            }
        }

        @Override
        public void commit (Object key)
        {
            if (Objects.equals(key, keyObject) == false)
            {
                throw new IllegalArgumentException("Wrong Key");
            }
            else
            {
                Verify.verify(dataQueue.size() < capacity);
                dataQueue.add(uncommitted);
                uncommitted = null;
                blockQueue.add(this);
            }
        }

        @Override
        public void unlock (Object key)
        {
            if (Objects.equals(key, keyObject) == false)
            {
                throw new IllegalArgumentException("Wrong Key");
            }
            else
            {
                state = keyObject;
                lock.release();
            }
        }

        @Override
        public int size ()
        {
            return dataQueue.size();
        }

        @Override
        public int capacity ()
        {
            return capacity;
        }

        @Override
        public boolean poll (final OperandStack out)
        {
            final OperandStack value = dataQueue.poll();
            out.set(value);
            if (value != null)
            {
                value.close();
            }
            return value != null;
        }
    }
}
