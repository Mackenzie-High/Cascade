package com.mackenziehigh.cascade.research;


import com.google.common.primitives.Ints;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author mackenzie
 */
public class Main01
{

    public static void main (String[] args)
    {
        final ConcurrentLinkedQueue<byte[]> up = new ConcurrentLinkedQueue<>();

        final ConcurrentLinkedQueue<byte[]> down = new ConcurrentLinkedQueue<>();

        final Runnable producer = () ->
        {
            while (true)
            {
//                try
                {
                    final byte[] bytes = up.poll();
                    if (bytes != null)
                    {
                        final int value = Ints.fromByteArray(bytes);
                        down.offer(Ints.toByteArray(value + 1));
                    }
                }
//                catch (InterruptedException ex)
//                {
//                    ex.printStackTrace(System.out);
//                }
            }
        };

        final Runnable consumer = () ->
        {
            while (true)
            {
//                try
                {
                    final byte[] bytes = down.poll();
                    if (bytes != null)
                    {
                        final int value = Ints.fromByteArray(bytes);
                        up.offer(Ints.toByteArray(value));

                        if (value % 1_000_000 == 0)
                        {
                            System.out.println("X = " + value);
                        }
                    }
                }
//                catch (InterruptedException ex)
//                {
//                    ex.printStackTrace(System.out);
//                }
            }
        };

        final Thread p = new Thread(producer);
        final Thread q = new Thread(consumer);
        p.start();
        q.start();
        up.add(Ints.toByteArray(0));
    }
}
