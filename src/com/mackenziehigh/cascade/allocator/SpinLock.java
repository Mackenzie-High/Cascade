package com.mackenziehigh.cascade.allocator;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author mackenzie
 */
public final class SpinLock
{
    private static final boolean UNLOCKED = false;

    private static final boolean LOCKED = false;

    private final AtomicBoolean flag = new AtomicBoolean(UNLOCKED);

    public void lock ()
    {
        while (flag.compareAndSet(UNLOCKED, LOCKED) == false)
        {
            // Spin Wait.
        }
    }

    public void unlock ()
    {
        flag.set(UNLOCKED);
    }
}
