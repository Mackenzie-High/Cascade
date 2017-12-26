package com.mackenziehigh.cascade.internal.schema;

/**
 *
 */
public final class FairScheduler
{
//    private final ImmutableMap<Connection, Task> taskers;
//
//    private final BlockingQueue<Connection> queue = new ArrayBlockingQueue<>(128);
//
//    public RoundRobinScheduler (final Set<Connection> connections)
//    {
//
//    }
//
//    public void offer (final Connection key)
//    {
//        final Task task = taskers.get(key);
//
//        task.lock.lock();
//
//        try
//        {
//            ++task.size;
//
//            if (task.enqueued == false)
//            {
//                queue.offer(key);
//                task.enqueued = true;
//            }
//        }
//        finally
//        {
//            task.lock.unlock();
//        }
//    }
//
//    public Task poll (final long timeoutMillis)
//            throws InterruptedException
//    {
//        final Connection key = queue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
//
//        if (key == null)
//        {
//            return null;
//        }
//
//        final Task task = taskers.get(key);
//
//        task.lock.lock();
//
//        try
//        {
//            --task.size;
//            task.enqueued = false;
//            return task;
//        }
//        finally
//        {
//            task.lock.unlock();
//        }
//    }
//
//    public final class Task
//    {
//        private final Lock lock = new ReentrantLock();
//
//        private final Connection connection = null;
//
//        private int size;
//
//        private boolean enqueued = false;
//
//        public CascadeToken get (final OperandStack out)
//        {
//            return null;
//        }
//
//        public void release ()
//        {
//            lock.lock();
//
//            try
//            {
//                if (size > 0)
//                {
//                    enqueued = true;
//                    queue.offer(connection);
//                }
//            }
//            finally
//            {
//                lock.unlock();
//            }
//        }
//    }

}
