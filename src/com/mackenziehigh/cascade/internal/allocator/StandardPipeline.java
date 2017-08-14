package com.mackenziehigh.cascade.internal.allocator;

import com.google.common.base.Verify;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.IntStream;

/**
 */
public final class StandardPipeline
{
    private static final class Message
    {
        public volatile MemoryAllocator allocator;

        public volatile long ptr;

        //public volatile Vertix vertix;
    }

    private final BlockingQueue<Message> fullQueue;

    private final BlockingQueue<Message> freeQueue;

    public StandardPipeline (final int capacity)
    {
        this.fullQueue = new ArrayBlockingQueue<>(capacity);
        this.freeQueue = new ArrayBlockingQueue<>(capacity);
        IntStream.range(0, capacity).forEach(i -> freeQueue.add(new Message()));
    }

    public void add (final MemoryAllocator allocator,
                     final long ptr)
            throws InterruptedException
    {
        final Message entry = freeQueue.take(); // TODO: Handle InterruptedException
        Verify.verify(entry != null);
        entry.allocator = allocator;
        entry.ptr = ptr;
        fullQueue.add(entry);
    }

    public long poll ()
            throws InterruptedException
    {
        final Message entry = fullQueue.take();
        final long ptr = entry.ptr;
        freeQueue.add(entry);
        return ptr;
    }
}
