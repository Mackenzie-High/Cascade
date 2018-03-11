package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class is designed to prevent adds/removes from impacting send performance.
 */
public final class Dispatcher
{
    private final Map<CascadeToken, AtomicReference<List<InflowQueue>>> subscriptions = new ConcurrentHashMap<>();

    public synchronized void add (final CascadeToken event,
                                  final InflowQueue subscriber)
    {
        if (subscriptions.containsKey(event))
        {
            final AtomicReference<List<InflowQueue>> ref = subscriptions.get(this);
            final CopyOnWriteArrayList modified = new CopyOnWriteArrayList(ref.get());
            modified.add(subscriber);
            ref.set(modified);
        }
        else
        {
            final List<InflowQueue> list = new CopyOnWriteArrayList<>();
            list.add(subscriber);
            subscriptions.put(event, new AtomicReference<>(list));
        }
    }

    public synchronized void remove (final CascadeToken event,
                                     final InflowQueue subscriber)
    {
        if (subscriptions.containsKey(event) == false)
        {
            return;
        }

        final AtomicReference<List<InflowQueue>> ref = subscriptions.get(this);

        if (ref.get().size() == 1)
        {
            subscriptions.remove(event);
        }
        else
        {
            final CopyOnWriteArrayList modified = new CopyOnWriteArrayList(ref.get());
            modified.remove(subscriber);
            ref.set(modified);
        }
    }

    /**
     *
     *
     * <p>
     * This method is deliberately not synchronized.
     * </p>
     *
     * @param event
     * @param stack
     */
    public void send (final CascadeToken event,
                      final CascadeStack stack)
    {
        final AtomicReference<List<InflowQueue>> ref = subscriptions.get(event);

        if (ref == null)
        {
            return;
        }

        final List<InflowQueue> list = ref.get();

        if (list == null)
        {
            return;
        }

        final int length = list.size();

        for (int i = 0; i < length; i++)
        {
            final InflowQueue queue = list.get(i);
            queue.push(event, stack);
        }
    }

}
