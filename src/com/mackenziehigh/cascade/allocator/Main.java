package com.mackenziehigh.cascade.allocator;

import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author mackenzie
 */
public class Main
{
    private static final long start = System.currentTimeMillis();

    private static final ConcurrentLongStack stack = new ConcurrentLongStack(1000);

    private static final Runnable action = new Runnable()
    {
        @Override
        public void run ()
        {
            final Object object = new Object();
            int counter = 0;
            final String name = Thread.currentThread().getName();
            final AtomicLong value = new AtomicLong();
            while (true)
            {

                stack.push(object.hashCode() + value.get());
                stack.pop(x -> value.set(x));

                if (++counter % (1000 * 1000) == 0)
                {
                    System.out.println(name + " = " + value + " @ " + (System.currentTimeMillis() - start));
                    return;
                }
            }
        }
    };

    public static void main (String[] args)
    {
        for (int i = 0; i < 40; i++)
        {
            final Thread thread = new Thread(action);
            thread.start();

        }
    }
}
