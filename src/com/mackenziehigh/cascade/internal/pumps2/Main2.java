package com.mackenziehigh.cascade.internal.pumps2;

import com.mackenziehigh.cascade.internal.pumps3.LongTransactionalMultiQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author mackenzie
 */
public final class Main2
{
    public static final Lock lock = new ReentrantLock();

    public static void main (String[] args)
    {
        long sum = 0;

        final int[] locals = new int[64];
        locals[0] = 128;
        final LongTransactionalMultiQueue tq = new LongTransactionalMultiQueue(64, locals);
        final LongTransactionalMultiQueue.TransactionQueue q = tq.members().get(0);

        for (int i = 0; i < 69; i++)
        {
            if (q.begin(1, TimeUnit.SECONDS))
            {
                q.commit(i);
            }
        }

        final AtomicLong out = new AtomicLong();
        for (int i = 0; i < 128; i++)
        {
            System.out.println("X = " + q.poll());
        }

    }
}
