package com.mackenziehigh.cascade.internal.allocator;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 *
 * @author mackenzie
 */
public final class MemoryAllocatorTester
{
    private final Thread[] threads;

    private final MemoryAllocator allocator;

    private final int dataSize;

    public MemoryAllocatorTester (final int producerCount,
                                  final int consumerCount,
                                  final MemoryAllocator allocator,
                                  final int dataSize,
                                  final int stopAtCount)
    {
        this.threads = new Thread[producerCount + consumerCount];
        this.allocator = allocator;
        this.dataSize = dataSize;

        final StandardPipeline pipeline = new StandardPipeline(10 * 1000);

        final Runnable producer = () ->
        {
            final byte[] data = randomPattern();
            final int limit = (((stopAtCount * consumerCount) / producerCount) + 1);
            for (int i = 0; i < limit; i++)
            {
                final int ptr = allocator.malloc(dataSize * 2);
                allocator.set(ptr, data);
                try
                {
                    pipeline.add(allocator, ptr);
                }
                catch (InterruptedException ex)
                {
                    ex.printStackTrace(System.out);
                }
            }
        };

        final Runnable consumer = () ->
        {
            int counter = 0;
            long sum = 0;
            final byte[] buffer = new byte[dataSize];
            final long start = System.nanoTime();
            while (counter < stopAtCount)
            {
                try
                {
                    final int ptr = pipeline.poll();
                    allocator.get(ptr, buffer);
                    allocator.decrement(ptr);
                    ++counter;

                    for (int i = 0; i < dataSize; i++)
                    {
                        sum += buffer[i];
                    }
                }
                catch (InterruptedException ex)
                {
                    ex.printStackTrace(System.out);
                }
            }
            final long end = System.nanoTime();
            final long millis = TimeUnit.NANOSECONDS.toMillis(end - start);
            System.out.println(Thread.currentThread().getName() + " = " + millis);
        };

        IntStream.range(0, producerCount).forEach(i -> threads[i] = new Thread(producer));
        IntStream.range(producerCount, threads.length).forEach(i -> threads[i] = new Thread(consumer));
    }

    public void start ()
    {
        Arrays.asList(threads).forEach(t -> t.start());
    }

    private byte[] randomPattern ()
    {
        final Random random = new Random(System.currentTimeMillis());
        final byte[] bytes = new byte[dataSize];
        random.nextBytes(bytes);
        return bytes;
    }

    public static void main (String[] args)
    {
        final MemoryAllocator allocator = new DynamicAllocator(20000);
        final int pcount = 10;
        final int ccount = 10;
        final int dsize = 100000;
        final int stopAt = 1000 * 1000;

        final MemoryAllocatorTester tester = new MemoryAllocatorTester(pcount, ccount, allocator, dsize, stopAt);
        tester.start();
    }
}
