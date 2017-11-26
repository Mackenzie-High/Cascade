package com.mackenziehigh.cascade.internal.pumps2;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test.
 */
public class Main1
{
    private static final Thread[] producers = new Thread[8];

    private static final ReservationLongQueue queue = new ReservationLongQueue(producers.length, 128);

    private static final Thread[] consumers = new Thread[4];

    private static void producerTask (int id)
    {
        int i = 0;

        try
        {
            while (true)
            {
                if (queue.reserveAsync(id))
                {
                    queue.put(id, id);
                }

                Thread.sleep(100);
            }
        }
        catch (Throwable ex)
        {
            ex.printStackTrace(System.out);
        }
    }

    private static void consumerTask ()
    {
        final AtomicLong i = new AtomicLong();

        while (true)
        {
            if (queue.poll(i, 1, TimeUnit.SECONDS))
            {
                System.out.println("X = " + i);
            }
            else
            {
                System.out.println("false");
            }
        }

    }

    public static void main (String[] args)
    {
        for (int i = 0; i < producers.length; i++)
        {
            final int id = i;
            producers[i] = new Thread(() -> producerTask(id % 4));
        }

        for (int i = 0; i < consumers.length; i++)
        {
            consumers[i] = new Thread(Main1::consumerTask);
        }

        for (Thread producer : producers)
        {
            producer.start();
        }

        for (Thread consumer : consumers)
        {
            consumer.start();
        }
    }
}
