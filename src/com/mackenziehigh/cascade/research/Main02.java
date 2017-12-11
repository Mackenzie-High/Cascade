package com.mackenziehigh.cascade.research;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mackenzie
 */
public class Main02
{

    public static void main (String[] args)
    {
        final Semaphore barrier = new Semaphore(1);
        barrier.drainPermits();

        final AtomicLong counter = new AtomicLong();

        final Runnable producer = () ->
        {
            while (true)
            {
                try
                {
                    barrier.tryAcquire(1, TimeUnit.SECONDS);

                    counter.incrementAndGet();

                    barrier.release();
                }
                catch (InterruptedException ex)
                {
                    Logger.getLogger(Main02.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        final Runnable consumer = () ->
        {
            while (true)
            {
                try
                {
                    barrier.acquire();

                    if (counter.get() % 1_000_000 == 0)
                    {
                        System.out.println("X = " + counter.get());
                    }

                    barrier.release();
                }
                catch (InterruptedException ex)
                {
                    Logger.getLogger(Main02.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        final Thread p = new Thread(producer);
        final Thread q = new Thread(consumer);
        p.start();
        q.start();
        barrier.release();
    }
}
